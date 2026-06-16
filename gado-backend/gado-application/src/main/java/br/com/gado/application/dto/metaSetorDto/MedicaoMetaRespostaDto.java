package br.com.gado.application.dto.metaSetorDto;

import java.time.LocalDate;

public class MedicaoMetaRespostaDto {

    private Long id;
    private Long loteId;
    private String loteDescricao;
    private LocalDate dataMedicao;

    /** Quantidade bruta lançada (Litros ou Peso Vivo em Kg). */
    private Double quantidadeLancada;

    /** Quantidade já convertida para a unidade da meta (Litros ou Arrobas). */
    private Double quantidadeConvertida;

    private String criadoPorEmail;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }

    public String getLoteDescricao() { return loteDescricao; }
    public void setLoteDescricao(String loteDescricao) { this.loteDescricao = loteDescricao; }

    public LocalDate getDataMedicao() { return dataMedicao; }
    public void setDataMedicao(LocalDate dataMedicao) { this.dataMedicao = dataMedicao; }

    public Double getQuantidadeLancada() { return quantidadeLancada; }
    public void setQuantidadeLancada(Double quantidadeLancada) { this.quantidadeLancada = quantidadeLancada; }

    public Double getQuantidadeConvertida() { return quantidadeConvertida; }
    public void setQuantidadeConvertida(Double quantidadeConvertida) { this.quantidadeConvertida = quantidadeConvertida; }

    public String getCriadoPorEmail() { return criadoPorEmail; }
    public void setCriadoPorEmail(String criadoPorEmail) { this.criadoPorEmail = criadoPorEmail; }
}
