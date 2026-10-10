package br.com.gado.services;

import br.com.gado.dto.SetorDto;
import br.com.gado.dto.loteDto.LoteDto;
import br.com.gado.dto.metaSetorDto.MetaSetorRespostaDto;
import br.com.gado.enums.EnTipoMeta;
import br.com.gado.enums.EnTipoSetor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SPdfRelatorioTest {

    @InjectMocks
    private SPdfRelatorio sPdfRelatorio;

    @Mock private SSetor setorService;
    @Mock private SLote loteService;
    @Mock private SMetaSetor metaSetorService;

    private static void assertEhPdfValido(byte[] pdf) {
        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    @Nested
    class RelatorioMetasTests {

        @Test
        void deveGerarPdf_ComMetasListadas() {
            MetaSetorRespostaDto meta = new MetaSetorRespostaDto();
            meta.setSetorNome("Pasto 1");
            meta.setTipoMeta(EnTipoMeta.LEITE);
            meta.setDataInicial(LocalDate.of(2026, 1, 1));
            meta.setDataFinal(LocalDate.of(2026, 1, 31));
            meta.setQuantidadeEsperada(1000.0);
            meta.setPercentualProgresso(50.0);
            meta.setValorEsperado(5000.0);
            meta.setValorRealizado(2500.0);
            when(metaSetorService.listarPorSetor(1L)).thenReturn(List.of(meta));

            byte[] pdf = sPdfRelatorio.gerarRelatorioMetas(1L);

            assertEhPdfValido(pdf);
        }

        @Test
        void deveGerarPdf_QuandoNenhumaMetaCadastrada() {
            when(metaSetorService.listarPorSetor(1L)).thenReturn(List.of());

            byte[] pdf = sPdfRelatorio.gerarRelatorioMetas(1L);

            assertEhPdfValido(pdf);
        }
    }

    @Nested
    class RelatorioLotesTests {

        @Test
        void deveGerarPdf_ComLotesListados() {
            LoteDto lote = new LoteDto();
            lote.setCodigo("LOT001");
            lote.setDescricao("Lote de engorda");
            lote.setDataCriacao(LocalDate.of(2026, 1, 1));
            lote.setTotalAnimais(10);
            when(loteService.listarTodos()).thenReturn(List.of(lote));

            byte[] pdf = sPdfRelatorio.gerarRelatorioLotes();

            assertEhPdfValido(pdf);
        }

        @Test
        void deveGerarPdf_QuandoNenhumLoteCadastrado() {
            when(loteService.listarTodos()).thenReturn(List.of());

            byte[] pdf = sPdfRelatorio.gerarRelatorioLotes();

            assertEhPdfValido(pdf);
        }
    }

    @Nested
    class RelatorioSetoresTests {

        @Test
        void deveGerarPdf_ComSetoresListados() {
            SetorDto setor = new SetorDto();
            setor.setNome("Pasto 1");
            setor.setTipo(EnTipoSetor.PASTO);
            setor.setCapacidadeMaxima(50);
            when(setorService.buscarTodos()).thenReturn(new java.util.ArrayList<>(List.of(setor)));

            byte[] pdf = sPdfRelatorio.gerarRelatorioSetores();

            assertEhPdfValido(pdf);
        }

        @Test
        void deveGerarPdf_QuandoNenhumSetorCadastrado() {
            when(setorService.buscarTodos()).thenReturn(new java.util.ArrayList<>());

            byte[] pdf = sPdfRelatorio.gerarRelatorioSetores();

            assertEhPdfValido(pdf);
        }
    }
}
