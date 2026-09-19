package br.com.gado.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser do XML padrão nfeProc/NFe (layout 4.00 da SEFAZ) — extrai apenas os campos usados por
 * SDocumentoEntrada.importarNfeXml: chave de acesso, número, série, data de emissão, valor total
 * e os itens (cProd/xProd/qCom/vUnCom/vProd). Não valida assinatura digital nem o schema completo
 * — a NF-e já chega autorizada pela SEFAZ; aqui só extraímos os dados para o Documento de Entrada.
 *
 * O XML vem de upload do usuário, então o DocumentBuilderFactory é configurado para bloquear
 * DOCTYPE/entidades externas (proteção contra XXE — leitura de arquivo local, SSRF, "billion laughs").
 */
public final class NfeXmlParser {

    private NfeXmlParser() {
    }

    public static DadosNfe parse(byte[] xmlBytes) {
        Document doc = parseDocument(xmlBytes);

        Element infNFe = primeiroElementoPorTag(doc, "infNFe");
        if (infNFe == null) {
            throw new IllegalArgumentException(
                    "XML inválido: não foi encontrada a tag <infNFe> — verifique se é um XML de NF-e (nfeProc).");
        }

        String chaveAcesso = extrairChaveAcesso(infNFe);

        Element ide = primeiroFilhoPorTag(infNFe, "ide");
        if (ide == null) {
            throw new IllegalArgumentException("XML inválido: tag <ide> não encontrada.");
        }
        String numeroDocumento = textoObrigatorio(ide, "nNF", "número da NF-e (nNF)");
        String serie = textoObrigatorio(ide, "serie", "série (serie)");
        LocalDate dataEmissao = extrairDataEmissao(ide);

        Element total = primeiroFilhoPorTag(infNFe, "total");
        Element icmsTot = total != null ? primeiroFilhoPorTag(total, "ICMSTot") : null;
        if (icmsTot == null) {
            throw new IllegalArgumentException("XML inválido: tag <total><ICMSTot> não encontrada.");
        }
        BigDecimal valorTotal = decimalObrigatorio(icmsTot, "vNF", "valor total (vNF)");

        List<ItemNfe> itens = extrairItens(infNFe);
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("XML inválido: nenhum item (<det>/<prod>) encontrado na NF-e.");
        }

        DadosNfe dados = new DadosNfe();
        dados.chaveAcesso = chaveAcesso;
        dados.numeroDocumento = numeroDocumento;
        dados.serie = serie;
        dados.dataEmissao = dataEmissao;
        dados.valorTotal = valorTotal;
        dados.itens = itens;
        return dados;
    }

    private static Document parseDocument(byte[] xmlBytes) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Bloqueia DOCTYPE por completo — sem ele não há como declarar entidade nenhuma
            // (nem interna, nem externa), o que já cobre leitura de arquivo local, SSRF e
            // "billion laughs". As duas flags abaixo são defesa em profundidade redundante.
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            try (InputStream input = new ByteArrayInputStream(xmlBytes)) {
                Document doc = builder.parse(new InputSource(input));
                doc.getDocumentElement().normalize();
                return doc;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Não foi possível interpretar o XML da NF-e: " + e.getMessage(), e);
        }
    }

    private static String extrairChaveAcesso(Element infNFe) {
        String id = infNFe.getAttribute("Id");
        String chave = id != null ? id.replaceAll("[^0-9]", "") : "";
        if (chave.length() != 44) {
            throw new IllegalArgumentException(
                    "XML inválido: não foi possível localizar a chave de acesso (44 dígitos) no atributo Id de <infNFe>.");
        }
        return chave;
    }

    private static LocalDate extrairDataEmissao(Element ide) {
        String dhEmi = textoDireto(ide, "dhEmi");
        if (dhEmi != null && !dhEmi.isBlank()) {
            try {
                return OffsetDateTime.parse(dhEmi).toLocalDate();
            } catch (Exception e) {
                throw new IllegalArgumentException("XML inválido: data de emissão (dhEmi) em formato inesperado.");
            }
        }
        String dEmi = textoDireto(ide, "dEmi");
        if (dEmi != null && !dEmi.isBlank()) {
            try {
                return LocalDate.parse(dEmi, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                throw new IllegalArgumentException("XML inválido: data de emissão (dEmi) em formato inesperado.");
            }
        }
        throw new IllegalArgumentException("XML inválido: data de emissão (dhEmi/dEmi) não encontrada em <ide>.");
    }

    private static List<ItemNfe> extrairItens(Element infNFe) {
        List<ItemNfe> itens = new ArrayList<>();
        NodeList filhos = infNFe.getChildNodes();
        for (int i = 0; i < filhos.getLength(); i++) {
            Node node = filhos.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE || !"det".equals(node.getNodeName())) {
                continue;
            }
            Element det = (Element) node;
            Element prod = primeiroFilhoPorTag(det, "prod");
            if (prod == null) {
                continue;
            }

            ItemNfe item = new ItemNfe();
            item.codigo = textoDireto(prod, "cProd");
            item.descricao = textoObrigatorio(prod, "xProd", "descrição do produto (xProd)");
            item.quantidade = decimalObrigatorio(prod, "qCom", "quantidade (qCom)");
            item.valorUnitario = decimalObrigatorio(prod, "vUnCom", "valor unitário (vUnCom)");
            item.valorTotal = decimalObrigatorio(prod, "vProd", "valor do item (vProd)");
            itens.add(item);
        }
        return itens;
    }

    // ── Helpers de navegação DOM ─────────────────────────────────────────

    private static Element primeiroElementoPorTag(Document doc, String tag) {
        NodeList lista = doc.getElementsByTagName(tag);
        return lista.getLength() > 0 ? (Element) lista.item(0) : null;
    }

    /** Só considera filhos DIRETOS (não desce em sub-elementos), evitando pegar a tag errada quando o mesmo nome aparece em outra parte do documento. */
    private static Element primeiroFilhoPorTag(Element pai, String tag) {
        NodeList filhos = pai.getChildNodes();
        for (int i = 0; i < filhos.getLength(); i++) {
            Node node = filhos.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && tag.equals(node.getNodeName())) {
                return (Element) node;
            }
        }
        return null;
    }

    private static String textoDireto(Element pai, String tag) {
        Element filho = primeiroFilhoPorTag(pai, tag);
        return filho != null ? filho.getTextContent().trim() : null;
    }

    private static String textoObrigatorio(Element pai, String tag, String descricaoCampo) {
        String texto = textoDireto(pai, tag);
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("XML inválido: " + descricaoCampo + " não encontrado(a).");
        }
        return texto;
    }

    private static BigDecimal decimalObrigatorio(Element pai, String tag, String descricaoCampo) {
        String texto = textoObrigatorio(pai, tag, descricaoCampo);
        try {
            return new BigDecimal(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "XML inválido: valor de " + descricaoCampo + " não é um número válido (\"" + texto + "\").");
        }
    }

    public static final class DadosNfe {
        public String chaveAcesso;
        public String numeroDocumento;
        public String serie;
        public LocalDate dataEmissao;
        public BigDecimal valorTotal;
        public List<ItemNfe> itens;
    }

    public static final class ItemNfe {
        public String codigo;
        public String descricao;
        public BigDecimal quantidade;
        public BigDecimal valorUnitario;
        public BigDecimal valorTotal;
    }
}
