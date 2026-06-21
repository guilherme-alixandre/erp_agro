package br.com.gado.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnTipoGadoTest {

    @Test
    void bovinoJovem_deveRetornarDescricaoCorreta() {
        assertEquals("Bovino jovem (macho)", EnTipoGado.BOVINO_JOVEM_50.getDescricao());
    }

    @Test
    void bovinoJovem_deveRetornarTaxaRendimentoCorreta() {
        assertEquals(0.50, EnTipoGado.BOVINO_JOVEM_50.getTaxaRendimento(), 1e-10);
    }

    @Test
    void novilhaDescarte_deveRetornarDescricaoCorreta() {
        assertEquals("Novilha / vaca de descarte", EnTipoGado.NOVILHA_DESCARTE_47_5.getDescricao());
    }

    @Test
    void novilhaDescarte_deveRetornarTaxaRendimentoCorreta() {
        assertEquals(0.475, EnTipoGado.NOVILHA_DESCARTE_47_5.getTaxaRendimento(), 1e-10);
    }

    @Test
    void confinamento_deveRetornarDescricaoCorreta() {
        assertEquals("Animal terminado em confinamento", EnTipoGado.CONFINAMENTO_56.getDescricao());
    }

    @Test
    void confinamento_deveRetornarTaxaRendimentoCorreta() {
        assertEquals(0.56, EnTipoGado.CONFINAMENTO_56.getTaxaRendimento(), 1e-10);
    }
}
