package br.com.gado.application.services;

import br.com.gado.application.dto.loteDto.LoteCadastroDto;
import br.com.gado.application.dto.loteDto.LotePutDto;
import br.com.gado.application.dto.loteDto.LoteRespostaDto;
import br.com.gado.application.dto.loteDto.LoteSetorCadastroDto;
import br.com.gado.domain.entities.*;
import br.com.gado.domain.enums.EnPerfilUsuario;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SLoteTest {

    @InjectMocks
    private SLote sLote;

    @Mock private ILote loteInterface;
    @Mock private ILoteSetor loteSetorInterface;
    @Mock private ISetor setorInterface;
    @Mock private IAnimal animalInterface;
    @Mock private IUsuario usuarioInterface;
    @Mock private IMetaSetor metaSetorInterface;

    private ELote loteEntity;
    private EUsuario usuarioEntity;
    private ESetor setorEntity;
    private EAnimal animalEntity;
    private ELoteSetor loteSetorEntity;

    private LoteCadastroDto loteCadastroDto;
    private LotePutDto lotePutDto;

    private final Long LOTE_ID = 1L;
    private final Long USUARIO_ID = 99L;
    private final Long SETOR_ID = 10L;
    private final Long ANIMAL_ID = 50L;
    private final String EMAIL_USUARIO = "joao@gado.com";

    @BeforeEach
    void setUp() {
        usuarioEntity = new EUsuario();
        usuarioEntity.setId(USUARIO_ID);
        usuarioEntity.setNome("João Silva");
        usuarioEntity.setEmail(EMAIL_USUARIO);
        usuarioEntity.setPerfil(EnPerfilUsuario.ADMINISTRADOR);

        setorEntity = new ESetor();
        setorEntity.setId(SETOR_ID);
        setorEntity.setNome("Pasto Fundo");
        setorEntity.setCapacidadeMaxima(100);

        animalEntity = new EAnimal();
        animalEntity.setId(ANIMAL_ID);
        animalEntity.setCodigoBrinco("BR123");
        animalEntity.setNome("Mimosa");

        loteEntity = new ELote();
        loteEntity.setId(LOTE_ID);
        loteEntity.setCodigo("LOT001");
        loteEntity.setCorBrinco("Vermelho");
        loteEntity.setStatus(EnStatus.A);
        loteEntity.setCriadoPor(usuarioEntity);
        loteEntity.setDataCriacao(LocalDate.now());

        loteSetorEntity = new ELoteSetor();
        loteSetorEntity.setId(500L);
        loteSetorEntity.setLote(loteEntity);
        loteSetorEntity.setSetor(setorEntity);
        loteSetorEntity.setAnimais(List.of(animalEntity));

        loteCadastroDto = new LoteCadastroDto();
        loteCadastroDto.setCorBrinco("Vermelho");
        loteCadastroDto.setDescricao("Lote Pasto Fundo");
        loteCadastroDto.setAlocacoes(new ArrayList<>());

        lotePutDto = new LotePutDto();
        lotePutDto.setCorBrinco("Azul");
        lotePutDto.setDescricao("Lote Atualizado");
    }

    @Nested
    class BuscaPorIdTests {
        @Test
        void deveRetornarLoteRespostaDto_QuandoLoteExistirEAtivo() {
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteSetorInterface.findByLote_Id(LOTE_ID)).thenReturn(List.of(loteSetorEntity));

            LoteRespostaDto resultado = sLote.buscaPorid(LOTE_ID);

            assertNotNull(resultado);
            assertEquals("LOT001", resultado.getCodigo());
            verify(loteInterface, times(1)).findByIdAndStatus(LOTE_ID, EnStatus.A);
        }

        @Test
        void deveLancarExcecao_AoBuscarLoteInexistenteOuInativo() {
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                sLote.buscaPorid(LOTE_ID);
            });

            assertEquals("Nenhum lote ativo encontrado para o ID: " + LOTE_ID, exception.getMessage());
        }
    }

    @Nested
    class CadastraTests {
        @Test
        void deveCadastrarLoteERetornarMensagemSucesso_QuandoUsuarioEAlocacoesValidos() {
            // Mock da validação de permissão e busca de usuário (chamado 2 vezes devido à estrutura do método)
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.of("LOT000"));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);
            when(setorInterface.findByIdAndStatus(SETOR_ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));

            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            alocacaoDto.setAnimaisIds(List.of(ANIMAL_ID));
            loteCadastroDto.setAlocacoes(List.of(alocacaoDto));

            when(animalInterface.findAllById(List.of(ANIMAL_ID))).thenReturn(List.of(animalEntity));
            when(loteSetorInterface.findBySetor_Id(SETOR_ID)).thenReturn(Collections.emptyList());

            String resultado = sLote.cadastra(EMAIL_USUARIO, loteCadastroDto);

            assertEquals("Lote LOT001 cadastrado com sucesso.", resultado);
            verify(loteInterface, times(1)).save(any(ELote.class));
            verify(loteSetorInterface, times(1)).save(any(ELoteSetor.class));
        }

        @Test
        void deveLancarExcecao_AoTentarCadastrarComUsuarioInexistente() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                sLote.cadastra(EMAIL_USUARIO, loteCadastroDto);
            });

            assertEquals("Usuário não encontrado.", exception.getMessage());
            verify(loteInterface, never()).save(any());
        }
    }

    @Nested
    class AlteraTests {
        @Test
        void deveAlterarLoteERetornarMensagemSucesso_QuandoDadosValidos() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);

            String resultado = sLote.altera(LOTE_ID, EMAIL_USUARIO, lotePutDto);

            assertEquals("Lote LOT001 atualizado com sucesso.", resultado);
            assertEquals("Azul", loteEntity.getCorBrinco());
            assertEquals("Lote Atualizado", loteEntity.getDescricao());
            verify(loteInterface, times(1)).save(loteEntity);
        }

        @Test
        void deveLancarExcecao_AoTentarAlterarLoteInexistente() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                sLote.altera(LOTE_ID, EMAIL_USUARIO, lotePutDto);
            });

            assertEquals("Nenhum lote ativo encontrado para o ID: " + LOTE_ID, exception.getMessage());
            verify(loteInterface, never()).save(loteEntity);
        }
    }

    @Nested
    class DeletaTests {
        @Test
        void deveInativarLote_QuandoPossuirMetaVinculada() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));

            // Simula que possui metas vinculadas
            when(loteSetorInterface.findByLote_Id(LOTE_ID)).thenReturn(List.of(loteSetorEntity));
            when(metaSetorInterface.findBySetor_Id(SETOR_ID)).thenReturn(List.of(new EMetaSetor()));

            String mensagem = sLote.deleta(LOTE_ID, EMAIL_USUARIO);

            assertEquals("Lote LOT001 inativado pois possui vínculos com metas.", mensagem);
            assertEquals(EnStatus.I, loteEntity.getStatus());
            verify(loteInterface, times(1)).save(loteEntity);
            verify(loteInterface, never()).delete(any());
        }

        @Test
        void deveExcluirDefinitivamente_QuandoNaoPossuirVinculos() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));

            // Sem metas vinculadas
            when(loteSetorInterface.findByLote_Id(LOTE_ID)).thenReturn(Collections.emptyList());

            String mensagem = sLote.deleta(LOTE_ID, EMAIL_USUARIO);

            assertEquals("Lote LOT001 excluído com sucesso.", mensagem);
            verify(loteSetorInterface, times(1)).deleteAll(anyList());
            verify(loteInterface, times(1)).delete(loteEntity);
        }
    }
}