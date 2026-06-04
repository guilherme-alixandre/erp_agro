package br.com.gado.application.dto.metaSetorDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class MedicaoMetaCadastroDto {

    @NotNull(message = "O ID da meta do setor é obrigatório.")
    private Long metaSetorId;

    @NotNull(message = "O ID do lote é obrigatório.")
    private Long loteId;

    @NotNull(message = "A data da medição é obrigatória.")
    private LocalDate dataMedicao;

    @NotNull(message = "A quantidade lançada é obrigatória.")
    @Positive(message = "A quantidade lançada deve ser maior que zero.")
    private Double quantidadeLancada;

    public Long getMetaSetorId() { return metaSetorId; }
    public void setMetaSetorId(Long metaSetorId) { this.metaSetorId = metaSetorId; }

    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }

    public LocalDate getDataMedicao() { return dataMedicao; }
    public void setDataMedicao(LocalDate dataMedicao) { this.dataMedicao = dataMedicao; }

    public Double getQuantidadeLancada() { return quantidadeLancada; }
    public void setQuantidadeLancada(Double quantidadeLancada) { this.quantidadeLancada = quantidadeLancada; }
}
