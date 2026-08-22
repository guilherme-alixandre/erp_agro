package br.com.gado.services;

import br.com.gado.dto.folhaPagamentoDto.EstornoPagamentoDto;
import br.com.gado.dto.folhaPagamentoDto.FuncionarioCadastroDto;
import br.com.gado.dto.folhaPagamentoDto.FuncionarioPutDto;
import br.com.gado.dto.folhaPagamentoDto.FuncionarioRespostaDto;
import br.com.gado.dto.folhaPagamentoDto.PagamentoFuncionarioCadastroDto;
import br.com.gado.dto.folhaPagamentoDto.PagamentoFuncionarioRespostaDto;
import br.com.gado.entities.EFuncionario;
import br.com.gado.entities.EPagamentoFuncionario;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnNaturezaFinanceira;
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
import java.time.LocalDateTime;
import java.util.Arrays;
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
        funcionario.setCargo(validaCargo(dto.getCargo()));
        funcionario.setDataAdmissao(dto.getDataAdmissao());
        funcionario.setSalarioBase(dto.getSalarioBase());
        funcionario.setPercentualInss(dto.getPercentualInss());
        funcionario.setPercentualFgts(dto.getPercentualFgts());
        funcionario.setValorValeTransporte(dto.getValorValeTransporte());
        funcionario.setValorValeAlimentacao(dto.getValorValeAlimentacao());
        funcionario.setValorPlanoSaude(dto.getValorPlanoSaude());
        // Um funcionário é sempre um custo — não há mais escolha de natureza financeira no cadastro.
        funcionario.setNaturezaFinanceira(EnNaturezaFinanceira.CUSTO);

        return toFuncionarioRespostaDto(funcionarioInterface.save(funcionario));
    }

    @Transactional
    public FuncionarioRespostaDto atualizarFuncionario(Long id, FuncionarioPutDto dto, String emailUsuarioLogado) {
        resolveUsuarioGerencial(emailUsuarioLogado);

        EFuncionario funcionario = funcionarioInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado."));

        if (dto.getNomeCompleto() != null && !dto.getNomeCompleto().isBlank()) {
            funcionario.setNomeCompleto(dto.getNomeCompleto().trim());
        }
        if (dto.getCargo() != null) {
            funcionario.setCargo(validaCargo(dto.getCargo()));
        }
        if (dto.getDataDemissao() != null) funcionario.setDataDemissao(dto.getDataDemissao());
        if (dto.getSalarioBase() != null) funcionario.setSalarioBase(dto.getSalarioBase());
        if (dto.getPercentualInss() != null) funcionario.setPercentualInss(dto.getPercentualInss());
        if (dto.getPercentualFgts() != null) funcionario.setPercentualFgts(dto.getPercentualFgts());
        if (dto.getValorValeTransporte() != null) funcionario.setValorValeTransporte(dto.getValorValeTransporte());
        if (dto.getValorValeAlimentacao() != null) funcionario.setValorValeAlimentacao(dto.getValorValeAlimentacao());
        if (dto.getValorPlanoSaude() != null) funcionario.setValorPlanoSaude(dto.getValorPlanoSaude());

        return toFuncionarioRespostaDto(funcionarioInterface.save(funcionario));
    }

    private String validaCargo(String cargo) {
        if (cargo == null || cargo.isBlank()) {
            throw new IllegalArgumentException("O cargo é obrigatório.");
        }
        String cargoLimpo = cargo.trim().toUpperCase();
        boolean valido = Arrays.stream(EnPerfilUsuario.values()).anyMatch(p -> p.name().equals(cargoLimpo));
        if (!valido) {
            throw new IllegalArgumentException("Cargo inválido. Selecione um dos cargos disponíveis no sistema.");
        }
        return cargoLimpo;
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
        BigDecimal valorBonus = dto.getValorBonus() != null ? dto.getValorBonus() : BigDecimal.ZERO;
        BigDecimal valorLiquido = valorBruto.subtract(descontoInss).subtract(descontoOutros).add(valorBonus);

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
        pagamento.setValorBonus(valorBonus);
        pagamento.setValorLiquido(valorLiquido);
        pagamento.setNaturezaFinanceiraSnapshot(funcionario.getNaturezaFinanceira());
        pagamento.setEstornado(false);

        EPagamentoFuncionario salvo = pagamentoInterface.save(pagamento);

        if (salvo.getStatusPagamento() == EnStatusDespesa.PAGO) {
            lancamentoFinanceiroService.registrarSaidaFolhaPagamento(salvo);
        }

        return toPagamentoRespostaDto(salvo);
    }

    /** Reverte um pagamento já lançado: estorna o lançamento financeiro correspondente. */
    @Transactional
    public PagamentoFuncionarioRespostaDto estornarPagamento(Long id, EstornoPagamentoDto dto, String emailUsuarioLogado) {
        resolveUsuarioGerencial(emailUsuarioLogado);

        EPagamentoFuncionario pagamento = pagamentoInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado."));

        if (Boolean.TRUE.equals(pagamento.getEstornado())) {
            throw new IllegalArgumentException("Este pagamento já foi estornado.");
        }

        if (pagamento.getStatusPagamento() == EnStatusDespesa.PAGO) {
            lancamentoFinanceiroService.estornarSaidaFolhaPagamento(pagamento);
        }

        pagamento.setEstornado(true);
        pagamento.setMotivoEstorno(dto.getMotivoEstorno().trim());
        pagamento.setEstornadoPorEmail(emailUsuarioLogado.trim());
        pagamento.setEstornadoEm(LocalDateTime.now());

        return toPagamentoRespostaDto(pagamentoInterface.save(pagamento));
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
        dto.setValorBonus(pagamento.getValorBonus());
        dto.setValorLiquido(pagamento.getValorLiquido());
        dto.setNaturezaFinanceiraSnapshot(pagamento.getNaturezaFinanceiraSnapshot());
        dto.setEstornado(pagamento.getEstornado());
        dto.setMotivoEstorno(pagamento.getMotivoEstorno());
        dto.setEstornadoPorEmail(pagamento.getEstornadoPorEmail());
        dto.setEstornadoEm(pagamento.getEstornadoEm());
        return dto;
    }
}
