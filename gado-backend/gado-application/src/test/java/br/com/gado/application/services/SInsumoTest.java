package br.com.gado.application.services;

import br.com.gado.application.dto.InsumoDto;
import br.com.gado.application.dto.insumoDto.VacinaCadastroDto;
import br.com.gado.application.dto.insumoDto.VacinaPutDto;
import br.com.gado.domain.entities.EInsumo;
import br.com.gado.domain.entities.EParceiro;
import br.com.gado.domain.enums.EnTipoInsumo;
import br.com.gado.infrastructure.persistence.repositories.IInsumo;
import br.com.gado.infrastructure.persistence.repositories.IParceiro;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SInsumoTest {

    @InjectMocks
    private SInsumo sInsumo;

    @Mock
    private IInsumo insumoInterface;

    @Mock
    private IParceiro parceiroInterface;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    private EInsumo vacinaEntity;
    private EParceiro parceiroEntity;
    private VacinaCadastroDto vacinaCadastroDto;
    private VacinaPutDto vacinaPutDto;
    private InsumoDto insumoDto;

    private final Long ID_TESTE = 1L;
    private final String NOME_VACINA = "Aftosa";

    @BeforeEach
    void setUp() {
        parceiroEntity = new EParceiro();
        parceiroEntity.setId(ID_TESTE);

        vacinaEntity = new EInsumo();
        vacinaEntity.setId(ID_TESTE);
        vacinaEntity.setNome(NOME_VACINA);
        vacinaEntity.setTipo(EnTipoInsumo.VACINA);
        vacinaEntity.setPendente(false);
        vacinaEntity.setParceiro(parceiroEntity);

        vacinaCadastroDto = new VacinaCadastroDto();
        vacinaCadastroDto.setNome(NOME_VACINA);
        vacinaCadastroDto.setPendente(true);

        vacinaPutDto = new VacinaPutDto();
        vacinaPutDto.setNome("Aftosa Atualizada");
        vacinaPutDto.setPendente(false);

        insumoDto = new InsumoDto();
        insumoDto.setNome("Milho");
        insumoDto.setParceiro_id(ID_TESTE);
    }

    @Nested
    class ListarVacinasTests {
        @Test
        void deveRetornarLista_QuandoBuscaForNulaOuEmBranco() {
            when(insumoInterface.findByTipoOrderByNomeAsc(EnTipoInsumo.VACINA)).thenReturn(List.of(vacinaEntity));

            List<InsumoDto> resultadoNulo = sInsumo.listarVacinas(null);
            List<InsumoDto> resultadoEmBranco = sInsumo.listarVacinas("   ");

            assertFalse(resultadoNulo.isEmpty());
            assertFalse(resultadoEmBranco.isEmpty());
            verify(insumoInterface, times(2)).findByTipoOrderByNomeAsc(EnTipoInsumo.VACINA);
        }

        @Test
        void deveRetornarListaFiltrada_QuandoBuscaForInformada() {
            when(insumoInterface.findByTipoAndNomeContainingIgnoreCaseOrderByNomeAsc(EnTipoInsumo.VACINA, "Aftosa"))
                    .thenReturn(List.of(vacinaEntity));

            List<InsumoDto> resultado = sInsumo.listarVacinas("Aftosa");

            assertFalse(resultado.isEmpty());
            verify(insumoInterface, times(1))
                    .findByTipoAndNomeContainingIgnoreCaseOrderByNomeAsc(EnTipoInsumo.VACINA, "Aftosa");
        }
    }

    @Nested
    class CriarVacinaTests {
        @Test
        void deveLancarExcecao_QuandoDtoOuNomeForemNulosVazios() {
            vacinaCadastroDto.setNome("");
            assertThrows(IllegalArgumentException.class, () -> sInsumo.criarVacina(null));
            assertThrows(IllegalArgumentException.class, () -> sInsumo.criarVacina(vacinaCadastroDto));
        }

        @Test
        void deveLancarExcecao_QuandoVacinaJaExistir() {
            when(insumoInterface.findFirstByTipoAndNomeIgnoreCase(EnTipoInsumo.VACINA, NOME_VACINA))
                    .thenReturn(Optional.of(vacinaEntity));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> sInsumo.criarVacina(vacinaCadastroDto));
            assertEquals("Vacina já cadastrada.", ex.getMessage());
        }

        @Test
        void deveCriarVacina_QuandoDadosForemValidos() {
            when(insumoInterface.findFirstByTipoAndNomeIgnoreCase(EnTipoInsumo.VACINA, NOME_VACINA))
                    .thenReturn(Optional.empty());
            when(insumoInterface.save(any(EInsumo.class))).thenReturn(vacinaEntity);

            InsumoDto resultado = sInsumo.criarVacina(vacinaCadastroDto);

            assertNotNull(resultado);
            verify(insumoInterface, times(1)).save(any(EInsumo.class));
        }
    }

    @Nested
    class AtualizarVacinaTests {
        @Test
        void deveLancarExcecao_QuandoNaoEncontrarVacina() {
            when(insumoInterface.findById(ID_TESTE)).thenReturn(Optional.empty());
            assertThrows(EntityNotFoundException.class, () -> sInsumo.atualizarVacina(ID_TESTE, vacinaPutDto));
        }

        @Test
        void deveLancarExcecao_QuandoInsumoNaoForVacina() {
            EInsumo naoVacina = new EInsumo();
            naoVacina.setTipo(null); // Qualquer coisa diferente de VACINA
            when(insumoInterface.findById(ID_TESTE)).thenReturn(Optional.of(naoVacina));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> sInsumo.atualizarVacina(ID_TESTE, vacinaPutDto));
            assertEquals("Insumo informado não é uma vacina.", ex.getMessage());
        }

        @Test
        void deveLancarExcecao_QuandoDtoForNulo() {
            when(insumoInterface.findById(ID_TESTE)).thenReturn(Optional.of(vacinaEntity));
            assertThrows(IllegalArgumentException.class, () -> sInsumo.atualizarVacina(ID_TESTE, null));
        }

        @Test
        void deveLancarExcecao_QuandoNomeForVazio() {
            when(insumoInterface.findById(ID_TESTE)).thenReturn(Optional.of(vacinaEntity));
            vacinaPutDto.setNome("   ");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> sInsumo.atualizarVacina(ID_TESTE, vacinaPutDto));
            assertEquals("Informe o nome da vacina.", ex.getMessage());
        }

        @Test
        void deveAtualizarVacina_QuandoDadosValidos() {
            when(insumoInterface.findById(ID_TESTE)).thenReturn(Optional.of(vacinaEntity));
            when(insumoInterface.save(any(EInsumo.class))).thenReturn(vacinaEntity);

            InsumoDto resultado = sInsumo.atualizarVacina(ID_TESTE, vacinaPutDto);

            assertNotNull(resultado);
            verify(insumoInterface, times(1)).save(vacinaEntity);
        }
    }

    @Nested
    class DeletarVacinaTests {
        @Test
        void deveDeletarVacina_QuandoSucesso() {
            when(insumoInterface.findById(ID_TESTE)).thenReturn(Optional.of(vacinaEntity));

            String msg = sInsumo.deletarVacina(ID_TESTE);

            assertEquals("Vacina deletada com sucesso", msg);
            verify(insumoInterface, times(1)).deleteById(ID_TESTE);
        }

        @Test
        void deveLancarExcecao_AoDeletarInsumoQueNaoEVacina() {
            EInsumo naoVacina = new EInsumo();
            naoVacina.setTipo(null);
            when(insumoInterface.findById(ID_TESTE)).thenReturn(Optional.of(naoVacina));

            assertThrows(IllegalArgumentException.class, () -> sInsumo.deletarVacina(ID_TESTE));
            verify(insumoInterface, never()).deleteById(anyLong());
        }
    }

    @Nested
    class BuscaPorIdTests {
        @Test
        void deveRetornarInsumoDto_QuandoEncontrado() {
            when(insumoInterface.findById(ID_TESTE)).thenReturn(Optional.of(vacinaEntity));
            assertNotNull(sInsumo.buscaPorId(ID_TESTE));
        }
    }

    @Nested
    class CadastraInsumoTests {
        @Test
        void deveLancarExcecao_QuandoDtoOuParceiroForemNulos() {
            assertThrows(IllegalArgumentException.class, () -> sInsumo.cadastraInsumo(null));

            insumoDto.setParceiro_id(null);
            assertThrows(IllegalArgumentException.class, () -> sInsumo.cadastraInsumo(insumoDto));
        }

        @Test
        void deveLancarExcecao_QuandoParceiroNaoEncontrado() {
            when(parceiroInterface.findById(ID_TESTE)).thenReturn(Optional.empty());
            assertThrows(EntityNotFoundException.class, () -> sInsumo.cadastraInsumo(insumoDto));
        }

        @Test
        void deveCadastrarInsumo_QuandoSucesso() {
            when(parceiroInterface.findById(ID_TESTE)).thenReturn(Optional.of(parceiroEntity));
            when(insumoInterface.save(any(EInsumo.class))).thenReturn(vacinaEntity);

            InsumoDto resultado = sInsumo.cadastraInsumo(insumoDto);

            assertNotNull(resultado);
            verify(insumoInterface, times(1)).save(any(EInsumo.class));
        }
    }

    @Nested
    class DeletaInsumoTests {
        @Test
        void deveRetornarMensagemErro_QuandoInsumoNaoEncontrado() {
            when(insumoInterface.findById(ID_TESTE)).thenReturn(Optional.empty());
            assertEquals("Nenhum insumo com esse id foi encontrado", sInsumo.deletaInsumo(ID_TESTE));
        }

        @Test
        void deveDeletarInsumo_QuandoEncontrado() {
            when(insumoInterface.findById(ID_TESTE)).thenReturn(Optional.of(vacinaEntity));
            assertEquals("Insumo deletado com sucesso", sInsumo.deletaInsumo(ID_TESTE));
            verify(insumoInterface, times(1)).deleteById(ID_TESTE);
        }
    }

    @Nested
    class AlteraInsumoTests {
        @Test
        void deveLancarExcecao_AoAlterarInsumoInexistente() {
            when(insumoInterface.findById(ID_TESTE)).thenReturn(Optional.empty());
            assertThrows(EntityNotFoundException.class, () -> sInsumo.alteraInsumo(ID_TESTE, insumoDto));
        }

        @Test
        void deveAlterarInsumo_QuandoSucesso() {
            when(insumoInterface.findById(ID_TESTE)).thenReturn(Optional.of(vacinaEntity));
            when(insumoInterface.save(any(EInsumo.class))).thenReturn(vacinaEntity);

            InsumoDto resultado = sInsumo.alteraInsumo(ID_TESTE, insumoDto);

            assertNotNull(resultado);
            assertTrue(modelMapper.getConfiguration().isSkipNullEnabled());
            verify(insumoInterface, times(1)).save(vacinaEntity);
        }
    }
}