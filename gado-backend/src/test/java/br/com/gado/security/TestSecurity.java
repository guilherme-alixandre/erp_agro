package br.com.gado.security;

import br.com.gado.enums.EnPerfilUsuario;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

/**
 * {@link JwtAuthenticationFilter} autentica com {@code new UsernamePasswordAuthenticationToken(email, null,
 * authorities)}, deixando o e-mail puro como principal (ver {@link SecurityUtils#currentUserEmail()}).
 * {@code @WithMockUser} não serve aqui porque cria um principal {@code UserDetails}, não uma String.
 *
 * Nos testes de controller (@WebMvcTest) não carregamos o {@code SecurityConfig} real — sem a
 * {@code SecurityFilterChain}, o request post-processor padrão do spring-security-test
 * ({@code SecurityMockMvcRequestPostProcessors.authentication(...)}) não tem filtro nenhum para
 * transferir a autenticação da requisição para o {@link SecurityContextHolder} antes do controller
 * rodar. Por isso este helper seta o contexto diretamente — funciona porque o MockMvc executa a
 * requisição de forma síncrona, na mesma thread do teste. Sempre chamar {@link #limpar()} num
 * {@code @AfterEach} para não vazar autenticação entre testes.
 */
public final class TestSecurity {

    private TestSecurity() {
    }

    public static RequestPostProcessor autenticadoComo(String email, EnPerfilUsuario perfil) {
        return request -> {
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
            var authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return request;
        };
    }

    public static void limpar() {
        SecurityContextHolder.clearContext();
    }
}
