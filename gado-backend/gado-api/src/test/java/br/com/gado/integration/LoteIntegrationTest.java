package br.com.gado.integration;

import br.com.gado.GadoApplication;
import br.com.gado.application.dto.loteDto.LoteCadastroDto;
import br.com.gado.application.dto.loteDto.LotePutDto;
import br.com.gado.application.dto.loteDto.LoteRespostaDto;
import br.com.gado.application.dto.loteDto.LoteSetorCadastroDto;
import br.com.gado.application.dto.loteDto.TransferenciaAnimalDto;
import br.com.gado.application.services.SLote;
import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.entities.ELote;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = GadoApplication.class)
@ActiveProfiles("test")
@Transactional
class LoteIntegrationTest {

    @Autowired
    private SLote loteService;

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

    // ── Happy Path ────────────────────────────────────────────────────────────

    @Test
    void deveCadastrarLoteComSucessoQuandoAdminEDadosValidos() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        ESetor setor = criarSetor(admin, 5);
        EAnimal animal1 = criarAnimal(admin, "LOT-ANI-" + sufixo());
        EAnimal animal2 = criarAnimal(admin, "LOT-ANI-" + sufixo());
        String descricao = "Lote Cadastro IT " + sufixo();

        LoteCadastroDto dto = novoLoteDto(descricao, "Azul", setor.getId(),
                List.of(animal1.getId(), animal2.getId()));

        // Act
        String resultado = loteService.cadastra(admin.getEmail(), dto);

        // Assert
        assertThat(resultado).contains("cadastrado com sucesso");
        ELote lote = buscarLotePorDescricao(descricao);
        assertThat(lote.getCodigo()).matches("LOT\\d{3}");
        assertThat(lote.getCriadoPor().getEmail()).isEqualTo(admin.getEmail());
        assertThat(loteSetorRepository.findByLote_Id(lote.getId())).hasSize(1);
        assertThat(loteSetorRepository.findByLote_Id(lote.getId()).get(0).getAnimais()).hasSize(2);
    }

    @Test
    void deveBuscarLotePorIdComSucessoQuandoExiste() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        ESetor setor = criarSetor(admin, 3);
        String descricao = "Lote Busca IT " + sufixo();
        loteService.cadastra(admin.getEmail(), novoLoteDto(descricao, "Verde", setor.getId(), List.of()));
        ELote lote = buscarLotePorDescricao(descricao);

        // Act
        LoteRespostaDto resultado = loteService.buscaPorid(lote.getId());

        // Assert
        assertThat(resultado.getId()).isEqualTo(lote.getId());
        assertThat(resultado.getCodigo()).isEqualTo(lote.getCodigo());
        assertThat(resultado.getDescricao()).isEqualTo(descricao);
        assertThat(resultado.getCriadoPorEmail()).isEqualTo(admin.getEmail());
    }

    @Test
    void deveAlterarLoteComSucessoQuandoCuidadorChefeEDadosValidos() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        EUsuario cuidadorChefe = criarUsuario(EnPerfilUsuario.CUIDADOR_CHEFE);
        ESetor setor = criarSetor(admin, 5);
        String descricao = "Lote CC Alterar IT " + sufixo();
        loteService.cadastra(admin.getEmail(), novoLoteDto(descricao, "Azul", setor.getId(), List.of()));
        ELote lote = buscarLotePorDescricao(descricao);

        LotePutDto putDto = new LotePutDto();
        putDto.setDescricao("Lote CC Alterado IT");
        putDto.setCorBrinco("Roxo");

        // Act
        String resultado = loteService.altera(lote.getId(), cuidadorChefe.getEmail(), putDto);

        // Assert
        assertThat(resultado).contains("atualizado com sucesso");
        LoteRespostaDto atualizado = loteService.buscaPorid(lote.getId());
        assertThat(atualizado.getDescricao()).isEqualTo("Lote CC Alterado IT");
        assertThat(atualizado.getAlteradoPorEmail()).isEqualTo(cuidadorChefe.getEmail());
    }

    @Test
    void deveAlterarLoteComSucessoQuandoGerenteEDadosValidos() {
        // Arrange
        EUsuario gerente = criarUsuario(EnPerfilUsuario.GERENTE);
        ESetor setorOriginal = criarSetor(gerente, 4);
        ESetor setorNovo = criarSetor(gerente, 4);
        EAnimal animal = criarAnimal(gerente, "LOT-ANI-" + sufixo());
        String descricao = "Lote Alterar IT " + sufixo();
        loteService.cadastra(gerente.getEmail(), novoLoteDto(descricao, "Azul", setorOriginal.getId(), List.of()));
        ELote lote = buscarLotePorDescricao(descricao);

        String descricaoAtualizada = "Lote Alterado IT " + sufixo();
        LotePutDto putDto = new LotePutDto();
        putDto.setDescricao(descricaoAtualizada);
        putDto.setCorBrinco("Verde");
        LoteSetorCadastroDto novaAlocacao = new LoteSetorCadastroDto();
        novaAlocacao.setSetorId(setorNovo.getId());
        novaAlocacao.setAnimaisIds(List.of(animal.getId()));
        putDto.setAlocacoes(List.of(novaAlocacao));

        // Act
        String resultado = loteService.altera(lote.getId(), gerente.getEmail(), putDto);

        // Assert
        assertThat(resultado).contains("atualizado com sucesso");
        LoteRespostaDto atualizado = loteService.buscaPorid(lote.getId());
        assertThat(atualizado.getDescricao()).isEqualTo(descricaoAtualizada);
        assertThat(atualizado.getCorBrinco()).isEqualTo("Verde");
        assertThat(atualizado.getAlteradoPorEmail()).isEqualTo(gerente.getEmail());
        assertThat(atualizado.getTotalAnimais()).isEqualTo(1);
        assertThat(atualizado.getAlocacoes().get(0).getSetorId()).isEqualTo(setorNovo.getId());
    }

    @Test
    void deveExcluirLoteDefinitivamenteQuandoSemVinculos() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        ESetor setor = criarSetor(admin, 3);
        String descricao = "Lote Delete IT " + sufixo();
        loteService.cadastra(admin.getEmail(), novoLoteDto(descricao, "Azul", setor.getId(), List.of()));
        ELote lote = buscarLotePorDescricao(descricao);

        // Act
        String resultado = loteService.deleta(lote.getId(), admin.getEmail());

        // Assert
        assertThat(resultado).contains(lote.getCodigo());
        assertThat(loteRepository.findById(lote.getId())).isEmpty();
        assertThat(loteSetorRepository.findByLote_Id(lote.getId())).isEmpty();
    }

    @Test
    void deveInativarLoteQuandoPossuiMetaVinculada() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        ESetor setor = criarSetor(admin, 3);
        String descricao = "Lote Soft Delete IT " + sufixo();
        loteService.cadastra(admin.getEmail(), novoLoteDto(descricao, "Azul", setor.getId(), List.of()));
        ELote lote = buscarLotePorDescricao(descricao);
        criarMetaSetor(setor);

        // Act
        String resultado = loteService.deleta(lote.getId(), admin.getEmail());

        // Assert
        assertThat(resultado).contains("inativado");
        ELote atualizado = loteRepository.findById(lote.getId()).orElseThrow();
        assertThat(atualizado.getStatus()).isEqualTo(EnStatus.I);
        assertThat(loteSetorRepository.findByLote_Id(lote.getId())).isNotEmpty();
    }

    @Test
    void devePermitirAlocarAnimalEmNovoLoteAposLoteAnteriorSerInativado() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        ESetor setor = criarSetor(admin, 50);
        EAnimal animal = criarAnimal(admin, "LOT-ANI-INATIVO-" + sufixo());

        String descricaoInativo = "Lote Inativo IT " + sufixo();
        loteService.cadastra(admin.getEmail(),
                novoLoteDto(descricaoInativo, "Azul", setor.getId(), List.of(animal.getId())));
        ELote loteInativo = buscarLotePorDescricao(descricaoInativo);

        criarMetaSetor(setor);
        loteService.deleta(loteInativo.getId(), admin.getEmail());
        assertThat(loteRepository.findById(loteInativo.getId()).orElseThrow().getStatus())
                .isEqualTo(EnStatus.I);

        // Act: mesmo animal deve poder ser alocado em novo lote ativo
        String descricaoNovo = "Lote Pos Inativo IT " + sufixo();
        String resultado = loteService.cadastra(admin.getEmail(),
                novoLoteDto(descricaoNovo, "Laranja", setor.getId(), List.of(animal.getId())));

        // Assert
        assertThat(resultado).contains("cadastrado com sucesso");
        assertThat(buscarLotePorDescricao(descricaoNovo).getStatus()).isEqualTo(EnStatus.A);
    }

    // ── Sad Path ──────────────────────────────────────────────────────────────

    @Test
    void deveLancarExcecaoAoCadastrarLoteSemEmailDeUsuario() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        ESetor setor = criarSetor(admin, 2);
        LoteCadastroDto dto = novoLoteDto("Lote Sem Email IT " + sufixo(), "Amarelo", setor.getId(), List.of());

        // Act & Assert
        assertThatThrownBy(() -> loteService.cadastra(null, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("e-mail");
    }

    @Test
    void deveLancarExcecaoAoCadastrarLoteComUsuarioSemPermissao() {
        // Arrange
        EUsuario cuidador = criarUsuario(EnPerfilUsuario.CUIDADOR);
        ESetor setor = criarSetor(cuidador, 2);
        LoteCadastroDto dto = novoLoteDto("Lote Sem Permissao IT " + sufixo(), "Amarelo", setor.getId(), List.of());

        // Act & Assert
        assertThatThrownBy(() -> loteService.cadastra(cuidador.getEmail(), dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Administradores e Gerentes");
    }

    @Test
    void deveLancarExcecaoAoCadastrarLoteComCapacidadeDeSetorExcedida() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        ESetor setorLimitado = criarSetor(admin, 1);
        EAnimal animal1 = criarAnimal(admin, "LOT-ANI-CAP1-" + sufixo());
        EAnimal animal2 = criarAnimal(admin, "LOT-ANI-CAP2-" + sufixo());

        loteService.cadastra(admin.getEmail(),
                novoLoteDto("Lote Capacidade Base IT " + sufixo(), "Azul", setorLimitado.getId(),
                        List.of(animal1.getId())));

        LoteCadastroDto dtoExcedente = novoLoteDto("Lote Capacidade Excedida IT " + sufixo(),
                "Vermelho", setorLimitado.getId(), List.of(animal2.getId()));

        // Act & Assert
        assertThatThrownBy(() -> loteService.cadastra(admin.getEmail(), dtoExcedente))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("capacidade máxima");
    }

    @Test
    void deveLancarExcecaoAoCadastrarLoteComAnimalJaAlocadoEmOutroLoteAtivo() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        ESetor setor = criarSetor(admin, 50);
        EAnimal animal = criarAnimal(admin, "LOT-ANI-CONF-" + sufixo());

        loteService.cadastra(admin.getEmail(),
                novoLoteDto("Lote Conflito Base IT " + sufixo(), "Azul", setor.getId(), List.of(animal.getId())));

        LoteCadastroDto dtoConflito = novoLoteDto("Lote Conflito Dup IT " + sufixo(),
                "Vermelho", setor.getId(), List.of(animal.getId()));

        // Act & Assert
        assertThatThrownBy(() -> loteService.cadastra(admin.getEmail(), dtoConflito))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já está alocado ao lote");
    }

    @Test
    void deveLancarExcecaoAoBuscarLoteComIdInexistente() {
        assertThatThrownBy(() -> loteService.buscaPorid(Long.MAX_VALUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nenhum lote ativo encontrado");
    }

    @Test
    void deveLancarExcecaoAoExcluirLoteComUsuarioSemPermissao() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        EUsuario financeiro = criarUsuario(EnPerfilUsuario.FINANCEIRO);
        ESetor setor = criarSetor(admin, 2);
        String descricao = "Lote Excluir Sem Permissao IT " + sufixo();
        loteService.cadastra(admin.getEmail(), novoLoteDto(descricao, "Azul", setor.getId(), List.of()));
        ELote lote = buscarLotePorDescricao(descricao);

        // Act & Assert
        assertThatThrownBy(() -> loteService.deleta(lote.getId(), financeiro.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Administradores e Gerentes");
    }

    @Test
    void deveLancarExcecaoAoAlterarLoteComIdInexistente() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        LotePutDto putDto = new LotePutDto();
        putDto.setDescricao("Descricao IT");

        // Act & Assert
        assertThatThrownBy(() -> loteService.altera(Long.MAX_VALUE, admin.getEmail(), putDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nenhum lote ativo encontrado");
    }

    @Test
    void deveLancarExcecaoAoAlterarLoteComUsuarioSemPermissao() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        EUsuario cuidador = criarUsuario(EnPerfilUsuario.CUIDADOR);
        ESetor setor = criarSetor(admin, 2);
        String descricao = "Lote Perm Edicao IT " + sufixo();
        loteService.cadastra(admin.getEmail(), novoLoteDto(descricao, "Azul", setor.getId(), List.of()));
        ELote lote = buscarLotePorDescricao(descricao);

        LotePutDto putDto = new LotePutDto();
        putDto.setDescricao("Alteracao Negada IT");

        // Act & Assert
        assertThatThrownBy(() -> loteService.altera(lote.getId(), cuidador.getEmail(), putDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Administradores, Gerentes e Cuidadores Chefe");
    }

    @Test
    void deveLancarExcecaoAoCadastrarLoteComAnimalJaAlocadoEmSetorDiferente() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        ESetor setorA = criarSetor(admin, 50);
        ESetor setorB = criarSetor(admin, 50);
        EAnimal animal = criarAnimal(admin, "LOT-ANI-SETORDIF-" + sufixo());

        loteService.cadastra(admin.getEmail(),
                novoLoteDto("Lote Setor A IT " + sufixo(), "Azul", setorA.getId(), List.of(animal.getId())));

        LoteCadastroDto dtoSetorB = novoLoteDto("Lote Setor B IT " + sufixo(),
                "Verde", setorB.getId(), List.of(animal.getId()));

        // Act & Assert
        assertThatThrownBy(() -> loteService.cadastra(admin.getEmail(), dtoSetorB))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já está alocado ao lote");
    }

    @Test
    void deveLancarExcecaoAoCadastrarLoteComAnimalComStatusBloqueado() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        ESetor setor = criarSetor(admin, 50);
        EAnimal animalVendido = criarAnimal(admin, "LOT-ANI-VEND-" + sufixo());
        animalVendido.setStatusAnimal(EnStatusAnimal.VENDIDO);
        animalRepository.save(animalVendido);

        LoteCadastroDto dto = novoLoteDto("Lote Animal Bloqueado IT " + sufixo(),
                "Amarelo", setor.getId(), List.of(animalVendido.getId()));

        // Act & Assert
        assertThatThrownBy(() -> loteService.cadastra(admin.getEmail(), dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser movimentado");
    }

    // ── Happy Path: Transferência de Animal ───────────────────────────────────

    @Test
    void deveTransferirAnimalComSucessoQuandoSetorDestinoTemCapacidade() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        ESetor setorOrigem = criarSetor(admin, 50);
        ESetor setorDestino = criarSetor(admin, 50);
        EAnimal animal = criarAnimal(admin, "LOT-ANI-TRANSF-" + sufixo());

        String descOrigem = "Lote Origem IT " + sufixo();
        loteService.cadastra(admin.getEmail(),
                novoLoteDto(descOrigem, "Azul", setorOrigem.getId(), List.of(animal.getId())));
        ELote loteOrigem = buscarLotePorDescricao(descOrigem);

        String descDestino = "Lote Destino IT " + sufixo();
        loteService.cadastra(admin.getEmail(),
                novoLoteDto(descDestino, "Verde", setorDestino.getId(), List.of()));
        ELote loteDestino = buscarLotePorDescricao(descDestino);

        TransferenciaAnimalDto dto = new TransferenciaAnimalDto();
        dto.setAnimalId(animal.getId());
        dto.setLoteDestinoId(loteDestino.getId());
        dto.setSetorDestinoId(setorDestino.getId());

        // Act
        String resultado = loteService.transferirAnimal(admin.getEmail(), dto);

        // Assert
        assertThat(resultado).contains(animal.getCodigoBrinco());
        assertThat(resultado).contains(loteDestino.getCodigo());
        assertThat(loteSetorRepository.findByLote_Id(loteOrigem.getId()).get(0).getAnimais())
                .noneMatch(a -> a.getId().equals(animal.getId()));
        assertThat(loteSetorRepository.findByLote_Id(loteDestino.getId()).get(0).getAnimais())
                .anyMatch(a -> a.getId().equals(animal.getId()));
    }

    // ── Sad Path: Transferência de Animal ─────────────────────────────────────

    @Test
    void deveLancarExcecaoAoTransferirAnimalParaOMesmoLoteESetor() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        ESetor setor = criarSetor(admin, 50);
        EAnimal animal = criarAnimal(admin, "LOT-ANI-SAME-" + sufixo());

        String descricao = "Lote Mesmo IT " + sufixo();
        loteService.cadastra(admin.getEmail(),
                novoLoteDto(descricao, "Azul", setor.getId(), List.of(animal.getId())));
        ELote lote = buscarLotePorDescricao(descricao);

        TransferenciaAnimalDto dto = new TransferenciaAnimalDto();
        dto.setAnimalId(animal.getId());
        dto.setLoteDestinoId(lote.getId());
        dto.setSetorDestinoId(setor.getId());

        // Act & Assert
        assertThatThrownBy(() -> loteService.transferirAnimal(admin.getEmail(), dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já está alocado no lote e setor informados");
    }

    @Test
    void deveLancarExcecaoAoTransferirAnimalComStatusBloqueado() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        ESetor setorOrigem = criarSetor(admin, 50);
        ESetor setorDestino = criarSetor(admin, 50);
        EAnimal animal = criarAnimal(admin, "LOT-ANI-OBITO-" + sufixo());

        String descOrigem = "Lote Origem Bloq IT " + sufixo();
        loteService.cadastra(admin.getEmail(),
                novoLoteDto(descOrigem, "Azul", setorOrigem.getId(), List.of(animal.getId())));
        ELote loteOrigem = buscarLotePorDescricao(descOrigem);

        String descDestino = "Lote Destino Bloq IT " + sufixo();
        loteService.cadastra(admin.getEmail(),
                novoLoteDto(descDestino, "Verde", setorDestino.getId(), List.of()));
        ELote loteDestino = buscarLotePorDescricao(descDestino);

        animal.setStatusAnimal(EnStatusAnimal.OBITO);
        animalRepository.save(animal);

        TransferenciaAnimalDto dto = new TransferenciaAnimalDto();
        dto.setAnimalId(animal.getId());
        dto.setLoteDestinoId(loteDestino.getId());
        dto.setSetorDestinoId(setorDestino.getId());

        // Act & Assert
        assertThatThrownBy(() -> loteService.transferirAnimal(admin.getEmail(), dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser movimentado");
    }

    @Test
    void deveLancarExcecaoAoTransferirAnimalParaSetorSemCapacidade() {
        // Arrange
        EUsuario admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
        ESetor setorOrigem = criarSetor(admin, 50);
        ESetor setorDestino = criarSetor(admin, 1);
        EAnimal animalTransf = criarAnimal(admin, "LOT-ANI-TRANSF2-" + sufixo());
        EAnimal animalOcupante = criarAnimal(admin, "LOT-ANI-OCUP-" + sufixo());

        String descOrigem = "Lote Origem Cap IT " + sufixo();
        loteService.cadastra(admin.getEmail(),
                novoLoteDto(descOrigem, "Azul", setorOrigem.getId(), List.of(animalTransf.getId())));
        ELote loteOrigem = buscarLotePorDescricao(descOrigem);

        String descDestino = "Lote Destino Cap IT " + sufixo();
        loteService.cadastra(admin.getEmail(),
                novoLoteDto(descDestino, "Verde", setorDestino.getId(), List.of(animalOcupante.getId())));
        ELote loteDestino = buscarLotePorDescricao(descDestino);

        TransferenciaAnimalDto dto = new TransferenciaAnimalDto();
        dto.setAnimalId(animalTransf.getId());
        dto.setLoteDestinoId(loteDestino.getId());
        dto.setSetorDestinoId(setorDestino.getId());

        // Act & Assert
        assertThatThrownBy(() -> loteService.transferirAnimal(admin.getEmail(), dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("excede a capacidade máxima");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private LoteCadastroDto novoLoteDto(String descricao, String corBrinco, Long setorId, List<Long> animaisIds) {
        LoteSetorCadastroDto alocacao = new LoteSetorCadastroDto();
        alocacao.setSetorId(setorId);
        alocacao.setAnimaisIds(animaisIds);

        LoteCadastroDto dto = new LoteCadastroDto();
        dto.setCorBrinco(corBrinco);
        dto.setDescricao(descricao);
        dto.setRacaPredominante("Nelore");
        dto.setDataCriacao(LocalDate.now());
        dto.setAlocacoes(List.of(alocacao));
        return dto;
    }

    private ELote buscarLotePorDescricao(String descricao) {
        return loteRepository.findAllByStatus(EnStatus.A)
                .stream()
                .filter(l -> descricao.equals(l.getDescricao()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Lote não encontrado para descrição: " + descricao));
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

    private void criarMetaSetor(ESetor setor) {
        EMetaSetor meta = new EMetaSetor();
        meta.setSetor(setor);
        meta.setDataInicial(LocalDate.now());
        meta.setDataFinal(LocalDate.now().plusMonths(1));
        meta.setTipoMeta(EnTipoMeta.LEITE);
        meta.setQuantidadeEsperada(1000.0);
        meta.setPrecoMedio(2.5);
        metaSetorRepository.save(meta);
    }

    private static String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }
}
