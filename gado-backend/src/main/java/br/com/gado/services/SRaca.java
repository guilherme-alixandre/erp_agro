package br.com.gado.services;

import br.com.gado.dto.insumoDto.InsumoEstoqueCadastroDto;
import br.com.gado.dto.insumoDto.InsumoEstoqueRespostaDto;
import br.com.gado.dto.insumoDto.InsumoEstoquePutDto;
import br.com.gado.dto.racaDto.RacaCadastroDto;
import br.com.gado.dto.racaDto.RacaPutDto;
import br.com.gado.dto.racaDto.RacaRespostaDto;
import br.com.gado.entities.EGrupoProduto;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.ERaca;
import br.com.gado.entities.EUnidadeMedida;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoInsumo;
import br.com.gado.repositories.IGrupoProduto;
import br.com.gado.repositories.IInsumo;
import br.com.gado.repositories.IRaca;
import br.com.gado.repositories.IUnidadeMedida;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Cadastro de Raças (mesmo padrão de SGrupoProduto). Cada raça criada gera automaticamente um
 * Produto (EInsumo) "Gado {nome}" no Grupo de Produto "Animais", usado para dar entrada
 * (compra) e saída (venda/abate) agregada de cabeças dessa raça.
 */
@Service
public class SRaca {

    private static final String NOME_GRUPO_ANIMAIS = "Animais";
    private static final String NOME_UNIDADE_CABECA = "CABECA";

    /** Mesmo conjunto de SInsumo.PERFIS_GESTAO_ESTOQUE — permite reaproveitar os métodos
     *  públicos de SInsumo (criarInsumoEstoque/inativarInsumo/reativarInsumo) sem atrito. */
    private static final Set<EnPerfilUsuario> PERFIS_GESTAO_RACA = Set.of(
            EnPerfilUsuario.ADMINISTRADOR, EnPerfilUsuario.GERENTE, EnPerfilUsuario.CUIDADOR_CHEFE);

    @Autowired
    private IRaca racaInterface;

    @Autowired
    private IGrupoProduto grupoProdutoInterface;

    @Autowired
    private IUnidadeMedida unidadeMedidaInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private IInsumo insumoInterface;

    @Autowired
    private SInsumo insumoService;

    // ── Permissão ──────────────────────────────────────────────────────

    private void validaGestaoRaca(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do usuário responsável pela operação.");
        }
        EUsuario usuario = usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (!PERFIS_GESTAO_RACA.contains(usuario.getPerfil())) {
            throw new IllegalArgumentException(
                    "Apenas Administradores, Gerentes ou Cuidadores Chefe podem gerenciar raças.");
        }
    }

    // ── Leitura ────────────────────────────────────────────────────────

    @Transactional
    public List<RacaRespostaDto> listar(String busca, String status) {
        String termo = busca == null ? "" : busca.trim();
        EnStatus filtroStatus = parseStatus(status);

        List<ERaca> racas;
        if (filtroStatus != null) {
            racas = termo.isBlank()
                    ? racaInterface.findByStatusOrderByNomeAsc(filtroStatus)
                    : racaInterface.findByStatusAndNomeContainingIgnoreCaseOrderByNomeAsc(filtroStatus, termo);
        } else {
            racas = termo.isBlank()
                    ? racaInterface.findAllByOrderByNomeAsc()
                    : racaInterface.findByNomeContainingIgnoreCaseOrderByNomeAsc(termo);
        }

        return racas.stream().map(this::toRespostaDto).collect(Collectors.toList());
    }

    private EnStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return EnStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido. Use 'A' (ativo) ou 'I' (inativo).");
        }
    }

    public RacaRespostaDto buscarPorId(Long id) {
        ERaca raca = racaInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Raça não encontrada."));
        return toRespostaDto(raca);
    }

    // ── Escrita ────────────────────────────────────────────────────────

    @Transactional
    public RacaRespostaDto criar(RacaCadastroDto dto, String emailUsuario) {
        validaGestaoRaca(emailUsuario);

        String nome = dto.getNome().trim();
        String sigla = dto.getSigla().trim().toUpperCase();

        if (racaInterface.findFirstByNomeIgnoreCase(nome).isPresent()) {
            throw new IllegalArgumentException("Já existe uma raça com esse nome.");
        }
        if (racaInterface.findFirstBySiglaIgnoreCase(sigla).isPresent()) {
            throw new IllegalArgumentException("Já existe uma raça com essa sigla.");
        }

        EGrupoProduto grupoAnimais = grupoProdutoInterface.findFirstByNomeIgnoreCase(NOME_GRUPO_ANIMAIS)
                .orElseThrow(() -> new IllegalStateException(
                        "Grupo de produto \"Animais\" não encontrado — verifique a migration V23."));
        EUnidadeMedida unidadeCabeca = unidadeMedidaInterface.findFirstByUnidadeIgnoreCase(NOME_UNIDADE_CABECA)
                .orElseThrow(() -> new IllegalStateException(
                        "Unidade de medida \"CABECA\" não encontrada — verifique a migration V23."));

        InsumoEstoqueCadastroDto produtoDto = new InsumoEstoqueCadastroDto();
        produtoDto.setNome("Gado " + nome);
        produtoDto.setTipo(EnTipoInsumo.ANIMAL);
        produtoDto.setGrupoProdutoId(grupoAnimais.getId());
        produtoDto.setUnidadeMedidaPrimariaId(unidadeCabeca.getId());
        InsumoEstoqueRespostaDto produtoResposta = insumoService.criarInsumoEstoque(produtoDto, emailUsuario);

        EInsumo produtoCriado = insumoInterface.findById(produtoResposta.getId())
                .orElseThrow(() -> new IllegalStateException("Falha ao criar o produto da raça."));

        ERaca raca = new ERaca();
        raca.setNome(nome);
        raca.setSigla(sigla);
        raca.setProduto(produtoCriado);

        return toRespostaDto(racaInterface.save(raca));
    }

    @Transactional
    public RacaRespostaDto atualizar(Long id, RacaPutDto dto, String emailUsuario) {
        validaGestaoRaca(emailUsuario);

        ERaca raca = racaInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Raça não encontrada."));

        if (dto.getNome() != null) {
            String nome = dto.getNome().trim();
            if (nome.isBlank()) {
                throw new IllegalArgumentException("O nome da raça não pode ser vazio.");
            }
            racaInterface.findFirstByNomeIgnoreCase(nome)
                    .filter(existente -> !existente.getId().equals(id))
                    .ifPresent(existente -> {
                        throw new IllegalArgumentException("Já existe uma raça com esse nome.");
                    });
            raca.setNome(nome);

            InsumoEstoquePutDto produtoPutDto = new InsumoEstoquePutDto();
            produtoPutDto.setNome("Gado " + nome);
            insumoService.atualizarDadosEstoque(raca.getProduto().getId(), produtoPutDto, emailUsuario);
        }

        if (dto.getSigla() != null) {
            String sigla = dto.getSigla().trim().toUpperCase();
            racaInterface.findFirstBySiglaIgnoreCase(sigla)
                    .filter(existente -> !existente.getId().equals(id))
                    .ifPresent(existente -> {
                        throw new IllegalArgumentException("Já existe uma raça com essa sigla.");
                    });
            raca.setSigla(sigla);
        }

        return toRespostaDto(racaInterface.save(raca));
    }

    @Transactional
    public String inativar(Long id, String emailUsuario) {
        validaGestaoRaca(emailUsuario);
        ERaca raca = racaInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Raça não encontrada."));
        raca.setStatus(EnStatus.I);
        racaInterface.save(raca);
        insumoService.inativarInsumo(raca.getProduto().getId(), emailUsuario);
        return "Raça inativada com sucesso.";
    }

    @Transactional
    public String reativar(Long id, String emailUsuario) {
        validaGestaoRaca(emailUsuario);
        ERaca raca = racaInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Raça não encontrada."));
        raca.setStatus(EnStatus.A);
        racaInterface.save(raca);
        insumoService.reativarInsumo(raca.getProduto().getId(), emailUsuario);
        return "Raça reativada com sucesso.";
    }

    private RacaRespostaDto toRespostaDto(ERaca raca) {
        RacaRespostaDto dto = new RacaRespostaDto();
        dto.setId(raca.getId());
        dto.setNome(raca.getNome());
        dto.setSigla(raca.getSigla());
        dto.setStatus(raca.getStatus());

        EInsumo produto = raca.getProduto();
        if (produto != null) {
            dto.setProdutoId(produto.getId());
            dto.setProdutoNome(produto.getNome());
            dto.setProdutoCodigo(produto.getCodigoProduto());
            dto.setSaldoAtual(produto.getSaldoAtual());
            dto.setPrecoCompraMedio(produto.getPrecoCompraMedio());
        }

        return dto;
    }
}
