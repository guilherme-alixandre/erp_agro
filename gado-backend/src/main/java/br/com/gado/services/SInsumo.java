package br.com.gado.services;

import br.com.gado.dto.insumoDto.EntradaEstoqueDto;
import br.com.gado.dto.insumoDto.InsumoEstoqueCadastroDto;
import br.com.gado.dto.insumoDto.InsumoEstoquePutDto;
import br.com.gado.dto.insumoDto.InsumoEstoqueRespostaDto;
import br.com.gado.entities.EGrupoProduto;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.EMovimentacaoEstoque;
import br.com.gado.entities.EParceiro;
import br.com.gado.entities.EUnidadeMedida;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoMovimentacaoEstoque;
import br.com.gado.repositories.IGrupoProduto;
import br.com.gado.repositories.IInsumo;
import br.com.gado.repositories.IMovimentacaoEstoque;
import br.com.gado.repositories.IParceiro;
import br.com.gado.repositories.IUnidadeMedida;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SInsumo {

    private static final Set<EnPerfilUsuario> PERFIS_GESTAO_ESTOQUE = Set.of(
            EnPerfilUsuario.ADMINISTRADOR, EnPerfilUsuario.GERENTE, EnPerfilUsuario.CUIDADOR_CHEFE);

    /** Tentativas de regerar o código sequencial em caso de corrida entre requisições concorrentes. */
    private static final int TENTATIVAS_MAXIMAS_CODIGO = 5;

    @Autowired
    private IInsumo insumoInterface;

    @Autowired
    private IGrupoProduto grupoProdutoInterface;

    @Autowired
    private IParceiro parceiroInterface;

    @Autowired
    private IUnidadeMedida unidadeMedidaInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private IMovimentacaoEstoque movimentacaoEstoqueInterface;

    // ── Permissões (Módulo de Estoque) ──────────────────────────────────

    /** Restrito a ADMINISTRADOR, GERENTE e CUIDADOR_CHEFE — gestão de estoque (cadastro, entradas, edição). */
    public void validaGestaoEstoque(String emailUsuario) {
        EUsuario usuario = resolveUsuarioObrigatorio(emailUsuario);
        if (!PERFIS_GESTAO_ESTOQUE.contains(usuario.getPerfil())) {
            throw new IllegalArgumentException(
                    "Apenas Administradores, Gerentes ou Cuidadores Chefe podem gerenciar o estoque de insumos.");
        }
    }

    private EUsuario resolveUsuarioObrigatorio(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do usuário responsável pela operação.");
        }
        return usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    // ── Estoque: consulta ────────────────────────────────────────────────

    /**
     * @param status opcional: "A" (apenas ativos), "I" (apenas inativos), nulo/vazio = todos
     *               (produtos inativos continuam visíveis na listagem, com a opção de reativar).
     */
    @Transactional
    public List<InsumoEstoqueRespostaDto> listarEstoque(String busca, String status) {
        String termo = busca == null ? "" : busca.trim();
        EnStatus filtroStatus = parseStatus(status);

        List<EInsumo> insumos;
        if (filtroStatus != null) {
            insumos = termo.isBlank()
                    ? insumoInterface.findByStatusOrderByNomeAsc(filtroStatus)
                    : insumoInterface.findByStatusAndNomeContainingIgnoreCaseOrderByNomeAsc(filtroStatus, termo);
        } else {
            insumos = termo.isBlank()
                    ? insumoInterface.findAllByOrderByNomeAsc()
                    : insumoInterface.findByNomeContainingIgnoreCaseOrderByNomeAsc(termo);
        }

        return insumos.stream().map(this::toEstoqueRespostaDto).collect(Collectors.toList());
    }

    private EnStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return EnStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido. Use 'A' (ativo) ou 'I' (inativo).");
        }
    }

    @Transactional
    public String inativarInsumo(Long id, String emailUsuario) {
        validaGestaoEstoque(emailUsuario);
        EInsumo insumo = insumoInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Insumo não encontrado."));
        insumo.setStatus(EnStatus.I);
        insumoInterface.save(insumo);
        return "Produto inativado com sucesso.";
    }

    @Transactional
    public String reativarInsumo(Long id, String emailUsuario) {
        validaGestaoEstoque(emailUsuario);
        EInsumo insumo = insumoInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Insumo não encontrado."));
        insumo.setStatus(EnStatus.A);
        insumoInterface.save(insumo);
        return "Produto reativado com sucesso.";
    }

    @Transactional
    public InsumoEstoqueRespostaDto buscarEstoquePorId(Long id) {
        EInsumo insumo = insumoInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Insumo não encontrado ou inativo."));
        return toEstoqueRespostaDto(insumo);
    }

    // ── Estoque: cadastro e edição (restrito) ───────────────────────────

    @Transactional
    public InsumoEstoqueRespostaDto criarInsumoEstoque(InsumoEstoqueCadastroDto dto, String emailUsuario) {
        validaGestaoEstoque(emailUsuario);

        EUnidadeMedida unidadePrimaria = resolveUnidade(dto.getUnidadeMedidaPrimariaId(),
                "Unidade de medida primária não encontrada.");

        EUnidadeMedida unidadeSecundaria = null;
        if (dto.getUnidadeMedidaSecundariaId() != null) {
            unidadeSecundaria = resolveUnidade(dto.getUnidadeMedidaSecundariaId(),
                    "Unidade de medida secundária não encontrada.");
            if (dto.getFatorConversao() == null) {
                throw new IllegalArgumentException(
                        "Informe o fator de conversão quando houver unidade secundária.");
            }
        }

        EGrupoProduto grupo = grupoProdutoInterface.findById(dto.getGrupoProdutoId())
                .orElseThrow(() -> new IllegalArgumentException("Grupo de produto não encontrado."));

        EInsumo insumo = new EInsumo();
        insumo.setNome(dto.getNome().trim());
        insumo.setTipo(dto.getTipo());
        insumo.setGrupoProduto(grupo);
        insumo.setUnidadeMedidaPrimaria(unidadePrimaria);
        insumo.setUnidadeMedidaSecundaria(unidadeSecundaria);
        insumo.setFatorConversao(dto.getFatorConversao());
        insumo.setEstoqueMinimo(dto.getEstoqueMinimo());
        insumo.setSaldoAtual(0.0);
        insumo.setPendente(Boolean.FALSE);

        if (dto.getParceiroId() != null) {
            insumo.setParceiro(resolveParceiro(dto.getParceiroId()));
        }

        return toEstoqueRespostaDto(salvarComCodigoSequencial(insumo, grupo));
    }

    @Transactional
    public InsumoEstoqueRespostaDto atualizarDadosEstoque(Long id, InsumoEstoquePutDto dto, String emailUsuario) {
        validaGestaoEstoque(emailUsuario);

        EInsumo insumo = insumoInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Insumo não encontrado ou inativo."));

        if (dto.getNome() != null) {
            if (dto.getNome().isBlank()) {
                throw new IllegalArgumentException("O nome do insumo não pode ser vazio.");
            }
            insumo.setNome(dto.getNome().trim());
        }

        if (dto.getUnidadeMedidaSecundariaId() != null) {
            insumo.setUnidadeMedidaSecundaria(
                    resolveUnidade(dto.getUnidadeMedidaSecundariaId(), "Unidade de medida secundária não encontrada."));
        }

        if (dto.getFatorConversao() != null) {
            insumo.setFatorConversao(dto.getFatorConversao());
        }

        if (insumo.getUnidadeMedidaSecundaria() != null && insumo.getFatorConversao() == null) {
            throw new IllegalArgumentException(
                    "Informe o fator de conversão quando houver unidade secundária.");
        }

        if (dto.getEstoqueMinimo() != null) insumo.setEstoqueMinimo(dto.getEstoqueMinimo());
        if (dto.getPrecoCompraMedio() != null) insumo.setPrecoCompraMedio(arredondarMoeda(dto.getPrecoCompraMedio()));
        if (dto.getPrecoUltimaCompra() != null) insumo.setPrecoUltimaCompra(arredondarMoeda(dto.getPrecoUltimaCompra()));
        if (dto.getParceiroId() != null) insumo.setParceiro(resolveParceiro(dto.getParceiroId()));

        return toEstoqueRespostaDto(insumoInterface.save(insumo));
    }

    /**
     * Entrada manual de estoque. Ponto único de entrada de saldo — preparado para,
     * no futuro, também ser chamado por um serviço de importação de XML de NF-e
     * (que preencheria quantidade/precoUnitario/numeroNf/chaveAcessoNf a partir do XML).
     */
    @Transactional
    public InsumoEstoqueRespostaDto registrarEntradaEstoque(Long id, EntradaEstoqueDto dto, String emailUsuario) {
        validaGestaoEstoque(emailUsuario);
        return aplicarEntradaEstoque(id, dto);
    }

    /**
     * Mesma lógica de registrarEntradaEstoque, sem a checagem de permissão de gestão de estoque —
     * para uso interno de outros serviços (ex: SDocumentoEntrada) que já validaram a permissão
     * adequada à própria operação (ex: aprovar um documento financeiro) antes de chamar aqui.
     */
    @Transactional
    InsumoEstoqueRespostaDto aplicarEntradaEstoque(Long id, EntradaEstoqueDto dto) {
        EInsumo insumo = insumoInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Insumo não encontrado ou inativo."));

        double saldoAnterior = insumo.getSaldoAtual() != null ? insumo.getSaldoAtual() : 0.0;
        Double precoMedioAnterior = insumo.getPrecoCompraMedio();
        double quantidadeEntrada = dto.getQuantidade();
        double precoUnitarioEntrada = dto.getPrecoUnitario();

        double novoPrecoMedio = (saldoAnterior <= 0 || precoMedioAnterior == null)
                ? precoUnitarioEntrada
                : ((saldoAnterior * precoMedioAnterior) + (quantidadeEntrada * precoUnitarioEntrada))
                        / (saldoAnterior + quantidadeEntrada);

        insumo.setSaldoAtual(saldoAnterior + quantidadeEntrada);
        insumo.setPrecoCompraMedio(arredondarMoeda(novoPrecoMedio));
        insumo.setPrecoUltimaCompra(arredondarMoeda(precoUnitarioEntrada));

        if (dto.getParceiroId() != null) insumo.setParceiro(resolveParceiro(dto.getParceiroId()));
        if (dto.getNumeroNf() != null) insumo.setNumeroNf(dto.getNumeroNf().trim());
        if (dto.getChaveAcessoNf() != null) insumo.setChaveAcessoNf(dto.getChaveAcessoNf().trim());

        EInsumo insumoSalvo = insumoInterface.save(insumo);

        // Ledger imutável da entrada — mesma tabela que, no futuro, o importador de XML de NF-e usará.
        EMovimentacaoEstoque movimentacao = new EMovimentacaoEstoque();
        movimentacao.setEnTipoMovimentacaoEstoque(EnTipoMovimentacaoEstoque.ENTRADA);
        movimentacao.setQuantidade(quantidadeEntrada);
        movimentacao.setValorUnitario(precoUnitarioEntrada);
        movimentacao.setDataMovimentacao(java.util.Date.from(
                (dto.getDataEntrada() != null ? dto.getDataEntrada() : LocalDateTime.now())
                        .atZone(java.time.ZoneId.systemDefault()).toInstant()));
        movimentacao.setInsumoId(insumoSalvo);
        movimentacao.setParceiroId(insumoSalvo.getParceiro());
        movimentacaoEstoqueInterface.save(movimentacao);

        return toEstoqueRespostaDto(insumoSalvo);
    }

    // ── Catálogo de Produtos: código sequencial ─────────────────────────

    /**
     * Calcula o próximo código de 8 dígitos do grupo: busca o maior codigoProduto já
     * emitido na faixa do grupo (via findFirstByCodigoProdutoBetweenOrderByCodigoProdutoDesc),
     * extrai os 6 dígitos sequenciais, incrementa e reformata com zeros à esquerda.
     * Sem produto anterior no grupo, começa em 1 (ex: "01000001").
     */
    private String gerarProximoCodigoProduto(EGrupoProduto grupo) {
        String prefixo = grupo.getCodigoPrefixo();
        String codigoInicio = prefixo + "000000";
        String codigoFim = prefixo + "999999";

        int proximoSequencial = insumoInterface
                .findFirstByCodigoProdutoBetweenOrderByCodigoProdutoDesc(codigoInicio, codigoFim)
                .map(ultimo -> Integer.parseInt(ultimo.getCodigoProduto().substring(2)) + 1)
                .orElse(1);

        if (proximoSequencial > 999_999) {
            throw new IllegalStateException(
                    "Grupo de produto \"" + grupo.getNome() + "\" atingiu o limite de 999999 produtos.");
        }

        return prefixo + String.format("%06d", proximoSequencial);
    }

    /**
     * Gera o código sequencial e grava o insumo. Como EAbstract usa GenerationType.IDENTITY,
     * o INSERT (e a violação da constraint única de codigo_produto, se houver) acontece já
     * no saveAndFlush(), permitindo recalcular o próximo sequencial e tentar de novo em caso
     * de corrida entre duas requisições concorrentes gerando código no mesmo grupo.
     */
    private EInsumo salvarComCodigoSequencial(EInsumo insumo, EGrupoProduto grupo) {
        for (int tentativa = 1; tentativa <= TENTATIVAS_MAXIMAS_CODIGO; tentativa++) {
            insumo.setCodigoProduto(gerarProximoCodigoProduto(grupo));
            try {
                return insumoInterface.saveAndFlush(insumo);
            } catch (DataIntegrityViolationException e) {
                if (tentativa == TENTATIVAS_MAXIMAS_CODIGO) {
                    throw new IllegalStateException(
                            "Não foi possível gerar um código único para o produto após "
                                    + TENTATIVAS_MAXIMAS_CODIGO + " tentativas.", e);
                }
            }
        }
        throw new IllegalStateException("Falha inesperada ao gerar o código do produto.");
    }

    /**
     * PLACEHOLDER — importação de entrada de NF-e (XML). Implementação futura fará:
     * 1. Parse do XML da NF-e (itens em det/prod).
     * 2. Para cada item, tentar localizar um EInsumo existente cujo codigoProduto já
     *    esteja mapeado ao código do produto do fornecedor (cProd) na nota — provavelmente
     *    via uma tabela de "de-para" entre cProd/EAN e o codigoProduto interno.
     * 3. Se encontrado: chamar registrarEntradaEstoque(...) para dar baixa da entrada
     *    no saldo, preço médio, numeroNf e chaveAcessoNf.
     * 4. Se não encontrado: resolver o EGrupoProduto do item, gerar um novo codigoProduto
     *    via gerarProximoCodigoProduto(...) e criar o EInsumo antes de registrar a entrada.
     * 5. Regra especial para o grupo "Animais": nunca reaproveitar um EInsumo existente —
     *    cada animal importado da NF-e deve gerar um novo registro único (nunca soma de
     *    saldo), pois é uma unidade individual e não um item de estoque fungível.
     */
    public void processarEntradaNfe(String xml) {
        // Implementação futura.
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private EUnidadeMedida resolveUnidade(Long id, String mensagemErro) {
        return unidadeMedidaInterface.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(mensagemErro));
    }

    private EParceiro resolveParceiro(Long id) {
        return parceiroInterface.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fornecedor (parceiro) não encontrado."));
    }

    static double arredondarMoeda(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private InsumoEstoqueRespostaDto toEstoqueRespostaDto(EInsumo insumo) {
        InsumoEstoqueRespostaDto dto = new InsumoEstoqueRespostaDto();
        dto.setId(insumo.getId());
        dto.setNome(insumo.getNome());
        dto.setTipo(insumo.getTipo());
        dto.setStatus(insumo.getStatus());
        dto.setCodigoProduto(insumo.getCodigoProduto());

        if (insumo.getGrupoProduto() != null) {
            dto.setGrupoProdutoId(insumo.getGrupoProduto().getId());
            dto.setGrupoProdutoNome(insumo.getGrupoProduto().getNome());
        }

        dto.setSaldoAtual(insumo.getSaldoAtual());
        dto.setEstoqueMinimo(insumo.getEstoqueMinimo());
        dto.setAbaixoDoEstoqueMinimo(
                insumo.getEstoqueMinimo() != null && insumo.getSaldoAtual() != null
                        && insumo.getSaldoAtual() < insumo.getEstoqueMinimo());

        if (insumo.getUnidadeMedidaPrimaria() != null) {
            dto.setUnidadeMedidaPrimariaId(insumo.getUnidadeMedidaPrimaria().getId());
            dto.setUnidadeMedidaPrimariaSigla(insumo.getUnidadeMedidaPrimaria().getUnidade());
        }
        if (insumo.getUnidadeMedidaSecundaria() != null) {
            dto.setUnidadeMedidaSecundariaId(insumo.getUnidadeMedidaSecundaria().getId());
            dto.setUnidadeMedidaSecundariaSigla(insumo.getUnidadeMedidaSecundaria().getUnidade());
        }

        dto.setFatorConversao(insumo.getFatorConversao());
        dto.setPrecoCompraMedio(insumo.getPrecoCompraMedio());
        dto.setPrecoUltimaCompra(insumo.getPrecoUltimaCompra());
        dto.setNumeroNf(insumo.getNumeroNf());
        dto.setChaveAcessoNf(insumo.getChaveAcessoNf());

        if (insumo.getParceiro() != null) {
            dto.setParceiroId(insumo.getParceiro().getId());
            dto.setParceiroNome(insumo.getParceiro().getNome());
        }

        return dto;
    }
}
