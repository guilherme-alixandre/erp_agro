package br.com.gado.integration;

import br.com.gado.GadoApplication;
import br.com.gado.application.dto.InsumoDto;
import br.com.gado.domain.entities.EInsumo;
import br.com.gado.domain.entities.EParceiro;
import br.com.gado.domain.enums.EnTipoInsumo;
import br.com.gado.domain.enums.EnTipoParceiro;
import br.com.gado.infrastructure.persistence.repositories.IInsumo;
import br.com.gado.infrastructure.persistence.repositories.IParceiro;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GadoApplication.class)
@AutoConfigureMockMvc
@Transactional
class InsumoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IInsumo insumoRepository;

    @Autowired
    private IParceiro parceiroRepository;

    @Test
    void crudVacina_quandoDadosValidos_persisteListaAtualizaEDeleta() throws Exception {
        String nome = "Vacina IT " + sufixo();

        MvcResult cadastro = mockMvc.perform(post("/api/insumos/vacinas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", nome, "pendente", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.tipo").value(EnTipoInsumo.VACINA.name()))
                .andExpect(jsonPath("$.pendente").value(true))
                .andReturn();

        InsumoDto vacina = read(cadastro, InsumoDto.class);
        assertNotNull(vacina.getId());

        mockMvc.perform(get("/api/insumos/vacinas").param("busca", nome))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(vacina.getId()))
                .andExpect(jsonPath("$[0].nome").value(nome));

        String nomeAtualizado = nome + " Atualizada";
        mockMvc.perform(put("/api/insumos/vacinas/{id}", vacina.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", nomeAtualizado, "pendente", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nomeAtualizado))
                .andExpect(jsonPath("$.pendente").value(false));

        mockMvc.perform(delete("/api/insumos/vacinas/{id}", vacina.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Vacina deletada com sucesso"));

        assertFalse(insumoRepository.findById(vacina.getId()).isPresent());
    }

    @Test
    void postVacina_quandoNomeDuplicado_retornaBadRequest() throws Exception {
        String nome = "Vacina Duplicada IT " + sufixo();
        Map<String, Object> payload = Map.of("nome", nome, "pendente", false);

        mockMvc.perform(post("/api/insumos/vacinas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/insumos/vacinas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void crudInsumo_quandoFornecedorExiste_persisteConsultaAtualizaEDeleta() throws Exception {
        EParceiro fornecedor = criarParceiro();
        String nome = "Racao IT " + sufixo();

        MvcResult cadastro = mockMvc.perform(post("/api/insumos/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(insumoPayload(nome, fornecedor.getId(), EnTipoInsumo.RACAO, 120.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.tipo").value(EnTipoInsumo.RACAO.name()))
                .andReturn();

        InsumoDto insumo = read(cadastro, InsumoDto.class);
        assertTrue(insumoRepository.findById(insumo.getId()).isPresent());

        mockMvc.perform(get("/api/insumos/{id}", insumo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(insumo.getId()))
                .andExpect(jsonPath("$.nome").value(nome));

        String nomeAtualizado = nome + " Premium";
        mockMvc.perform(put("/api/insumos/{id}", insumo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(insumoPayload(nomeAtualizado, fornecedor.getId(), EnTipoInsumo.RACAO, 180.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nomeAtualizado))
                .andExpect(jsonPath("$.saldoAtual").value(180.0));

        mockMvc.perform(delete("/api/insumos/{id}", insumo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Insumo deletado com sucesso"));

        assertFalse(insumoRepository.findById(insumo.getId()).isPresent());
    }

    @Test
    void postInsumo_quandoFornecedorAusenteOuInvalido_retornaErro() throws Exception {
        mockMvc.perform(post("/api/insumos/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(insumoPayload("Insumo Sem Fornecedor IT", null, EnTipoInsumo.OUTROS, 1.0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());

        mockMvc.perform(post("/api/insumos/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(insumoPayload("Insumo Fornecedor Invalido IT", Long.MAX_VALUE, EnTipoInsumo.OUTROS, 1.0))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void putVacina_quandoInsumoNaoEhVacina_retornaBadRequest() throws Exception {
        EInsumo racao = new EInsumo();
        racao.setNome("Racao Nao Vacina IT " + sufixo());
        racao.setTipo(EnTipoInsumo.RACAO);
        racao.setPendente(false);
        EInsumo salvo = insumoRepository.save(racao);

        mockMvc.perform(put("/api/insumos/vacinas/{id}", salvo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "Tentativa IT"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void getInsumo_quandoIdNaoExiste_retornaNotFound() throws Exception {
        mockMvc.perform(get("/api/insumos/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    private Map<String, Object> insumoPayload(String nome, Long parceiroId, EnTipoInsumo tipo, double saldoAtual) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("nome", nome);
        payload.put("estoqueMinimo", 10.0);
        payload.put("saldoAtual", saldoAtual);
        payload.put("parceiro_id", parceiroId);
        payload.put("tipo", tipo.name());
        payload.put("pendente", false);
        return payload;
    }

    private EParceiro criarParceiro() {
        EParceiro parceiro = new EParceiro();
        parceiro.setNome("Fornecedor IT " + sufixo());
        parceiro.setCpfCnpj("DOC-" + sufixo());
        parceiro.setEndereco("Endereco IT");
        parceiro.setTelefone("31999990000");
        parceiro.setTipo(EnTipoParceiro.FORNECEDOR);
        parceiro.setDataCadastro(LocalDateTime.now());
        return parceiroRepository.save(parceiro);
    }

    private <T> T read(MvcResult result, Class<T> type) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), type);
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    private static String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }
}
