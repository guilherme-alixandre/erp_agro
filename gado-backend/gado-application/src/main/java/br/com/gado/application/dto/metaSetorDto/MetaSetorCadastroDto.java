package br.com.gado.application.dto.metaSetorDto;

import br.com.gado.domain.enums.EnTipoGado;
import br.com.gado.domain.enums.EnTipoMeta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class MetaSetorCadastroDto {

    @NotNull(message = "O ID do setor é obrigatório.")
    private Long setorId;

    @NotNull(message = "A data inicial é obrigatória.")
    private LocalDate dataInicial;

    @NotNull(message = "A data final é obrigatória.")
    private LocalDate dataFinal;

    @NotNull(message = "O tipo de meta é obrigatório (LEITE ou ARROBA).")
    private EnTipoMeta tipoMeta;

    @NotNull(message = "A quantidade esperada é obrigatória.")
    @Positive(message = "A quantidade esperada deve ser maior que zero.")
    private Double quantidadeEsperada;

    @NotNull(message = "O preço médio é obrigatório.")
    @Positive(message = "O preço médio deve ser maior que zero.")
    private Double precoMedio;

    /**
     * Obrigatório quando tipoMeta == ARROBA. Ignorado (e anulado) quando tipoMeta == LEITE.
     * A validação de negócio é feita no Service.
     */
    private EnTipoGado tipoGado;

    public Long getSetorId() { return setorId; }
    public void setSetorId(Long setorId) { this.setorId = setorId; }

    public LocalDate getDataInicial() { return dataInicial; }
    public void setDataInicial(LocalDate dataInicial) { this.dataInicial = dataInicial; }

    public LocalDate getDataFinal() { return dataFinal; }
    public void setDataFinal(LocalDate dataFinal) { this.dataFinal = dataFinal; }

    public EnTipoMeta getTipoMeta() { return tipoMeta; }
    public void setTipoMeta(EnTipoMeta tipoMeta) { this.tipoMeta = tipoMeta; }

    public Double getQuantidadeEsperada() { return quantidadeEsperada; }
    public void setQuantidadeEsperada(Double quantidadeEsperada) { this.quantidadeEsperada = quantidadeEsperada; }

    public Double getPrecoMedio() { return precoMedio; }
    public void setPrecoMedio(Double precoMedio) { this.precoMedio = precoMedio; }

    public EnTipoGado getTipoGado() { return tipoGado; }
    public void setTipoGado(EnTipoGado tipoGado) { this.tipoGado = tipoGado; }
}
