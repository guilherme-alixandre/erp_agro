package br.com.gado.application.dto;

import br.com.gado.application.dto.usuarioDto.UsuarioDto;
import br.com.gado.application.dto.usuarioDto.UsuarioLoginDto;
import br.com.gado.application.dto.usuarioDto.UsuarioPutDto;
import br.com.gado.application.dto.usuarioDto.UsuarioRespostaDto;
import br.com.gado.domain.enums.EnPerfilUsuario;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioDtosTest {

    @Nested
    class UsuarioRespostaDtoTest {
        @Test
        void deveTestarGettersSettersEEqualsHashCode() {
            UsuarioRespostaDto dto1 = new UsuarioRespostaDto();
            LocalDateTime agora = LocalDateTime.now();

            dto1.setNome("Gabriel");
            dto1.setEmail("teste@gado.com");
            dto1.setPerfil(EnPerfilUsuario.ADMINISTRADOR);
            dto1.setDataCadastro(agora);

            // Testando Getters
            assertEquals("Gabriel", dto1.getNome());
            assertEquals("teste@gado.com", dto1.getEmail());
            assertEquals(EnPerfilUsuario.ADMINISTRADOR, dto1.getPerfil());
            assertEquals(agora, dto1.getDataCadastro());

            // Testando Equals e HashCode
            UsuarioRespostaDto dto2 = new UsuarioRespostaDto();
            dto2.setNome("Gabriel");
            dto2.setEmail("teste@gado.com");
            dto2.setPerfil(EnPerfilUsuario.ADMINISTRADOR);
            dto2.setDataCadastro(agora);

            UsuarioRespostaDto dtoDiferente = new UsuarioRespostaDto();
            dtoDiferente.setNome("Outro Nome");

            assertEquals(dto1, dto1);
            assertEquals(dto1, dto2);
            assertNotEquals(dto1, dtoDiferente);
            assertNotEquals(null, dto1);
            assertNotEquals(new Object(), dto1);

            assertEquals(dto1.hashCode(), dto2.hashCode());
            assertNotEquals(dto1.hashCode(), dtoDiferente.hashCode());

            // Testando toString gerado pelo Lombok
            assertTrue(dto1.toString().contains("Gabriel"));
        }
    }

    @Nested
    class UsuarioPutDtoTest {
        @Test
        void deveTestarGettersSettersEEqualsHashCode() {
            UsuarioPutDto dto1 = new UsuarioPutDto();
            dto1.setNome("Gabriel Atualizado");
            dto1.setPerfil(EnPerfilUsuario.GERENTE);

            assertEquals("Gabriel Atualizado", dto1.getNome());
            assertEquals(EnPerfilUsuario.GERENTE, dto1.getPerfil());

            UsuarioPutDto dto2 = new UsuarioPutDto();
            dto2.setNome("Gabriel Atualizado");
            dto2.setPerfil(EnPerfilUsuario.GERENTE);

            UsuarioPutDto dtoDiferente = new UsuarioPutDto();
            dtoDiferente.setNome("Diferente");

            assertEquals(dto1, dto2);
            assertNotEquals(dto1, dtoDiferente);
            assertEquals(dto1.hashCode(), dto2.hashCode());
            assertTrue(dto1.toString().contains("Gabriel Atualizado"));
        }
    }

    @Nested
    class UsuarioLoginDtoTest {
        @Test
        void deveTestarGettersSettersEEqualsHashCode() {
            UsuarioLoginDto dto1 = new UsuarioLoginDto();
            dto1.setEmail("login@gado.com");
            dto1.setSenha("senhaForte123");

            assertEquals("login@gado.com", dto1.getEmail());
            assertEquals("senhaForte123", dto1.getSenha());

            UsuarioLoginDto dto2 = new UsuarioLoginDto();
            dto2.setEmail("login@gado.com");
            dto2.setSenha("senhaForte123");

            UsuarioLoginDto dtoDiferente = new UsuarioLoginDto();
            dtoDiferente.setEmail("outro@gado.com");

            assertEquals(dto1, dto2);
            assertNotEquals(dto1, dtoDiferente);
            assertEquals(dto1.hashCode(), dto2.hashCode());
            assertTrue(dto1.toString().contains("login@gado.com"));
        }
    }

    @Nested
    class UsuarioDtoTest {
        @Test
        void deveTestarGettersSettersEEqualsHashCode() {
            UsuarioDto dto1 = new UsuarioDto();
            LocalDateTime agora = LocalDateTime.now();

            dto1.setNome("Gabriel Dto");
            dto1.setEmail("dto@gado.com");
            dto1.setPerfil(EnPerfilUsuario.CUIDADOR);
            dto1.setDataCadastro(agora);

            assertEquals("Gabriel Dto", dto1.getNome());
            assertEquals("dto@gado.com", dto1.getEmail());
            assertEquals(EnPerfilUsuario.CUIDADOR, dto1.getPerfil());
            assertEquals(agora, dto1.getDataCadastro());

            UsuarioDto dto2 = new UsuarioDto();
            dto2.setNome("Gabriel Dto");
            dto2.setEmail("dto@gado.com");
            dto2.setPerfil(EnPerfilUsuario.CUIDADOR);
            dto2.setDataCadastro(agora);

            UsuarioDto dtoDiferente = new UsuarioDto();
            dtoDiferente.setNome("Diferente");

            assertEquals(dto1, dto2);
            assertNotEquals(dto1, dtoDiferente);
            assertEquals(dto1.hashCode(), dto2.hashCode());
            assertTrue(dto1.toString().contains("Gabriel Dto"));
        }
    }
}