package br.com.gado.integration;

import br.com.gado.GadoApplication;
import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.entities.ELote;
import br.com.gado.domain.entities.ELoteSetor;
import br.com.gado.domain.entities.EMetaSetor;
import br.com.gado.domain.entities.ESetor;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnPerfilUsuario;
import br.com.gado.domain.enums.EnSexoAnimal;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.domain.enums.EnStatusAnimal;
import br.com.gado.domain.enums.EnTipoMeta;
import br.com.gado.domain.enums.EnTipoSetor;
import br.com.gado.infrastructure.persistence.repositories.IAnimal;
import br.com.gado.infrastructure.persistence.repositories.ILote;
import br.com.gado.infrastructure.persistence.repositories.ILoteSetor;
import br.com.gado.infrastructure.persistence.repositories.IMetaSetor;
import br.com.gado.infrastructure.persistence.repositories.ISetor;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GadoApplication.class)
@AutoConfigureMockMvc
@Transactional
class LoteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ILote loteRepository;

    @Autowired
    private ILoteSetor loteSetorRepository;

    @Autowired
    private ISetor setorRepository;

    @Autowired
    private IAnimal animalRepository;

    @Autowired
    private IUsuario usuarioRepository;

    @Autowired
    private IMetaSetor metaSetorRepository;

    @Test
    void postLote_quandoPayloadValido_criaCodigoSequencialEAlocacao() throws Exception {
        EUsuario usuario = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        ESetor setor = criarSetor(usuario, 5);
        EAnimal animal1 = criarAnimal(usuario, "LOT-ANI-" + sufixo());
        EAnimal animal2 = criarAnimal(usuario, "LOT-ANI-" + sufixo());
        String descricao = "Lote IT " + sufixo();

        mockMvc.perform(post("/api/lotes")
                        .header("X-Usuario-Email", usuario.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(lotePayload(descricao, "Azul", setor.getId(), List.of(animal1.getId(), animal2.getId())))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("cadastrado com sucesso")));

        ELote lote = buscarLotePorDescricao(descricao);
        assertTrue(lote.getCodigo().matches("LOT\\d{3}"));

        List<ELoteSetor> alocacoes = loteSetorRepository.findByLote_Id(lote.getId());
        assertEquals(1, alocacoes.size());
        assertEquals(setor.getId(), alocacoes.get(0).getSetor().getId());
        assertEquals(2, alocacoes.get(0).getAnimais().size());

        mockMvc.perform(get("/api/lotes/{id}", lote.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(lote.getCodigo()))
                .andExpect(jsonPath("$.descricao").value(descricao))
                .andExpect(jsonPath("$.criadoPorEmail").value(usuario.getEmail()))
                .andExpect(jsonPath("$.totalAnimais").value(2))
                .andExpect(jsonPath("$.alocacoes[0].setorId").value(setor.getId()));
    }

    @Test
    void putLote_quandoUsuarioAutorizado_atualizaCamposERealocaAnimais() throws Exception {
        EUsuario usuario = criarUsuario(EnPerfilUsuario.GERENTE);
        ESetor setorOriginal = criarSetor(usuario, 4);
        ESetor setorNovo = criarSetor(usuario, 4);
        EAnimal animal = criarAnimal(usuario, "LOT-ANI-" + sufixo());
        ELote lote = criarLoteViaEndpoint(usuario, setorOriginal, List.of(), "Lote Alteracao IT " + sufixo());

        String descricaoAtualizada = "Lote Alterado IT " + sufixo();
        mockMvc.perform(put("/api/lotes/{id}", lote.getId())
                        .header("X-Usuario-Email", usuario.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(lotePayload(descricaoAtualizada, "Verde", setorNovo.getId(), List.of(animal.getId())))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("atualizado com sucesso")));

        mockMvc.perform(get("/api/lotes/{id}", lote.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value(descricaoAtualizada))
                .andExpect(jsonPath("$.corBrinco").value("Verde"))
                .andExpect(jsonPath("$.alteradoPorEmail").value(usuario.getEmail()))
                .andExpect(jsonPath("$.totalAnimais").value(1))
                .andExpect(jsonPath("$.alocacoes[0].setorId").value(setorNovo.getId()))
                .andExpect(jsonPath("$.alocacoes[0].animais[0].id").value(animal.getId()));
    }

    @Test
    void deleteLote_quandoSemVinculos_removeDefinitivamente() throws Exception {
        EUsuario usuario = criarUsuario(EnPerfilUsuario.CUIDADOR);
        ESetor setor = criarSetor(usuario, 3);
        ELote lote = criarLoteViaEndpoint(usuario, setor, List.of(), "Lote Delete IT " + sufixo());

        mockMvc.perform(delete("/api/lotes/{id}", lote.getId())
                        .header("X-Usuario-Email", usuario.getEmail()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("LOT")));

        assertFalse(loteRepository.findById(lote.getId()).isPresent());
        assertTrue(loteSetorRepository.findByLote_Id(lote.getId()).isEmpty());
    }

    @Test
    void deleteLote_quandoPossuiMetaVinculada_inativaSemRemover() throws Exception {
        EUsuario usuario = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        ESetor setor = criarSetor(usuario, 3);
        ELote lote = criarLoteViaEndpoint(usuario, setor, List.of(), "Lote Soft Delete IT " + sufixo());
        criarMetaSetor(setor);

        mockMvc.perform(delete("/api/lotes/{id}", lote.getId())
                        .header("X-Usuario-Email", usuario.getEmail()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("inativado")));

        ELote atualizado = loteRepository.findById(lote.getId()).orElseThrow();
        assertEquals(EnStatus.I, atualizado.getStatus());
        assertFalse(loteSetorRepository.findByLote_Id(lote.getId()).isEmpty());
    }

    @Test
    void postLote_quandoDadosInvalidosOuUsuarioSemPermissao_retornaBadRequest() throws Exception {
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        EUsuario financeiro = criarUsuario(EnPerfilUsuario.FINANCEIRO);
        ESetor setor = criarSetor(admin, 2);
        EAnimal animal1 = criarAnimal(admin, "LOT-ANI-" + sufixo());
        EAnimal animal2 = criarAnimal(admin, "LOT-ANI-" + sufixo());

        mockMvc.perform(post("/api/lotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(lotePayload("Lote Sem Header IT", "Amarelo", setor.getId(), List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());

        mockMvc.perform(post("/api/lotes")
                        .header("X-Usuario-Email", financeiro.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(lotePayload("Lote Sem Permissao IT", "Amarelo", setor.getId(), List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());

        mockMvc.perform(post("/api/lotes")
                        .header("X-Usuario-Email", admin.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(lotePayload("Lote Sem Alocacao IT", "Amarelo", null, List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());

        mockMvc.perform(post("/api/lotes")
                        .header("X-Usuario-Email", admin.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(lotePayload("Lote Capacidade IT", "Amarelo", setor.getId(), List.of(animal1.getId(), animal2.getId())))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/lotes")
                        .header("X-Usuario-Email", admin.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(lotePayload("Lote Capacidade Excedida IT", "Amarelo", setor.getId(), List.of(animal1.getId())))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void getPutDeleteLote_quandoIdNaoExiste_retornaBadRequest() throws Exception {
        EUsuario usuario = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        ESetor setor = criarSetor(usuario, 2);

        mockMvc.perform(get("/api/lotes/{id}", Long.MAX_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());

        mockMvc.perform(put("/api/lotes/{id}", Long.MAX_VALUE)
                        .header("X-Usuario-Email", usuario.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(lotePayload("Lote Ausente IT", "Roxo", setor.getId(), List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());

        mockMvc.perform(delete("/api/lotes/{id}", Long.MAX_VALUE)
                        .header("X-Usuario-Email", usuario.getEmail()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    private ELote criarLoteViaEndpoint(EUsuario usuario, ESetor setor, List<Long> animaisIds, String descricao) throws Exception {
        mockMvc.perform(post("/api/lotes")
                        .header("X-Usuario-Email", usuario.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(lotePayload(descricao, "Azul", setor.getId(), animaisIds))))
                .andExpect(status().isOk());
        return buscarLotePorDescricao(descricao);
    }

    private Map<String, Object> lotePayload(String descricao, String corBrinco, Long setorId, List<Long> animaisIds) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("corBrinco", corBrinco);
        payload.put("descricao", descricao);
        payload.put("racaPredominante", "Nelore");
        payload.put("dataCriacao", LocalDate.now().toString());
        if (setorId == null) {
            payload.put("alocacoes", List.of());
        } else {
            payload.put("alocacoes", List.of(Map.of("setorId", setorId, "animaisIds", animaisIds)));
        }
        return payload;
    }

    private ELote buscarLotePorDescricao(String descricao) {
        return loteRepository.findAllByStatus(EnStatus.A)
                .stream()
                .filter(lote -> descricao.equals(lote.getDescricao()))
                .findFirst()
                .orElseThrow();
    }

    private EUsuario criarUsuario(EnPerfilUsuario perfil) {
        EUsuario usuario = new EUsuario();
        usuario.setNome("Usuario Lote IT " + sufixo());
        usuario.setEmail("lote-" + sufixo() + "@it.local");
        usuario.setSenha("senha-it");
        usuario.setPerfil(perfil);
        usuario.setDataCadastro(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    private ESetor criarSetor(EUsuario usuario, int capacidadeMaxima) {
        ESetor setor = new ESetor();
        setor.setNome("Setor IT " + sufixo());
        setor.setCapacidadeMaxima(capacidadeMaxima);
        setor.setMetaTexto("Meta IT");
        setor.setMetaProducaoLeite(100.0);
        setor.setMetaArrobaAbate(20.0);
        setor.setTipo(EnTipoSetor.PASTO);
        setor.setCriadoPor(usuario);
        return setorRepository.save(setor);
    }

    private EAnimal criarAnimal(EUsuario usuario, String brinco) {
        EAnimal animal = new EAnimal();
        animal.setCodigoBrinco(brinco);
        animal.setNome("Animal Lote IT " + sufixo());
        animal.setCor("Branco");
        animal.setDataNascimento(LocalDateTime.now().minusYears(2));
        animal.setPesoAtual(430.0);
        animal.setRaca("Nelore");
        animal.setAlturaCernelha(1.36);
        animal.setPerimetroToracico(1.88);
        animal.setComprimentoCorporal(2.0);
        animal.setSexo(EnSexoAnimal.F);
        animal.setStatusAnimal(EnStatusAnimal.ATIVO);
        animal.setUsuario(usuario);
        return animalRepository.save(animal);
    }

    private EMetaSetor criarMetaSetor(ESetor setor) {
        EMetaSetor meta = new EMetaSetor();
        meta.setSetor(setor);
        meta.setDataInicial(LocalDate.now());
        meta.setDataFinal(LocalDate.now().plusMonths(1));
        meta.setTipoMeta(EnTipoMeta.LEITE);
        meta.setQuantidadeEsperada(1000.0);
        meta.setPrecoMedio(2.5);
        return metaSetorRepository.save(meta);
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    private static String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }
}
