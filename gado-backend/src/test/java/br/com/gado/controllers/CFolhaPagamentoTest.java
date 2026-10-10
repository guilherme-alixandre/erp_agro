package br.com.gado.controllers;

import br.com.gado.dto.folhaPagamentoDto.EstornoPagamentoDto;
import br.com.gado.dto.folhaPagamentoDto.FuncionarioCadastroDto;
import br.com.gado.dto.folhaPagamentoDto.FuncionarioRespostaDto;
import br.com.gado.dto.folhaPagamentoDto.PagamentoFuncionarioCadastroDto;
import br.com.gado.dto.folhaPagamentoDto.PagamentoFuncionarioRespostaDto;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.security.JwtService;
import br.com.gado.security.TestSecurity;
import br.com.gado.services.SFolhaPagamento;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CFolhaPagamento.class)
class CFolhaPagamentoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SFolhaPagamento folhaPagamentoService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String USUARIO = "gerente@fazenda.com";

    @AfterEach
    void limparSecurityContext() {
        TestSecurity.limpar();
    }

    @Test
    void listarFuncionarios_deveRetornar200() throws Exception {
        when(folhaPagamentoService.listarFuncionarios(USUARIO)).thenReturn(List.of());

        mockMvc.perform(get("/api/folha-pagamento/funcionarios")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.GERENTE)))
                .andExpect(status().isOk());
    }

    @Test
    void cadastrarFuncionario_deveRetornar201() throws Exception {
        FuncionarioCadastroDto dto = new FuncionarioCadastroDto();
        dto.setNomeCompleto("João Cuidador");
        dto.setCpf("12345678909");
        dto.setCargo("CUIDADOR");
        dto.setDataAdmissao(LocalDate.of(2025, 1, 1));
        dto.setSalarioBase(new BigDecimal("3000.00"));
        dto.setPercentualInss(new BigDecimal("9.00"));
        dto.setPercentualFgts(new BigDecimal("8.00"));

        FuncionarioRespostaDto criado = new FuncionarioRespostaDto();
        criado.setId(1L);
        criado.setNomeCompleto("João Cuidador");
        when(folhaPagamentoService.cadastrarFuncionario(any(FuncionarioCadastroDto.class), eq(USUARIO)))
                .thenReturn(criado);

        mockMvc.perform(post("/api/folha-pagamento/funcionarios")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.GERENTE))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void cadastrarFuncionario_deveRetornar400_QuandoCamposObrigatoriosAusentes() throws Exception {
        FuncionarioCadastroDto dto = new FuncionarioCadastroDto();

        mockMvc.perform(post("/api/folha-pagamento/funcionarios")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.GERENTE))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void lancarPagamento_deveRetornar201() throws Exception {
        PagamentoFuncionarioCadastroDto dto = new PagamentoFuncionarioCadastroDto();
        dto.setFuncionarioId(1L);
        dto.setAnoReferencia(2026);
        dto.setMesReferencia(3);
        dto.setDataPagamento(LocalDate.now());

        PagamentoFuncionarioRespostaDto criado = new PagamentoFuncionarioRespostaDto();
        criado.setId(1L);
        when(folhaPagamentoService.lancarPagamento(any(PagamentoFuncionarioCadastroDto.class), eq(USUARIO)))
                .thenReturn(criado);

        mockMvc.perform(post("/api/folha-pagamento/pagamentos")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.GERENTE))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void estornarPagamento_deveRetornar200() throws Exception {
        EstornoPagamentoDto dto = new EstornoPagamentoDto();
        dto.setMotivoEstorno("Pagamento em duplicidade");

        PagamentoFuncionarioRespostaDto estornado = new PagamentoFuncionarioRespostaDto();
        estornado.setId(1L);
        estornado.setEstornado(true);
        when(folhaPagamentoService.estornarPagamento(eq(1L), any(EstornoPagamentoDto.class), eq(USUARIO)))
                .thenReturn(estornado);

        mockMvc.perform(post("/api/folha-pagamento/pagamentos/1/estornar")
                        .with(TestSecurity.autenticadoComo(USUARIO, EnPerfilUsuario.GERENTE))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estornado").value(true));
    }
}
