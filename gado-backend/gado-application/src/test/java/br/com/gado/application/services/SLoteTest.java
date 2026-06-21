package br.com.gado.application.services;

import br.com.gado.application.dto.loteDto.LoteCadastroDto;
import br.com.gado.application.dto.loteDto.LotePutDto;
import br.com.gado.application.dto.loteDto.LoteRespostaDto;
import br.com.gado.application.dto.loteDto.LoteSetorCadastroDto;
import br.com.gado.domain.entities.*;
import br.com.gado.domain.enums.EnPerfilUsuario;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.domain.enums.EnStatusAnimal;
import br.com.gado.infrastructure.persistence.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.gado.application.dto.loteDto.TransferenciaAnimalDto;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

        @Test
        void deveRetornarDtoSemDadosDeCriador_QuandoLoteNaoTiverCriadoPor() {
            loteEntity.setCriadoPor(null);
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteSetorInterface.findByLote_Id(LOTE_ID)).thenReturn(Collections.emptyList());

            LoteRespostaDto resultado = sLote.buscaPorid(LOTE_ID);

            assertNull(resultado.getCriadoPorNome());
            assertNull(resultado.getCriadoPorEmail());
            assertEquals(0, resultado.getTotalAnimais());
            assertTrue(resultado.getAlocacoes().isEmpty());
        }

        @Test
        void deveRetornarDtoComDadosDeAlteracao_QuandoLoteTiverSidoAlterado() {
            loteEntity.setAlteradoPor(usuarioEntity);
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteSetorInterface.findByLote_Id(LOTE_ID)).thenReturn(List.of(loteSetorEntity));

            LoteRespostaDto resultado = sLote.buscaPorid(LOTE_ID);

            assertEquals(usuarioEntity.getNome(), resultado.getAlteradoPorNome());
            assertEquals(usuarioEntity.getEmail(), resultado.getAlteradoPorEmail());
            assertEquals(1, resultado.getTotalAnimais());
        }
    }

    @Nested
    class ListarTodosTests {
        @Test
        void deveRetornarListaDeLotes_QuandoExistiremLotesAtivos() {
            when(loteInterface.findAllByStatus(EnStatus.A)).thenReturn(new ArrayList<>(List.of(loteEntity)));
            when(loteSetorInterface.findByLote_Id(LOTE_ID)).thenReturn(List.of(loteSetorEntity));

            List<LoteRespostaDto> resultado = sLote.listarTodos();

            assertEquals(1, resultado.size());
            assertEquals("LOT001", resultado.get(0).getCodigo());
        }

        @Test
        void deveRetornarListaVazia_QuandoNaoExistiremLotesAtivos() {
            when(loteInterface.findAllByStatus(EnStatus.A)).thenReturn(new ArrayList<>());

            List<LoteRespostaDto> resultado = sLote.listarTodos();

            assertTrue(resultado.isEmpty());
        }
    }

    @Nested
    class GerarProximoCodigoTests {
        @Test
        void deveGerarLot001_QuandoNaoExistirCodigoAnterior() {
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.empty());

            assertEquals("LOT001", sLote.gerarProximoCodigo());
        }

        @Test
        void deveIncrementarUltimoCodigo_QuandoExistirCodigoValido() {
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.of("LOT042"));

            assertEquals("LOT043", sLote.gerarProximoCodigo());
        }

        @Test
        void deveRetornarLot001_QuandoUltimoCodigoEstiverCorrompido() {
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.of("LOTabc"));

            assertEquals("LOT001", sLote.gerarProximoCodigo());
        }
    }

    @Nested
    class ValidaPermissaoTests {
        @Test
        void deveLancarExcecao_QuandoEmailForNulo() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> sLote.validaPermissao(null));
            assertEquals("Informe o e-mail do usuário responsável pela operação.", ex.getMessage());
        }

        @Test
        void deveLancarExcecao_QuandoEmailForBlank() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> sLote.validaPermissao("   "));
            assertEquals("Informe o e-mail do usuário responsável pela operação.", ex.getMessage());
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioNaoEncontrado() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> sLote.validaPermissao(EMAIL_USUARIO));
            assertEquals("Usuário não encontrado.", ex.getMessage());
        }

        @Test
        void deveLancarExcecao_QuandoPerfilNaoForPermitido() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> sLote.validaPermissao(EMAIL_USUARIO));
            assertEquals("Apenas Administradores e Gerentes podem criar ou excluir lotes.", ex.getMessage());
        }

        @Test
        void naoDeveLancarExcecao_QuandoPerfilForAdminOuGerente() {
            usuarioEntity.setPerfil(EnPerfilUsuario.ADMINISTRADOR);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            assertDoesNotThrow(() -> sLote.validaPermissao(EMAIL_USUARIO));

            usuarioEntity.setPerfil(EnPerfilUsuario.GERENTE);
            assertDoesNotThrow(() -> sLote.validaPermissao(EMAIL_USUARIO));
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

        @Test
        void deveLancarExcecao_QuandoSetorNaoEncontrado() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.of("LOT000"));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);
            when(setorInterface.findByIdAndStatus(SETOR_ID, EnStatus.A)).thenReturn(Optional.empty());

            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            loteCadastroDto.setAlocacoes(List.of(alocacaoDto));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                sLote.cadastra(EMAIL_USUARIO, loteCadastroDto);
            });

            assertEquals("Setor não encontrado ou inativo: ID " + SETOR_ID, exception.getMessage());
            verify(loteSetorInterface, never()).save(any());
        }

        @Test
        void deveCadastrarSemAnimais_QuandoAnimaisIdsForVazio() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.of("LOT000"));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);
            when(setorInterface.findByIdAndStatus(SETOR_ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));

            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            alocacaoDto.setAnimaisIds(Collections.emptyList());
            loteCadastroDto.setAlocacoes(List.of(alocacaoDto));

            String resultado = sLote.cadastra(EMAIL_USUARIO, loteCadastroDto);

            assertEquals("Lote LOT001 cadastrado com sucesso.", resultado);
            verify(animalInterface, never()).findAllById(any());
            verify(loteSetorInterface, times(1)).save(any(ELoteSetor.class));
        }

        @Test
        void deveLancarExcecao_QuandoAlgumAnimalNaoForEncontrado() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.of("LOT000"));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);
            when(setorInterface.findByIdAndStatus(SETOR_ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));

            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            alocacaoDto.setAnimaisIds(List.of(ANIMAL_ID, 999L));
            loteCadastroDto.setAlocacoes(List.of(alocacaoDto));

            when(animalInterface.findAllById(List.of(ANIMAL_ID, 999L))).thenReturn(List.of(animalEntity));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                sLote.cadastra(EMAIL_USUARIO, loteCadastroDto);
            });

            assertTrue(exception.getMessage().contains("não foram encontrados"));
            verify(loteSetorInterface, never()).save(any());
        }

        @Test
        void deveLancarExcecao_QuandoAnimalJaAlocadoEmOutroLote() {
            ELote outroLote = new ELote();
            outroLote.setId(2L);
            outroLote.setCodigo("LOT999");

            ELoteSetor conflito = new ELoteSetor();
            conflito.setLote(outroLote);

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.of("LOT000"));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);
            when(setorInterface.findByIdAndStatus(SETOR_ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));

            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            alocacaoDto.setAnimaisIds(List.of(ANIMAL_ID));
            loteCadastroDto.setAlocacoes(List.of(alocacaoDto));

            when(animalInterface.findAllById(List.of(ANIMAL_ID))).thenReturn(List.of(animalEntity));
            when(loteSetorInterface.findConflitosAtivos(ANIMAL_ID, LOTE_ID, EnStatus.A)).thenReturn(List.of(conflito));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                sLote.cadastra(EMAIL_USUARIO, loteCadastroDto);
            });

            assertTrue(exception.getMessage().contains("já está alocado ao lote LOT999"));
            verify(loteSetorInterface, never()).save(any());
        }

        @Test
        void deveLancarExcecao_QuandoAnimalComStatusVendidoForAdicionado() {
            animalEntity.setStatusAnimal(EnStatusAnimal.VENDIDO);

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.of("LOT000"));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);
            when(setorInterface.findByIdAndStatus(SETOR_ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));

            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            alocacaoDto.setAnimaisIds(List.of(ANIMAL_ID));
            loteCadastroDto.setAlocacoes(List.of(alocacaoDto));

            when(animalInterface.findAllById(List.of(ANIMAL_ID))).thenReturn(List.of(animalEntity));
            when(loteSetorInterface.findConflitosAtivos(ANIMAL_ID, LOTE_ID, EnStatus.A)).thenReturn(Collections.emptyList());

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    sLote.cadastra(EMAIL_USUARIO, loteCadastroDto));

            assertTrue(exception.getMessage().contains("VENDIDO"));
            verify(loteSetorInterface, never()).save(any());
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioNaoEncontradoNoCadastraAposPermissaoValidada() {
            // validaPermissao (1ª chamada) encontra o usuário, mas a busca interna do cadastra (2ª chamada) não
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity))
                    .thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sLote.cadastra(EMAIL_USUARIO, loteCadastroDto));
            assertEquals("Usuário não encontrado.", ex.getMessage());
        }

        @Test
        void deveLancarExcecao_QuandoAnimalComStatusObitoOuAbatidoForAdicionado() {
            for (EnStatusAnimal status : List.of(EnStatusAnimal.OBITO, EnStatusAnimal.ABATIDO)) {
                animalEntity.setStatusAnimal(status);

                when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
                when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.of("LOT000"));
                when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);
                when(setorInterface.findByIdAndStatus(SETOR_ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));

                LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
                alocacaoDto.setSetorId(SETOR_ID);
                alocacaoDto.setAnimaisIds(List.of(ANIMAL_ID));
                loteCadastroDto.setAlocacoes(List.of(alocacaoDto));

                when(animalInterface.findAllById(List.of(ANIMAL_ID))).thenReturn(List.of(animalEntity));
                when(loteSetorInterface.findConflitosAtivos(ANIMAL_ID, LOTE_ID, EnStatus.A)).thenReturn(Collections.emptyList());

                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        sLote.cadastra(EMAIL_USUARIO, loteCadastroDto));

                assertTrue(exception.getMessage().contains(status.name()),
                        "Esperava mensagem com " + status.name());
            }
        }

        @Test
        void deveLancarExcecao_QuandoCapacidadeMaximaExcedida() {
            setorEntity.setCapacidadeMaxima(1);
            EAnimal animalEntity2 = new EAnimal();
            animalEntity2.setId(51L);
            animalEntity2.setCodigoBrinco("BR456");

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.of("LOT000"));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);
            when(setorInterface.findByIdAndStatus(SETOR_ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));

            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            alocacaoDto.setAnimaisIds(List.of(ANIMAL_ID, 51L));
            loteCadastroDto.setAlocacoes(List.of(alocacaoDto));

            when(animalInterface.findAllById(List.of(ANIMAL_ID, 51L))).thenReturn(List.of(animalEntity, animalEntity2));
            when(loteSetorInterface.findConflitosAtivos(anyLong(), anyLong(), any(EnStatus.class))).thenReturn(Collections.emptyList());
            when(loteSetorInterface.findBySetor_Id(SETOR_ID)).thenReturn(Collections.emptyList());

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                sLote.cadastra(EMAIL_USUARIO, loteCadastroDto);
            });

            assertTrue(exception.getMessage().contains("excede a capacidade máxima"));
            verify(loteSetorInterface, never()).save(any());
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
        void deveLancarExcecao_QuandoUsuarioNaoEncontradoNoAlteraAposPermissaoValidada() {
            // validaPermissaoEdicao (1ª chamada) encontra o usuário, mas a busca interna do altera (2ª chamada) não
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioEntity))
                    .thenReturn(Optional.empty());
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sLote.altera(LOTE_ID, EMAIL_USUARIO, lotePutDto));
            assertEquals("Usuário não encontrado.", ex.getMessage());
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

        @Test
        void deveManterCorBrincoEDescricaoOriginais_QuandoNaoInformados() {
            loteEntity.setDescricao("Descricao Original");
            LotePutDto dtoVazio = new LotePutDto();

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);

            sLote.altera(LOTE_ID, EMAIL_USUARIO, dtoVazio);

            assertEquals("Vermelho", loteEntity.getCorBrinco());
            assertEquals("Descricao Original", loteEntity.getDescricao());
            assertNull(loteEntity.getRacaPredominante());
        }

        @Test
        void deveManterCorBrincoOriginal_QuandoCorBrincoForBlank() {
            lotePutDto.setCorBrinco("   ");

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);

            sLote.altera(LOTE_ID, EMAIL_USUARIO, lotePutDto);

            assertEquals("Vermelho", loteEntity.getCorBrinco());
        }

        @Test
        void deveAtualizarRacaPredominante_QuandoInformada() {
            lotePutDto.setRacaPredominante("Nelore");

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);

            sLote.altera(LOTE_ID, EMAIL_USUARIO, lotePutDto);

            assertEquals("Nelore", loteEntity.getRacaPredominante());
        }

        @Test
        void deveLancarExcecao_QuandoAnimalAbatidoForRemovidoDaAlocacao() {
            animalEntity.setStatusAnimal(EnStatusAnimal.ABATIDO);
            loteSetorEntity.setAnimais(List.of(animalEntity));

            // DTO sem o animal bloqueado — tentativa de remover
            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            alocacaoDto.setAnimaisIds(Collections.emptyList());
            lotePutDto.setAlocacoes(List.of(alocacaoDto));

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteSetorInterface.findByLote_Id(LOTE_ID)).thenReturn(List.of(loteSetorEntity));
            when(animalInterface.findById(ANIMAL_ID)).thenReturn(Optional.of(animalEntity));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    sLote.altera(LOTE_ID, EMAIL_USUARIO, lotePutDto));

            assertTrue(exception.getMessage().contains("congelado"));
            verify(loteSetorInterface, never()).deleteAll(any());
        }

        @Test
        void devePermitirAtualizacao_QuandoAnimalBloqueadoPermaneceNoLote() {
            animalEntity.setStatusAnimal(EnStatusAnimal.OBITO);
            loteSetorEntity.setAnimais(List.of(animalEntity));

            // DTO inclui o animal bloqueado — permanece no lote
            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            alocacaoDto.setAnimaisIds(List.of(ANIMAL_ID));
            lotePutDto.setAlocacoes(List.of(alocacaoDto));

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);
            when(loteSetorInterface.findByLote_Id(LOTE_ID)).thenReturn(List.of(loteSetorEntity));
            when(setorInterface.findByIdAndStatus(SETOR_ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));
            when(animalInterface.findAllById(List.of(ANIMAL_ID))).thenReturn(List.of(animalEntity));
            when(loteSetorInterface.findConflitosAtivos(ANIMAL_ID, LOTE_ID, EnStatus.A)).thenReturn(Collections.emptyList());
            when(loteSetorInterface.findBySetor_Id(SETOR_ID)).thenReturn(Collections.emptyList());

            String resultado = sLote.altera(LOTE_ID, EMAIL_USUARIO, lotePutDto);

            assertEquals("Lote LOT001 atualizado com sucesso.", resultado);
            verify(loteSetorInterface, times(1)).save(any(ELoteSetor.class));
        }

        @Test
        void deveSubstituirAlocacoes_QuandoAlocacoesForemInformadas() {
            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            alocacaoDto.setAnimaisIds(Collections.emptyList());
            lotePutDto.setAlocacoes(List.of(alocacaoDto));

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);
            when(loteSetorInterface.findByLote_Id(LOTE_ID)).thenReturn(List.of(loteSetorEntity));
            when(setorInterface.findByIdAndStatus(SETOR_ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));

            sLote.altera(LOTE_ID, EMAIL_USUARIO, lotePutDto);

            verify(loteSetorInterface, times(1)).deleteAll(List.of(loteSetorEntity));
            verify(loteSetorInterface, times(1)).save(any(ELoteSetor.class));
            assertTrue(loteEntity.getAlocacoes().isEmpty());
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

        @Test
        void deveInativarLote_QuandoApenasMovimentacaoFinanceiraEstiverVinculada() {
            // temMovimentacaoFinanceira é um placeholder que sempre retorna false;
            // usamos spy para forçar true e cobrir a branch "movimentações financeiras" no ternário
            SLote spySLote = spy(sLote);
            doReturn(true).when(spySLote).temMovimentacaoFinanceira(anyLong());

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteSetorInterface.findByLote_Id(LOTE_ID)).thenReturn(Collections.emptyList()); // temMeta = false
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);

            String mensagem = spySLote.deleta(LOTE_ID, EMAIL_USUARIO);

            assertTrue(mensagem.contains("movimentações financeiras"));
            assertEquals(EnStatus.I, loteEntity.getStatus());
        }

        @Test
        void temMovimentacaoFinanceira_DeveRetornarFalse_PoisAindaEhPlaceholder() {
            assertFalse(sLote.temMovimentacaoFinanceira(LOTE_ID));
        }

        @Test
        void deveInativarLote_QuandoPossuirSomenteMovimentacaoFinanceira() {
            // temMeta = false, temMovimentacao = true is a placeholder (always false),
            // so this test covers the case where a setor with no metas is linked
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            // findByLote_Id returns a loteSetor whose setor has NO metas => temMeta = false
            when(loteSetorInterface.findByLote_Id(LOTE_ID)).thenReturn(List.of(loteSetorEntity));
            when(metaSetorInterface.findBySetor_Id(SETOR_ID)).thenReturn(Collections.emptyList());

            // Since temMovimentacaoFinanceira is always false, this path ends in hard delete
            String mensagem = sLote.deleta(LOTE_ID, EMAIL_USUARIO);

            assertEquals("Lote LOT001 excluído com sucesso.", mensagem);
        }
    }

    @Nested
    class ValidaPermissaoEdicaoTests {

        @Test
        void deveLancarExcecao_QuandoEmailForNulo() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sLote.validaPermissaoEdicao(null));
            assertEquals("Informe o e-mail do usuário responsável pela operação.", ex.getMessage());
        }

        @Test
        void deveLancarExcecao_QuandoEmailForBlank() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sLote.validaPermissaoEdicao("   "));
            assertEquals("Informe o e-mail do usuário responsável pela operação.", ex.getMessage());
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioNaoEncontrado() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.empty());
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sLote.validaPermissaoEdicao(EMAIL_USUARIO));
            assertEquals("Usuário não encontrado.", ex.getMessage());
        }

        @Test
        void deveLancarExcecao_QuandoPerfilForCuidador() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sLote.validaPermissaoEdicao(EMAIL_USUARIO));
            assertTrue(ex.getMessage().contains("Cuidadores Chefe"));
        }

        @Test
        void deveLancarExcecao_QuandoPerfilForFinanceiro() {
            usuarioEntity.setPerfil(EnPerfilUsuario.FINANCEIRO);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            assertThrows(IllegalArgumentException.class,
                    () -> sLote.validaPermissaoEdicao(EMAIL_USUARIO));
        }

        @Test
        void naoDeveLancarExcecao_QuandoPerfilForCuidadorChefe() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            assertDoesNotThrow(() -> sLote.validaPermissaoEdicao(EMAIL_USUARIO));
        }

        @Test
        void naoDeveLancarExcecao_QuandoPerfilForAdminOuGerente() {
            usuarioEntity.setPerfil(EnPerfilUsuario.ADMINISTRADOR);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            assertDoesNotThrow(() -> sLote.validaPermissaoEdicao(EMAIL_USUARIO));

            usuarioEntity.setPerfil(EnPerfilUsuario.GERENTE);
            assertDoesNotThrow(() -> sLote.validaPermissaoEdicao(EMAIL_USUARIO));
        }
    }

    @Nested
    class ValidaPermissaoTransferenciaTests {

        @Test
        void deveLancarExcecao_QuandoEmailForNulo() {
            assertThrows(IllegalArgumentException.class,
                    () -> sLote.validaPermissaoTransferencia(null));
        }

        @Test
        void deveLancarExcecao_QuandoEmailForBlank() {
            assertThrows(IllegalArgumentException.class,
                    () -> sLote.validaPermissaoTransferencia("  "));
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioNaoEncontrado() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class,
                    () -> sLote.validaPermissaoTransferencia(EMAIL_USUARIO));
        }

        @Test
        void deveLancarExcecao_QuandoPerfilForCuidador() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sLote.validaPermissaoTransferencia(EMAIL_USUARIO));
            assertTrue(ex.getMessage().contains("transferir animais"));
        }

        @Test
        void naoDeveLancarExcecao_QuandoPerfilForCuidadorChefe() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            assertDoesNotThrow(() -> sLote.validaPermissaoTransferencia(EMAIL_USUARIO));
        }
    }

    @Nested
    class TransferirAnimalTests {

        private TransferenciaAnimalDto buildDto(Long animalId, Long loteDestinoId, Long setorDestinoId) {
            TransferenciaAnimalDto dto = new TransferenciaAnimalDto();
            dto.setAnimalId(animalId);
            dto.setLoteDestinoId(loteDestinoId);
            dto.setSetorDestinoId(setorDestinoId);
            return dto;
        }

        @Test
        void deveLancarExcecao_QuandoAnimalNaoEncontrado() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(animalInterface.findById(ANIMAL_ID)).thenReturn(Optional.empty());

            TransferenciaAnimalDto dto = buildDto(ANIMAL_ID, 2L, 20L);
            assertThrows(IllegalArgumentException.class,
                    () -> sLote.transferirAnimal(EMAIL_USUARIO, dto));
        }

        @Test
        void deveLancarExcecao_QuandoAnimalTemStatusBloqueado() {
            animalEntity.setStatusAnimal(EnStatusAnimal.VENDIDO);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(animalInterface.findById(ANIMAL_ID)).thenReturn(Optional.of(animalEntity));

            TransferenciaAnimalDto dto = buildDto(ANIMAL_ID, 2L, 20L);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sLote.transferirAnimal(EMAIL_USUARIO, dto));
            assertTrue(ex.getMessage().contains("VENDIDO"));
        }

        @Test
        void deveLancarExcecao_QuandoAnimalNaoEstaAlocadoEmNenhumLoteAtivo() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(animalInterface.findById(ANIMAL_ID)).thenReturn(Optional.of(animalEntity));
            when(loteSetorInterface.findByAnimalIdAndLoteAtivo(ANIMAL_ID, EnStatus.A)).thenReturn(Collections.emptyList());

            TransferenciaAnimalDto dto = buildDto(ANIMAL_ID, 2L, 20L);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sLote.transferirAnimal(EMAIL_USUARIO, dto));
            assertTrue(ex.getMessage().contains("não está alocado"));
        }

        @Test
        void deveLancarExcecao_QuandoLoteDestinoNaoEncontrado() {
            Long LOTE_DESTINO_ID = 2L;
            Long SETOR_DESTINO_ID = 20L;

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(animalInterface.findById(ANIMAL_ID)).thenReturn(Optional.of(animalEntity));
            when(loteSetorInterface.findByAnimalIdAndLoteAtivo(ANIMAL_ID, EnStatus.A)).thenReturn(List.of(loteSetorEntity));
            when(loteInterface.findByIdAndStatus(LOTE_DESTINO_ID, EnStatus.A)).thenReturn(Optional.empty());

            TransferenciaAnimalDto dto = buildDto(ANIMAL_ID, LOTE_DESTINO_ID, SETOR_DESTINO_ID);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sLote.transferirAnimal(EMAIL_USUARIO, dto));
            assertTrue(ex.getMessage().contains("Lote de destino"));
        }

        @Test
        void deveLancarExcecao_QuandoSetorDestinoNaoEncontrado() {
            Long LOTE_DESTINO_ID = 2L;
            Long SETOR_DESTINO_ID = 20L;

            ELote loteDestino = new ELote();
            loteDestino.setId(LOTE_DESTINO_ID);
            loteDestino.setCodigo("LOT002");

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(animalInterface.findById(ANIMAL_ID)).thenReturn(Optional.of(animalEntity));
            when(loteSetorInterface.findByAnimalIdAndLoteAtivo(ANIMAL_ID, EnStatus.A)).thenReturn(List.of(loteSetorEntity));
            when(loteInterface.findByIdAndStatus(LOTE_DESTINO_ID, EnStatus.A)).thenReturn(Optional.of(loteDestino));
            when(setorInterface.findByIdAndStatus(SETOR_DESTINO_ID, EnStatus.A)).thenReturn(Optional.empty());

            TransferenciaAnimalDto dto = buildDto(ANIMAL_ID, LOTE_DESTINO_ID, SETOR_DESTINO_ID);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sLote.transferirAnimal(EMAIL_USUARIO, dto));
            assertTrue(ex.getMessage().contains("Setor de destino"));
        }

        @Test
        void deveLancarExcecao_QuandoAnimalJaEstaNoMesmoLoteESetor() {
            // O loteSetorOrigem aponta para LOTE_ID / SETOR_ID
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(animalInterface.findById(ANIMAL_ID)).thenReturn(Optional.of(animalEntity));
            when(loteSetorInterface.findByAnimalIdAndLoteAtivo(ANIMAL_ID, EnStatus.A)).thenReturn(List.of(loteSetorEntity));

            ELote mesmoLote = loteEntity; // mesma instância => mesmo ID
            ESetor mesmoSetor = setorEntity;

            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(mesmoLote));
            when(setorInterface.findByIdAndStatus(SETOR_ID, EnStatus.A)).thenReturn(Optional.of(mesmoSetor));

            TransferenciaAnimalDto dto = buildDto(ANIMAL_ID, LOTE_ID, SETOR_ID);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sLote.transferirAnimal(EMAIL_USUARIO, dto));
            assertTrue(ex.getMessage().contains("já está alocado"));
        }

        @Test
        void deveLancarExcecao_QuandoCapacidadeMaximaDoSetorDestinoExcedida() {
            Long LOTE_DESTINO_ID = 2L;
            Long SETOR_DESTINO_ID = 20L;

            ELote loteDestino = new ELote();
            loteDestino.setId(LOTE_DESTINO_ID);
            loteDestino.setCodigo("LOT002");

            ESetor setorDestino = new ESetor();
            setorDestino.setId(SETOR_DESTINO_ID);
            setorDestino.setNome("Curral");
            setorDestino.setCapacidadeMaxima(1); // capacidade cheia

            EAnimal animalExistente = new EAnimal();
            animalExistente.setId(99L);

            ELoteSetor loteSetorDestino = new ELoteSetor();
            loteSetorDestino.setAnimais(List.of(animalExistente));

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(animalInterface.findById(ANIMAL_ID)).thenReturn(Optional.of(animalEntity));
            when(loteSetorInterface.findByAnimalIdAndLoteAtivo(ANIMAL_ID, EnStatus.A)).thenReturn(List.of(loteSetorEntity));
            when(loteInterface.findByIdAndStatus(LOTE_DESTINO_ID, EnStatus.A)).thenReturn(Optional.of(loteDestino));
            when(setorInterface.findByIdAndStatus(SETOR_DESTINO_ID, EnStatus.A)).thenReturn(Optional.of(setorDestino));
            when(loteSetorInterface.findBySetor_Id(SETOR_DESTINO_ID)).thenReturn(List.of(loteSetorDestino));

            TransferenciaAnimalDto dto = buildDto(ANIMAL_ID, LOTE_DESTINO_ID, SETOR_DESTINO_ID);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sLote.transferirAnimal(EMAIL_USUARIO, dto));
            assertTrue(ex.getMessage().contains("excede a capacidade máxima"));
        }

        @Test
        void deveTransferirAnimal_QuandoJaExisteLoteSetorNoDestino() {
            Long LOTE_DESTINO_ID = 2L;
            Long SETOR_DESTINO_ID = 20L;

            ELote loteDestino = new ELote();
            loteDestino.setId(LOTE_DESTINO_ID);
            loteDestino.setCodigo("LOT002");

            ESetor setorDestino = new ESetor();
            setorDestino.setId(SETOR_DESTINO_ID);
            setorDestino.setNome("Curral");
            setorDestino.setCapacidadeMaxima(0); // 0 = sem limite

            ELoteSetor loteSetorDestino = new ELoteSetor();
            loteSetorDestino.setId(600L);
            loteSetorDestino.setLote(loteDestino);
            loteSetorDestino.setSetor(setorDestino);
            loteSetorDestino.setAnimais(new ArrayList<>());

            // origem tem o animal
            ELoteSetor origem = new ELoteSetor();
            origem.setId(loteSetorEntity.getId());
            origem.setLote(loteEntity);
            origem.setSetor(setorEntity);
            origem.setAnimais(new ArrayList<>(List.of(animalEntity)));

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(animalInterface.findById(ANIMAL_ID)).thenReturn(Optional.of(animalEntity));
            when(loteSetorInterface.findByAnimalIdAndLoteAtivo(ANIMAL_ID, EnStatus.A)).thenReturn(List.of(origem));
            when(loteInterface.findByIdAndStatus(LOTE_DESTINO_ID, EnStatus.A)).thenReturn(Optional.of(loteDestino));
            when(setorInterface.findByIdAndStatus(SETOR_DESTINO_ID, EnStatus.A)).thenReturn(Optional.of(setorDestino));
            when(loteSetorInterface.findBySetor_Id(SETOR_DESTINO_ID)).thenReturn(Collections.emptyList());
            when(loteSetorInterface.findByLote_Id(LOTE_DESTINO_ID)).thenReturn(List.of(loteSetorDestino));
            when(loteSetorInterface.save(any(ELoteSetor.class))).thenAnswer(inv -> inv.getArgument(0));

            TransferenciaAnimalDto dto = buildDto(ANIMAL_ID, LOTE_DESTINO_ID, SETOR_DESTINO_ID);
            String resultado = sLote.transferirAnimal(EMAIL_USUARIO, dto);

            assertTrue(resultado.contains("LOT002"));
            assertTrue(resultado.contains("Curral"));
        }

        @Test
        void deveTransferirAnimal_QuandoLoteSetorDestinoNaoExisteEDeveSerCriado() {
            Long LOTE_DESTINO_ID = 2L;
            Long SETOR_DESTINO_ID = 20L;

            ELote loteDestino = new ELote();
            loteDestino.setId(LOTE_DESTINO_ID);
            loteDestino.setCodigo("LOT002");

            ESetor setorDestino = new ESetor();
            setorDestino.setId(SETOR_DESTINO_ID);
            setorDestino.setNome("Curral Novo");
            setorDestino.setCapacidadeMaxima(0);

            ELoteSetor origem = new ELoteSetor();
            origem.setId(loteSetorEntity.getId());
            origem.setLote(loteEntity);
            origem.setSetor(setorEntity);
            origem.setAnimais(new ArrayList<>(List.of(animalEntity)));

            ELoteSetor novoLoteSetor = new ELoteSetor();
            novoLoteSetor.setId(700L);
            novoLoteSetor.setLote(loteDestino);
            novoLoteSetor.setSetor(setorDestino);
            novoLoteSetor.setAnimais(new ArrayList<>());

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(animalInterface.findById(ANIMAL_ID)).thenReturn(Optional.of(animalEntity));
            when(loteSetorInterface.findByAnimalIdAndLoteAtivo(ANIMAL_ID, EnStatus.A)).thenReturn(List.of(origem));
            when(loteInterface.findByIdAndStatus(LOTE_DESTINO_ID, EnStatus.A)).thenReturn(Optional.of(loteDestino));
            when(setorInterface.findByIdAndStatus(SETOR_DESTINO_ID, EnStatus.A)).thenReturn(Optional.of(setorDestino));
            when(loteSetorInterface.findBySetor_Id(SETOR_DESTINO_ID)).thenReturn(Collections.emptyList());
            // No loteSetor exists for the destination lote => empty list, so a new one is created
            when(loteSetorInterface.findByLote_Id(LOTE_DESTINO_ID)).thenReturn(Collections.emptyList());
            when(loteSetorInterface.save(any(ELoteSetor.class))).thenReturn(novoLoteSetor);

            TransferenciaAnimalDto dto = buildDto(ANIMAL_ID, LOTE_DESTINO_ID, SETOR_DESTINO_ID);
            String resultado = sLote.transferirAnimal(EMAIL_USUARIO, dto);

            assertTrue(resultado.contains("transferido"));
        }
    }

    @Nested
    class AlteraAlocacoesAdicionaisTests {

        @Test
        void deveIgnorarAlocacoes_QuandoAlocacoesForemNulas() {
            lotePutDto.setAlocacoes(null);

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);

            String resultado = sLote.altera(LOTE_ID, EMAIL_USUARIO, lotePutDto);

            assertEquals("Lote LOT001 atualizado com sucesso.", resultado);
            verify(loteSetorInterface, never()).findByLote_Id(any());
        }

        @Test
        void deveIgnorarAlocacoes_QuandoAlocacoesForemListaVazia() {
            lotePutDto.setAlocacoes(Collections.emptyList());

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);

            String resultado = sLote.altera(LOTE_ID, EMAIL_USUARIO, lotePutDto);

            assertEquals("Lote LOT001 atualizado com sucesso.", resultado);
            verify(loteSetorInterface, never()).findByLote_Id(any());
        }

        @Test
        void deveLancarExcecao_QuandoAnimalVendidoForRemovidoDaAlocacao() {
            animalEntity.setStatusAnimal(EnStatusAnimal.VENDIDO);
            loteSetorEntity.setAnimais(List.of(animalEntity));

            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            alocacaoDto.setAnimaisIds(Collections.emptyList());
            lotePutDto.setAlocacoes(List.of(alocacaoDto));

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteSetorInterface.findByLote_Id(LOTE_ID)).thenReturn(List.of(loteSetorEntity));
            when(animalInterface.findById(ANIMAL_ID)).thenReturn(Optional.of(animalEntity));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sLote.altera(LOTE_ID, EMAIL_USUARIO, lotePutDto));
            assertTrue(ex.getMessage().contains("congelado"));
        }

        @Test
        void deveLancarExcecao_QuandoAnimalComStatusObitoForRemovidoDaAlocacao() {
            animalEntity.setStatusAnimal(EnStatusAnimal.OBITO);
            loteSetorEntity.setAnimais(List.of(animalEntity));

            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            alocacaoDto.setAnimaisIds(Collections.emptyList());
            lotePutDto.setAlocacoes(List.of(alocacaoDto));

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteSetorInterface.findByLote_Id(LOTE_ID)).thenReturn(List.of(loteSetorEntity));
            when(animalInterface.findById(ANIMAL_ID)).thenReturn(Optional.of(animalEntity));

            assertThrows(IllegalArgumentException.class,
                    () -> sLote.altera(LOTE_ID, EMAIL_USUARIO, lotePutDto));
        }
    }

    @Nested
    class CadastraAdicionaisTests {

        @Test
        void deveCadastrarLote_QuandoDataCriacaoForInformada() {
            LocalDate dataEspecifica = LocalDate.of(2024, 6, 1);
            loteCadastroDto.setDataCriacao(dataEspecifica);

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.of("LOT000"));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);

            String resultado = sLote.cadastra(EMAIL_USUARIO, loteCadastroDto);

            assertEquals("Lote LOT001 cadastrado com sucesso.", resultado);
            verify(loteInterface, times(1)).save(any(ELote.class));
        }

        @Test
        void deveCadastrarLote_QuandoSetorComCapacidadeZeroEAnimaisAdicionados() {
            // setorEntity.capacidadeMaxima = 0 => unlimited => capacity check condition (>0) is false => no throw
            setorEntity.setCapacidadeMaxima(0);

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.of("LOT000"));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);
            when(setorInterface.findByIdAndStatus(SETOR_ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));

            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            alocacaoDto.setAnimaisIds(List.of(ANIMAL_ID));
            loteCadastroDto.setAlocacoes(List.of(alocacaoDto));

            when(animalInterface.findAllById(List.of(ANIMAL_ID))).thenReturn(List.of(animalEntity));
            when(loteSetorInterface.findConflitosAtivos(ANIMAL_ID, LOTE_ID, EnStatus.A)).thenReturn(Collections.emptyList());
            when(loteSetorInterface.findBySetor_Id(SETOR_ID)).thenReturn(Collections.emptyList());

            String resultado = sLote.cadastra(EMAIL_USUARIO, loteCadastroDto);

            assertEquals("Lote LOT001 cadastrado com sucesso.", resultado);
        }

        @Test
        void deveCadastrarLote_QuandoAlocacaoNaoTemAnimaisIds() {
            // animaisIds == null => !isEmpty check skips animal validation entirely
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.of("LOT000"));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);
            when(setorInterface.findByIdAndStatus(SETOR_ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));

            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            alocacaoDto.setAnimaisIds(null); // null ids
            loteCadastroDto.setAlocacoes(List.of(alocacaoDto));

            String resultado = sLote.cadastra(EMAIL_USUARIO, loteCadastroDto);

            assertEquals("Lote LOT001 cadastrado com sucesso.", resultado);
            verify(animalInterface, never()).findAllById(any());
        }
    }

    @Nested
    class AlteraAdicionaisTests {

        @Test
        void deveIgnorarCorBrinco_QuandoCorBrincoForNulo() {
            lotePutDto.setCorBrinco(null);

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);

            String resultado = sLote.altera(LOTE_ID, EMAIL_USUARIO, lotePutDto);

            assertEquals("Lote LOT001 atualizado com sucesso.", resultado);
            assertEquals("Vermelho", loteEntity.getCorBrinco()); // unchanged
        }

        @Test
        void devePermitirAtualizacao_QuandoAnimalRemovidoNaoEncontradoNoBanco() {
            // animal id present in old alocacoes but findById returns empty => ifPresent does nothing => no throw
            loteSetorEntity.setAnimais(List.of(animalEntity));

            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            alocacaoDto.setAnimaisIds(Collections.emptyList()); // animal being "removed"
            lotePutDto.setAlocacoes(List.of(alocacaoDto));

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);
            when(loteSetorInterface.findByLote_Id(LOTE_ID)).thenReturn(List.of(loteSetorEntity));
            // animal not found in DB => ifPresent body never runs => no exception
            when(animalInterface.findById(ANIMAL_ID)).thenReturn(Optional.empty());
            when(setorInterface.findByIdAndStatus(SETOR_ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));

            String resultado = sLote.altera(LOTE_ID, EMAIL_USUARIO, lotePutDto);

            assertEquals("Lote LOT001 atualizado com sucesso.", resultado);
        }

        @Test
        void deveAtualizarLote_QuandoCuidadorChefeEdita() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);

            String resultado = sLote.altera(LOTE_ID, EMAIL_USUARIO, lotePutDto);

            assertEquals("Lote LOT001 atualizado com sucesso.", resultado);
        }
    }

    @Nested
    class DeletaAdicionaisTests {

        @Test
        void deveInativarLote_MensagemDeMovimentacao_QuandoTemMetaEMovimentacao() {
            // Both temMeta=true and temMovimentacao (placeholder always false),
            // but with temMeta=true the "metas" text appears
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteSetorInterface.findByLote_Id(LOTE_ID)).thenReturn(List.of(loteSetorEntity));
            when(metaSetorInterface.findBySetor_Id(SETOR_ID)).thenReturn(List.of(new EMetaSetor()));

            String mensagem = sLote.deleta(LOTE_ID, EMAIL_USUARIO);

            assertTrue(mensagem.contains("metas"));
        }
    }

    @Nested
    class TransferirAnimalAdicionaisTests {

        @Test
        void naoDeveLancarExcecao_QuandoPermissaoTransferenciaForGerente() {
            usuarioEntity.setPerfil(EnPerfilUsuario.GERENTE);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            assertDoesNotThrow(() -> sLote.validaPermissaoTransferencia(EMAIL_USUARIO));
        }

        @Test
        void deveLancarExcecao_QuandoLoteIgualMasSetorDiferente_NaoLancaExcecaoSamePlace() {
            // loteSetorOrigem.getLote().getId() == dto.getLoteDestinoId() but setor is different
            Long LOTE_DESTINO_ID = LOTE_ID; // same lote
            Long SETOR_DESTINO_ID = 999L;   // different setor

            ESetor setorDestino = new ESetor();
            setorDestino.setId(SETOR_DESTINO_ID);
            setorDestino.setNome("Outro Setor");
            setorDestino.setCapacidadeMaxima(0);

            ELoteSetor origem = new ELoteSetor();
            origem.setId(loteSetorEntity.getId());
            origem.setLote(loteEntity); // LOTE_ID
            origem.setSetor(setorEntity); // SETOR_ID (10L)
            origem.setAnimais(new ArrayList<>(List.of(animalEntity)));

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(animalInterface.findById(ANIMAL_ID)).thenReturn(Optional.of(animalEntity));
            when(loteSetorInterface.findByAnimalIdAndLoteAtivo(ANIMAL_ID, EnStatus.A)).thenReturn(List.of(origem));
            when(loteInterface.findByIdAndStatus(LOTE_DESTINO_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(setorInterface.findByIdAndStatus(SETOR_DESTINO_ID, EnStatus.A)).thenReturn(Optional.of(setorDestino));
            when(loteSetorInterface.findBySetor_Id(SETOR_DESTINO_ID)).thenReturn(Collections.emptyList());
            when(loteSetorInterface.findByLote_Id(LOTE_DESTINO_ID)).thenReturn(Collections.emptyList());
            when(loteSetorInterface.save(any(ELoteSetor.class))).thenAnswer(inv -> {
                ELoteSetor ls = inv.getArgument(0);
                if (ls.getAnimais() == null) ls.setAnimais(new ArrayList<>());
                return ls;
            });

            TransferenciaAnimalDto dto = new TransferenciaAnimalDto();
            dto.setAnimalId(ANIMAL_ID);
            dto.setLoteDestinoId(LOTE_DESTINO_ID);
            dto.setSetorDestinoId(SETOR_DESTINO_ID);

            // Same lote, different setor => transfer should succeed (not throw "already allocated" exception)
            String resultado = sLote.transferirAnimal(EMAIL_USUARIO, dto);
            assertTrue(resultado.contains("transferido"));
        }

        @Test
        void deveTransferirAnimal_QuandoSetorDestinoSemLimiteCapacidadeEAnimalExistente() {
            // setorDestino.getCapacidadeMaxima() == 0 (unlimited), and there's already one animal
            // but that animal's ID != dto.getAnimalId() so it counts => capacidade 0 skips check
            Long LOTE_DESTINO_ID = 2L;
            Long SETOR_DESTINO_ID = 20L;

            ELote loteDestino = new ELote();
            loteDestino.setId(LOTE_DESTINO_ID);
            loteDestino.setCodigo("LOT002");

            ESetor setorDestino = new ESetor();
            setorDestino.setId(SETOR_DESTINO_ID);
            setorDestino.setNome("Pasto Livre");
            setorDestino.setCapacidadeMaxima(0); // unlimited

            // Animal already in setor destino (different from the one being transferred)
            EAnimal outroAnimal = new EAnimal();
            outroAnimal.setId(999L);

            ELoteSetor loteSetorDestino = new ELoteSetor();
            loteSetorDestino.setId(600L);
            loteSetorDestino.setLote(loteDestino);
            loteSetorDestino.setSetor(setorDestino);
            loteSetorDestino.setAnimais(new ArrayList<>(List.of(outroAnimal)));

            ELoteSetor origem = new ELoteSetor();
            origem.setId(loteSetorEntity.getId());
            origem.setLote(loteEntity);
            origem.setSetor(setorEntity);
            origem.setAnimais(new ArrayList<>(List.of(animalEntity)));

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(animalInterface.findById(ANIMAL_ID)).thenReturn(Optional.of(animalEntity));
            when(loteSetorInterface.findByAnimalIdAndLoteAtivo(ANIMAL_ID, EnStatus.A)).thenReturn(List.of(origem));
            when(loteInterface.findByIdAndStatus(LOTE_DESTINO_ID, EnStatus.A)).thenReturn(Optional.of(loteDestino));
            when(setorInterface.findByIdAndStatus(SETOR_DESTINO_ID, EnStatus.A)).thenReturn(Optional.of(setorDestino));
            when(loteSetorInterface.findBySetor_Id(SETOR_DESTINO_ID)).thenReturn(List.of(loteSetorDestino));
            when(loteSetorInterface.findByLote_Id(LOTE_DESTINO_ID)).thenReturn(List.of(loteSetorDestino));
            when(loteSetorInterface.save(any(ELoteSetor.class))).thenAnswer(inv -> inv.getArgument(0));

            TransferenciaAnimalDto dto = new TransferenciaAnimalDto();
            dto.setAnimalId(ANIMAL_ID);
            dto.setLoteDestinoId(LOTE_DESTINO_ID);
            dto.setSetorDestinoId(SETOR_DESTINO_ID);

            String resultado = sLote.transferirAnimal(EMAIL_USUARIO, dto);
            assertTrue(resultado.contains("LOT002"));
        }

        @Test
        void deveTransferirAnimal_QuandoSetorComCapacidadeEAnimalJaEstaNoSetor() {
            // setorDestino.getCapacidadeMaxima() > 0, capacity NOT exceeded
            // The transferred animal IS in the setor but its ID equals dto.getAnimalId() => filtered out => jaAlocados=0
            Long LOTE_DESTINO_ID = 2L;
            Long SETOR_DESTINO_ID = 20L;

            ELote loteDestino = new ELote();
            loteDestino.setId(LOTE_DESTINO_ID);
            loteDestino.setCodigo("LOT002");

            ESetor setorDestino = new ESetor();
            setorDestino.setId(SETOR_DESTINO_ID);
            setorDestino.setNome("Pasto Controlado");
            setorDestino.setCapacidadeMaxima(5); // limited but not exceeded

            // The animal being transferred (ANIMAL_ID=50) is in the setor destino loteSetor
            ELoteSetor loteSetorDestino = new ELoteSetor();
            loteSetorDestino.setId(600L);
            loteSetorDestino.setLote(loteDestino);
            loteSetorDestino.setSetor(setorDestino);
            loteSetorDestino.setAnimais(new ArrayList<>(List.of(animalEntity))); // same animal being transferred

            ELoteSetor origem = new ELoteSetor();
            origem.setId(loteSetorEntity.getId());
            origem.setLote(loteEntity);
            origem.setSetor(setorEntity);
            origem.setAnimais(new ArrayList<>(List.of(animalEntity)));

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(animalInterface.findById(ANIMAL_ID)).thenReturn(Optional.of(animalEntity));
            when(loteSetorInterface.findByAnimalIdAndLoteAtivo(ANIMAL_ID, EnStatus.A)).thenReturn(List.of(origem));
            when(loteInterface.findByIdAndStatus(LOTE_DESTINO_ID, EnStatus.A)).thenReturn(Optional.of(loteDestino));
            when(setorInterface.findByIdAndStatus(SETOR_DESTINO_ID, EnStatus.A)).thenReturn(Optional.of(setorDestino));
            // jaAlocados: the animal being transferred is filtered out => 0 animals => 0 + 1 <= 5 => ok
            when(loteSetorInterface.findBySetor_Id(SETOR_DESTINO_ID)).thenReturn(List.of(loteSetorDestino));
            when(loteSetorInterface.findByLote_Id(LOTE_DESTINO_ID)).thenReturn(List.of(loteSetorDestino));
            when(loteSetorInterface.save(any(ELoteSetor.class))).thenAnswer(inv -> inv.getArgument(0));

            TransferenciaAnimalDto dto = new TransferenciaAnimalDto();
            dto.setAnimalId(ANIMAL_ID);
            dto.setLoteDestinoId(LOTE_DESTINO_ID);
            dto.setSetorDestinoId(SETOR_DESTINO_ID);

            String resultado = sLote.transferirAnimal(EMAIL_USUARIO, dto);
            assertTrue(resultado.contains("transferido"));
        }
    }

    @Nested
    class AplicarAlocacoesAdicionaisTests {

        @Test
        void deveCadastrar_QuandoCapacidadeDoSetorNaoEhExcedida() {
            // setor.capacidadeMaxima > 0, totalDepois <= capacidade => no exception
            setorEntity.setCapacidadeMaxima(10); // can hold 10

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.of("LOT000"));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);
            when(setorInterface.findByIdAndStatus(SETOR_ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));

            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            alocacaoDto.setAnimaisIds(List.of(ANIMAL_ID));
            loteCadastroDto.setAlocacoes(List.of(alocacaoDto));

            when(animalInterface.findAllById(List.of(ANIMAL_ID))).thenReturn(List.of(animalEntity));
            when(loteSetorInterface.findConflitosAtivos(ANIMAL_ID, LOTE_ID, EnStatus.A)).thenReturn(Collections.emptyList());
            // already 0 animals in setor, adding 1 <= 10 => ok
            when(loteSetorInterface.findBySetor_Id(SETOR_ID)).thenReturn(Collections.emptyList());

            String resultado = sLote.cadastra(EMAIL_USUARIO, loteCadastroDto);

            assertEquals("Lote LOT001 cadastrado com sucesso.", resultado);
        }

        @Test
        void deveCadastrar_QuandoCapacidadeDoSetorMaiorQueZeroMasNaoExcedidaComAnimaisExistentes() {
            // setor.capacidadeMaxima > 0, jaAlocados=1, adding 1 => totalDepois=2 <= 5 => no exception
            setorEntity.setCapacidadeMaxima(5);

            EAnimal animalExistente = new EAnimal();
            animalExistente.setId(77L);
            ELoteSetor existingLoteSetor = new ELoteSetor();
            existingLoteSetor.setAnimais(List.of(animalExistente));

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.of("LOT000"));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);
            when(setorInterface.findByIdAndStatus(SETOR_ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));

            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            alocacaoDto.setAnimaisIds(List.of(ANIMAL_ID));
            loteCadastroDto.setAlocacoes(List.of(alocacaoDto));

            when(animalInterface.findAllById(List.of(ANIMAL_ID))).thenReturn(List.of(animalEntity));
            when(loteSetorInterface.findConflitosAtivos(ANIMAL_ID, LOTE_ID, EnStatus.A)).thenReturn(Collections.emptyList());
            // 1 existing animal + 1 new = 2 <= 5 => ok
            when(loteSetorInterface.findBySetor_Id(SETOR_ID)).thenReturn(List.of(existingLoteSetor));

            String resultado = sLote.cadastra(EMAIL_USUARIO, loteCadastroDto);

            assertEquals("Lote LOT001 cadastrado com sucesso.", resultado);
        }

        @Test
        void deveAlterarAlocacao_QuandoAnimaisIdsNulosNoDtoDeAlocacao() {
            // aloc.getAnimaisIds() == null => filter skips => animaisNoDto is empty
            loteSetorEntity.setAnimais(List.of(animalEntity));
            animalEntity.setStatusAnimal(null); // not blocked

            LoteSetorCadastroDto alocacaoDto = new LoteSetorCadastroDto();
            alocacaoDto.setSetorId(SETOR_ID);
            alocacaoDto.setAnimaisIds(null); // null ids in the DTO alocacao
            lotePutDto.setAlocacoes(List.of(alocacaoDto));

            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            when(loteInterface.findByIdAndStatus(LOTE_ID, EnStatus.A)).thenReturn(Optional.of(loteEntity));
            when(loteInterface.save(any(ELote.class))).thenReturn(loteEntity);
            when(loteSetorInterface.findByLote_Id(LOTE_ID)).thenReturn(List.of(loteSetorEntity));
            // The old animal (ANIMAL_ID) is not in animaisNoDto (empty set) => findById called
            when(animalInterface.findById(ANIMAL_ID)).thenReturn(Optional.of(animalEntity));
            when(setorInterface.findByIdAndStatus(SETOR_ID, EnStatus.A)).thenReturn(Optional.of(setorEntity));

            String resultado = sLote.altera(LOTE_ID, EMAIL_USUARIO, lotePutDto);

            assertEquals("Lote LOT001 atualizado com sucesso.", resultado);
        }
    }

    @Nested
    class ValidaPermissaoExtraTests {

        @Test
        void deveLancarExcecao_QuandoPerfilForFinanceiro_NaValidaPermissao() {
            usuarioEntity.setPerfil(EnPerfilUsuario.FINANCEIRO);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sLote.validaPermissao(EMAIL_USUARIO));
            assertEquals("Apenas Administradores e Gerentes podem criar ou excluir lotes.", ex.getMessage());
        }

        @Test
        void deveLancarExcecao_QuandoPerfilForCuidadorChefe_NaValidaPermissao() {
            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sLote.validaPermissao(EMAIL_USUARIO));
            assertEquals("Apenas Administradores e Gerentes podem criar ou excluir lotes.", ex.getMessage());
        }
    }
}