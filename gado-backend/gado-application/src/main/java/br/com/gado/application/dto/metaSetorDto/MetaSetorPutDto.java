package br.com.gado.application.dto.metaSetorDto;

import br.com.gado.domain.enums.EnTipoGado;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Permite atualizar campos editáveis da MetaSetor.
 * Campos nulos são ignorados (atualização parcial).
 * O tipoMeta e o setorId não são alteráveis após a criação.
 */
public class MetaSetorPutDto {

    private LocalDate dataInicial;

    private LocalDate dataFinal;

    @Positive(message = "A quantidade esperada deve ser maior que zero.")
    private Double quantidadeEsperada;

    @Positive(message = "O preço médio deve ser maior que zero.")
    private Double precoMedio;

    private EnTipoGado tipoGado;

    public LocalDate getDataInicial() { return dataInicial; }
    public void setDataInicial(LocalDate dataInicial) { this.dataInicial = dataInicial; }

    public LocalDate getDataFinal() { return dataFinal; }
    public void setDataFinal(LocalDate dataFinal) { this.dataFinal = dataFinal; }

    public Double getQuantidadeEsperada() { return quantidadeEsperada; }
    public void setQuantidadeEsperada(Double quantidadeEsperada) { this.quantidadeEsperada = quantidadeEsperada; }

    public Double getPrecoMedio() { return precoMedio; }
    public void setPrecoMedio(Double precoMedio) { this.precoMedio = precoMedio; }

    public EnTipoGado getTipoGado() { return tipoGado; }
    public void setTipoGado(EnTipoGado tipoGado) { this.tipoGado = tipoGado; }
}
