package br.com.gado.services;

import br.com.gado.dto.documentoEntradaDto.DocumentoEntradaItemRespostaDto;
import br.com.gado.dto.documentoEntradaDto.DocumentoEntradaRespostaDto;
import br.com.gado.dto.documentoEntradaDto.NfeItemUpdateDto;
import br.com.gado.dto.documentoEntradaDto.NfeUpdateDto;
import br.com.gado.dto.documentoEntradaDto.ReciboSimplesCadastroDto;
import br.com.gado.dto.documentoEntradaDto.RecusaDocumentoDto;
import br.com.gado.dto.documentoEntradaDto.VincularProdutoDto;
import br.com.gado.dto.insumoDto.EntradaEstoqueDto;
import br.com.gado.entities.EDocumentoEntrada;
import br.com.gado.entities.EDocumentoEntradaItem;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.EParceiro;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnStatusAprovacaoFinanceira;
import br.com.gado.enums.EnTipoDocumentoFinanceiro;
import br.com.gado.enums.EnTipoMovimentacaoEstoque;
import br.com.gado.repositories.IDocumentoEntrada;
import br.com.gado.repositories.IDocumentoEntradaItem;
import br.com.gado.repositories.IInsumo;
import br.com.gado.repositories.IParceiro;
import br.com.gado.repositories.IUsuario;
import br.com.gado.util.SenhaUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Documentos de Entrada: importação de NF-e via XML, cadastro manual de recibos não fiscais,
 * fluxo de aprovação, edição segura (com senha) e vinculação de itens ao catálogo.
 *
 * A identidade de quem chama vem do JWT autenticado (o controller resolve o e-mail via
 * SecurityUtils.currentUserEmail() e o repassa aqui), e a permissão é verificada manualmente
 * neste service, comparando EUsuario.perfil contra os conjuntos abaixo. Por isso os métodos
 * sensíveis recebem um parâmetro a mais de e-mail.
 */
@Service
public class SDocumentoEntrada {

    /** Regra 1: módulo inteiro só acessível a ADMINISTRADOR, GERENTE e FINANCEIRO. */
    private static final Set<EnPerfilUsuario> PERFIS_MODULO =
            EnumSet.of(EnPerfilUsuario.ADMINISTRADOR, EnPerfilUsuario.GERENTE, EnPerfilUsuario.FINANCEIRO);

    /** Regra 2: alterar valores de NF-e e aprovar/recusar despesas é exclusivo de Admin/Gerente. */
    private static final Set<EnPerfilUsuario> PERFIS_GERENCIAIS =
            EnumSet.of(EnPerfilUsuario.ADMINISTRADOR, EnPerfilUsuario.GERENTE);

    @Autowired
    private IDocumentoEntrada documentoInterface;

    @Autowired
    private IDocumentoEntradaItem itemInterface;

    @Autowired
    private IParceiro parceiroInterface;

    @Autowired
    private IInsumo insumoInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private SLancamentoFinanceiro lancamentoFinanceiroService;

    @Autowired
    private SInsumo insumoService;

    // ── Permissões ───────────────────────────────────────────────────────

    private EUsuario resolveUsuarioModulo(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do usuário responsável pela operação.");
        }
        EUsuario usuario = usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (!PERFIS_MODULO.contains(usuario.getPerfil())) {
            throw new IllegalArgumentException(
                    "Apenas Administrador, Gerente ou Financeiro podem acessar o módulo financeiro.");
        }
        return usuario;
    }

    private EUsuario resolveUsuarioGerencial(String emailUsuario) {
        EUsuario usuario = resolveUsuarioModulo(emailUsuario);
        if (!PERFIS_GERENCIAIS.contains(usuario.getPerfil())) {
            throw new IllegalArgumentException("Apenas Administrador ou Gerente podem realizar esta ação.");
        }
        return usuario;
    }

    // ── Regra 1: Importação XML (mock inicial) ────────────────────────────

    /**
     * Simula a extração de dados de um XML de NF-e e salva o documento. NF-e é um documento
     * fiscal já validado pela SEFAZ, então nasce direto APROVADO (diferente do RECIBO_SIMPLES,
     * que sempre nasce pendente) e já gera lançamento no razão financeiro.
     *
     * TODO: substituir simularExtracaoXml por um parser real do XML padrão nfeProc (JAXB contra
     * o schema da NF-e), extraindo emitente por CNPJ, itens por NCM/EAN, impostos etc.
     */
    @Transactional
    public DocumentoEntradaRespostaDto importarNfeXml(MultipartFile file, Long fornecedorId, String emailUsuarioLogado) throws IOException {
        resolveUsuarioModulo(emailUsuarioLogado);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Selecione o arquivo XML da NF-e.");
        }
        if (fornecedorId == null) {
            throw new IllegalArgumentException("Selecione o fornecedor.");
        }
        EParceiro fornecedor = parceiroInterface.findById(fornecedorId)
                .orElseThrow(() -> new IllegalArgumentException("Fornecedor não encontrado."));

        DadosNfeExtraidos dados = simularExtracaoXml(file);

        if (documentoInterface.findByChaveAcessoNfe(dados.chaveAcesso).isPresent()) {
            throw new IllegalArgumentException("Esta NF-e já foi importada (chave de acesso duplicada).");
        }

        EDocumentoEntrada documento = new EDocumentoEntrada();
        documento.setTipoDocumento(EnTipoDocumentoFinanceiro.NF_E);
        documento.setStatusAprovacao(EnStatusAprovacaoFinanceira.APROVADO);
        documento.setNumeroDocumento(dados.numeroDocumento);
        documento.setSerie(dados.serie);
        documento.setChaveAcessoNfe(dados.chaveAcesso);
        documento.setDataEmissao(dados.dataEmissao);
        documento.setDataEntrada(LocalDate.now());
        documento.setValorTotal(dados.valorTotal);
        documento.setXmlOriginal(new String(file.getBytes(), StandardCharsets.UTF_8));
        documento.setFornecedor(fornecedor);
        documento.setCriadoPorEmail(emailUsuarioLogado.trim());
        documento.setAprovadoPorEmail(emailUsuarioLogado.trim());
        documento.setAprovadoEm(LocalDateTime.now());

        List<EDocumentoEntradaItem> itens = new ArrayList<>();
        for (ItemExtraido itemExtraido : dados.itens) {
            EDocumentoEntradaItem item = new EDocumentoEntradaItem();
            item.setDocumentoEntrada(documento);
            item.setDescricaoXml(itemExtraido.descricao);
            item.setCodigoXml(itemExtraido.codigo);
            item.setQuantidade(itemExtraido.quantidade);
            item.setValorUnitario(itemExtraido.valorUnitario);
            item.setValorTotal(itemExtraido.valorTotal);
            item.setVinculado(false);
            itens.add(item);
        }
        documento.setItens(itens);

        EDocumentoEntrada salvo = documentoInterface.save(documento);

        for (EDocumentoEntradaItem item : salvo.getItens()) {
            lancamentoFinanceiroService.registrarOuAtualizarSaidaItemDocumento(item, emailUsuarioLogado);
            aplicarEntradaEstoqueSeNecessario(item, salvo.getDataEmissao());
        }

        return toRespostaDto(salvo);
    }

    /**
     * Dá entrada no saldo/custo médio do produto vinculado a este item, uma única vez
     * (controlado por entradaEstoqueAplicada) — chamado quando o documento se torna APROVADO
     * (ou, se o produto só for vinculado depois, no momento da vinculação a um documento já
     * aprovado). Itens sem produto vinculado (ex: NF-e ainda não conciliada) não afetam estoque.
     */
    private void aplicarEntradaEstoqueSeNecessario(EDocumentoEntradaItem item, LocalDate dataEmissao) {
        if (item.getProduto() == null || Boolean.TRUE.equals(item.getEntradaEstoqueAplicada())) {
            return;
        }

        EntradaEstoqueDto entradaDto = new EntradaEstoqueDto();
        entradaDto.setQuantidade(item.getQuantidade().doubleValue());
        entradaDto.setPrecoUnitario(item.getValorUnitario().doubleValue());
        entradaDto.setDataEntrada(dataEmissao.atStartOfDay());

        insumoService.aplicarEntradaEstoque(item.getProduto().getId(), entradaDto);

        item.setEntradaEstoqueAplicada(true);
        itemInterface.save(item);
    }

    private DadosNfeExtraidos simularExtracaoXml(MultipartFile file) {
        String chave = UUID.randomUUID().toString().replaceAll("[^0-9]", "");
        chave = (chave + "00000000000000000000000000000000000000000000").substring(0, 44);

        DadosNfeExtraidos dados = new DadosNfeExtraidos();
        dados.numeroDocumento = "MOCK-" + System.currentTimeMillis();
        dados.serie = "1";
        dados.chaveAcesso = chave;
        dados.dataEmissao = LocalDate.now();
        dados.valorTotal = new BigDecimal("100.00");

        ItemExtraido item = new ItemExtraido();
        item.descricao = "Item extraído do XML (mock) - " + file.getOriginalFilename();
        item.codigo = "MOCK";
        item.quantidade = BigDecimal.ONE;
        item.valorUnitario = new BigDecimal("100.00");
        item.valorTotal = new BigDecimal("100.00");
        dados.itens = List.of(item);

        return dados;
    }

    private static final class DadosNfeExtraidos {
        String numeroDocumento;
        String serie;
        String chaveAcesso;
        LocalDate dataEmissao;
        BigDecimal valorTotal;
        List<ItemExtraido> itens;
    }

    private static final class ItemExtraido {
        String descricao;
        String codigo;
        BigDecimal quantidade;
        BigDecimal valorUnitario;
        BigDecimal valorTotal;
    }

    // ── Cadastro manual de RECIBO_SIMPLES (nasce sempre PENDENTE) ─────────

    @Transactional
    public DocumentoEntradaRespostaDto cadastrarReciboSimples(ReciboSimplesCadastroDto dto, String emailUsuarioLogado) {
        resolveUsuarioModulo(emailUsuarioLogado);

        EInsumo produto = insumoInterface.findByIdAndStatus(dto.getProdutoId(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Produto não encontrado ou inativo. Cadastre o produto no catálogo antes de lançar o recibo."));

        EDocumentoEntrada documento = new EDocumentoEntrada();
        documento.setTipoDocumento(EnTipoDocumentoFinanceiro.RECIBO_SIMPLES);
        documento.setStatusAprovacao(EnStatusAprovacaoFinanceira.PENDENTE_APROVACAO);
        documento.setDataEmissao(dto.getDataEmissao());
        documento.setDataEntrada(LocalDate.now());
        documento.setValorTotal(dto.getValorTotal());
        documento.setCriadoPorEmail(emailUsuarioLogado.trim());

        if (dto.getFornecedorId() != null) {
            EParceiro fornecedor = parceiroInterface.findById(dto.getFornecedorId())
                    .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado."));
            documento.setFornecedor(fornecedor);
        }

        BigDecimal valorUnitario = dto.getValorTotal().divide(dto.getQuantidade(), 4, java.math.RoundingMode.HALF_UP);

        EDocumentoEntradaItem item = new EDocumentoEntradaItem();
        item.setDocumentoEntrada(documento);
        item.setDescricaoXml(dto.getDescricao().trim());
        item.setQuantidade(dto.getQuantidade());
        item.setValorUnitario(valorUnitario);
        item.setValorTotal(dto.getValorTotal());
        item.setProduto(produto);
        item.setVinculado(true);
        item.setVinculadoPorEmail(emailUsuarioLogado.trim());
        item.setVinculadoEm(LocalDateTime.now());
        item.setNaturezaFinanceira(dto.getNaturezaFinanceira());
        documento.setItens(List.of(item));

        // Não gera lançamento financeiro nem entrada de estoque aqui: um recibo PENDENTE ainda
        // não é despesa confirmada — ambos só se aplicam após aprovarDocumento.
        EDocumentoEntrada salvo = documentoInterface.save(documento);
        return toRespostaDto(salvo);
    }

    // ── Regra 3: Aprovação/recusa (exclusivo Admin/Gerente) ───────────────

    @Transactional
    public DocumentoEntradaRespostaDto aprovarDocumento(Long idDocumento, String emailUsuarioLogado) {
        resolveUsuarioGerencial(emailUsuarioLogado);

        EDocumentoEntrada documento = documentoInterface.findById(idDocumento)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado."));

        if (documento.getStatusAprovacao() != EnStatusAprovacaoFinanceira.PENDENTE_APROVACAO) {
            throw new IllegalStateException("Este documento não está pendente de aprovação.");
        }

        documento.setStatusAprovacao(EnStatusAprovacaoFinanceira.APROVADO);
        documento.setAprovadoPorEmail(emailUsuarioLogado.trim());
        documento.setAprovadoEm(LocalDateTime.now());
        EDocumentoEntrada salvo = documentoInterface.save(documento);

        for (EDocumentoEntradaItem item : salvo.getItens()) {
            lancamentoFinanceiroService.registrarOuAtualizarSaidaItemDocumento(item, emailUsuarioLogado);
            aplicarEntradaEstoqueSeNecessario(item, salvo.getDataEmissao());
        }

        return toRespostaDto(salvo);
    }

    /**
     * Exclui/estorna um documento de entrada já lançado: reverte a entrada de estoque aplicada
     * (se houver) e o lançamento financeiro correspondente, e inativa o documento. Restrito aos
     * mesmos perfis do módulo (Gerente, Administrador, Financeiro).
     *
     * Limitação conhecida: o custo médio ponderado do produto não é recalculado retroativamente
     * ao "como era antes" desta entrada (isso exigiria um histórico completo de movimentações) —
     * apenas a quantidade é revertida do saldo. Bloqueamos a exclusão se o saldo já foi consumido
     * a ponto de não comportar a reversão (ficaria negativo).
     */
    @Transactional
    public String excluirDocumento(Long idDocumento, String emailUsuarioLogado) {
        resolveUsuarioModulo(emailUsuarioLogado);

        EDocumentoEntrada documento = documentoInterface.findById(idDocumento)
                .filter(d -> d.getStatus() == EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado."));

        for (EDocumentoEntradaItem item : documento.getItens()) {
            if (Boolean.TRUE.equals(item.getEntradaEstoqueAplicada()) && item.getProduto() != null) {
                EInsumo produto = item.getProduto();
                double saldoAtual = produto.getSaldoAtual() != null ? produto.getSaldoAtual() : 0.0;
                double quantidade = item.getQuantidade().doubleValue();
                if (saldoAtual < quantidade) {
                    throw new IllegalArgumentException(String.format(
                            "Não é possível excluir: o produto \"%s\" já teve parte deste estoque consumida "
                                    + "(saldo atual %.2f, quantidade desta entrada %.2f).",
                            produto.getNome(), saldoAtual, quantidade));
                }
            }
        }

        for (EDocumentoEntradaItem item : documento.getItens()) {
            if (Boolean.TRUE.equals(item.getEntradaEstoqueAplicada()) && item.getProduto() != null) {
                EInsumo produto = item.getProduto();
                double saldoAtual = produto.getSaldoAtual() != null ? produto.getSaldoAtual() : 0.0;
                produto.setSaldoAtual(saldoAtual - item.getQuantidade().doubleValue());
                EInsumo produtoSalvo = insumoInterface.save(produto);

                insumoService.registrarMovimentacao(produtoSalvo, EnTipoMovimentacaoEstoque.SAIDA,
                        item.getQuantidade().doubleValue(), item.getValorUnitario().doubleValue(),
                        LocalDateTime.now(), documento.getFornecedor(), null, null);
            }
            lancamentoFinanceiroService.estornarSaidaItemDocumento(item);
        }

        documento.setStatus(EnStatus.I);
        documentoInterface.save(documento);

        return "Documento excluído com sucesso.";
    }

    @Transactional
    public DocumentoEntradaRespostaDto recusarDocumento(Long idDocumento, RecusaDocumentoDto dto, String emailUsuarioLogado) {
        resolveUsuarioGerencial(emailUsuarioLogado);

        EDocumentoEntrada documento = documentoInterface.findById(idDocumento)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado."));

        if (documento.getStatusAprovacao() != EnStatusAprovacaoFinanceira.PENDENTE_APROVACAO) {
            throw new IllegalStateException("Este documento não está pendente de aprovação.");
        }

        documento.setStatusAprovacao(EnStatusAprovacaoFinanceira.RECUSADO);
        documento.setJustificativaRecusa(dto.getJustificativa().trim());
        // Nunca esteve APROVADO, então não existe lançamento financeiro a reverter.
        return toRespostaDto(documentoInterface.save(documento));
    }

    // ── Regra 2: Edição segura de NF-e (dupla validação por senha) ────────

    /**
     * Corrige dados extraídos incorretamente de uma NF-e. Restrito a Administrador/Gerente
     * (Financeiro só pode vincular produto — ver vincularProduto) e exige a senha de quem está
     * chamando, conferida via SenhaUtil contra o mesmo hash usado no login (SUsuario).
     */
    @Transactional
    public DocumentoEntradaRespostaDto editarNfe(Long idNfe, NfeUpdateDto dto, String senhaConfirmacao,
                                                  String emailUsuarioLogado) {
        EUsuario usuario = resolveUsuarioGerencial(emailUsuarioLogado);

        if (!SenhaUtil.confere(senhaConfirmacao, usuario.getSenha())) {
            throw new IllegalArgumentException("Senha de confirmação inválida.");
        }

        EDocumentoEntrada documento = documentoInterface.findById(idNfe)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado."));

        if (documento.getTipoDocumento() != EnTipoDocumentoFinanceiro.NF_E) {
            throw new IllegalArgumentException("editarNfe só se aplica a documentos do tipo NF_E.");
        }

        if (dto.getNumeroDocumento() != null) {
            documento.setNumeroDocumento(dto.getNumeroDocumento().trim());
        }
        if (dto.getSerie() != null) {
            documento.setSerie(dto.getSerie().trim());
        }
        if (dto.getDataEmissao() != null) {
            documento.setDataEmissao(dto.getDataEmissao());
        }
        if (dto.getFornecedorId() != null) {
            EParceiro fornecedor = parceiroInterface.findById(dto.getFornecedorId())
                    .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado."));
            documento.setFornecedor(fornecedor);
        }
        if (dto.getValorTotal() != null) {
            documento.setValorTotal(dto.getValorTotal());
        }

        boolean documentoJaAprovado = documento.getStatusAprovacao() == EnStatusAprovacaoFinanceira.APROVADO;

        if (dto.getItens() != null) {
            for (NfeItemUpdateDto itemDto : dto.getItens()) {
                EDocumentoEntradaItem item = documento.getItens().stream()
                        .filter(i -> i.getId().equals(itemDto.getItemId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                "O item " + itemDto.getItemId() + " não pertence a este documento."));

                if (itemDto.getQuantidade() != null) {
                    item.setQuantidade(itemDto.getQuantidade());
                }
                if (itemDto.getValorUnitario() != null) {
                    item.setValorUnitario(itemDto.getValorUnitario());
                }
                if (itemDto.getValorTotal() != null) {
                    item.setValorTotal(itemDto.getValorTotal());
                }

                if (documentoJaAprovado) {
                    lancamentoFinanceiroService.registrarOuAtualizarSaidaItemDocumento(item, emailUsuarioLogado);
                }
            }
        }

        documento.setUltimaEdicaoPorEmail(emailUsuarioLogado.trim());
        documento.setUltimaEdicaoEm(LocalDateTime.now());

        return toRespostaDto(documentoInterface.save(documento));
    }

    // ── Vinculação de produto (Financeiro pode; nunca altera valores) ────

    /**
     * Liga o item importado ao catálogo (EInsumo) e reclassifica sua natureza financeira pelo
     * grupoProduto do produto vinculado. VincularProdutoDto só carrega o id do produto — não há
     * como este método alterar quantidade/valor, o que torna a restrição "Financeiro só vincula,
     * não altera valores" estrutural, não apenas uma checagem de perfil.
     */
    @Transactional
    public DocumentoEntradaItemRespostaDto vincularProduto(Long idItem, VincularProdutoDto dto, String emailUsuarioLogado) {
        resolveUsuarioModulo(emailUsuarioLogado);

        EDocumentoEntradaItem item = itemInterface.findById(idItem)
                .orElseThrow(() -> new EntityNotFoundException("Item não encontrado."));

        EInsumo produto = insumoInterface.findByIdAndStatus(dto.getProdutoId(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Produto do catálogo não encontrado ou inativo."));

        item.setProduto(produto);
        item.setVinculado(true);
        item.setVinculadoPorEmail(emailUsuarioLogado.trim());
        item.setVinculadoEm(LocalDateTime.now());
        if (produto.getGrupoProduto() != null) {
            item.setNaturezaFinanceira(produto.getGrupoProduto().getNaturezaFinanceira());
        }

        EDocumentoEntradaItem salvo = itemInterface.save(item);

        if (salvo.getDocumentoEntrada().getStatusAprovacao() == EnStatusAprovacaoFinanceira.APROVADO) {
            lancamentoFinanceiroService.registrarOuAtualizarSaidaItemDocumento(salvo, emailUsuarioLogado);
            aplicarEntradaEstoqueSeNecessario(salvo, salvo.getDocumentoEntrada().getDataEmissao());
        }

        return toItemRespostaDto(salvo);
    }

    // ── Leitura ────────────────────────────────────────────────────────

    @Transactional
    public DocumentoEntradaRespostaDto buscarPorId(Long id, String emailUsuarioLogado) {
        resolveUsuarioModulo(emailUsuarioLogado);
        EDocumentoEntrada documento = documentoInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado."));
        return toRespostaDto(documento);
    }

    @Transactional
    public List<DocumentoEntradaRespostaDto> listarTodos(String emailUsuarioLogado) {
        resolveUsuarioModulo(emailUsuarioLogado);
        return documentoInterface.findByStatusOrderByDataEntradaDesc(EnStatus.A)
                .stream().map(this::toRespostaDto).collect(Collectors.toList());
    }

    @Transactional
    public List<DocumentoEntradaRespostaDto> listarPendentesAprovacao(String emailUsuarioLogado) {
        resolveUsuarioModulo(emailUsuarioLogado);
        return documentoInterface.findByStatusAprovacaoOrderByDataEntradaDesc(EnStatusAprovacaoFinanceira.PENDENTE_APROVACAO)
                .stream().map(this::toRespostaDto).collect(Collectors.toList());
    }

    // ── Mapeamento ────────────────────────────────────────────────────

    private DocumentoEntradaRespostaDto toRespostaDto(EDocumentoEntrada documento) {
        DocumentoEntradaRespostaDto dto = new DocumentoEntradaRespostaDto();
        dto.setId(documento.getId());
        dto.setTipoDocumento(documento.getTipoDocumento());
        dto.setStatusAprovacao(documento.getStatusAprovacao());
        dto.setNumeroDocumento(documento.getNumeroDocumento());
        dto.setSerie(documento.getSerie());
        dto.setChaveAcessoNfe(documento.getChaveAcessoNfe());
        dto.setDataEmissao(documento.getDataEmissao());
        dto.setDataEntrada(documento.getDataEntrada());
        dto.setValorTotal(documento.getValorTotal());
        dto.setJustificativaRecusa(documento.getJustificativaRecusa());
        dto.setCriadoPorEmail(documento.getCriadoPorEmail());
        dto.setAprovadoPorEmail(documento.getAprovadoPorEmail());
        dto.setAprovadoEm(documento.getAprovadoEm());
        dto.setUltimaEdicaoPorEmail(documento.getUltimaEdicaoPorEmail());
        dto.setUltimaEdicaoEm(documento.getUltimaEdicaoEm());

        if (documento.getFornecedor() != null) {
            dto.setFornecedorId(documento.getFornecedor().getId());
            dto.setFornecedorNome(documento.getFornecedor().getNome());
        }

        dto.setItens(documento.getItens().stream().map(this::toItemRespostaDto).collect(Collectors.toList()));
        return dto;
    }

    private DocumentoEntradaItemRespostaDto toItemRespostaDto(EDocumentoEntradaItem item) {
        DocumentoEntradaItemRespostaDto dto = new DocumentoEntradaItemRespostaDto();
        dto.setId(item.getId());
        dto.setDescricaoXml(item.getDescricaoXml());
        dto.setCodigoXml(item.getCodigoXml());
        dto.setQuantidade(item.getQuantidade());
        dto.setValorUnitario(item.getValorUnitario());
        dto.setValorTotal(item.getValorTotal());
        dto.setVinculado(item.getVinculado());
        dto.setVinculadoPorEmail(item.getVinculadoPorEmail());
        dto.setVinculadoEm(item.getVinculadoEm());
        dto.setNaturezaFinanceira(item.getNaturezaFinanceira());
        if (item.getProduto() != null) {
            dto.setProdutoId(item.getProduto().getId());
            dto.setProdutoNome(item.getProduto().getNome());
        }
        return dto;
    }
}
