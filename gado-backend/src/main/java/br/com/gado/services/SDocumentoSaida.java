package br.com.gado.services;

import br.com.gado.dto.documentoSaidaDto.DocumentoSaidaRespostaDto;
import br.com.gado.dto.documentoSaidaDto.VendaAnimalCadastroDto;
import br.com.gado.dto.documentoSaidaDto.VendaLeiteCadastroDto;
import br.com.gado.dto.documentoSaidaDto.VendaLeiteItemCadastroDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.EDocumentoSaida;
import br.com.gado.entities.ELote;
import br.com.gado.entities.ELoteSetor;
import br.com.gado.entities.EMetaSetor;
import br.com.gado.entities.EUsuario;
import br.com.gado.entities.EVendaAnimalItem;
import br.com.gado.entities.EVendaLeiteItem;
import br.com.gado.entities.EVendaMetaLote;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnStatusAnimal;
import br.com.gado.enums.EnTipoDocumentoSaida;
import br.com.gado.enums.EnTipoMeta;
import br.com.gado.repositories.IDocumentoSaida;
import br.com.gado.repositories.ILote;
import br.com.gado.repositories.ILoteSetor;
import br.com.gado.repositories.IMetaSetor;
import br.com.gado.repositories.IAnimal;
import br.com.gado.repositories.IUsuario;
import br.com.gado.repositories.IVendaMetaLote;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Vendas (leite e animais) — geram receita para a fazenda e, no caso de animais, dão baixa
 * automática no status do animal (VENDIDO/ABATIDO) e o liberam de qualquer setor alocado.
 */
@Service
public class SDocumentoSaida {

    private static final Set<EnPerfilUsuario> PERFIS_MODULO =
            EnumSet.of(EnPerfilUsuario.ADMINISTRADOR, EnPerfilUsuario.GERENTE, EnPerfilUsuario.FINANCEIRO);

    @Autowired
    private IDocumentoSaida documentoSaidaInterface;

    @Autowired
    private ILote loteInterface;

    @Autowired
    private ILoteSetor loteSetorInterface;

    @Autowired
    private IAnimal animalInterface;

    @Autowired
    private IMetaSetor metaSetorInterface;

    @Autowired
    private IVendaMetaLote vendaMetaLoteInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private SLancamentoFinanceiro lancamentoFinanceiroService;

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

    // ── Venda de leite ───────────────────────────────────────────────────

    @Transactional
    public DocumentoSaidaRespostaDto cadastrarVendaLeite(VendaLeiteCadastroDto dto, String emailUsuarioLogado) {
        resolveUsuarioModulo(emailUsuarioLogado);

        BigDecimal totalLitros = dto.getItens().stream()
                .map(VendaLeiteItemCadastroDto::getLitros)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal precoLitro = dto.getPrecoLitro();
        BigDecimal valorTotal = dto.getValorTotal();
        if (precoLitro == null && valorTotal == null) {
            throw new IllegalArgumentException("Informe o preço por litro ou o valor total da venda.");
        }
        if (precoLitro == null) {
            precoLitro = valorTotal.divide(totalLitros, 4, RoundingMode.HALF_UP);
        }
        if (valorTotal == null) {
            valorTotal = precoLitro.multiply(totalLitros).setScale(2, RoundingMode.HALF_UP);
        }

        EDocumentoSaida documento = new EDocumentoSaida();
        documento.setTipoDocumento(EnTipoDocumentoSaida.VENDA_LEITE);
        documento.setNumeroDocumento(dto.getNumeroDocumento());
        documento.setChaveAcesso(dto.getChaveAcesso());
        documento.setDataEmissao(dto.getDataEmissao());
        documento.setValorTotal(valorTotal);
        documento.setCriadoPorEmail(emailUsuarioLogado.trim());

        List<EVendaLeiteItem> itens = new ArrayList<>();
        for (VendaLeiteItemCadastroDto itemDto : dto.getItens()) {
            ELote lote = loteInterface.findByIdAndStatus(itemDto.getLoteId(), EnStatus.A)
                    .orElseThrow(() -> new IllegalArgumentException("Lote não encontrado ou inativo: " + itemDto.getLoteId()));

            EVendaLeiteItem item = new EVendaLeiteItem();
            item.setDocumentoSaida(documento);
            item.setLote(lote);
            item.setLitros(itemDto.getLitros());
            item.setPrecoLitro(precoLitro);
            item.setValorTotal(itemDto.getLitros().multiply(precoLitro).setScale(2, RoundingMode.HALF_UP));
            itens.add(item);
        }
        documento.setItensLeite(itens);

        EDocumentoSaida salvo = documentoSaidaInterface.save(documento);

        for (EVendaLeiteItem item : salvo.getItensLeite()) {
            registrarVendidoNaMeta(item.getLote(), item.getLitros().doubleValue(), salvo.getDataEmissao(), emailUsuarioLogado);
        }

        lancamentoFinanceiroService.registrarEntradaVenda(
                salvo.getId(), "Venda de leite" + (salvo.getNumeroDocumento() != null ? " - NF " + salvo.getNumeroDocumento() : ""),
                salvo.getValorTotal(), salvo.getDataEmissao(), emailUsuarioLogado.trim());

        return toRespostaDto(salvo);
    }

    /**
     * Se houver uma EMetaSetor de LEITE ativa no setor onde o lote está atualmente alocado,
     * cobrindo a data da venda, registra os litros vendidos para alimentar a barra "Vendido".
     * Um lote alocado a mais de um setor conta a venda para a primeira meta de LEITE encontrada.
     */
    private void registrarVendidoNaMeta(ELote lote, double litros, LocalDate dataVenda, String emailUsuarioLogado) {
        List<ELoteSetor> alocacoes = loteSetorInterface.findByLote_Id(lote.getId());
        for (ELoteSetor alocacao : alocacoes) {
            List<EMetaSetor> metas = metaSetorInterface.findBySetor_IdAndStatus(alocacao.getSetor().getId(), EnStatus.A);
            for (EMetaSetor meta : metas) {
                if (meta.getTipoMeta() != EnTipoMeta.LEITE) continue;
                if (dataVenda.isBefore(meta.getDataInicial()) || dataVenda.isAfter(meta.getDataFinal())) continue;

                EVendaMetaLote venda = new EVendaMetaLote();
                venda.setMetaSetor(meta);
                venda.setLote(lote);
                venda.setDataVenda(dataVenda);
                venda.setLitrosVendidos(litros);
                venda.setCriadoPorEmail(emailUsuarioLogado != null ? emailUsuarioLogado.trim() : null);
                vendaMetaLoteInterface.save(venda);
                return;
            }
        }
    }

    // ── Venda / abate de animais ─────────────────────────────────────────

    @Transactional
    public DocumentoSaidaRespostaDto cadastrarVendaAnimal(VendaAnimalCadastroDto dto, String emailUsuarioLogado) {
        resolveUsuarioModulo(emailUsuarioLogado);

        if (dto.getDestino() != EnStatusAnimal.VENDIDO && dto.getDestino() != EnStatusAnimal.ABATIDO) {
            throw new IllegalArgumentException("Destino inválido: use VENDIDO (vivo) ou ABATIDO.");
        }

        List<EAnimal> animais = animalInterface.findAllById(dto.getAnimalIds());
        if (animais.size() != dto.getAnimalIds().size()) {
            throw new IllegalArgumentException("Um ou mais animais informados não foram encontrados.");
        }
        for (EAnimal animal : animais) {
            if (animal.getStatusAnimal() != EnStatusAnimal.ATIVO && animal.getStatusAnimal() != EnStatusAnimal.OBSERVACAO) {
                throw new IllegalArgumentException(
                        "O animal " + animal.getCodigoBrinco() + " possui status " + animal.getStatusAnimal()
                                + " e não pode ser vendido/abatido.");
            }
        }

        EDocumentoSaida documento = new EDocumentoSaida();
        documento.setTipoDocumento(EnTipoDocumentoSaida.VENDA_ANIMAL);
        documento.setNumeroDocumento(dto.getNumeroDocumento());
        documento.setChaveAcesso(dto.getChaveAcesso());
        documento.setDataEmissao(dto.getDataEmissao());
        documento.setValorTotal(dto.getValorTotal());
        documento.setCriadoPorEmail(emailUsuarioLogado.trim());

        BigDecimal valorPorAnimal = dto.getValorTotal()
                .divide(BigDecimal.valueOf(animais.size()), 2, RoundingMode.HALF_UP);

        List<EVendaAnimalItem> itens = new ArrayList<>();
        for (EAnimal animal : animais) {
            // Libera o animal de qualquer setor onde esteja alocado — status VENDIDO/ABATIDO
            // não deve mais contar para a capacidade do setor.
            for (ELoteSetor alocacao : loteSetorInterface.findByAnimalIdAndLoteAtivo(animal.getId(), EnStatus.A)) {
                alocacao.getAnimais().removeIf(a -> a.getId().equals(animal.getId()));
                loteSetorInterface.save(alocacao);
            }

            animal.setStatusAnimal(dto.getDestino());
            animalInterface.save(animal);

            EVendaAnimalItem item = new EVendaAnimalItem();
            item.setDocumentoSaida(documento);
            item.setAnimal(animal);
            item.setDestino(dto.getDestino());
            item.setValorVenda(valorPorAnimal);
            itens.add(item);
        }
        documento.setItensAnimal(itens);

        EDocumentoSaida salvo = documentoSaidaInterface.save(documento);

        lancamentoFinanceiroService.registrarEntradaVenda(
                salvo.getId(),
                (dto.getDestino() == EnStatusAnimal.ABATIDO ? "Abate de animais" : "Venda de animais")
                        + (salvo.getNumeroDocumento() != null ? " - NF " + salvo.getNumeroDocumento() : ""),
                salvo.getValorTotal(), salvo.getDataEmissao(), emailUsuarioLogado.trim());

        return toRespostaDto(salvo);
    }

    // ── Leitura ──────────────────────────────────────────────────────────

    @Transactional
    public List<DocumentoSaidaRespostaDto> listarTodos(String emailUsuarioLogado) {
        resolveUsuarioModulo(emailUsuarioLogado);
        return documentoSaidaInterface.findByStatusOrderByDataEmissaoDesc(EnStatus.A)
                .stream().map(this::toRespostaDto).collect(Collectors.toList());
    }

    private DocumentoSaidaRespostaDto toRespostaDto(EDocumentoSaida documento) {
        DocumentoSaidaRespostaDto dto = new DocumentoSaidaRespostaDto();
        dto.setId(documento.getId());
        dto.setTipoDocumento(documento.getTipoDocumento());
        dto.setNumeroDocumento(documento.getNumeroDocumento());
        dto.setChaveAcesso(documento.getChaveAcesso());
        dto.setDataEmissao(documento.getDataEmissao());
        dto.setValorTotal(documento.getValorTotal());
        dto.setCriadoPorEmail(documento.getCriadoPorEmail());

        dto.setItensLeite(documento.getItensLeite().stream().map(i -> {
            DocumentoSaidaRespostaDto.ItemLeiteDto item = new DocumentoSaidaRespostaDto.ItemLeiteDto();
            item.setId(i.getId());
            item.setLoteId(i.getLote().getId());
            item.setLoteCodigo(i.getLote().getCodigo());
            item.setLitros(i.getLitros());
            item.setPrecoLitro(i.getPrecoLitro());
            item.setValorTotal(i.getValorTotal());
            return item;
        }).collect(Collectors.toList()));

        dto.setItensAnimal(documento.getItensAnimal().stream().map(i -> {
            DocumentoSaidaRespostaDto.ItemAnimalDto item = new DocumentoSaidaRespostaDto.ItemAnimalDto();
            item.setId(i.getId());
            item.setAnimalId(i.getAnimal().getId());
            item.setAnimalCodigoBrinco(i.getAnimal().getCodigoBrinco());
            item.setDestino(i.getDestino());
            item.setValorVenda(i.getValorVenda());
            return item;
        }).collect(Collectors.toList()));

        return dto;
    }
}
