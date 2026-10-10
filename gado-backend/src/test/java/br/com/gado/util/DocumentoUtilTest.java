package br.com.gado.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentoUtilTest {

    // CPF/CNPJ gerados algoritmicamente (mesmo cálculo de dígito verificador do DocumentoUtil),
    // não pertencem a nenhuma pessoa ou empresa real.
    private static final String CPF_VALIDO = "12345678909";
    private static final String CNPJ_VALIDO = "12345678000195";

    @Test
    void deveValidarCpfComDigitosVerificadoresCorretos() {
        assertThat(DocumentoUtil.isCpfValido(CPF_VALIDO)).isTrue();
    }

    @Test
    void deveValidarCpfFormatadoComPontuacao() {
        assertThat(DocumentoUtil.isCpfValido("123.456.789-09")).isTrue();
    }

    @Test
    void deveInvalidarCpfComDigitoVerificadorErrado() {
        assertThat(DocumentoUtil.isCpfValido("12345678900")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"00000000000", "11111111111", "99999999999"})
    void deveInvalidarCpfComTodosOsDigitosIguais(String cpf) {
        assertThat(DocumentoUtil.isCpfValido(cpf)).isFalse();
    }

    @Test
    void deveInvalidarCpfComTamanhoDiferenteDe11Digitos() {
        assertThat(DocumentoUtil.isCpfValido("123456789")).isFalse();
    }

    @Test
    void deveInvalidarCpfNulo() {
        assertThat(DocumentoUtil.isCpfValido(null)).isFalse();
    }

    @Test
    void deveValidarCnpjComDigitosVerificadoresCorretos() {
        assertThat(DocumentoUtil.isCnpjValido(CNPJ_VALIDO)).isTrue();
    }

    @Test
    void deveValidarCnpjFormatadoComPontuacao() {
        assertThat(DocumentoUtil.isCnpjValido("12.345.678/0001-95")).isTrue();
    }

    @Test
    void deveInvalidarCnpjComDigitoVerificadorErrado() {
        assertThat(DocumentoUtil.isCnpjValido("12345678000100")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"00000000000000", "11111111111111"})
    void deveInvalidarCnpjComTodosOsDigitosIguais(String cnpj) {
        assertThat(DocumentoUtil.isCnpjValido(cnpj)).isFalse();
    }

    @Test
    void devePermitirCpfOuCnpjValidoConformeOComprimento() {
        assertThat(DocumentoUtil.isCpfOuCnpjValido(CPF_VALIDO)).isTrue();
        assertThat(DocumentoUtil.isCpfOuCnpjValido(CNPJ_VALIDO)).isTrue();
    }

    @Test
    void deveInvalidarCpfOuCnpjComComprimentoQueNaoEDeNenhumDosDois() {
        assertThat(DocumentoUtil.isCpfOuCnpjValido("123")).isFalse();
    }
}
