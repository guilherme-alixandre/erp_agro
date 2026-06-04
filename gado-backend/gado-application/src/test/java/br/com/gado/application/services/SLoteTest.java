package br.com.gado.application.services;

import br.com.gado.application.dto.loteDto.LoteCadastroDto;
import br.com.gado.application.dto.loteDto.LoteDto;
import br.com.gado.application.dto.loteDto.LotePutDto;
import br.com.gado.domain.entities.ELote;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.infrastructure.persistence.repositories.ILote;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SLoteTest {

    @InjectMocks
    private SLote sLote;

    @Mock
    private ILote loteInterface;

    @Mock
    private IUsuario usuarioInterface;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    private ELote loteEntity;
    private EUsuario usuarioEntity;
    private LoteCadastroDto loteCadastroDto;
    private LotePutDto lotePutDto;

    private final Long LOTE_ID = 1L;
    private final Long USUARIO_ID = 99L;

    @BeforeEach
    void setUp() {
        usuarioEntity = new EUsuario();
        usuarioEntity.setId(USUARIO_ID);

        loteEntity = new ELote();
        loteEntity.setId(LOTE_ID);
        loteEntity.setUsuario(usuarioEntity);
        // Se houver nome/descrição, inicialize aqui. Ex: loteEntity.setDescricao("Lote Pasto Fundo");

        loteCadastroDto = new LoteCadastroDto();
        loteCadastroDto.setUsuario_id(USUARIO_ID);
        // loteCadastroDto.setDescricao("Lote Pasto Fundo");

        lotePutDto = new LotePutDto();
        // lotePutDto.setDescricao("Lote Atualizado");
    }

    @Nested
    class BuscaPorIdTests {
        @Test
        void deveRetornarLoteDto_QuandoLoteExistir() {
            when(loteInterface.findById(LOTE_ID)).thenReturn(Optional.of(loteEntity));

            LoteDto resultado = sLote.buscaPorId(LOTE_ID);

            assertNotNull(resultado);
            verify(loteInterface, times(1)).findById(LOTE_ID);
        }

        @Test
        void deveLancarExcecao_AoBuscarLoteInexistente() {
            when(loteInterface.findById(LOTE_ID)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                sLote.buscaPorId(LOTE_ID);
            });

            assertEquals("nenhum lote encontrado", exception.getMessage());
        }
    }

    @Nested
    class CadastraTests {
        @Test
        void deveCadastrarLoteERetornarDto_QuandoUsuarioExistir() {
            when(usuarioInterface.findById(USUARIO_ID)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);

            LoteDto resultado = sLote.cadastra(loteCadastroDto);

            assertNotNull(resultado);
            verify(usuarioInterface, times(1)).findById(USUARIO_ID);
            verify(loteInterface, times(1)).save(any(ELote.class));
        }

        @Test
        void deveLancarExcecao_AoTentarCadastrarComUsuarioInexistente() {
            when(usuarioInterface.findById(USUARIO_ID)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                sLote.cadastra(loteCadastroDto);
            });

            assertEquals("Nenhum usuário com esse id foi encontrado", exception.getMessage());
            verify(loteInterface, never()).save(any());
        }
    }

    @Nested
    class DeletaTests {
        @Test
        void deveRetornarMensagem_QuandoLoteNaoForEncontrado() {
            when(loteInterface.existsById(LOTE_ID)).thenReturn(false);

            String mensagem = sLote.deleta(LOTE_ID);

            assertEquals("nenhum lote foi encontrado", mensagem);
            verify(loteInterface, never()).deleteById(anyLong());
        }

        @Test
        void deveDeletarERetornarMensagem_QuandoSucesso() {
            when(loteInterface.existsById(LOTE_ID)).thenReturn(true);
            doNothing().when(loteInterface).deleteById(LOTE_ID); // Para métodos void, doNothing é o padrão, mas fica explícito

            String mensagem = sLote.deleta(LOTE_ID);

            assertEquals("lote deletado com sucesso", mensagem);
            verify(loteInterface, times(1)).deleteById(LOTE_ID);
        }

        @Test
        void deveRetornarMensagemDeErro_QuandoOcorrerExcecaoNoBanco() {
            when(loteInterface.existsById(LOTE_ID)).thenReturn(true);

            // Usando doThrow para métodos que retornam void (deleteById)
            doThrow(new RuntimeException("DataIntegrityViolationException"))
                    .when(loteInterface).deleteById(LOTE_ID);

            String mensagem = sLote.deleta(LOTE_ID);

            assertEquals("Erro: esse lote possui transações vinculadas e não pode ser excluido", mensagem);
            verify(loteInterface, times(1)).deleteById(LOTE_ID);
        }
    }

    @Nested
    class AlteraTests {
        @Test
        void deveAlterarLoteERetornarDto_QuandoSucesso() {
            when(loteInterface.findById(LOTE_ID)).thenReturn(Optional.of(loteEntity));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);

            LoteDto resultado = sLote.altera(LOTE_ID, lotePutDto);

            assertNotNull(resultado);
            assertTrue(modelMapper.getConfiguration().isSkipNullEnabled());
            verify(loteInterface, times(1)).save(loteEntity);
        }

        @Test
        void deveLancarExcecao_AoTentarAlterarLoteInexistente() {
            when(loteInterface.findById(LOTE_ID)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                sLote.altera(LOTE_ID, lotePutDto);
            });

            assertEquals("nenhum lote encontrado", exception.getMessage());
            verify(loteInterface, never()).save(any());
        }
    }
}