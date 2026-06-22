package br.com.gado.application.dto.metaSetorDto;

import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class MedicaoMetaPutDto {

    private Long loteId;
    private LocalDate dataMedicao;

    @Positive(message = "A quantidade deve ser maior que zero.")
    private Double quantidadeLancada;

    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }

    public LocalDate getDataMedicao() { return dataMedicao; }
    public void setDataMedicao(LocalDate dataMedicao) { this.dataMedicao = dataMedicao; }

    public Double getQuantidadeLancada() { return quantidadeLancada; }
    public void setQuantidadeLancada(Double quantidadeLancada) { this.quantidadeLancada = quantidadeLancada; }
}
