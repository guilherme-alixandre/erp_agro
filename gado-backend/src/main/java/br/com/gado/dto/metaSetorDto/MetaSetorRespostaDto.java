package br.com.gado.dto.metaSetorDto;

import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoGado;
import br.com.gado.enums.EnTipoMeta;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class MetaSetorRespostaDto {

    private Long id;
    private Long setorId;
    private String setorNome;
    private LocalDate dataInicial;
    private LocalDate dataFinal;
    private EnTipoMeta tipoMeta;
    private Double quantidadeEsperada;
    private Double precoMedio;
    private EnTipoGado tipoGado;
    private EnStatus status;

    /** Soma de todas as medições já convertidas para a unidade da meta (L ou @). */
    private Double quantidadeRealizada;

    /** (quantidadeRealizada / quantidadeEsperada) * 100, arredondado em 2 casas. */
    private Double percentualProgresso;

    /** Soma de litros vendidos (EVendaMetaLote) — só se aplica a metas de LEITE. */
    private Double quantidadeVendida;

    /** (quantidadeVendida / quantidadeEsperada) * 100, arredondado em 2 casas. */
    private Double percentualVendido;

    /** quantidadeRealizada * precoMedio */
    private Double valorRealizado;

    /** quantidadeEsperada * precoMedio */
    private Double valorEsperado;

    private List<MedicaoMetaRespostaDto> medicoes = new ArrayList<>();
}
