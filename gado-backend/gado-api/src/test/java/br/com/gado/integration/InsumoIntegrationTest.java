package br.com.gado.integration;

import br.com.gado.GadoApplication;
import br.com.gado.application.dto.InsumoDto;
import br.com.gado.application.dto.insumoDto.VacinaCadastroDto;
import br.com.gado.application.dto.insumoDto.VacinaPutDto;
import br.com.gado.application.services.SInsumo;
import br.com.gado.domain.entities.EInsumo;
import br.com.gado.domain.entities.EParceiro;
import br.com.gado.domain.enums.EnTipoInsumo;
import br.com.gado.domain.enums.EnTipoParceiro;
import br.com.gado.infrastructure.persistence.repositories.IInsumo;
import br.com.gado.infrastructure.persistence.repositories.IParceiro;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = GadoApplication.class)
@ActiveProfiles("test")
@Transactional
class InsumoIntegrationTest {

    @Autowired
    private SInsumo insumoService;

    @Autowired
    private IInsumo insumoRepository;

    @Autowired
    private IParceiro parceiroRepository;

    // ── Happy Path: Vacinas ───────────────────────────────────────────────────

    @Test
    void deveCriarVacinaComSucessoQuandoDadosValidos() {
        // Arrang
        VacinaCadastroDto dto = new VacinaCadastroDto();
        dto.setNome("Vacina IT " + sufixo());
        dto.setPendente(true);

        // Act
        InsumoDto resultado = insumoService.criarVacina(dto);

        // Assert
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getNome()).isEqualTo(dto.getNome());
        assertThat(resultado.getTipo()).isEqualTo(EnTipoInsumo.VACINA);
        assertThat(resultado.getPendente()).isTrue();
        assertThat(insumoRepository.findById(resultado.getId())).isPresent();
    }

    @Test
    void deveListarVacinasFiltrandoPorNomeComSucesso() {
        // Arrange
        String sufixo = sufixo();
        VacinaCadastroDto dto = new VacinaCadastroDto();
        dto.setNome("Vacina Busca IT " + sufixo);
        dto.setPendente(false);
        InsumoDto criada = insumoService.criarVacina(dto);

        // Act
        List<InsumoDto> resultado = insumoService.listarVacinas("Busca IT " + sufixo);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(criada.getId());
        assertThat(resultado.get(0).getNome()).isEqualTo(dto.getNome());
    }

    @Test
    void deveAtualizarVacinaComSucessoQuandoExisteEEhVacina() {
        // Arrange
        String nomeOriginal = "Vacina Original IT " + sufixo();
        VacinaCadastroDto cadDto = new VacinaCadastroDto();
        cadDto.setNome(nomeOriginal);
        cadDto.setPendente(true);
        InsumoDto criada = insumoService.criarVacina(cadDto);

        VacinaPutDto putDto = new VacinaPutDto();
        putDto.setNome(nomeOriginal + " Atualizada");
        putDto.setPendente(false);

        // Act
        InsumoDto resultado = insumoService.atualizarVacina(criada.getId(), putDto);

        // Assert
        assertThat(resultado.getNome()).isEqualTo(putDto.getNome());
        assertThat(resultado.getPendente()).isFalse();
        assertThat(resultado.getTipo()).isEqualTo(EnTipoInsumo.VACINA);
    }

    @Test
    void deveDeletarVacinaComSucessoQuandoExiste() {
        // Arrange
        VacinaCadastroDto dto = new VacinaCadastroDto();
        dto.setNome("Vacina Delete IT " + sufixo());
        dto.setPendente(false);
        InsumoDto criada = insumoService.criarVacina(dto);

        // Act
        String resultado = insumoService.deletarVacina(criada.getId());

        // Assert
        assertThat(resultado).isEqualTo("Vacina deletada com sucesso");
        assertThat(insumoRepository.findById(criada.getId())).isEmpty();
    }

    // ── Sad Path: Vacinas ─────────────────────────────────────────────────────

    @Test
    void deveLancarExcecaoAoCriarVacinaComNomeDuplicado() {
        // Arrange
        String nomeRepetido = "Vacina Duplicada IT " + sufixo();
        VacinaCadastroDto primeira = new VacinaCadastroDto();
        primeira.setNome(nomeRepetido);
        primeira.setPendente(false);
        insumoService.criarVacina(primeira);

        VacinaCadastroDto duplicada = new VacinaCadastroDto();
        duplicada.setNome(nomeRepetido);
        duplicada.setPendente(true);

        // Act & Assert
        assertThatThrownBy(() -> insumoService.criarVacina(duplicada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Vacina já cadastrada");
    }

    @Test
    void deveLancarExcecaoAoAtualizarInsumoQueNaoEhVacina() {
        // Arrange
        EInsumo racao = new EInsumo();
        racao.setNome("Racao IT " + sufixo());
        racao.setTipo(EnTipoInsumo.RACAO);
        racao.setPendente(false);
        EInsumo salva = insumoRepository.save(racao);

        VacinaPutDto putDto = new VacinaPutDto();
        putDto.setNome("Tentativa de Nome IT");

        // Act & Assert
        assertThatThrownBy(() -> insumoService.atualizarVacina(salva.getId(), putDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não é uma vacina");
    }

    // ── Happy Path: Insumos ───────────────────────────────────────────────────

    @Test
    void deveCadastrarInsumoComSucessoQuandoFornecedorExiste() {
        // Arrange
        EParceiro fornecedor = criarParceiro();
        InsumoDto dto = novoInsumoDto("Racao IT " + sufixo(), fornecedor.getId(), EnTipoInsumo.RACAO, 120.0);

        // Act
        InsumoDto resultado = insumoService.cadastraInsumo(dto);

        // Assert
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getNome()).isEqualTo(dto.getNome());
        assertThat(resultado.getTipo()).isEqualTo(EnTipoInsumo.RACAO);
        assertThat(resultado.getSaldoAtual()).isEqualTo(120.0);
        assertThat(insumoRepository.findById(resultado.getId())).isPresent();
    }

    @Test
    void deveBuscarInsumoPorIdComSucessoQuandoExiste() {
        // Arrange
        EParceiro fornecedor = criarParceiro();
        InsumoDto criado = insumoService.cadastraInsumo(
                novoInsumoDto("Insumo Busca IT " + sufixo(), fornecedor.getId(), EnTipoInsumo.OUTROS, 50.0));

        // Act
        InsumoDto resultado = insumoService.buscaPorId(criado.getId());

        // Assert
        assertThat(resultado.getId()).isEqualTo(criado.getId());
        assertThat(resultado.getNome()).isEqualTo(criado.getNome());
        assertThat(resultado.getSaldoAtual()).isEqualTo(50.0);
    }

    @Test
    void deveAlterarInsumoComSucessoQuandoDadosValidos() {
        // Arrange
        EParceiro fornecedor = criarParceiro();
        String nomeOriginal = "Insumo Original IT " + sufixo();
        InsumoDto criado = insumoService.cadastraInsumo(
                novoInsumoDto(nomeOriginal, fornecedor.getId(), EnTipoInsumo.RACAO, 100.0));

        InsumoDto atualizacao = novoInsumoDto(nomeOriginal + " Premium", fornecedor.getId(), EnTipoInsumo.RACAO, 180.0);

        // Act
        InsumoDto resultado = insumoService.alteraInsumo(criado.getId(), atualizacao);

        // Assert
        assertThat(resultado.getNome()).isEqualTo(nomeOriginal + " Premium");
        assertThat(resultado.getSaldoAtual()).isEqualTo(180.0);
    }

    @Test
    void deveDeletarInsumoComSucessoQuandoExiste() {
        // Arrange
        EParceiro fornecedor = criarParceiro();
        InsumoDto criado = insumoService.cadastraInsumo(
                novoInsumoDto("Insumo Delete IT " + sufixo(), fornecedor.getId(), EnTipoInsumo.OUTROS, 30.0));

        // Act
        String resultado = insumoService.deletaInsumo(criado.getId());

        // Assert
        assertThat(resultado).isEqualTo("Insumo deletado com sucesso");
        assertThat(insumoRepository.findById(criado.getId())).isEmpty();
    }

    // ── Sad Path: Insumos ─────────────────────────────────────────────────────

    @Test
    void deveLancarExcecaoAoBuscarInsumoComIdInexistente() {
        assertThatThrownBy(() -> insumoService.buscaPorId(Long.MAX_VALUE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Insumo não encontrado");
    }

    @Test
    void deveLancarExcecaoAoCadastrarInsumoSemFornecedor() {
        // Arrange
        InsumoDto dto = novoInsumoDto("Insumo Sem Fornecedor IT " + sufixo(), null, EnTipoInsumo.OUTROS, 10.0);

        // Act & Assert
        assertThatThrownBy(() -> insumoService.cadastraInsumo(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("parceiro_id");
    }

    @Test
    void deveLancarExcecaoAoCadastrarInsumoComFornecedorInexistente() {
        // Arrange
        InsumoDto dto = novoInsumoDto("Insumo Fornecedor Invalido IT " + sufixo(), Long.MAX_VALUE, EnTipoInsumo.OUTROS, 10.0);

        // Act & Assert
        assertThatThrownBy(() -> insumoService.cadastraInsumo(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("fornecedor");
    }

    @Test
    void deveLancarExcecaoAoCriarVacinaComNomeVazio() {
        // Arrange
        VacinaCadastroDto dto = new VacinaCadastroDto();
        dto.setNome("   ");
        dto.setPendente(false);

        // Act & Assert
        assertThatThrownBy(() -> insumoService.criarVacina(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome da vacina");
    }

    @Test
    void deveLancarExcecaoAoAtualizarVacinaComIdInexistente() {
        // Arrange
        VacinaPutDto putDto = new VacinaPutDto();
        putDto.setNome("Nome Qualquer IT");

        // Act & Assert
        assertThatThrownBy(() -> insumoService.atualizarVacina(Long.MAX_VALUE, putDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Vacina não encontrada");
    }

    @Test
    void deveLancarExcecaoAoDeletarVacinaComIdInexistente() {
        assertThatThrownBy(() -> insumoService.deletarVacina(Long.MAX_VALUE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Vacina não encontrada");
    }

    @Test
    void deveRetornarMensagemQuandoDeletarInsumoComIdInexistente() {
        // Act
        String resultado = insumoService.deletaInsumo(Long.MAX_VALUE);

        // Assert
        assertThat(resultado).isEqualTo("Nenhum insumo com esse id foi encontrado");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private InsumoDto novoInsumoDto(String nome, Long parceiroId, EnTipoInsumo tipo, double saldoAtual) {
        InsumoDto dto = new InsumoDto();
        dto.setNome(nome);
        dto.setEstoqueMinimo(10.0);
        dto.setSaldoAtual(saldoAtual);
        dto.setParceiro_id(parceiroId);
        dto.setTipo(tipo);
        dto.setPendente(false);
        return dto;
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

    private static String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }
}
