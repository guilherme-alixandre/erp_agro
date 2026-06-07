package br.com.gado.application.services;

import br.com.gado.application.dto.SetorDto;
import br.com.gado.application.dto.loteDto.LoteRespostaDto;
import br.com.gado.application.dto.metaSetorDto.MetaSetorRespostaDto;
import br.com.gado.domain.enums.EnStatus;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SPdfRelatorio {

    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color COR_CABECALHO = new Color(45, 80, 22);
    private static final Color COR_LINHA_PAR = new Color(240, 248, 234);

    @Autowired
    private SSetor setorService;

    @Autowired
    private SLote loteService;

    @Autowired
    private SMetaSetor metaSetorService;

    // ── Relatório de Setores ──────────────────────────────────────────────────

    public byte[] gerarRelatorioSetores() {
        List<SetorDto> setores = setorService.buscarTodos();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            addTitulo(doc, "Relatório de Setores");
            addDataGeracao(doc);

            if (setores.isEmpty()) {
                doc.add(new Paragraph("Nenhum setor cadastrado.", corpo()));
            } else {
                PdfPTable tabela = novaTabela(5, new float[]{3f, 2f, 2f, 1.5f, 3f});
                addCabecalho(tabela, "Nome", "Tipo", "Cap. Máxima", "Status", "Criado por");
                boolean par = false;
                for (SetorDto s : setores) {
                    Color bg = par ? COR_LINHA_PAR : Color.WHITE;
                    addCelula(tabela, s.getNome(), bg);
                    addCelula(tabela, s.getTipo() != null ? s.getTipo().name() : "-", bg);
                    addCelula(tabela, String.valueOf(s.getCapacidadeMaxima()), bg);
                    addCelula(tabela, statusLabel(s.getStatus()), bg);
                    addCelula(tabela, coalesce(s.getCriadoPorNome()), bg);
                    par = !par;
                }
                doc.add(tabela);
                addRodape(doc, setores.size() + " setor(es) listado(s)");
            }
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF de setores.", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // ── Relatório de Lotes ────────────────────────────────────────────────────

    public byte[] gerarRelatorioLotes() {
        List<LoteRespostaDto> lotes = loteService.listarTodos();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            addTitulo(doc, "Relatório de Lotes");
            addDataGeracao(doc);

            if (lotes.isEmpty()) {
                doc.add(new Paragraph("Nenhum lote cadastrado.", corpo()));
            } else {
                PdfPTable tabela = novaTabela(7, new float[]{1.5f, 3f, 2f, 2f, 1.5f, 2f, 3f});
                addCabecalho(tabela, "Código", "Descrição", "Raça", "Cor Brinco", "Animais", "Data Criação", "Criado por");
                boolean par = false;
                for (LoteRespostaDto l : lotes) {
                    Color bg = par ? COR_LINHA_PAR : Color.WHITE;
                    addCelula(tabela, l.getCodigo(), bg);
                    addCelula(tabela, coalesce(l.getDescricao()), bg);
                    addCelula(tabela, coalesce(l.getRacaPredominante()), bg);
                    addCelula(tabela, l.getCorBrinco(), bg);
                    addCelula(tabela, String.valueOf(l.getTotalAnimais()), bg);
                    addCelula(tabela, l.getDataCriacao() != null ? l.getDataCriacao().format(BR_DATE) : "-", bg);
                    addCelula(tabela, coalesce(l.getCriadoPorNome()), bg);
                    par = !par;
                }
                doc.add(tabela);
                addRodape(doc, lotes.size() + " lote(s) listado(s)");
            }
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF de lotes.", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // ── Relatório de Metas ────────────────────────────────────────────────────

    public byte[] gerarRelatorioMetas(Long setorId) {
        List<MetaSetorRespostaDto> metas = metaSetorService.listarPorSetor(setorId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            addTitulo(doc, "Relatório de Metas por Setor");
            addDataGeracao(doc);

            if (metas.isEmpty()) {
                doc.add(new Paragraph("Nenhuma meta cadastrada para este setor.", corpo()));
            } else {
                PdfPTable tabela = novaTabela(8, new float[]{2f, 1.5f, 2f, 2f, 2f, 1.5f, 2.5f, 2.5f});
                addCabecalho(tabela,
                        "Setor", "Tipo", "Início", "Fim",
                        "Qtd. Esperada", "Progresso", "Valor Esperado", "Valor Realizado");
                boolean par = false;
                for (MetaSetorRespostaDto m : metas) {
                    Color bg = par ? COR_LINHA_PAR : Color.WHITE;
                    String unidade = "LEITE".equals(m.getTipoMeta() != null ? m.getTipoMeta().name() : "") ? "L" : "@";
                    addCelula(tabela, m.getSetorNome(), bg);
                    addCelula(tabela, m.getTipoMeta() != null ? m.getTipoMeta().name() : "-", bg);
                    addCelula(tabela, formatData(m.getDataInicial()), bg);
                    addCelula(tabela, formatData(m.getDataFinal()), bg);
                    addCelula(tabela, formatDouble(m.getQuantidadeEsperada()) + " " + unidade, bg);
                    addCelula(tabela, formatDouble(m.getPercentualProgresso()) + "%", bg);
                    addCelula(tabela, "R$ " + formatDouble(m.getValorEsperado()), bg);
                    addCelula(tabela, "R$ " + formatDouble(m.getValorRealizado()), bg);
                    par = !par;
                }
                doc.add(tabela);
                addRodape(doc, metas.size() + " meta(s) listada(s)");
            }
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF de metas.", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // ── Helpers de construção do PDF ──────────────────────────────────────────

    private void addTitulo(Document doc, String titulo) throws DocumentException {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Font.BOLD, COR_CABECALHO);
        Paragraph p = new Paragraph(titulo, font);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(4f);
        doc.add(p);
    }

    private void addDataGeracao(Document doc) throws DocumentException {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.ITALIC, Color.GRAY);
        Paragraph p = new Paragraph("Gerado em: " + LocalDate.now().format(BR_DATE), font);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(12f);
        doc.add(p);
    }

    private void addRodape(Document doc, String texto) throws DocumentException {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.ITALIC, Color.GRAY);
        Paragraph p = new Paragraph(texto, font);
        p.setAlignment(Element.ALIGN_RIGHT);
        p.setSpacingBefore(8f);
        doc.add(p);
    }

    private PdfPTable novaTabela(int colunas, float[] larguras) throws DocumentException {
        PdfPTable tabela = new PdfPTable(colunas);
        tabela.setWidthPercentage(100);
        tabela.setWidths(larguras);
        tabela.setSpacingBefore(10f);
        return tabela;
    }

    private void addCabecalho(PdfPTable tabela, String... colunas) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, Color.WHITE);
        for (String col : colunas) {
            PdfPCell cell = new PdfPCell(new Phrase(col, font));
            cell.setBackgroundColor(COR_CABECALHO);
            cell.setPadding(7f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tabela.addCell(cell);
        }
    }

    private void addCelula(PdfPTable tabela, String texto, Color bg) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 9);
        PdfPCell cell = new PdfPCell(new Phrase(texto != null ? texto : "-", font));
        cell.setBackgroundColor(bg);
        cell.setPadding(5f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tabela.addCell(cell);
    }

    private Font corpo() {
        return FontFactory.getFont(FontFactory.HELVETICA, 11);
    }

    // ── Utilitários ───────────────────────────────────────────────────────────

    private String statusLabel(EnStatus status) {
        if (status == null) return "-";
        return status == EnStatus.A ? "Ativo" : "Inativo";
    }

    private String coalesce(String valor) {
        return valor != null && !valor.isBlank() ? valor : "-";
    }

    private String formatData(LocalDate data) {
        return data != null ? data.format(BR_DATE) : "-";
    }

    private String formatDouble(Double valor) {
        if (valor == null) return "-";
        return String.format("%.2f", valor).replace(".", ",");
    }
}
