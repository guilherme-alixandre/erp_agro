package br.com.gado.dto.folhaPagamentoDto;

import br.com.gado.enums.EnNaturezaFinanceira;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FuncionarioCadastroDto {

    @NotBlank(message = "O nome do funcionário é obrigatório.")
    private String nomeCompleto;

    @NotBlank(message = "O CPF é obrigatório.")
    private String cpf;

    @NotBlank(message = "O cargo é obrigatório.")
    private String cargo;

    @NotNull(message = "A data de admissão é obrigatória.")
    private LocalDate dataAdmissao;

    @NotNull(message = "O salário base é obrigatório.")
    private BigDecimal salarioBase;

    @NotNull(message = "O percentual de INSS é obrigatório.")
    private BigDecimal percentualInss;

    @NotNull(message = "O percentual de FGTS é obrigatório.")
    private BigDecimal percentualFgts;

    private BigDecimal valorValeTransporte;
    private BigDecimal valorValeAlimentacao;
    private BigDecimal valorPlanoSaude;

    @NotNull(message = "Classifique o funcionário como CUSTO (produção) ou GASTO (administrativo).")
    private EnNaturezaFinanceira naturezaFinanceira;

    /** Opcional — só quando o funcionário também é um usuário com login no sistema. */
    private Long usuarioId;
}
