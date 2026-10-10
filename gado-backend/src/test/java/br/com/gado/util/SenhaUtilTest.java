package br.com.gado.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class SenhaUtilTest {

    // SHA-256 de "senha-antiga-sha256", calculado fora do SenhaUtil para o fixture não depender
    // da própria lógica que o teste valida.
    private static final String SHA256_DE_SENHA_ANTIGA =
            "7754b74cbdcbe636f261a74d3871529a6bcd1b9278b7b28705a3212c72c30d90";

    @Test
    void deveConferirSenhaContraHashBCrypt() {
        String hashBcrypt = new BCryptPasswordEncoder().encode("senhaSegura123");

        assertThat(SenhaUtil.confere("senhaSegura123", hashBcrypt)).isTrue();
        assertThat(SenhaUtil.confere("senhaErrada", hashBcrypt)).isFalse();
    }

    @Test
    void deveConferirSenhaContraHashSha256Legado() {
        assertThat(SenhaUtil.confere("senha-antiga-sha256", SHA256_DE_SENHA_ANTIGA)).isTrue();
        assertThat(SenhaUtil.confere("senha-errada", SHA256_DE_SENHA_ANTIGA)).isFalse();
    }

    @Test
    void deveConferirSenhaContraTextoPuroLegado() {
        assertThat(SenhaUtil.confere("123mudar", "123mudar")).isTrue();
        assertThat(SenhaUtil.confere("outraSenha", "123mudar")).isFalse();
    }

    @Test
    void deveRetornarFalsoQuandoSenhaInformadaForNulaOuVazia() {
        assertThat(SenhaUtil.confere(null, "qualquerHash")).isFalse();
        assertThat(SenhaUtil.confere("", "qualquerHash")).isFalse();
        assertThat(SenhaUtil.confere("   ", "qualquerHash")).isFalse();
    }

    @Test
    void deveRetornarFalsoQuandoSenhaArmazenadaForNulaOuVazia() {
        assertThat(SenhaUtil.confere("senha", null)).isFalse();
        assertThat(SenhaUtil.confere("senha", "")).isFalse();
    }
}
