package br.com.gado.services;

import br.com.gado.dto.InsumoDto;
import br.com.gado.dto.insumoDto.EntradaEstoqueDto;
import br.com.gado.dto.insumoDto.InsumoEstoqueCadastroDto;
import br.com.gado.dto.insumoDto.InsumoEstoquePutDto;
import br.com.gado.dto.insumoDto.InsumoEstoqueRespostaDto;
import br.com.gado.dto.insumoDto.VacinaCadastroDto;
import br.com.gado.dto.insumoDto.VacinaPutDto;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.EMovimentacaoEstoque;
import br.com.gado.entities.EParceiro;
import br.com.gado.entities.EUnidadeMedida;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoInsumo;
import br.com.gado.enums.EnTipoMovimentacaoEstoque;
import br.com.gado.repositories.IInsumo;
import br.com.gado.repositories.IMovimentacaoEstoque;
import br.com.gado.repositories.IParceiro;
import br.com.gado.repositories.IUnidadeMedida;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private IInsumo insumoInterface;

    @Autowired
    private IParceiro parceiroInterface;

    @Autowired
    private IUnidadeMedida unidadeMedidaInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private IMovimentacaoEstoque movimentacaoEstoqueInterface;

    @Autowired
    private ModelMapper modelMapper;

    public List<InsumoDto> listarVacinas(String busca) {
        String termo = busca == null ? "" : busca.trim();
        List<EInsumo> vacinas = termo.isBlank()
                ? insumoInterface.findByTipoOrderByNomeAsc(EnTipoInsumo.VACINA)
                : insumoInterface.findByTipoAndNomeContainingIgnoreCaseOrderByNomeAsc(EnTipoInsumo.VACINA, termo);

        return vacinas.stream()
                .map(vacina -> modelMapper.map(vacina, InsumoDto.class))
                .toList();
    }

    @Transactional
    public InsumoDto criarVacina(VacinaCadastroDto dto) {
        if (dto == null || dto.getNome() == null || dto.getNome().isBlank()) {
            throw new IllegalArgumentException("Informe o nome da vacina.");
        }

        String nome = dto.getNome().trim();
        if (insumoInterface.findFirstByTipoAndNomeIgnoreCase(EnTipoInsumo.VACINA, nome).isPresent()) {
            throw new IllegalArgumentException("Vacina já cadastrada.");
        }

        EInsumo vacina = new EInsumo();
        vacina.setNome(nome);
        vacina.setTipo(EnTipoInsumo.VACINA);
        vacina.setPendente(dto.getPendente() != null ? dto.getPendente() : Boolean.FALSE);

        EInsumo salva = insumoInterface.save(vacina);
        return modelMapper.map(salva, InsumoDto.class);
    }

    @Transactional
    public InsumoDto atualizarVacina(Long id, VacinaPutDto dto) {
        EInsumo vacina = insumoInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vacina não encontrada."));

        if (vacina.getTipo() != EnTipoInsumo.VACINA) {
            throw new IllegalArgumentException("Insumo informado não é uma vacina.");
        }

        if (dto == null) {
            throw new IllegalArgumentException("Dados de atualização ausentes.");
        }

        if (dto.getNome() != null) {
            String nome = dto.getNome().trim();
            if (nome.isBlank()) {
                throw new IllegalArgumentException("Informe o nome da vacina.");
            }
            vacina.setNome(nome);
        }

        if (dto.getPendente() != null) {
            vacina.setPendente(dto.getPendente());
        }

        EInsumo salva = insumoInterface.save(vacina);
        return modelMapper.map(salva, InsumoDto.class);
    }

    @Transactional
    public String deletarVacina(Long id) {
        EInsumo vacina = insumoInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vacina não encontrada."));

        if (vacina.getTipo() != EnTipoInsumo.VACINA) {
            throw new IllegalArgumentException("Insumo informado não é uma vacina.");
        }

        insumoInterface.deleteById(id);
        return "Vacina deletada com sucesso";
    }

    public InsumoDto buscaPorId(Long id) {
        EInsumo insumo = insumoInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Insumo não encontrado"));
        return modelMapper.map(insumo, InsumoDto.class);
    }

    @Transactional
    public InsumoDto cadastraInsumo(InsumoDto dto) {
        EInsumo insumo = modelMapper.map(dto, EInsumo.class);

        EParceiro parceiro = parceiroInterface.findById(dto.getParceiro_id())
                .orElseThrow(() -> new EntityNotFoundException("Id do fornecedor não encontrado"));

        insumo.setParceiro(parceiro);

        EInsumo insumoSalvo = insumoInterface.save(insumo);
        return modelMapper.map(insumoSalvo, InsumoDto.class);
    }

    @Transactional
    public String deletaInsumo(Long id) {
        if (insumoInterface.findById(id).isEmpty()) {
            return "nenhum insumo com esse id foi encontrado";
        }

        insumoInterface.deleteById(id);
        return "insumo deletado com sucesso";
    }

    @Transactional
    public InsumoDto alteraInsumo(Long id, InsumoDto dto) {
        EInsumo insumo = insumoInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("nenhum insumo encontrado com esse id"));

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(dto, insumo);
        EInsumo insumoAtualizado = insumoInterface.save(insumo);
        return modelMapper.map(insumoAtualizado, InsumoDto.class);
    }

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

    @Transactional
    public List<InsumoEstoqueRespostaDto> listarEstoque(String busca) {
        String termo = busca == null ? "" : busca.trim();
        List<EInsumo> insumos = termo.isBlank()
                ? insumoInterface.findByStatusAndTipoNotOrderByNomeAsc(EnStatus.A, EnTipoInsumo.VACINA)
                : insumoInterface.findByStatusAndTipoNotAndNomeContainingIgnoreCaseOrderByNomeAsc(
                        EnStatus.A, EnTipoInsumo.VACINA, termo);

        return insumos.stream().map(this::toEstoqueRespostaDto).collect(Collectors.toList());
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

        EInsumo insumo = new EInsumo();
        insumo.setNome(dto.getNome().trim());
        insumo.setTipo(dto.getTipo());
        insumo.setUnidadeMedidaPrimaria(unidadePrimaria);
        insumo.setUnidadeMedidaSecundaria(unidadeSecundaria);
        insumo.setFatorConversao(dto.getFatorConversao());
        insumo.setEstoqueMinimo(dto.getEstoqueMinimo());
        insumo.setSaldoAtual(0.0);
        insumo.setPendente(Boolean.FALSE);

        if (dto.getParceiroId() != null) {
            insumo.setParceiro(resolveParceiro(dto.getParceiroId()));
        }

        return toEstoqueRespostaDto(insumoInterface.save(insumo));
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
