package br.com.gado.security;

import br.com.gado.enums.EnPerfilUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    // Segredo aleatório gerado só para este teste — não é o segredo real de produção.
    private static final String SECRET = "+dkINIl0KdZCeg4q/StJw7BzmqZeDsmgRdLaM4CFIJk=";

    private final JwtService jwtService = new JwtService(SECRET, 3_600_000L);

    @Test
    void deveGerarTokenEValidarExtraindoEmailEPerfil() {
        String token = jwtService.gerarToken("cuidador@fazenda.com", EnPerfilUsuario.CUIDADOR);

        Claims claims = jwtService.validarEExtrairClaims(token);

        assertThat(jwtService.extrairEmail(claims)).isEqualTo("cuidador@fazenda.com");
        assertThat(jwtService.extrairPerfil(claims)).isEqualTo(EnPerfilUsuario.CUIDADOR);
    }

    @Test
    void deveLancarExcecao_QuandoTokenExpirado() {
        JwtService servicoComExpiracaoNoPassado = new JwtService(SECRET, -1000L);
        String tokenExpirado = servicoComExpiracaoNoPassado.gerarToken("cuidador@fazenda.com", EnPerfilUsuario.CUIDADOR);

        assertThatThrownBy(() -> jwtService.validarEExtrairClaims(tokenExpirado))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void deveLancarExcecao_QuandoAssinaturaInvalida() {
        JwtService outroServico = new JwtService("YW5vdXRyYS1jaGF2ZS1kZS10ZXN0ZS1jb20tMzItYnl0ZXMh", 3_600_000L);
        String tokenComOutraChave = outroServico.gerarToken("cuidador@fazenda.com", EnPerfilUsuario.CUIDADOR);

        assertThatThrownBy(() -> jwtService.validarEExtrairClaims(tokenComOutraChave))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void deveLancarExcecao_QuandoTokenMalformado() {
        assertThatThrownBy(() -> jwtService.validarEExtrairClaims("token-invalido-qualquer"))
                .isInstanceOf(MalformedJwtException.class);
    }
}
