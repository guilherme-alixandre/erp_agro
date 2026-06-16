package br.com.gado.application.services;

import br.com.gado.application.dto.SetorDto;
import br.com.gado.application.dto.loteDto.LoteRespostaDto;
import br.com.gado.application.dto.metaSetorDto.MetaSetorRespostaDto;
import br.com.gado.domain.enums.EnStatus;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.OutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SPdfRelatorioTest {

    @InjectMocks
    private SPdfRelatorio sPdfRelatorio;

    @Mock
    private SSetor setorService;

    @Mock
    private SLote loteService;

    @Mock
    private SMetaSetor metaSetorService;

    @Nested
    class GerarRelatorioSetoresTests {

        @Test
        void deveGerarPdfDeSetoresComSucesso_QuandoListaEstiverPreenchida() {

            SetorDto setor1 = mock(SetorDto.class, RETURNS_DEEP_STUBS);
            when(setor1.getNome()).thenReturn("Pasto Alfa");
            when(setor1.getTipo().name()).thenReturn("PASTO");
            when(setor1.getCapacidadeMaxima()).thenReturn(100);
            when(setor1.getStatus()).thenReturn(EnStatus.A);
            when(setor1.getCriadoPorNome()).thenReturn("Gabriel");

            SetorDto setor2 = mock(SetorDto.class, RETURNS_DEEP_STUBS);
            when(setor2.getNome()).thenReturn("Confinamento Beta");
            when(setor2.getTipo()).thenReturn(null);
            when(setor2.getCapacidadeMaxima()).thenReturn(50);
            when(setor2.getStatus()).thenReturn(EnStatus.I);
            when(setor2.getCriadoPorNome()).thenReturn("");

            SetorDto setor3 = mock(SetorDto.class, RETURNS_DEEP_STUBS);
            when(setor3.getStatus()).thenReturn(null);
            when(setor3.getCriadoPorNome()).thenReturn(null);

            when(setorService.buscarTodos()).thenReturn(new ArrayList<>(List.of(setor1, setor2, setor3)));

            byte[] resultado = sPdfRelatorio.gerarRelatorioSetores();

            assertNotNull(resultado);
            assertTrue(resultado.length > 0);
            verify(setorService, times(1)).buscarTodos();
        }

        @Test
        void deveGerarPdfDeSetoresVazio_QuandoNaoHouverDados() {
            when(setorService.buscarTodos()).thenReturn(new ArrayList<>());

            byte[] resultado = sPdfRelatorio.gerarRelatorioSetores();

            assertNotNull(resultado);
            assertTrue(resultado.length > 0);
            verify(setorService, times(1)).buscarTodos();
        }

        @Test
        void deveLancarRuntimeException_QuandoOcorrerDocumentExceptionNosSetores() {
            when(setorService.buscarTodos()).thenReturn(new ArrayList<>());


            try (MockedStatic<PdfWriter> pdfWriterMockedStatic = mockStatic(PdfWriter.class)) {
                pdfWriterMockedStatic.when(() -> PdfWriter.getInstance(any(Document.class), any(OutputStream.class)))
                        .thenThrow(new DocumentException("Erro simulado no iText"));

                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                    sPdfRelatorio.gerarRelatorioSetores();
                });

                assertEquals("Erro ao gerar PDF de setores.", exception.getMessage());
            }
        }
    }

    @Nested
    class GerarRelatorioLotesTests {

        @Test
        void deveGerarPdfDeLotesComSucesso_QuandoListaEstiverPreenchida() {
            LoteRespostaDto lote1 = mock(LoteRespostaDto.class);
            when(lote1.getCodigo()).thenReturn("L001");
            when(lote1.getDescricao()).thenReturn("Lote de Cria");
            when(lote1.getRacaPredominante()).thenReturn("Nelore");
            when(lote1.getCorBrinco()).thenReturn("Verde");
            when(lote1.getTotalAnimais()).thenReturn(30);
            when(lote1.getDataCriacao()).thenReturn(LocalDate.of(2026, 5, 20));
            when(lote1.getCriadoPorNome()).thenReturn("Gabriel");

            LoteRespostaDto lote2 = mock(LoteRespostaDto.class);
            when(lote2.getCodigo()).thenReturn("L002");
            when(lote2.getDescricao()).thenReturn(null);
            when(lote2.getRacaPredominante()).thenReturn(" ");
            when(lote2.getDataCriacao()).thenReturn(null);

            when(loteService.listarTodos()).thenReturn(List.of(lote1, lote2));

            byte[] resultado = sPdfRelatorio.gerarRelatorioLotes();

            assertNotNull(resultado);
            assertTrue(resultado.length > 0);
            verify(loteService, times(1)).listarTodos();
        }

        @Test
        void deveGerarPdfDeLotesVazio_QuandoNaoHouverDados() {
            when(loteService.listarTodos()).thenReturn(Collections.emptyList());

            byte[] resultado = sPdfRelatorio.gerarRelatorioLotes();

            assertNotNull(resultado);
            assertTrue(resultado.length > 0);
            verify(loteService, times(1)).listarTodos();
        }

        @Test
        void deveLancarRuntimeException_QuandoOcorrerDocumentExceptionNosLotes() {
            when(loteService.listarTodos()).thenReturn(Collections.emptyList());

            try (MockedStatic<PdfWriter> pdfWriterMockedStatic = mockStatic(PdfWriter.class)) {
                pdfWriterMockedStatic.when(() -> PdfWriter.getInstance(any(Document.class), any(OutputStream.class)))
                        .thenThrow(new DocumentException("Erro simulado no iText"));

                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                    sPdfRelatorio.gerarRelatorioLotes();
                });

                assertEquals("Erro ao gerar PDF de lotes.", exception.getMessage());
            }
        }
    }

    @Nested
    class GerarRelatorioMetasTests {

        @Test
        void deveGerarPdfDeMetasComSucesso_QuandoListaEstiverPreenchida() {
            Long setorId = 1L;


            MetaSetorRespostaDto metaLeite = mock(MetaSetorRespostaDto.class, RETURNS_DEEP_STUBS);
            when(metaLeite.getSetorNome()).thenReturn("Setor A");
            when(metaLeite.getTipoMeta().name()).thenReturn("LEITE");
            when(metaLeite.getDataInicial()).thenReturn(LocalDate.of(2026, 1, 1));
            when(metaLeite.getDataFinal()).thenReturn(LocalDate.of(2026, 12, 31));
            when(metaLeite.getQuantidadeEsperada()).thenReturn(5000.50);
            when(metaLeite.getPercentualProgresso()).thenReturn(75.5);
            when(metaLeite.getValorEsperado()).thenReturn(15000.00);
            when(metaLeite.getValorRealizado()).thenReturn(12000.00);


            MetaSetorRespostaDto metaCorte = mock(MetaSetorRespostaDto.class, RETURNS_DEEP_STUBS);
            when(metaCorte.getSetorNome()).thenReturn("Setor B");
            when(metaCorte.getTipoMeta().name()).thenReturn("CORTE");
            when(metaCorte.getDataInicial()).thenReturn(null);
            when(metaCorte.getQuantidadeEsperada()).thenReturn(null);
            when(metaCorte.getPercentualProgresso()).thenReturn(0.0);
            when(metaCorte.getValorEsperado()).thenReturn(null);
            when(metaCorte.getValorRealizado()).thenReturn(null);


            MetaSetorRespostaDto metaSemTipo = mock(MetaSetorRespostaDto.class, RETURNS_DEEP_STUBS);
            when(metaSemTipo.getTipoMeta()).thenReturn(null);

            when(metaSetorService.listarPorSetor(setorId)).thenReturn(List.of(metaLeite, metaCorte, metaSemTipo));

            byte[] resultado = sPdfRelatorio.gerarRelatorioMetas(setorId);

            assertNotNull(resultado);
            assertTrue(resultado.length > 0);
            verify(metaSetorService, times(1)).listarPorSetor(setorId);
        }

        @Test
        void deveGerarPdfDeMetasVazio_QuandoNaoHouverDados() {
            Long setorId = 1L;
            when(metaSetorService.listarPorSetor(setorId)).thenReturn(Collections.emptyList());

            byte[] resultado = sPdfRelatorio.gerarRelatorioMetas(setorId);

            assertNotNull(resultado);
            assertTrue(resultado.length > 0);
            verify(metaSetorService, times(1)).listarPorSetor(setorId);
        }

        @Test
        void deveLancarRuntimeException_QuandoOcorrerDocumentExceptionNasMetas() {
            Long setorId = 1L;
            when(metaSetorService.listarPorSetor(setorId)).thenReturn(Collections.emptyList());

            try (MockedStatic<PdfWriter> pdfWriterMockedStatic = mockStatic(PdfWriter.class)) {
                pdfWriterMockedStatic.when(() -> PdfWriter.getInstance(any(Document.class), any(OutputStream.class)))
                        .thenThrow(new DocumentException("Erro simulado no iText"));

                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                    sPdfRelatorio.gerarRelatorioMetas(setorId);
                });

                assertEquals("Erro ao gerar PDF de metas.", exception.getMessage());
            }
        }
    }
}