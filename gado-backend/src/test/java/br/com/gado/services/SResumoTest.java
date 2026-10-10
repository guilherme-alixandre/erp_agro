package br.com.gado.services;

import br.com.gado.dto.insumoDto.InsumoEstoqueRespostaDto;
import br.com.gado.dto.lancamentoFinanceiroDto.FontesReceitaDto;
import br.com.gado.dto.lancamentoFinanceiroDto.ResumoMensalDto;
import br.com.gado.dto.resumoDto.ResumoDto;
import br.com.gado.dto.tarefaDto.TarefaRespostaDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.ELote;
import br.com.gado.entities.ESetor;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IAnimal;
import br.com.gado.repositories.ILote;
import br.com.gado.repositories.ISetor;
import br.com.gado.repositories.IUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SResumoTest {

    @InjectMocks
    private SResumo sResumo;

    @Mock private IUsuario usuarioInterface;
    @Mock private IAnimal animalInterface;
    @Mock private ILote loteInterface;
    @Mock private ISetor setorInterface;
    @Mock private SInsumo insumoService;
    @Mock private STarefa tarefaService;
    @Mock private SLancamentoFinanceiro lancamentoFinanceiroService;

    private EUsuario admin;
    private EUsuario cuidador;

    @BeforeEach
    void setUp() {
        admin = new EUsuario();
        admin.setEmail("admin@fazenda.com");
        admin.setPerfil(EnPerfilUsuario.ADMINISTRADOR);

        cuidador = new EUsuario();
        cuidador.setEmail("cuidador@fazenda.com");
        cuidador.setPerfil(EnPerfilUsuario.CUIDADOR);
    }

    private void stubUsuario(EUsuario usuario) {
        when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A))
                .thenReturn(Optional.of(usuario));
    }

    private InsumoEstoqueRespostaDto insumoAbaixoDoMinimo() {
        InsumoEstoqueRespostaDto dto = new InsumoEstoqueRespostaDto();
        dto.setId(1L);
        dto.setNome("Ração");
        dto.setAbaixoDoEstoqueMinimo(true);
        return dto;
    }

    private InsumoEstoqueRespostaDto insumoOk() {
        InsumoEstoqueRespostaDto dto = new InsumoEstoqueRespostaDto();
        dto.setId(2L);
        dto.setNome("Sal Mineral");
        dto.setAbaixoDoEstoqueMinimo(false);
        return dto;
    }

    private TarefaRespostaDto tarefaPendente() {
        TarefaRespostaDto dto = new TarefaRespostaDto();
        dto.setId(1L);
        dto.setStatusConclusao(false);
        return dto;
    }

    private TarefaRespostaDto tarefaConcluida() {
        TarefaRespostaDto dto = new TarefaRespostaDto();
        dto.setId(2L);
        dto.setStatusConclusao(true);
        return dto;
    }

    @Test
    void deveGerarResumoCompleto_QuandoUsuarioAdministrador() {
        stubUsuario(admin);
        when(animalInterface.findAllByStatus(EnStatus.A)).thenReturn(Optional.of(new ArrayList<>(List.of(new EAnimal()))));
        when(loteInterface.findAllByStatus(EnStatus.A)).thenReturn(new ArrayList<>(List.of(new ELote())));
        when(setorInterface.findAllByStatus(EnStatus.A)).thenReturn(new ArrayList<>(List.of(new ESetor())));
        when(lancamentoFinanceiroService.gerarResumoMensalInterno(anyInt(), anyInt())).thenReturn(
                new ResumoMensalDto(2026, 3, new BigDecimal("1000"), new BigDecimal("300"),
                        new BigDecimal("200"), new BigDecimal("500"), new BigDecimal("500")));
        when(lancamentoFinanceiroService.analisarFontesReceita(anyInt(), anyInt()))
                .thenReturn(new FontesReceitaDto("Leite", new BigDecimal("800"), "Nelore", new BigDecimal("200")));
        when(insumoService.listarEstoque("", "A")).thenReturn(List.of(insumoAbaixoDoMinimo(), insumoOk()));
        when(tarefaService.listarMinhasTarefas(admin.getEmail())).thenReturn(List.of(tarefaPendente(), tarefaConcluida()));

        ResumoDto resultado = sResumo.gerarResumo(admin.getEmail());

        assertThat(resultado.isFinanceiroVisivel()).isTrue();
        assertThat(resultado.getTotalAnimais()).isEqualTo(1L);
        assertThat(resultado.getTotalLotes()).isEqualTo(1L);
        assertThat(resultado.getTotalSetores()).isEqualTo(1L);
        assertThat(resultado.getVendasMensais()).isEqualByComparingTo("1000");
        assertThat(resultado.getLucroLiquido()).isEqualByComparingTo("500");
        assertThat(resultado.getMaiorFonteReceitaLabel()).isEqualTo("Leite");
        assertThat(resultado.getAlertasEstoque()).hasSize(1);
        assertThat(resultado.getAlertasEstoque().get(0).getInsumoNome()).isEqualTo("Ração");
        assertThat(resultado.getTarefasPendentes()).hasSize(1);
    }

    @Test
    void deveOcultarDadosFinanceiros_QuandoUsuarioCuidador() {
        stubUsuario(cuidador);
        when(insumoService.listarEstoque("", "A")).thenReturn(List.of());
        when(tarefaService.listarMinhasTarefas(cuidador.getEmail())).thenReturn(List.of());

        ResumoDto resultado = sResumo.gerarResumo(cuidador.getEmail());

        assertThat(resultado.isFinanceiroVisivel()).isFalse();
        assertThat(resultado.getTotalAnimais()).isNull();
        verifyNoInteractions(animalInterface, loteInterface, setorInterface, lancamentoFinanceiroService);
    }

    @Test
    void deveLancarExcecao_QuandoEmailEmBranco() {
        assertThatThrownBy(() -> sResumo.gerarResumo(" "))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(insumoService, tarefaService);
    }
}
