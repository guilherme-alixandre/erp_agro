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
            usuarioEntity.setPerfil(EnPerfilUsuario.FINANCEIRO);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> sLote.validaPermissao(EMAIL_USUARIO));
            assertEquals("Apenas Administradores, Gerentes e Cuidadores podem gerenciar lotes.", ex.getMessage());
        }

        @Test
        void naoDeveLancarExcecao_QuandoPerfilForGerenteOuCuidador() {
            usuarioEntity.setPerfil(EnPerfilUsuario.GERENTE);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_USUARIO, EnStatus.A)).thenReturn(Optional.of(usuarioEntity));
            assertDoesNotThrow(() -> sLote.validaPermissao(EMAIL_USUARIO));

            usuarioEntity.setPerfil(EnPerfilUsuario.CUIDADOR);
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
            when(loteSetorInterface.findByAnimais_IdAndLote_IdNot(ANIMAL_ID, LOTE_ID)).thenReturn(List.of(conflito));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                sLote.cadastra(EMAIL_USUARIO, loteCadastroDto);
            });

            assertTrue(exception.getMessage().contains("já está alocado ao lote LOT999"));
            verify(loteSetorInterface, never()).save(any());
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
            when(loteSetorInterface.findByAnimais_IdAndLote_IdNot(anyLong(), anyLong())).thenReturn(Collections.emptyList());
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
        void temMovimentacaoFinanceira_DeveRetornarFalse_PoisAindaEhPlaceholder() {
            assertFalse(sLote.temMovimentacaoFinanceira(LOTE_ID));
        }
    }
}