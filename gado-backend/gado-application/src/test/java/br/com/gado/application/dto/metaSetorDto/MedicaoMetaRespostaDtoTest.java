package br.com.gado.application.dto.metaSetorDto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MedicaoMetaRespostaDtoTest {

    @Test
    void todosGettersESetters_DevemFuncionar() {
        MedicaoMetaRespostaDto dto = new MedicaoMetaRespostaDto();

        dto.setId(1L);
        assertEquals(1L, dto.getId());

        dto.setLoteId(2L);
        assertEquals(2L, dto.getLoteId());

        dto.setLoteDescricao("Lote Teste");
        assertEquals("Lote Teste", dto.getLoteDescricao());

        LocalDate data = LocalDate.of(2025, 1, 15);
        dto.setDataMedicao(data);
        assertEquals(data, dto.getDataMedicao());

        dto.setQuantidadeLancada(100.5);
        assertEquals(100.5, dto.getQuantidadeLancada());

        dto.setQuantidadeConvertida(7.5);
        assertEquals(7.5, dto.getQuantidadeConvertida());

        dto.setCriadoPorEmail("user@gado.com");
        assertEquals("user@gado.com", dto.getCriadoPorEmail());

        dto.setCriadoPorNome("João");
        assertEquals("João", dto.getCriadoPorNome());

        dto.setCriadoPorPerfil("CUIDADOR");
        assertEquals("CUIDADOR", dto.getCriadoPorPerfil());
    }

    @Test
    void valoresNulos_DevemSerAceitosSemExcecao() {
        MedicaoMetaRespostaDto dto = new MedicaoMetaRespostaDto();

        dto.setId(null);
        assertNull(dto.getId());

        dto.setLoteId(null);
        assertNull(dto.getLoteId());

        dto.setLoteDescricao(null);
        assertNull(dto.getLoteDescricao());

        dto.setDataMedicao(null);
        assertNull(dto.getDataMedicao());

        dto.setQuantidadeLancada(null);
        assertNull(dto.getQuantidadeLancada());

        dto.setQuantidadeConvertida(null);
        assertNull(dto.getQuantidadeConvertida());

        dto.setCriadoPorEmail(null);
        assertNull(dto.getCriadoPorEmail());

        dto.setCriadoPorNome(null);
        assertNull(dto.getCriadoPorNome());

        dto.setCriadoPorPerfil(null);
        assertNull(dto.getCriadoPorPerfil());
    }
}
