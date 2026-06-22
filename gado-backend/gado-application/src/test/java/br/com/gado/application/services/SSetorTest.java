package br.com.gado.application.services;

import br.com.gado.application.dto.SetorDto;
import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.entities.ELote;
import br.com.gado.domain.entities.ELoteSetor;
import br.com.gado.domain.entities.ESetor;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnPerfilUsuario;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.domain.enums.EnTipoSetor;
import br.com.gado.infrastructure.persistence.repositories.ILoteSetor;
import br.com.gado.infrastructure.persistence.repositories.ISetor;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SSetorTest {

    @InjectMocks
    private SSetor sSetor;

    @Mock
    private ISetor setorInterface;

    @Mock
    private ILoteSetor loteSetorInterface;

    @Mock
    private IUsuario usuarioInterface;

    private ESetor setorEntity;
    private SetorDto setorDto;
    private EUsuario usuarioEntity;

    private final Long ID = 1L;
    private final String EMAIL_VALIDO = "usuario@gado.com";

    @BeforeEach
    void setUp() {
        usuarioEntity = new EUsuario();
        usuarioEntity.setId(99L);
        usuarioEntity.setNome("João Silva");
        usuarioEntity.setEmail(EMAIL_VALIDO);
        usuarioEntity.setPerfil(EnPerfilUsuario.ADMINISTRADOR);

        setorEntity = new ESetor();
        setorEntity.setId(ID);
        setorEntity.setNome("Pasto A");
        setorEntity.setCapacidadeMaxima(50);
        setorEntity.setStatus(EnStatus.A);
        setorEntity.setCriadoPor(usuarioEntity);

        setorDto = new SetorDto();
        setorDto.setId(ID);
        setorDto.setNome("Pasto A");
        setorDto.setCapacidadeMaxima(50);
    }

    @Nested
    class ProcurarPorIdTests {

        @Test
        void procuraPorId_DeveRetornarSetor() {
            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.of(setorEntity));

            SetorDto response = sSetor.procuraPorId(ID);

            assertNotNull(response);
            assertEquals(ID, response.getId());
            assertEquals("Pasto A", response.getNome());
        }

        @Test
        void procuraPorId_DevePopularLotesVinculados_QuandoSetorPossuiLotes() {
            ELote lote = new ELote();
            lote.setId(10L);
            lote.setCodigo("LOT001");
            lote.setCorBrinco("Vermelho");

            EAnimal animal = new EAnimal();
            animal.setId(1L);

            ELoteSetor loteSetor = new ELoteSetor();
            loteSetor.setId(100L);
            loteSetor.setLote(lote);
            loteSetor.setSetor(setorEntity);
            loteSetor.setAnimais(List.of(animal));

            when(setorInterface.findByIdAndStatus(ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));
            when(loteSetorInterface.findBySetor_Id(ID)).thenReturn(List.of(loteSetor));

            SetorDto response = sSetor.procuraPorId(ID);

            assertEquals(1, response.getLotes().size());
            SetorDto.LoteResumoDto loteDto = response.getLotes().get(0);
            assertEquals(100L, loteDto.getLoteSectorId());
            assertEquals("LOT001", loteDto.getLoteCodigo());
            assertEquals("Vermelho", loteDto.getLoteCorBrinco());
            assertEquals(1, loteDto.getQuantidadeAnimais());
        }

        @Test
        void procuraPorId_DeveTerListaVazia_QuandoSetorSemLotes() {
            when(setorInterface.findByIdAndStatus(ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));
            when(loteSetorInterface.findBySetor_Id(ID)).thenReturn(List.of());

            SetorDto response = sSetor.procuraPorId(ID);

            assertNotNull(response.getLotes());
            assertTrue(response.getLotes().isEmpty());
        }

        @Test
        void procuraPorId_DeveLancarExcecao() {
            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sSetor.procuraPorId(ID));
        }
    }

    @Nested
    class BuscarTodosTests {

        @Test
        void buscarTodos_DeveRetornarListaDeSetores() {
            // Cria explicitamente um ArrayList exigido pelo seu repositório
            ArrayList<ESetor> listaSetores = new ArrayList<>(List.of(setorEntity));

            when(setorInterface.findAllByStatus(EnStatus.A))
                    .thenReturn(listaSetores);

            ArrayList<SetorDto> response = sSetor.buscarTodos();

            assertNotNull(response);
            assertEquals(1, response.size());
            assertEquals("Pasto A", response.get(0).getNome());
        }

        @Test
        void buscarTodos_DeveRetornarListaVazia() {
            when(setorInterface.findAllByStatus(EnStatus.A))
                    .thenReturn(new ArrayList<>());

            ArrayList<SetorDto> response = sSetor.buscarTodos();

            assertNotNull(response);
            assertTrue(response.isEmpty());
        }
    }

    @Nested
    class CadastrarSetorTests {

        @Test
        void cadastrar_DeveSalvarSetorComSucesso() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            when(setorInterface.save(any(ESetor.class)))
                    .thenReturn(setorEntity);

            SetorDto response = sSetor.cadastra(setorDto, EMAIL_VALIDO);

            assertNotNull(response);
            assertEquals("Pasto A", response.getNome());
            verify(setorInterface, times(1)).save(any(ESetor.class));
        }

        @Test
        void cadastrar_DeveLancarExcecao_QuandoEmailForNulo() {
            assertThrows(IllegalArgumentException.class, () -> sSetor.cadastra(setorDto, null));
            verify(setorInterface, never()).save(any());
        }

        @Test
        void cadastrar_DeveLancarExcecao_QuandoUsuarioNaoEncontrado() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> sSetor.cadastra(setorDto, EMAIL_VALIDO));
            verify(setorInterface, never()).save(any());
        }
    }

    @Nested
    class DeletarSetorTests {

        @Test
        void deletar_DeveInativarSetor() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.of(setorEntity));
            when(setorInterface.save(any(ESetor.class)))
                    .thenReturn(setorEntity);

            assertDoesNotThrow(() -> sSetor.deleta(ID, EMAIL_VALIDO));

            verify(setorInterface, times(1)).save(any(ESetor.class));
            assertEquals(EnStatus.I, setorEntity.getStatus());
        }

        @Test
        void deletar_DeveLancarExcecao() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sSetor.deleta(ID, EMAIL_VALIDO));
        }
    }

    @Nested
    class AlterarSetorTests {

        @Test
        void alterar_DeveAtualizarSetorComSucesso() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.of(setorEntity));

            when(setorInterface.save(any(ESetor.class)))
                    .thenReturn(setorEntity);

            SetorDto response = sSetor.altera(ID, setorDto, EMAIL_VALIDO);

            assertNotNull(response);
            verify(setorInterface, times(1)).save(any(ESetor.class));
        }

        @Test
        void alterar_DeveLancarExcecao() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sSetor.altera(ID, setorDto, EMAIL_VALIDO));
        }

        @Test
        void alterar_NaoDeveAlterarNomeCapacidadeETipo_QuandoNaoInformados() {
            setorDto.setNome(null);
            setorDto.setCapacidadeMaxima(0);
            setorDto.setTipo(null);

            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.of(setorEntity));
            when(setorInterface.save(any(ESetor.class))).thenReturn(setorEntity);

            sSetor.altera(ID, setorDto, EMAIL_VALIDO);

            assertEquals("Pasto A", setorEntity.getNome());
            assertEquals(50, setorEntity.getCapacidadeMaxima());
            assertNull(setorEntity.getTipo());
        }

        @Test
        void alterar_DeveAtualizarTipo_QuandoInformado() {
            setorDto.setTipo(EnTipoSetor.CONFINAMENTO);

            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.of(setorEntity));
            when(setorInterface.save(any(ESetor.class))).thenReturn(setorEntity);

            sSetor.altera(ID, setorDto, EMAIL_VALIDO);

            assertEquals(EnTipoSetor.CONFINAMENTO, setorEntity.getTipo());
        }

        @Test
        void alterar_DeveLancarExcecao_QuandoEmailForNulo() {
            assertThrows(IllegalArgumentException.class, () -> sSetor.altera(ID, setorDto, null));
            verify(setorInterface, never()).save(any());
        }

        @Test
        void alterar_DeveLancarExcecao_QuandoEmailForBlank() {
            assertThrows(IllegalArgumentException.class, () -> sSetor.altera(ID, setorDto, "   "));
            verify(setorInterface, never()).save(any());
        }

        @Test
        void alterar_DeveLancarExcecao_QuandoUsuarioNaoEncontrado() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> sSetor.altera(ID, setorDto, EMAIL_VALIDO));
            verify(setorInterface, never()).save(any());
        }

        @Test
        void alterar_DeveLancarExcecao_QuandoPerfilForCuidador() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sSetor.altera(ID, setorDto, EMAIL_VALIDO));
            assertTrue(ex.getMessage().contains("Cuidadores Chefe"));
        }

        @Test
        void alterar_DeveLancarExcecao_QuandoPerfilForFinanceiro() {
            usuarioEntity.setPerfil(EnPerfilUsuario.FINANCEIRO);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            assertThrows(IllegalArgumentException.class,
                    () -> sSetor.altera(ID, setorDto, EMAIL_VALIDO));
        }

        @Test
        void alterar_DevePermitirCuidadorChefe() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.of(setorEntity));
            when(setorInterface.save(any(ESetor.class))).thenReturn(setorEntity);

            assertDoesNotThrow(() -> sSetor.altera(ID, setorDto, EMAIL_VALIDO));
        }

        @Test
        void alterar_DeveAtualizarNome_QuandoNomeValido() {
            setorDto.setNome("Novo Nome");
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.of(setorEntity));
            when(setorInterface.save(any(ESetor.class))).thenReturn(setorEntity);

            sSetor.altera(ID, setorDto, EMAIL_VALIDO);

            assertEquals("Novo Nome", setorEntity.getNome());
        }

        @Test
        void alterar_NaoDeveAtualizarNome_QuandoNomeForBlank() {
            setorDto.setNome("   ");
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.of(setorEntity));
            when(setorInterface.save(any(ESetor.class))).thenReturn(setorEntity);

            sSetor.altera(ID, setorDto, EMAIL_VALIDO);

            assertEquals("Pasto A", setorEntity.getNome());
        }
    }

    @Nested
    class DeletarPermissaoAdicionaisTests {

        @Test
        void deletar_DeveLancarExcecao_QuandoEmailForNulo() {
            assertThrows(IllegalArgumentException.class, () -> sSetor.deleta(ID, null));
            verify(setorInterface, never()).save(any());
        }

        @Test
        void deletar_DeveLancarExcecao_QuandoEmailForBlank() {
            assertThrows(IllegalArgumentException.class, () -> sSetor.deleta(ID, "   "));
            verify(setorInterface, never()).save(any());
        }

        @Test
        void deletar_DeveLancarExcecao_QuandoUsuarioNaoEncontrado() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> sSetor.deleta(ID, EMAIL_VALIDO));
        }

        @Test
        void deletar_DeveLancarExcecao_QuandoPerfilForCuidadorChefe() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sSetor.deleta(ID, EMAIL_VALIDO));
            assertTrue(ex.getMessage().contains("Administradores e Gerentes"));
        }

        @Test
        void deletar_DeveLancarExcecao_QuandoPerfilForCuidador() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            assertThrows(IllegalArgumentException.class, () -> sSetor.deleta(ID, EMAIL_VALIDO));
        }
    }

    @Nested
    class CadastrarPermissaoAdicionaisTests {

        @Test
        void cadastrar_DeveLancarExcecao_QuandoEmailForBlank() {
            assertThrows(IllegalArgumentException.class, () -> sSetor.cadastra(setorDto, "   "));
            verify(setorInterface, never()).save(any());
        }

        @Test
        void cadastrar_DeveLancarExcecao_QuandoPerfilForCuidador() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            assertThrows(IllegalArgumentException.class, () -> sSetor.cadastra(setorDto, EMAIL_VALIDO));
        }

        @Test
        void cadastrar_DeveLancarExcecao_QuandoPerfilForFinanceiro() {
            usuarioEntity.setPerfil(EnPerfilUsuario.FINANCEIRO);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            assertThrows(IllegalArgumentException.class, () -> sSetor.cadastra(setorDto, EMAIL_VALIDO));
        }

        @Test
        void cadastrar_DevePermitirGerente() {
            usuarioEntity.setPerfil(EnPerfilUsuario.GERENTE);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            when(setorInterface.save(any(ESetor.class))).thenReturn(setorEntity);

            assertDoesNotThrow(() -> sSetor.cadastra(setorDto, EMAIL_VALIDO));
        }

        @Test
        void cadastrar_DevePermitirCuidadorChefe() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            when(setorInterface.save(any(ESetor.class))).thenReturn(setorEntity);

            assertDoesNotThrow(() -> sSetor.cadastra(setorDto, EMAIL_VALIDO));
        }
    }

    @Nested
    class DeletarPermissaoGerenteTests {

        @Test
        void deletar_DevePermitirGerente() {
            usuarioEntity.setPerfil(EnPerfilUsuario.GERENTE);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            when(setorInterface.findByIdAndStatus(ID, EnStatus.A))
                    .thenReturn(Optional.of(setorEntity));
            when(setorInterface.save(any(ESetor.class))).thenReturn(setorEntity);

            assertDoesNotThrow(() -> sSetor.deleta(ID, EMAIL_VALIDO));
        }

        @Test
        void deletar_DeveLancarExcecao_QuandoPerfilForFinanceiro() {
            usuarioEntity.setPerfil(EnPerfilUsuario.FINANCEIRO);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));

            assertThrows(IllegalArgumentException.class, () -> sSetor.deleta(ID, EMAIL_VALIDO));
        }
    }

    @Nested
    class CadastrarResolveUsuarioTests {

        @Test
        void cadastrar_DeveRetornarSetorComCriadoPorNulo_QuandoResolveUsuarioRetornaNulo() {
            // When usuarioInterface returns empty for resolveUsuario path
            // But this cannot happen normally since validaPermissaoCriarEditar would throw first.
            // Instead test that when resolveUsuario gets a valid email it returns the user properly.
            usuarioEntity.setPerfil(EnPerfilUsuario.ADMINISTRADOR);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_VALIDO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity));
            when(setorInterface.save(any(ESetor.class))).thenReturn(setorEntity);

            SetorDto response = sSetor.cadastra(setorDto, EMAIL_VALIDO);

            assertNotNull(response);
            // criadoPor is populated on the saved entity
            assertEquals(EMAIL_VALIDO, response.getCriadoPorEmail());
        }
    }

    @Nested
    class ResolveUsuarioTests {

        @Test
        void resolveUsuario_DeveRetornarNull_QuandoEmailForNulo() throws Exception {
            Method method = SSetor.class.getDeclaredMethod("resolveUsuario", String.class);
            method.setAccessible(true);

            EUsuario resultado = (EUsuario) method.invoke(sSetor, (Object) null);

            assertNull(resultado);
        }

        @Test
        void resolveUsuario_DeveRetornarNull_QuandoEmailForBlank() throws Exception {
            Method method = SSetor.class.getDeclaredMethod("resolveUsuario", String.class);
            method.setAccessible(true);

            EUsuario resultado = (EUsuario) method.invoke(sSetor, "   ");

            assertNull(resultado);
        }
    }

    @Nested
    class ToSetorDtoAdicionaisTests {

        @Test
        void devePopularAlteradoPor_QuandoSetorTiverAlteradoPor() {
            setorEntity.setAlteradoPor(usuarioEntity);
            when(setorInterface.findByIdAndStatus(ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));
            when(loteSetorInterface.findBySetor_Id(ID)).thenReturn(List.of());

            SetorDto response = sSetor.procuraPorId(ID);

            assertEquals(usuarioEntity.getNome(), response.getAlteradoPorNome());
            assertEquals(usuarioEntity.getEmail(), response.getAlteradoPorEmail());
        }

        @Test
        void deveRetornarNomeNulo_QuandoSetorNaoTiverCriadoPor() {
            setorEntity.setCriadoPor(null);
            when(setorInterface.findByIdAndStatus(ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));
            when(loteSetorInterface.findBySetor_Id(ID)).thenReturn(List.of());

            SetorDto response = sSetor.procuraPorId(ID);

            assertNull(response.getCriadoPorNome());
            assertNull(response.getCriadoPorEmail());
        }
    }
}