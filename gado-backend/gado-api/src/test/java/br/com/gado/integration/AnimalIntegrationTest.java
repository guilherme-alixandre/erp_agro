package br.com.gado.integration;

import br.com.gado.GadoApplication;
import br.com.gado.application.dto.AnimalDto;
import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnPerfilUsuario;
import br.com.gado.domain.enums.EnSexoAnimal;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.domain.enums.EnStatusAnimal;
import br.com.gado.infrastructure.persistence.repositories.IAnimal;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
class AnimalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IUsuario usuarioRepository;

    @Autowired
    private IAnimal animalRepository;

    @Test
    void postAnimal_quandoUsuarioExiste_persisteAnimalERetornaCreated() throws Exception {
        EUsuario usuario = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        String brinco = "ANI-" + sufixo();
        AnimalDto dto = novoAnimalDto(brinco);

        mockMvc.perform(post("/api/animais/usuarios/{email}", usuario.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoBrinco").value(brinco))
                .andExpect(jsonPath("$.nome").value(dto.getNome()))
                .andExpect(jsonPath("$.sexo").value(EnSexoAnimal.F.name()))
                .andExpect(jsonPath("$.statusAnimal").value(EnStatusAnimal.ATIVO.name()));

        assertTrue(animalRepository.findByCodigoBrincoAndStatus(brinco, EnStatus.A).isPresent());
    }

    @Test
    void getPutDeleteAnimal_quandoAnimalAtivo_consultaAtualizaEInativa() throws Exception {
        EUsuario usuario = criarUsuario(EnPerfilUsuario.CUIDADOR);
        EAnimal animal = criarAnimal(usuario, "ANI-" + sufixo());

        mockMvc.perform(get("/api/animais/{brinco}", animal.getCodigoBrinco()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoBrinco").value(animal.getCodigoBrinco()))
                .andExpect(jsonPath("$.nome").value(animal.getNome()));

        AnimalDto atualizacao = new AnimalDto();
        atualizacao.setNome("Animal IT Atualizado");
        atualizacao.setPesoAtual(512.5);
        atualizacao.setCor("Preto");

        mockMvc.perform(put("/api/animais/{brinco}", animal.getCodigoBrinco())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(atualizacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Animal IT Atualizado"))
                .andExpect(jsonPath("$.pesoAtual").value(512.5))
                .andExpect(jsonPath("$.cor").value("Preto"));

        mockMvc.perform(delete("/api/animais/{brinco}", animal.getCodigoBrinco()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("animal deletado com sucesso"));

        assertFalse(animalRepository.findByCodigoBrincoAndStatus(animal.getCodigoBrinco(), EnStatus.A).isPresent());
        assertEquals(EnStatus.I,
                animalRepository.findByCodigoBrincoAndStatus(animal.getCodigoBrinco(), EnStatus.I)
                        .orElseThrow()
                        .getStatus());
    }

    @Test
    void postAnimal_quandoUsuarioNaoExiste_retornaNotFound() throws Exception {
        AnimalDto dto = novoAnimalDto("ANI-" + sufixo());

        mockMvc.perform(post("/api/animais/usuarios/{email}", "ausente-" + sufixo() + "@it.local")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void getPutDeleteAnimal_quandoBrincoNaoExiste_retornaNotFound() throws Exception {
        String brincoAusente = "ANI-" + sufixo();

        mockMvc.perform(get("/api/animais/{brinco}", brincoAusente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").exists());

        mockMvc.perform(put("/api/animais/{brinco}", brincoAusente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(novoAnimalDto("ANI-" + sufixo()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").exists());

        mockMvc.perform(delete("/api/animais/{brinco}", brincoAusente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    private AnimalDto novoAnimalDto(String brinco) {
        AnimalDto dto = new AnimalDto();
        dto.setStatus(EnStatus.A);
        dto.setCodigoBrinco(brinco);
        dto.setNome("Animal IT " + brinco);
        dto.setCor("Marrom");
        dto.setDataNascimento(LocalDateTime.now().minusYears(2));
        dto.setPesoAtual(420.0);
        dto.setRaca("Nelore");
        dto.setAlturaCernelha(1.35);
        dto.setPerimetroToracico(1.9);
        dto.setComprimentoCorporal(2.1);
        dto.setSexo(EnSexoAnimal.F);
        dto.setStatusAnimal(EnStatusAnimal.ATIVO);
        return dto;
    }

    private EUsuario criarUsuario(EnPerfilUsuario perfil) {
        EUsuario usuario = new EUsuario();
        usuario.setNome("Usuario IT " + sufixo());
        usuario.setEmail("usuario-" + sufixo() + "@it.local");
        usuario.setSenha("senha-it");
        usuario.setPerfil(perfil);
        usuario.setDataCadastro(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    private EAnimal criarAnimal(EUsuario usuario, String brinco) {
        EAnimal animal = new EAnimal();
        animal.setCodigoBrinco(brinco);
        animal.setNome("Animal IT " + brinco);
        animal.setCor("Branco");
        animal.setDataNascimento(LocalDateTime.now().minusYears(3));
        animal.setPesoAtual(450.0);
        animal.setRaca("Angus");
        animal.setAlturaCernelha(1.42);
        animal.setPerimetroToracico(1.95);
        animal.setComprimentoCorporal(2.05);
        animal.setSexo(EnSexoAnimal.M);
        animal.setStatusAnimal(EnStatusAnimal.ATIVO);
        animal.setUsuario(usuario);
        return animalRepository.save(animal);
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    private static String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }
}
