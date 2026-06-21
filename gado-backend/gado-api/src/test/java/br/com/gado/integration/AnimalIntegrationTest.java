package br.com.gado.integration;

import br.com.gado.GadoApplication;
import br.com.gado.application.dto.AnimalDto;
import br.com.gado.application.services.SAnimal;
import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnPerfilUsuario;
import br.com.gado.domain.enums.EnSexoAnimal;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.domain.enums.EnStatusAnimal;
import br.com.gado.infrastructure.persistence.repositories.IAnimal;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = GadoApplication.class)
@ActiveProfiles("test")
@Transactional
class AnimalIntegrationTest {

    @Autowired
    private SAnimal animalService;

    @Autowired
    private IAnimal animalRepository;

    @Autowired
    private IUsuario usuarioRepository;

    // ── Happy Path ────────────────────────────────────────────────────────────

    @Test
    void deveCadastrarAnimalComSucessoQuandoUsuarioExiste() {
        // Arrange
        EUsuario usuario = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        String brinco = "ANI-" + sufixo();
        AnimalDto dto = novoAnimalDto(brinco);

        // Act
        AnimalDto resultado = animalService.cadastraAnimal(usuario.getEmail(), dto);

        // Assert
        assertThat(resultado.getCodigoBrinco()).isEqualTo(brinco);
        assertThat(resultado.getNome()).isEqualTo(dto.getNome());
        assertThat(resultado.getSexo()).isEqualTo(EnSexoAnimal.F);
        assertThat(resultado.getStatusAnimal()).isEqualTo(EnStatusAnimal.ATIVO);
        assertThat(animalRepository.findByCodigoBrincoAndStatus(brinco, EnStatus.A)).isPresent();
    }

    @Test
    void deveBuscarAnimalPorBrincoComSucessoQuandoExiste() {
        // Arrange
        EUsuario usuario = criarUsuario(EnPerfilUsuario.CUIDADOR);
        EAnimal animal = criarAnimal(usuario, "ANI-" + sufixo());

        // Act
        AnimalDto resultado = animalService.buscarPorBrinco(animal.getCodigoBrinco());

        // Assert
        assertThat(resultado.getCodigoBrinco()).isEqualTo(animal.getCodigoBrinco());
        assertThat(resultado.getNome()).isEqualTo(animal.getNome());
        assertThat(resultado.getCriadoPorEmail()).isEqualTo(usuario.getEmail());
    }

    @Test
    void deveAlterarAnimalComSucessoQuandoDadosValidos() {
        // Arrange
        EUsuario usuario = criarUsuario(EnPerfilUsuario.CUIDADOR);
        EAnimal animal = criarAnimal(usuario, "ANI-" + sufixo());

        AnimalDto atualizacao = new AnimalDto();
        atualizacao.setNome("Animal Atualizado IT");
        atualizacao.setPesoAtual(512.5);
        atualizacao.setCor("Preto");

        // Act
        AnimalDto resultado = animalService.alteraAnimal(null, animal.getCodigoBrinco(), atualizacao);

        // Assert
        assertThat(resultado.getNome()).isEqualTo("Animal Atualizado IT");
        assertThat(resultado.getPesoAtual()).isEqualTo(512.5);
        assertThat(resultado.getCor()).isEqualTo("Preto");
    }

    @Test
    void deveDeletarAnimalLogicamenteComSucessoQuandoExiste() {
        // Arrange
        EUsuario usuario = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        EAnimal animal = criarAnimal(usuario, "ANI-" + sufixo());
        String brinco = animal.getCodigoBrinco();

        // Act
        String resultado = animalService.deletaAnimal(null, brinco);

        // Assert
        assertThat(resultado).isEqualTo("animal deletado com sucesso");
        assertThat(animalRepository.findByCodigoBrincoAndStatus(brinco, EnStatus.A)).isEmpty();
        assertThat(animalRepository.findByCodigoBrincoAndStatus(brinco, EnStatus.I))
                .isPresent()
                .hasValueSatisfying(a -> assertThat(a.getStatus()).isEqualTo(EnStatus.I));
    }

    // ── Sad Path ──────────────────────────────────────────────────────────────

    @Test
    void deveLancarExcecaoAoCadastrarAnimalQuandoUsuarioNaoExiste() {
        // Arrange
        AnimalDto dto = novoAnimalDto("ANI-" + sufixo());
        String emailInexistente = "ausente-" + sufixo() + "@it.local";

        // Act & Assert
        assertThatThrownBy(() -> animalService.cadastraAnimal(emailInexistente, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    void deveLancarExcecaoAoBuscarAnimalComBrincoInexistente() {
        // Arrange
        String brincoInexistente = "ANI-" + sufixo();

        // Act & Assert
        assertThatThrownBy(() -> animalService.buscarPorBrinco(brincoInexistente))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("animal não encontrado");
    }

    @Test
    void deveLancarExcecaoAoAlterarAnimalComBrincoInexistente() {
        // Arrange
        String brincoInexistente = "ANI-" + sufixo();
        AnimalDto atualizacao = new AnimalDto();
        atualizacao.setNome("Nome Qualquer IT");

        // Act & Assert
        assertThatThrownBy(() -> animalService.alteraAnimal(null, brincoInexistente, atualizacao))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("animal não encontrado");
    }

    @Test
    void deveLancarExcecaoAoDeletarAnimalComBrincoInexistente() {
        // Arrange
        String brincoInexistente = "ANI-" + sufixo();

        // Act & Assert
        assertThatThrownBy(() -> animalService.deletaAnimal(null, brincoInexistente))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("animal não encontrado");
    }

    @Test
    void deveListarTodosOsAnimaisAtivos() {
        // Arrange
        EUsuario usuario = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        criarAnimal(usuario, "ANI-LIST-" + sufixo());
        criarAnimal(usuario, "ANI-LIST-" + sufixo());

        // Act
        var resultado = animalService.buscarTodosAnimais();

        // Assert
        assertThat(resultado).hasSizeGreaterThanOrEqualTo(2);
        assertThat(resultado).allMatch(a -> a.getCodigoBrinco() != null);
    }

    @Test
    void deveLancarExcecaoQuandoCuidadorTentaDeletarAnimalDeOutroUsuario() {
        // Arrange
        EUsuario dono = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        EUsuario cuidador = criarUsuario(EnPerfilUsuario.CUIDADOR);
        EAnimal animalDoDono = criarAnimal(dono, "ANI-" + sufixo());

        // Act & Assert
        assertThatThrownBy(() -> animalService.deletaAnimal(cuidador.getEmail(), animalDoDono.getCodigoBrinco()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("você cadastrou");
    }

    @Test
    void deveLancarExcecaoQuandoCuidadorChefeTentaDeletarAnimalDeOutroUsuario() {
        // Arrange
        EUsuario dono = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        EUsuario cuidadorChefe = criarUsuario(EnPerfilUsuario.CUIDADOR_CHEFE);
        EAnimal animalDoDono = criarAnimal(dono, "ANI-" + sufixo());

        // Act & Assert
        assertThatThrownBy(() -> animalService.deletaAnimal(cuidadorChefe.getEmail(), animalDoDono.getCodigoBrinco()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("você cadastrou");
    }

    @Test
    void deveLancarExcecaoQuandoCuidadorTentaAlterarAnimalDeOutroUsuario() {
        // Arrange
        EUsuario dono = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        EUsuario cuidador = criarUsuario(EnPerfilUsuario.CUIDADOR);
        EAnimal animalDoDono = criarAnimal(dono, "ANI-" + sufixo());

        AnimalDto atualizacao = new AnimalDto();
        atualizacao.setNome("Tentativa de Alteracao IT");

        // Act & Assert
        assertThatThrownBy(() -> animalService.alteraAnimal(cuidador.getEmail(), animalDoDono.getCodigoBrinco(), atualizacao))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("você cadastrou");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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

    private static String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }
}
