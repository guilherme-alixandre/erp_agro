package br.com.gado.infrastructure.persistence.converters;

import br.com.gado.domain.enums.EnStatusAnimal;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter(autoApply = true)
public class EnStatusAnimalConverter implements AttributeConverter<EnStatusAnimal, String> {

    private static final Logger log = LoggerFactory.getLogger(EnStatusAnimalConverter.class);

    @Override
    public String convertToDatabaseColumn(EnStatusAnimal attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public EnStatusAnimal convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return EnStatusAnimal.valueOf(dbData.trim());
        } catch (IllegalArgumentException e) {
            log.warn("Valor desconhecido para EnStatusAnimal no banco: '{}'. Usando ATIVO como fallback.", dbData);
            return EnStatusAnimal.ATIVO;
        }
    }
}
