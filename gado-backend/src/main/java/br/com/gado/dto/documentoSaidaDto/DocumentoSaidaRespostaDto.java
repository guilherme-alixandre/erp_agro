package br.com.gado.dto.documentoSaidaDto;

import br.com.gado.enums.EnStatusAnimal;
import br.com.gado.enums.EnTipoDocumentoSaida;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class DocumentoSaidaRespostaDto {
    private Long id;
    private EnTipoDocumentoSaida tipoDocumento;
    private String numeroDocumento;
    private String chaveAcesso;
    private LocalDate dataEmissao;
    private BigDecimal valorTotal;
    private String criadoPorEmail;

    private List<ItemLeiteDto> itensLeite = new ArrayList<>();
    private List<ItemAnimalDto> itensAnimal = new ArrayList<>();

    @Data
    public static class ItemLeiteDto {
        private Long id;
        private Long loteId;
        private String loteCodigo;
        private BigDecimal litros;
        private BigDecimal precoLitro;
        private BigDecimal valorTotal;
    }

    @Data
    public static class ItemAnimalDto {
        private Long id;
        private Long animalId;
        private String animalCodigoBrinco;
        private EnStatusAnimal destino;
        private BigDecimal valorVenda;
    }
}
