package br.com.gado.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUtilsTest {

    @AfterEach
    void limparSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void currentUserEmail_deveRetornarNull_QuandoSemAutenticacao() {
        assertThat(SecurityUtils.currentUserEmail()).isNull();
    }

    @Test
    void currentUserEmail_deveRetornarPrincipal_QuandoAutenticado() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "cuidador@fazenda.com", null, List.of(new SimpleGrantedAuthority("ROLE_CUIDADOR"))));

        assertThat(SecurityUtils.currentUserEmail()).isEqualTo("cuidador@fazenda.com");
    }

    @Test
    void isAdministrador_deveRetornarFalso_QuandoSemAutenticacao() {
        assertThat(SecurityUtils.isAdministrador()).isFalse();
    }

    @Test
    void isAdministrador_deveRetornarTrue_QuandoTemRoleAdministrador() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin@fazenda.com", null, List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))));

        assertThat(SecurityUtils.isAdministrador()).isTrue();
    }

    @Test
    void isAdministrador_deveRetornarFalso_QuandoOutroPerfil() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "cuidador@fazenda.com", null, List.of(new SimpleGrantedAuthority("ROLE_CUIDADOR"))));

        assertThat(SecurityUtils.isAdministrador()).isFalse();
    }
}
