package br.com.gado.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NfeXmlParserTest {

    private static final String CHAVE_44_DIGITOS = "35240111222333000181550010000123451987654321";

    private static byte[] recurso(String caminho) {
        try (InputStream in = NfeXmlParserTest.class.getResourceAsStream(caminho)) {
            if (in == null) {
                throw new IllegalStateException("Fixture não encontrada: " + caminho);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] xml(String conteudo) {
        return conteudo.getBytes(StandardCharsets.UTF_8);
    }

    private static String nfeCompleta(String tagIde, String tagTotal, String tagDet) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <nfeProc>
                  <NFe>
                    <infNFe Id="NFe%s" versao="4.00">
                      %s
                      %s
                      %s
                    </infNFe>
                  </NFe>
                </nfeProc>
                """.formatted(CHAVE_44_DIGITOS, tagIde, tagTotal, tagDet);
    }

    private static final String IDE_PADRAO = """
            <ide>
              <nNF>100</nNF>
              <serie>1</serie>
              <dhEmi>2026-01-15T10:00:00-03:00</dhEmi>
            </ide>""";

    private static final String TOTAL_PADRAO = """
            <total>
              <ICMSTot>
                <vNF>50.00</vNF>
              </ICMSTot>
            </total>""";

    private static final String DET_PADRAO = """
            <det nItem="1">
              <prod>
                <cProd>P1</cProd>
                <xProd>Produto Teste</xProd>
                <qCom>1.0000</qCom>
                <vUnCom>50.0000000000</vUnCom>
                <vProd>50.00</vProd>
              </prod>
            </det>""";

    @Nested
    class ParseValido {

        @Test
        void deveExtrairTodosOsCamposQuandoXmlDaRacaoValido() {
            NfeXmlParser.DadosNfe dados = NfeXmlParser.parse(recurso("/nfe/nfe-racao-valida.xml"));

            assertThat(dados.chaveAcesso).isEqualTo(CHAVE_44_DIGITOS).hasSize(44);
            assertThat(dados.numeroDocumento).isEqualTo("12345");
            assertThat(dados.serie).isEqualTo("1");
            assertThat(dados.dataEmissao).isEqualTo(LocalDate.of(2026, 8, 10));
            assertThat(dados.valorTotal).isEqualByComparingTo("2500.00");
            assertThat(dados.itens).hasSize(1);

            NfeXmlParser.ItemNfe item = dados.itens.get(0);
            assertThat(item.codigo).isEqualTo("RAC-25KG-001");
            assertThat(item.descricao).isEqualTo("RACAO BOVINA ENGORDA - SACO 25KG");
            assertThat(item.quantidade).isEqualByComparingTo("40.0000");
            assertThat(item.valorUnitario).isEqualByComparingTo("62.5000000000");
            assertThat(item.valorTotal).isEqualByComparingTo("2500.00");
        }

        @Test
        void deveExtrairMultiplosItensQuandoXmlDoMedicamentoValido() {
            NfeXmlParser.DadosNfe dados = NfeXmlParser.parse(recurso("/nfe/nfe-medicamento-valida.xml"));

            assertThat(dados.itens).hasSize(2);
            assertThat(dados.itens).extracting(i -> i.codigo)
                    .containsExactly("VAC-AFT-50ML", "MED-VERM-1L");
            assertThat(dados.valorTotal).isEqualByComparingTo("1700.00");
        }

        @Test
        void deveAceitarDataDeEmissaoNoFormatoDEmiQuandoDhEmiAusente() {
            String semDhEmi = """
                    <ide>
                      <nNF>100</nNF>
                      <serie>1</serie>
                      <dEmi>2026-03-20</dEmi>
                    </ide>""";
            byte[] doc = xml(nfeCompleta(semDhEmi, TOTAL_PADRAO, DET_PADRAO));

            NfeXmlParser.DadosNfe dados = NfeXmlParser.parse(doc);

            assertThat(dados.dataEmissao).isEqualTo(LocalDate.of(2026, 3, 20));
        }
    }

    @Nested
    class ParseInvalido {

        @Test
        void deveLancarExcecaoQuandoInfNFeAusente() {
            byte[] doc = xml("""
                    <?xml version="1.0"?>
                    <nfeProc><NFe><outraTag/></NFe></nfeProc>
                    """);

            assertThatThrownBy(() -> NfeXmlParser.parse(doc))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("infNFe");
        }

        @Test
        void deveLancarExcecaoQuandoChaveDeAcessoNaoTem44Digitos() {
            byte[] doc = xml("""
                    <?xml version="1.0"?>
                    <nfeProc><NFe><infNFe Id="NFe12345">
                      %s
                      %s
                      %s
                    </infNFe></NFe></nfeProc>
                    """.formatted(IDE_PADRAO, TOTAL_PADRAO, DET_PADRAO));

            assertThatThrownBy(() -> NfeXmlParser.parse(doc))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("chave de acesso");
        }

        @Test
        void deveLancarExcecaoQuandoIdeAusente() {
            byte[] doc = xml(nfeCompleta("", TOTAL_PADRAO, DET_PADRAO));

            assertThatThrownBy(() -> NfeXmlParser.parse(doc))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("<ide>");
        }

        @Test
        void deveLancarExcecaoQuandoNumeroDaNotaAusente() {
            String semNNF = """
                    <ide>
                      <serie>1</serie>
                      <dhEmi>2026-01-15T10:00:00-03:00</dhEmi>
                    </ide>""";
            byte[] doc = xml(nfeCompleta(semNNF, TOTAL_PADRAO, DET_PADRAO));

            assertThatThrownBy(() -> NfeXmlParser.parse(doc))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nNF");
        }

        @Test
        void deveLancarExcecaoQuandoSerieAusente() {
            String semSerie = """
                    <ide>
                      <nNF>100</nNF>
                      <dhEmi>2026-01-15T10:00:00-03:00</dhEmi>
                    </ide>""";
            byte[] doc = xml(nfeCompleta(semSerie, TOTAL_PADRAO, DET_PADRAO));

            assertThatThrownBy(() -> NfeXmlParser.parse(doc))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("serie");
        }

        @Test
        void deveLancarExcecaoQuandoDataDeEmissaoAusente() {
            String semData = """
                    <ide>
                      <nNF>100</nNF>
                      <serie>1</serie>
                    </ide>""";
            byte[] doc = xml(nfeCompleta(semData, TOTAL_PADRAO, DET_PADRAO));

            assertThatThrownBy(() -> NfeXmlParser.parse(doc))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("dhEmi/dEmi");
        }

        @Test
        void deveLancarExcecaoQuandoIcmsTotAusente() {
            byte[] doc = xml(nfeCompleta(IDE_PADRAO, "", DET_PADRAO));

            assertThatThrownBy(() -> NfeXmlParser.parse(doc))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ICMSTot");
        }

        @Test
        void deveLancarExcecaoQuandoValorTotalNaoENumerico() {
            String totalInvalido = """
                    <total>
                      <ICMSTot>
                        <vNF>não-é-numero</vNF>
                      </ICMSTot>
                    </total>""";
            byte[] doc = xml(nfeCompleta(IDE_PADRAO, totalInvalido, DET_PADRAO));

            assertThatThrownBy(() -> NfeXmlParser.parse(doc))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("vNF");
        }

        @Test
        void deveLancarExcecaoQuandoNenhumItemEncontrado() {
            byte[] doc = xml(nfeCompleta(IDE_PADRAO, TOTAL_PADRAO, ""));

            assertThatThrownBy(() -> NfeXmlParser.parse(doc))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nenhum item");
        }

        @Test
        void deveLancarExcecaoQuandoQuantidadeDoItemNaoENumerica() {
            String detInvalido = """
                    <det nItem="1">
                      <prod>
                        <cProd>P1</cProd>
                        <xProd>Produto Teste</xProd>
                        <qCom>abc</qCom>
                        <vUnCom>50.0000000000</vUnCom>
                        <vProd>50.00</vProd>
                      </prod>
                    </det>""";
            byte[] doc = xml(nfeCompleta(IDE_PADRAO, TOTAL_PADRAO, detInvalido));

            assertThatThrownBy(() -> NfeXmlParser.parse(doc))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("qCom");
        }

        @Test
        @DisplayName("DOCTYPE deve ser bloqueado (proteção XXE), mesmo sem entidade externa declarada")
        void deveLancarExcecaoQuandoXmlContemDoctype() {
            byte[] doc = xml("""
                    <?xml version="1.0"?>
                    <!DOCTYPE nfeProc [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                    <nfeProc><NFe><infNFe Id="NFe%s">
                      %s
                      %s
                      %s
                    </infNFe></NFe></nfeProc>
                    """.formatted(CHAVE_44_DIGITOS, IDE_PADRAO, TOTAL_PADRAO, DET_PADRAO));

            assertThatThrownBy(() -> NfeXmlParser.parse(doc))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
