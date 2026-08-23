package br.com.gado.services;

import br.com.gado.dto.insumoDto.InsumoEstoqueRespostaDto;
import br.com.gado.dto.lancamentoFinanceiroDto.FontesReceitaDto;
import br.com.gado.dto.lancamentoFinanceiroDto.ResumoMensalDto;
import br.com.gado.dto.resumoDto.AlertaEstoqueResumoDto;
import br.com.gado.dto.resumoDto.ResumoDto;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IAnimal;
import br.com.gado.repositories.ILote;
import br.com.gado.repositories.ISetor;
import br.com.gado.repositories.IUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Agrega dados de vários módulos para a tela "Resumo" (dashboard inicial). */
@Service
public class SResumo {

    private static final Set<EnPerfilUsuario> PERFIS_FINANCEIRO_RESUMO =
            EnumSet.of(EnPerfilUsuario.ADMINISTRADOR, EnPerfilUsuario.GERENTE);

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private IAnimal animalInterface;

    @Autowired
    private ILote loteInterface;

    @Autowired
    private ISetor setorInterface;

    @Autowired
    private SInsumo insumoService;

    @Autowired
    private STarefa tarefaService;

    @Autowired
    private SLancamentoFinanceiro lancamentoFinanceiroService;

    @Transactional
    public ResumoDto gerarResumo(String email) {
        EUsuario usuario = resolveUsuarioAtivo(email);

        ResumoDto dto = new ResumoDto();
        dto.setFinanceiroVisivel(PERFIS_FINANCEIRO_RESUMO.contains(usuario.getPerfil()));

        if (dto.isFinanceiroVisivel()) {
            dto.setTotalAnimais((long) animalInterface.findAllByStatus(EnStatus.A).orElseGet(java.util.ArrayList::new).size());
            dto.setTotalLotes((long) loteInterface.findAllByStatus(EnStatus.A).size());
            dto.setTotalSetores((long) setorInterface.findAllByStatus(EnStatus.A).size());

            LocalDate hoje = LocalDate.now();
            ResumoMensalDto financeiro = lancamentoFinanceiroService.gerarResumoMensalInterno(hoje.getYear(), hoje.getMonthValue());
            dto.setVendasMensais(financeiro.getTotalEntradas());
            dto.setGastosMensais(financeiro.getTotalSaidas());
            dto.setLucroLiquido(financeiro.getLucroLiquido());

            FontesReceitaDto fontes = lancamentoFinanceiroService.analisarFontesReceita(hoje.getYear(), hoje.getMonthValue());
            dto.setMaiorFonteReceitaLabel(fontes.getMaiorFonteLabel());
            dto.setMaiorFonteReceitaValor(fontes.getMaiorFonteValor());
            dto.setMenorFonteReceitaLabel(fontes.getMenorFonteLabel());
            dto.setMenorFonteReceitaValor(fontes.getMenorFonteValor());
        }

        dto.setAlertasEstoque(insumoService.listarEstoque("", "A").stream()
                .filter(InsumoEstoqueRespostaDto::getAbaixoDoEstoqueMinimo)
                .map(this::toAlertaDto)
                .collect(Collectors.toList()));

        dto.setTarefasPendentes(tarefaService.listarMinhasTarefas(email).stream()
                .filter(t -> !t.isStatusConclusao())
                .collect(Collectors.toList()));

        return dto;
    }

    private EUsuario resolveUsuarioAtivo(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do usuário responsável pela operação.");
        }
        return usuarioInterface.findByEmailAndStatus(email.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    private AlertaEstoqueResumoDto toAlertaDto(InsumoEstoqueRespostaDto insumo) {
        AlertaEstoqueResumoDto dto = new AlertaEstoqueResumoDto();
        dto.setInsumoId(insumo.getId());
        dto.setInsumoNome(insumo.getNome());
        dto.setSaldoAtual(insumo.getSaldoAtual());
        dto.setEstoqueMinimo(insumo.getEstoqueMinimo());
        dto.setUnidadeMedidaSigla(insumo.getUnidadeMedidaPrimariaSigla());
        return dto;
    }
}
