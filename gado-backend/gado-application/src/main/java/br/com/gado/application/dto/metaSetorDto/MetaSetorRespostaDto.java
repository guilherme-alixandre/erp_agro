package br.com.gado.application.dto.metaSetorDto;

import br.com.gado.domain.enums.EnTipoGado;
import br.com.gado.domain.enums.EnTipoMeta;

import java.time.LocalDate;
import java.util.List;

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

    // ── Campos calculados pelo Service ──────────────────────────────────────

    /** Soma de todas as medições já convertidas para a unidade da meta (L ou @). */
    private Double quantidadeRealizada;

    /** (quantidadeRealizada / quantidadeEsperada) * 100, arredondado em 2 casas. */
    private Double percentualProgresso;

    /** quantidadeRealizada * precoMedio */
    private Double valorRealizado;

    /** quantidadeEsperada * precoMedio */
    private Double valorEsperado;

    private List<MedicaoMetaRespostaDto> medicoes;

    // ── Getters e Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSetorId() { return setorId; }
    public void setSetorId(Long setorId) { this.setorId = setorId; }

    public String getSetorNome() { return setorNome; }
    public void setSetorNome(String setorNome) { this.setorNome = setorNome; }

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

    public Double getQuantidadeRealizada() { return quantidadeRealizada; }
    public void setQuantidadeRealizada(Double quantidadeRealizada) { this.quantidadeRealizada = quantidadeRealizada; }

    public Double getPercentualProgresso() { return percentualProgresso; }
    public void setPercentualProgresso(Double percentualProgresso) { this.percentualProgresso = percentualProgresso; }

    public Double getValorRealizado() { return valorRealizado; }
    public void setValorRealizado(Double valorRealizado) { this.valorRealizado = valorRealizado; }

    public Double getValorEsperado() { return valorEsperado; }
    public void setValorEsperado(Double valorEsperado) { this.valorEsperado = valorEsperado; }

    public List<MedicaoMetaRespostaDto> getMedicoes() { return medicoes; }
    public void setMedicoes(List<MedicaoMetaRespostaDto> medicoes) { this.medicoes = medicoes; }
}
