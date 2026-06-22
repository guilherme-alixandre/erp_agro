package br.com.gado.application.dto.metaSetorDto;

import br.com.gado.domain.enums.EnTipoGado;
import br.com.gado.domain.enums.EnTipoMeta;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MetaSetorRespostaDtoTest {

    @Test
    void todosGettersESetters_DevemFuncionar() {
        MetaSetorRespostaDto dto = new MetaSetorRespostaDto();

        dto.setId(10L);
        assertEquals(10L, dto.getId());

        dto.setSetorId(20L);
        assertEquals(20L, dto.getSetorId());

        dto.setSetorNome("Pasto Norte");
        assertEquals("Pasto Norte", dto.getSetorNome());

        LocalDate inicio = LocalDate.of(2025, 1, 1);
        dto.setDataInicial(inicio);
        assertEquals(inicio, dto.getDataInicial());

        LocalDate fim = LocalDate.of(2025, 12, 31);
        dto.setDataFinal(fim);
        assertEquals(fim, dto.getDataFinal());

        dto.setTipoMeta(EnTipoMeta.LEITE);
        assertEquals(EnTipoMeta.LEITE, dto.getTipoMeta());

        dto.setQuantidadeEsperada(1000.0);
        assertEquals(1000.0, dto.getQuantidadeEsperada());

        dto.setPrecoMedio(2.50);
        assertEquals(2.50, dto.getPrecoMedio());

        dto.setTipoGado(EnTipoGado.BOVINO_JOVEM_50);
        assertEquals(EnTipoGado.BOVINO_JOVEM_50, dto.getTipoGado());

        dto.setQuantidadeRealizada(500.0);
        assertEquals(500.0, dto.getQuantidadeRealizada());

        dto.setPercentualProgresso(50.0);
        assertEquals(50.0, dto.getPercentualProgresso());

        dto.setValorRealizado(1250.0);
        assertEquals(1250.0, dto.getValorRealizado());

        dto.setValorEsperado(2500.0);
        assertEquals(2500.0, dto.getValorEsperado());

        MedicaoMetaRespostaDto medicao = new MedicaoMetaRespostaDto();
        List<MedicaoMetaRespostaDto> medicoes = List.of(medicao);
        dto.setMedicoes(medicoes);
        assertEquals(medicoes, dto.getMedicoes());
        assertEquals(1, dto.getMedicoes().size());
    }

    @Test
    void valoresNulos_DevemSerAceitosSemExcecao() {
        MetaSetorRespostaDto dto = new MetaSetorRespostaDto();

        dto.setId(null);
        assertNull(dto.getId());

        dto.setSetorId(null);
        assertNull(dto.getSetorId());

        dto.setSetorNome(null);
        assertNull(dto.getSetorNome());

        dto.setDataInicial(null);
        assertNull(dto.getDataInicial());

        dto.setDataFinal(null);
        assertNull(dto.getDataFinal());

        dto.setTipoMeta(null);
        assertNull(dto.getTipoMeta());

        dto.setQuantidadeEsperada(null);
        assertNull(dto.getQuantidadeEsperada());

        dto.setPrecoMedio(null);
        assertNull(dto.getPrecoMedio());

        dto.setTipoGado(null);
        assertNull(dto.getTipoGado());

        dto.setQuantidadeRealizada(null);
        assertNull(dto.getQuantidadeRealizada());

        dto.setPercentualProgresso(null);
        assertNull(dto.getPercentualProgresso());

        dto.setValorRealizado(null);
        assertNull(dto.getValorRealizado());

        dto.setValorEsperado(null);
        assertNull(dto.getValorEsperado());

        dto.setMedicoes(null);
        assertNull(dto.getMedicoes());
    }

    @Test
    void tipoMetaArroba_DeveFuncionar() {
        MetaSetorRespostaDto dto = new MetaSetorRespostaDto();
        dto.setTipoMeta(EnTipoMeta.ARROBA);
        dto.setTipoGado(EnTipoGado.CONFINAMENTO_56);
        assertEquals(EnTipoMeta.ARROBA, dto.getTipoMeta());
        assertEquals(EnTipoGado.CONFINAMENTO_56, dto.getTipoGado());
    }
}
