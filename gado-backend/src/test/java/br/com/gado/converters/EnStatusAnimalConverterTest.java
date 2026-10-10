package br.com.gado.converters;

import br.com.gado.enums.EnStatusAnimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnStatusAnimalConverterTest {

    private final EnStatusAnimalConverter converter = new EnStatusAnimalConverter();

    @Test
    void convertToDatabaseColumn_deveRetornarNomeDoEnum() {
        assertThat(converter.convertToDatabaseColumn(EnStatusAnimal.VENDIDO)).isEqualTo("VENDIDO");
    }

    @Test
    void convertToDatabaseColumn_deveRetornarNull_QuandoAtributoNulo() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertToEntityAttribute_deveConverterValorValido() {
        assertThat(converter.convertToEntityAttribute("OBITO")).isEqualTo(EnStatusAnimal.OBITO);
    }

    @Test
    void convertToEntityAttribute_deveRetornarNull_QuandoValorNuloOuVazio() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
        assertThat(converter.convertToEntityAttribute("")).isNull();
        assertThat(converter.convertToEntityAttribute("  ")).isNull();
    }

    @Test
    void convertToEntityAttribute_deveUsarAtivoComoFallback_QuandoValorDesconhecido() {
        assertThat(converter.convertToEntityAttribute("VALOR_INEXISTENTE")).isEqualTo(EnStatusAnimal.ATIVO);
    }
}
