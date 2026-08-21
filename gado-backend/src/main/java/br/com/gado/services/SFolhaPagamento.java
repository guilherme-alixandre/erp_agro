package br.com.gado.services;

import br.com.gado.dto.folhaPagamentoDto.FuncionarioCadastroDto;
import br.com.gado.dto.folhaPagamentoDto.FuncionarioRespostaDto;
import br.com.gado.dto.folhaPagamentoDto.PagamentoFuncionarioCadastroDto;
import br.com.gado.dto.folhaPagamentoDto.PagamentoFuncionarioRespostaDto;
import br.com.gado.entities.EFuncionario;
import br.com.gado.entities.EPagamentoFuncionario;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnStatusDespesa;
import br.com.gado.repositories.IFuncionario;
import br.com.gado.repositories.IPagamentoFuncionario;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Funcionários e Folha de Pagamento. Cadastro de funcionário e lançamento de pagamento ficam
 * restritos a Administrador/Gerente (dado salarial sensível) — a leitura fica aberta aos três
 * perfis do módulo, incluindo Financeiro. Ajuste esse corte se o requisito for outro; não havia
 * uma regra explícita para esta subseção além do acesso geral ao módulo.
 */
@Service
public class SFolhaPagamento {

    private static final Set<EnPerfilUsuario> PERFIS_MODULO =
            EnumSet.of(EnPerfilUsuario.ADMINISTRADOR, EnPerfilUsuario.GERENTE, EnPerfilUsuario.FINANCEIRO);

    private static final Set<EnPerfilUsuario> PERFIS_GERENCIAIS =
            EnumSet.of(EnPerfilUsuario.ADMINISTRADOR, EnPerfilUsuario.GERENTE);

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    @Autowired
    private IFuncionario funcionarioInterface;

    @Autowired
    private IPagamentoFuncionario pagamentoInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private SLancamentoFinanceiro lancamentoFinanceiroService;

    // ── Permissões ───────────────────────────────────────────────────────

    private EUsuario resolveUsuarioModulo(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do usuário responsável pela operação.");
        }
        EUsuario usuario = usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (!PERFIS_MODULO.contains(usuario.getPerfil())) {
            throw new IllegalArgumentException(
                    "Apenas Administrador, Gerente ou Financeiro podem acessar o módulo financeiro.");
        }
        return usuario;
    }

    private void resolveUsuarioGerencial(String emailUsuario) {
        EUsuario usuario = resolveUsuarioModulo(emailUsuario);
        if (!PERFIS_GERENCIAIS.contains(usuario.getPerfil())) {
            throw new IllegalArgumentException("Apenas Administrador ou Gerente podem realizar esta ação.");
        }
    }

    // ── Funcionários ───────────────────────────────────────────────────

    @Transactional
    public FuncionarioRespostaDto cadastrarFuncionario(FuncionarioCadastroDto dto, String emailUsuarioLogado) {
        resolveUsuarioGerencial(emailUsuarioLogado);

        if (funcionarioInterface.findByCpf(dto.getCpf()).isPresent()) {
            throw new IllegalArgumentException("Já existe um funcionário cadastrado com este CPF.");
        }

        EFuncionario funcionario = new EFuncionario();
        funcionario.setNomeCompleto(dto.getNomeCompleto().trim());
        funcionario.setCpf(dto.getCpf().trim());
        funcionario.setCargo(dto.getCargo().trim());
        funcionario.setDataAdmissao(dto.getDataAdmissao());
        funcionario.setSalarioBase(dto.getSalarioBase());
        funcionario.setPercentualInss(dto.getPercentualInss());
        funcionario.setPercentualFgts(dto.getPercentualFgts());
        funcionario.setValorValeTransporte(dto.getValorValeTransporte());
        funcionario.setValorValeAlimentacao(dto.getValorValeAlimentacao());
        funcionario.setValorPlanoSaude(dto.getValorPlanoSaude());
        funcionario.setNaturezaFinanceira(dto.getNaturezaFinanceira());

        return toFuncionarioRespostaDto(funcionarioInterface.save(funcionario));
    }

    @Transactional
    public List<FuncionarioRespostaDto> listarFuncionarios(String emailUsuarioLogado) {
        resolveUsuarioModulo(emailUsuarioLogado);
        return funcionarioInterface.findByStatusOrderByNomeCompletoAsc(EnStatus.A)
                .stream().map(this::toFuncionarioRespostaDto).collect(Collectors.toList());
    }

    // ── Folha de pagamento ─────────────────────────────────────────────

    /**
     * Lança o pagamento de um funcionário para um Bloco Ano/Mês. valorBruto, descontoInss,
     * encargoFgts e valorBeneficios são calculados a partir do cadastro do funcionário —
     * PagamentoFuncionarioCadastroDto só traz a referência, a data e ajustes do mês.
     * Um pagamento com dataPagamento futura nasce PENDENTE e só entra no razão financeiro
     * (SLancamentoFinanceiro) quando, de fato, PAGO.
     */
    @Transactional
    public PagamentoFuncionarioRespostaDto lancarPagamento(PagamentoFuncionarioCadastroDto dto, String emailUsuarioLogado) {
        resolveUsuarioGerencial(emailUsuarioLogado);

        EFuncionario funcionario = funcionarioInterface.findById(dto.getFuncionarioId())
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado."));

        if (pagamentoInterface.findByFuncionarioIdAndAnoReferenciaAndMesReferencia(
                dto.getFuncionarioId(), dto.getAnoReferencia(), dto.getMesReferencia()).isPresent()) {
            throw new IllegalArgumentException("Já existe um pagamento lançado para este funcionário neste mês.");
        }

        BigDecimal valorBruto = funcionario.getSalarioBase();
        BigDecimal descontoInss = valorBruto
                .multiply(funcionario.getPercentualInss())
                .divide(CEM, 2, RoundingMode.HALF_UP);
        BigDecimal descontoOutros = dto.getDescontoOutros() != null ? dto.getDescontoOutros() : BigDecimal.ZERO;
        BigDecimal encargoFgts = valorBruto
                .multiply(funcionario.getPercentualFgts())
                .divide(CEM, 2, RoundingMode.HALF_UP);
        BigDecimal valorBeneficios = somaNaoNulos(
                funcionario.getValorValeTransporte(), funcionario.getValorValeAlimentacao(), funcionario.getValorPlanoSaude());
        BigDecimal valorLiquido = valorBruto.subtract(descontoInss).subtract(descontoOutros);

        EPagamentoFuncionario pagamento = new EPagamentoFuncionario();
        pagamento.setFuncionario(funcionario);
        pagamento.setAnoReferencia(dto.getAnoReferencia());
        pagamento.setMesReferencia(dto.getMesReferencia());
        pagamento.setDataPagamento(dto.getDataPagamento());
        pagamento.setStatusPagamento(dto.getDataPagamento().isAfter(LocalDate.now())
                ? EnStatusDespesa.PENDENTE : EnStatusDespesa.PAGO);
        pagamento.setValorBruto(valorBruto);
        pagamento.setDescontoInss(descontoInss);
        pagamento.setDescontoOutros(descontoOutros);
        pagamento.setEncargoFgts(encargoFgts);
        pagamento.setValorBeneficios(valorBeneficios);
        pagamento.setValorLiquido(valorLiquido);
        pagamento.setNaturezaFinanceiraSnapshot(funcionario.getNaturezaFinanceira());

        EPagamentoFuncionario salvo = pagamentoInterface.save(pagamento);

        if (salvo.getStatusPagamento() == EnStatusDespesa.PAGO) {
            lancamentoFinanceiroService.registrarSaidaFolhaPagamento(salvo);
        }

        return toPagamentoRespostaDto(salvo);
    }

    @Transactional
    public List<PagamentoFuncionarioRespostaDto> listarPagamentosPorBloco(int ano, int mes, String emailUsuarioLogado) {
        resolveUsuarioModulo(emailUsuarioLogado);
        return pagamentoInterface.findByAnoReferenciaAndMesReferencia(ano, mes)
                .stream().map(this::toPagamentoRespostaDto).collect(Collectors.toList());
    }

    private BigDecimal somaNaoNulos(BigDecimal... valores) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal valor : valores) {
            if (valor != null) {
                total = total.add(valor);
            }
        }
        return total;
    }

    // ── Mapeamento ────────────────────────────────────────────────────

    private FuncionarioRespostaDto toFuncionarioRespostaDto(EFuncionario funcionario) {
        FuncionarioRespostaDto dto = new FuncionarioRespostaDto();
        dto.setId(funcionario.getId());
        dto.setNomeCompleto(funcionario.getNomeCompleto());
        dto.setCpf(funcionario.getCpf());
        dto.setCargo(funcionario.getCargo());
        dto.setDataAdmissao(funcionario.getDataAdmissao());
        dto.setDataDemissao(funcionario.getDataDemissao());
        dto.setSalarioBase(funcionario.getSalarioBase());
        dto.setPercentualInss(funcionario.getPercentualInss());
        dto.setPercentualFgts(funcionario.getPercentualFgts());
        dto.setValorValeTransporte(funcionario.getValorValeTransporte());
        dto.setValorValeAlimentacao(funcionario.getValorValeAlimentacao());
        dto.setValorPlanoSaude(funcionario.getValorPlanoSaude());
        dto.setNaturezaFinanceira(funcionario.getNaturezaFinanceira());
        return dto;
    }

    private PagamentoFuncionarioRespostaDto toPagamentoRespostaDto(EPagamentoFuncionario pagamento) {
        PagamentoFuncionarioRespostaDto dto = new PagamentoFuncionarioRespostaDto();
        dto.setId(pagamento.getId());
        dto.setFuncionarioId(pagamento.getFuncionario().getId());
        dto.setFuncionarioNome(pagamento.getFuncionario().getNomeCompleto());
        dto.setAnoReferencia(pagamento.getAnoReferencia());
        dto.setMesReferencia(pagamento.getMesReferencia());
        dto.setDataPagamento(pagamento.getDataPagamento());
        dto.setStatusPagamento(pagamento.getStatusPagamento());
        dto.setValorBruto(pagamento.getValorBruto());
        dto.setDescontoInss(pagamento.getDescontoInss());
        dto.setDescontoOutros(pagamento.getDescontoOutros());
        dto.setEncargoFgts(pagamento.getEncargoFgts());
        dto.setValorBeneficios(pagamento.getValorBeneficios());
        dto.setValorLiquido(pagamento.getValorLiquido());
        dto.setNaturezaFinanceiraSnapshot(pagamento.getNaturezaFinanceiraSnapshot());
        return dto;
    }
}
