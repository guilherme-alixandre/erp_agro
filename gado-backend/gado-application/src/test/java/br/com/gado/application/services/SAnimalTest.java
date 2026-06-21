package br.com.gado.application.services;

import br.com.gado.application.dto.AnimalDto;
import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnPerfilUsuario;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.IAnimal;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SAnimalTest {

    @InjectMocks
    private SAnimal sAnimal;

    @Mock
    private IAnimal animalInterface;

    @Mock
    private IUsuario usuarioInterface;

    // Usamos @Spy para o ModelMapper rodar sua lógica real de conversão e configuração
    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    private EAnimal animalEntity;
    private AnimalDto animalDto;
    private EUsuario usuarioEntity;

    private final String BRINCO_TESTE = "BRINCO-123";
    private final String EMAIL_TESTE = "produtor@gado.com";

    @BeforeEach
    void setUp() {
        usuarioEntity = new EUsuario();
        usuarioEntity.setId(1L);
        usuarioEntity.setEmail(EMAIL_TESTE);

        animalEntity = new EAnimal();
        animalEntity.setId(1L);
        animalEntity.setCodigoBrinco(BRINCO_TESTE);
        animalEntity.setStatus(EnStatus.A);
        animalEntity.setUsuario(usuarioEntity);

        animalDto = new AnimalDto();
        // Presumindo que AnimalDto tenha um setCodigoBrinco
        // animalDto.setCodigoBrinco(BRINCO_TESTE);
    }

    @Nested
    class BuscarPorBrincoTests {
        @Test
        void deveRetornarAnimalDto_QuandoEncontrarBrinco() {
            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));

            AnimalDto resultado = sAnimal.buscarPorBrinco(BRINCO_TESTE);

            assertNotNull(resultado);
            verify(animalInterface, times(1)).findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A);
        }

        @Test
        void deveLancarExcecao_QuandoBrincoNaoExistir() {
            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                sAnimal.buscarPorBrinco(BRINCO_TESTE);
            });

            assertEquals("animal não encontrado", exception.getMessage());
        }
    }

    @Nested
    class BuscarTodosAnimaisTests {
        @Test
        void deveRetornarListaDeAnimais_QuandoExistiremAnimaisAtivos() {
            ArrayList<EAnimal> lista = new ArrayList<>(List.of(animalEntity));
            when(animalInterface.findAllByStatus(EnStatus.A)).thenReturn(Optional.of(lista));

            ArrayList<AnimalDto> resultado = sAnimal.buscarTodosAnimais();

            assertFalse(resultado.isEmpty());
            assertEquals(1, resultado.size());
            verify(animalInterface, times(1)).findAllByStatus(EnStatus.A);
        }

        @Test
        void deveLancarExcecao_QuandoNaoExistirNenhumAnimalAtivo() {
            when(animalInterface.findAllByStatus(EnStatus.A)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                sAnimal.buscarTodosAnimais();
            });

            assertEquals("nenhum animal encontrado", exception.getMessage());
        }
    }

    @Nested
    class CadastraAnimalTests {
        @Test
        void deveCadastrarAnimal_QuandoUsuarioExistir() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            when(animalInterface.save(any(EAnimal.class))).thenReturn(animalEntity);

            AnimalDto resultado = sAnimal.cadastraAnimal(EMAIL_TESTE, animalDto);

            assertNotNull(resultado);
            verify(usuarioInterface, times(1)).findByEmailAndStatus(EMAIL_TESTE, EnStatus.A);
            verify(animalInterface, times(1)).save(any(EAnimal.class));
        }

        @Test
        void deveLancarExcecao_AoCadastrarComUsuarioInexistente() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_TESTE, EnStatus.A))
                    .thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                sAnimal.cadastraAnimal(EMAIL_TESTE, animalDto);
            });

            assertEquals("Usuário não encontrado", exception.getMessage());
            verify(animalInterface, never()).save(any(EAnimal.class));
        }
    }

    @Nested
    class DeletaAnimalTests {
        @Test
        void deveDeletarLogicamenteAnimal_QuandoBrincoExistir() {
            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            String mensagem = sAnimal.deletaAnimal(EMAIL_TESTE, BRINCO_TESTE);

            assertEquals("animal deletado com sucesso", mensagem);
            assertEquals(EnStatus.I, animalEntity.getStatus());
            verify(animalInterface, times(1)).save(animalEntity);
        }

        @Test
        void deveLancarExcecao_AoTentarDeletarAnimalInexistente() {
            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                sAnimal.deletaAnimal(EMAIL_TESTE, BRINCO_TESTE);
            });

            assertEquals("animal não encontrado", exception.getMessage());
            verify(animalInterface, never()).save(any());
        }
    }

    @Nested
    class AlteraAnimalTests {
        @Test
        void deveAlterarAnimalERetornarDto_QuandoBrincoExistir() {
            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            when(animalInterface.save(any(EAnimal.class))).thenReturn(animalEntity);

            AnimalDto resultado = sAnimal.alteraAnimal(EMAIL_TESTE, BRINCO_TESTE, animalDto);

            assertNotNull(resultado);
            verify(animalInterface, times(1)).save(animalEntity);
            assertTrue(modelMapper.getConfiguration().isSkipNullEnabled());
        }

        @Test
        void deveLancarExcecao_AoTentarAlterarAnimalInexistente() {
            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                sAnimal.alteraAnimal(EMAIL_TESTE, BRINCO_TESTE, animalDto);
            });

            assertEquals("animal não encontrado", exception.getMessage());
            verify(animalInterface, never()).save(any());
        }

        @Test
        void deveAlterarAnimal_QuandoEmailForNulo() {
            // emailUsuario null => skips permission check
            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));
            when(animalInterface.save(any(EAnimal.class))).thenReturn(animalEntity);

            AnimalDto resultado = sAnimal.alteraAnimal(null, BRINCO_TESTE, animalDto);

            assertNotNull(resultado);
            verify(usuarioInterface, never()).findByEmailAndStatus(any(), any());
        }

        @Test
        void deveAlterarAnimal_QuandoEmailForBlank() {
            // emailUsuario blank => skips permission check
            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));
            when(animalInterface.save(any(EAnimal.class))).thenReturn(animalEntity);

            AnimalDto resultado = sAnimal.alteraAnimal("   ", BRINCO_TESTE, animalDto);

            assertNotNull(resultado);
            verify(usuarioInterface, never()).findByEmailAndStatus(any(), any());
        }

        @Test
        void deveLancarExcecao_QuandoCuidadorTentaAlterarAnimalDeOutro() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR);
            // animal belongs to a different user
            EUsuario outroUsuario = new EUsuario();
            outroUsuario.setEmail("outro@gado.com");
            animalEntity.setUsuario(outroUsuario);

            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sAnimal.alteraAnimal(EMAIL_TESTE, BRINCO_TESTE, animalDto));
            assertEquals("Você só pode editar animais que você cadastrou.", ex.getMessage());
            verify(animalInterface, never()).save(any());
        }

        @Test
        void deveAlterarAnimal_QuandoCuidadorAlteraSeuProprioAnimal() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR);
            // animal belongs to the requesting user
            animalEntity.setUsuario(usuarioEntity);

            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            when(animalInterface.save(any(EAnimal.class))).thenReturn(animalEntity);

            AnimalDto resultado = sAnimal.alteraAnimal(EMAIL_TESTE, BRINCO_TESTE, animalDto);
            assertNotNull(resultado);
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioNaoEncontradoNaAlteracao() {
            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_TESTE, EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sAnimal.alteraAnimal(EMAIL_TESTE, BRINCO_TESTE, animalDto));
        }
    }

    @Nested
    class DeletaAnimalAdicionaisTests {

        @Test
        void deveDeletarAnimal_QuandoEmailForNulo() {
            // null email => skips permission block
            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));

            String resultado = sAnimal.deletaAnimal(null, BRINCO_TESTE);

            assertEquals("animal deletado com sucesso", resultado);
            verify(usuarioInterface, never()).findByEmailAndStatus(any(), any());
        }

        @Test
        void deveDeletarAnimal_QuandoEmailForBlank() {
            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));

            String resultado = sAnimal.deletaAnimal("  ", BRINCO_TESTE);

            assertEquals("animal deletado com sucesso", resultado);
            verify(usuarioInterface, never()).findByEmailAndStatus(any(), any());
        }

        @Test
        void deveLancarExcecao_QuandoCuidadorChefeTentaDeletarAnimalDeOutro() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);
            EUsuario outroUsuario = new EUsuario();
            outroUsuario.setEmail("outro@gado.com");
            animalEntity.setUsuario(outroUsuario);

            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sAnimal.deletaAnimal(EMAIL_TESTE, BRINCO_TESTE));
            assertEquals("Você só pode excluir animais que você cadastrou.", ex.getMessage());
        }

        @Test
        void deveDeletarAnimal_QuandoCuidadorDeletaSeuProprioAnimal() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR);
            animalEntity.setUsuario(usuarioEntity);

            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            String resultado = sAnimal.deletaAnimal(EMAIL_TESTE, BRINCO_TESTE);
            assertEquals("animal deletado com sucesso", resultado);
        }

        @Test
        void deveDeletarAnimal_QuandoCuidadorChefeDeletaSeuProprioAnimal() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);
            animalEntity.setUsuario(usuarioEntity); // same user

            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            String resultado = sAnimal.deletaAnimal(EMAIL_TESTE, BRINCO_TESTE);
            assertEquals("animal deletado com sucesso", resultado);
        }

        @Test
        void deveDeletarAnimal_QuandoAnimalSemUsuario() {
            // animal.getUsuario() is null => dono = null, email != null => throws
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR);
            animalEntity.setUsuario(null);

            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sAnimal.deletaAnimal(EMAIL_TESTE, BRINCO_TESTE));
            assertEquals("Você só pode excluir animais que você cadastrou.", ex.getMessage());
        }
    }

    @Nested
    class AlteraAnimalPerfilAdicionaisTests {

        @Test
        void deveAlterarAnimal_QuandoCuidadorChefeAlteraAnimalDeOutroUsuario() {
            // CUIDADOR_CHEFE is not restricted to own animals — only CUIDADOR is
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);
            EUsuario outroUsuario = new EUsuario();
            outroUsuario.setEmail("outro@gado.com");
            animalEntity.setUsuario(outroUsuario);

            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            when(animalInterface.save(any(EAnimal.class))).thenReturn(animalEntity);

            AnimalDto resultado = sAnimal.alteraAnimal(EMAIL_TESTE, BRINCO_TESTE, animalDto);

            assertNotNull(resultado);
            verify(animalInterface, times(1)).save(any(EAnimal.class));
        }

        @Test
        void deveLancarExcecao_QuandoCuidadorAlteraAnimalSemUsuario() {
            // CUIDADOR attempting to edit animal that has no usuario => dono = null => email != null => throw
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR);
            animalEntity.setUsuario(null);

            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sAnimal.alteraAnimal(EMAIL_TESTE, BRINCO_TESTE, animalDto));
            assertEquals("Você só pode editar animais que você cadastrou.", ex.getMessage());
        }

        @Test
        void deveDeletarAnimal_QuandoAdministradorDeletaAnimalDeOutroUsuario() {
            // ADMINISTRADOR is not CUIDADOR or CUIDADOR_CHEFE => no restriction
            usuarioEntity.setPerfil(EnPerfilUsuario.ADMINISTRADOR);
            EUsuario outroUsuario = new EUsuario();
            outroUsuario.setEmail("outro@gado.com");
            animalEntity.setUsuario(outroUsuario);

            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            String resultado = sAnimal.deletaAnimal(EMAIL_TESTE, BRINCO_TESTE);

            assertEquals("animal deletado com sucesso", resultado);
        }
    }

    @Nested
    class ToDtoTests {

        @Test
        void deveRetornarDtoSemEmail_QuandoAnimalNaoTemUsuario() {
            animalEntity.setUsuario(null);
            when(animalInterface.findByCodigoBrincoAndStatus(BRINCO_TESTE, EnStatus.A))
                    .thenReturn(Optional.of(animalEntity));

            AnimalDto resultado = sAnimal.buscarPorBrinco(BRINCO_TESTE);

            assertNotNull(resultado);
            assertNull(resultado.getCriadoPorEmail());
        }
    }
}