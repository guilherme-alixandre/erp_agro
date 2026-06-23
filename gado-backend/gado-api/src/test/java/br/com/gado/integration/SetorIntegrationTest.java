package br.com.gado.integration;

import br.com.gado.GadoApplication;
import br.com.gado.application.dto.SetorDto;
import br.com.gado.application.services.SSetor;
import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.entities.ELote;
import br.com.gado.domain.entities.ELoteSetor;
import br.com.gado.domain.entities.ESetor;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnPerfilUsuario;
import br.com.gado.domain.enums.EnSexoAnimal;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.domain.enums.EnStatusAnimal;
import br.com.gado.domain.enums.EnTipoSetor;
import br.com.gado.infrastructure.persistence.repositories.IAnimal;
import br.com.gado.infrastructure.persistence.repositories.ILote;
import br.com.gado.infrastructure.persistence.repositories.ILoteSetor;
import br.com.gado.infrastructure.persistence.repositories.ISetor;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = GadoApplication.class)
@ActiveProfiles("test")
@Transactional
class SetorIntegrationTest {

    @Autowired private SSetor setorService;
    @Autowired private ISetor setorRepository;
    @Autowired private IUsuario usuarioRepository;
    @Autowired private ILote loteRepository;
    @Autowired private ILoteSetor loteSetorRepository;
    @Autowired private IAnimal animalRepository;

    private EUsuario admin;

    @BeforeEach
    void setUp() {
        admin = criarUsuario(EnPerfilUsuario.ADMINISTRADOR);
    }

    // ── Happy Path ────────────────────────────────────────────────────────────

    @Nested
    class CadastroHappyPath {

        @Test
        void deveCadastrarSetorComSucesso_QuandoAdminEDadosValidos() {
            SetorDto dto = novoSetorDto("Pasto Norte " + sufixo(), 30);

            SetorDto resultado = setorService.cadastra(dto, admin.getEmail());

            assertThat(resultado.getId()).isNotNull();
            assertThat(resultado.getNome()).isEqualTo(dto.getNome());
            assertThat(resultado.getCapacidadeMaxima()).isEqualTo(30);
            assertThat(resultado.getCriadoPorEmail()).isEqualTo(admin.getEmail());
        }

        @Test
        void deveCadastrarSetor_QuandoGerenteExecuta() {
            EUsuario gerente = criarUsuario(EnPerfilUsuario.GERENTE);
            SetorDto dto = novoSetorDto("Pasto Gerente " + sufixo(), 10);

            SetorDto resultado = setorService.cadastra(dto, gerente.getEmail());

            assertThat(resultado.getId()).isNotNull();
            assertThat(resultado.getCriadoPorEmail()).isEqualTo(gerente.getEmail());
        }

        @Test
        void deveCadastrarSetor_QuandoCuidadorChefeExecuta() {
            EUsuario cuidadorChefe = criarUsuario(EnPerfilUsuario.CUIDADOR_CHEFE);
            SetorDto dto = novoSetorDto("Pasto CC " + sufixo(), 5);

            SetorDto resultado = setorService.cadastra(dto, cuidadorChefe.getEmail());

            assertThat(resultado.getId()).isNotNull();
        }
    }

    @Nested
    class BuscaHappyPath {

        @Test
        void deveBuscarSetorPorId_QuandoExiste() {
            SetorDto criado = setorService.cadastra(novoSetorDto("Pasto Busca " + sufixo(), 20), admin.getEmail());

            SetorDto resultado = setorService.procuraPorId(criado.getId());

            assertThat(resultado.getId()).isEqualTo(criado.getId());
            assertThat(resultado.getNome()).isEqualTo(criado.getNome());
        }

        @Test
        void deveBuscarTodosSetores_RetornandoApenasAtivos() {
            String nome1 = "Pasto Lista 1 " + sufixo();
            String nome2 = "Pasto Lista 2 " + sufixo();
            setorService.cadastra(novoSetorDto(nome1, 10), admin.getEmail());
            setorService.cadastra(novoSetorDto(nome2, 20), admin.getEmail());

            ArrayList<SetorDto> resultado = setorService.buscarTodos();

            assertThat(resultado).extracting(SetorDto::getNome)
                    .contains(nome1, nome2);
        }

        @Test
        void deveBuscarSetorComLotesVinculados() {
            ESetor setor = criarSetorDireto("Pasto Lote Vinc " + sufixo(), 50);
            ELote lote = criarLoteDireto(setor);

            SetorDto resultado = setorService.procuraPorId(setor.getId());

            assertThat(resultado.getLotes()).hasSize(1);
            assertThat(resultado.getLotes().get(0).getLoteCodigo()).isEqualTo(lote.getCodigo());
        }

        @Test
        void deveBuscarSetorComCriadoPorNulo_QuandoSetorNaoTemCriador() {
            ESetor setor = new ESetor();
            setor.setNome("Pasto Sem Criador " + sufixo());
            setor.setCapacidadeMaxima(10);
            ESetor salvo = setorRepository.save(setor);

            SetorDto resultado = setorService.procuraPorId(salvo.getId());

            assertThat(resultado.getCriadoPorNome()).isNull();
            assertThat(resultado.getCriadoPorEmail()).isNull();
        }

        @Test
        void deveBuscarSetorComAlteradoPor_QuandoSetorFoiAlterado() {
            SetorDto criado = setorService.cadastra(novoSetorDto("Pasto Alterado " + sufixo(), 10), admin.getEmail());
            SetorDto putDto = novoSetorDto("Pasto Alterado v2 " + sufixo(), 15);
            setorService.altera(criado.getId(), putDto, admin.getEmail());

            SetorDto resultado = setorService.procuraPorId(criado.getId());

            assertThat(resultado.getAlteradoPorEmail()).isEqualTo(admin.getEmail());
        }

        @Test
        void deveBuscarSetorComLotesVazios_QuandoNaoHaAlocacoes() {
            SetorDto criado = setorService.cadastra(novoSetorDto("Pasto Sem Lotes " + sufixo(), 10), admin.getEmail());

            SetorDto resultado = setorService.procuraPorId(criado.getId());

            assertThat(resultado.getLotes()).isEmpty();
        }
    }

    @Nested
    class AlteracaoHappyPath {

        @Test
        void deveAlterarNomeECapacidade_QuandoDadosValidos() {
            SetorDto criado = setorService.cadastra(novoSetorDto("Pasto Original " + sufixo(), 10), admin.getEmail());
            SetorDto putDto = novoSetorDto("Pasto Atualizado " + sufixo(), 25);

            SetorDto resultado = setorService.altera(criado.getId(), putDto, admin.getEmail());

            assertThat(resultado.getNome()).isEqualTo(putDto.getNome());
            assertThat(resultado.getCapacidadeMaxima()).isEqualTo(25);
        }

        @Test
        void deveAlterarTipo_QuandoInformado() {
            SetorDto criado = setorService.cadastra(novoSetorDto("Pasto Tipo " + sufixo(), 10), admin.getEmail());
            SetorDto putDto = novoSetorDto("Pasto Tipo " + sufixo(), 10);
            putDto.setTipo(EnTipoSetor.CONFINAMENTO);

            setorService.altera(criado.getId(), putDto, admin.getEmail());

            ESetor setorAtualizado = setorRepository.findByIdAndStatus(criado.getId(), EnStatus.A).orElseThrow();
            assertThat(setorAtualizado.getTipo()).isEqualTo(EnTipoSetor.CONFINAMENTO);
        }

        @Test
        void deveManterNomeOriginal_QuandoNomeForNuloOuBlank() {
            String nomeOriginal = "Pasto Manter Nome " + sufixo();
            SetorDto criado = setorService.cadastra(novoSetorDto(nomeOriginal, 10), admin.getEmail());

            SetorDto putDtoNulo = new SetorDto();
            putDtoNulo.setNome(null);
            setorService.altera(criado.getId(), putDtoNulo, admin.getEmail());

            ESetor atualizadoComNulo = setorRepository.findByIdAndStatus(criado.getId(), EnStatus.A).orElseThrow();
            assertThat(atualizadoComNulo.getNome()).isEqualTo(nomeOriginal);

            SetorDto putDtoBlank = new SetorDto();
            putDtoBlank.setNome("   ");
            setorService.altera(criado.getId(), putDtoBlank, admin.getEmail());

            ESetor atualizadoComBlank = setorRepository.findByIdAndStatus(criado.getId(), EnStatus.A).orElseThrow();
            assertThat(atualizadoComBlank.getNome()).isEqualTo(nomeOriginal);
        }

        @Test
        void deveManterCapacidadeOriginal_QuandoCapacidadeForZero() {
            SetorDto criado = setorService.cadastra(novoSetorDto("Pasto Manter Cap " + sufixo(), 15), admin.getEmail());
            SetorDto putDto = new SetorDto();
            putDto.setCapacidadeMaxima(0);

            setorService.altera(criado.getId(), putDto, admin.getEmail());

            ESetor atualizado = setorRepository.findByIdAndStatus(criado.getId(), EnStatus.A).orElseThrow();
            assertThat(atualizado.getCapacidadeMaxima()).isEqualTo(15);
        }

        @Test
        void deveNaoAlterarTipo_QuandoTipoForNulo() {
            SetorDto criado = setorService.cadastra(novoSetorDto("Pasto Tipo Nulo " + sufixo(), 10), admin.getEmail());
            ESetor antes = setorRepository.findByIdAndStatus(criado.getId(), EnStatus.A).orElseThrow();
            EnTipoSetor tipoOriginal = antes.getTipo();

            SetorDto putDto = new SetorDto();
            putDto.setTipo(null);
            setorService.altera(criado.getId(), putDto, admin.getEmail());

            ESetor depois = setorRepository.findByIdAndStatus(criado.getId(), EnStatus.A).orElseThrow();
            assertThat(depois.getTipo()).isEqualTo(tipoOriginal);
        }
    }

    @Nested
    class ExclusaoHappyPath {

        @Test
        void deveDeletarSetor_InativandoNoDb() {
            SetorDto criado = setorService.cadastra(novoSetorDto("Pasto Delete " + sufixo(), 5), admin.getEmail());

            setorService.deleta(criado.getId(), admin.getEmail());

            assertThat(setorRepository.findByIdAndStatus(criado.getId(), EnStatus.A)).isEmpty();
            assertThat(setorRepository.findById(criado.getId()).orElseThrow().getStatus()).isEqualTo(EnStatus.I);
        }

        @Test
        void deveDeletarSetor_QuandoGerenteExecuta() {
            EUsuario gerente = criarUsuario(EnPerfilUsuario.GERENTE);
            SetorDto criado = setorService.cadastra(novoSetorDto("Pasto Del Gerente " + sufixo(), 5), admin.getEmail());

            setorService.deleta(criado.getId(), gerente.getEmail());

            assertThat(setorRepository.findByIdAndStatus(criado.getId(), EnStatus.A)).isEmpty();
        }
    }

    // ── Sad Path ──────────────────────────────────────────────────────────────

    @Nested
    class CadastroSadPath {

        @Test
        void deveLancarExcecao_AoCadastrarComEmailNulo() {
            assertThatThrownBy(() -> setorService.cadastra(novoSetorDto("X", 1), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("e-mail");
        }

        @Test
        void deveLancarExcecao_AoCadastrarComEmailBlank() {
            assertThatThrownBy(() -> setorService.cadastra(novoSetorDto("X", 1), "   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("e-mail");
        }

        @Test
        void deveLancarExcecao_AoCadastrarComUsuarioInexistente() {
            assertThatThrownBy(() -> setorService.cadastra(novoSetorDto("X", 1), "inexistente@it.local"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Usuário não encontrado");
        }

        @Test
        void deveLancarExcecao_AoCadastrarComPerfilCuidador() {
            EUsuario cuidador = criarUsuario(EnPerfilUsuario.CUIDADOR);
            assertThatThrownBy(() -> setorService.cadastra(novoSetorDto("X", 1), cuidador.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cuidadores Chefe");
        }

        @Test
        void deveLancarExcecao_AoCadastrarComPerfilFinanceiro() {
            EUsuario financeiro = criarUsuario(EnPerfilUsuario.FINANCEIRO);
            assertThatThrownBy(() -> setorService.cadastra(novoSetorDto("X", 1), financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cuidadores Chefe");
        }
    }

    @Nested
    class BuscaSadPath {

        @Test
        void deveLancarExcecao_AoBuscarSetorComIdInexistente() {
            assertThatThrownBy(() -> setorService.procuraPorId(Long.MAX_VALUE))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("nenhum setor encontrado");
        }

        @Test
        void deveLancarExcecao_AoBuscarSetorInativo() {
            SetorDto criado = setorService.cadastra(novoSetorDto("Pasto Inativo " + sufixo(), 5), admin.getEmail());
            setorService.deleta(criado.getId(), admin.getEmail());

            assertThatThrownBy(() -> setorService.procuraPorId(criado.getId()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class AlteracaoSadPath {

        @Test
        void deveLancarExcecao_AoAlterarSetorComIdInexistente() {
            assertThatThrownBy(() -> setorService.altera(Long.MAX_VALUE, novoSetorDto("X", 1), admin.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Setor não encontrado");
        }

        @Test
        void deveLancarExcecao_AoAlterarComEmailNulo() {
            SetorDto criado = setorService.cadastra(novoSetorDto("Pasto Alt Email Nulo " + sufixo(), 5), admin.getEmail());
            assertThatThrownBy(() -> setorService.altera(criado.getId(), novoSetorDto("X", 1), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("e-mail");
        }

        @Test
        void deveLancarExcecao_AoAlterarComPerfilCuidador() {
            EUsuario cuidador = criarUsuario(EnPerfilUsuario.CUIDADOR);
            SetorDto criado = setorService.cadastra(novoSetorDto("Pasto Alt Perm " + sufixo(), 5), admin.getEmail());
            assertThatThrownBy(() -> setorService.altera(criado.getId(), novoSetorDto("X", 1), cuidador.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cuidadores Chefe");
        }
    }

    @Nested
    class ExclusaoSadPath {

        @Test
        void deveLancarExcecao_AoExcluirSetorComIdInexistente() {
            assertThatThrownBy(() -> setorService.deleta(Long.MAX_VALUE, admin.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Setor não encontrado ou inativo");
        }

        @Test
        void deveLancarExcecao_AoExcluirComEmailNulo() {
            assertThatThrownBy(() -> setorService.deleta(1L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("e-mail");
        }

        @Test
        void deveLancarExcecao_AoExcluirComPerfilCuidadorChefe() {
            EUsuario cuidadorChefe = criarUsuario(EnPerfilUsuario.CUIDADOR_CHEFE);
            SetorDto criado = setorService.cadastra(novoSetorDto("Pasto Del CC " + sufixo(), 5), admin.getEmail());
            assertThatThrownBy(() -> setorService.deleta(criado.getId(), cuidadorChefe.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Administradores e Gerentes");
        }

        @Test
        void deveLancarExcecao_AoExcluirComPerfilCuidador() {
            EUsuario cuidador = criarUsuario(EnPerfilUsuario.CUIDADOR);
            SetorDto criado = setorService.cadastra(novoSetorDto("Pasto Del C " + sufixo(), 5), admin.getEmail());
            assertThatThrownBy(() -> setorService.deleta(criado.getId(), cuidador.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Administradores e Gerentes");
        }

        @Test
        void deveLancarExcecao_AoExcluirComPerfilFinanceiro() {
            EUsuario financeiro = criarUsuario(EnPerfilUsuario.FINANCEIRO);
            SetorDto criado = setorService.cadastra(novoSetorDto("Pasto Del F " + sufixo(), 5), admin.getEmail());
            assertThatThrownBy(() -> setorService.deleta(criado.getId(), financeiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Administradores e Gerentes");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private SetorDto novoSetorDto(String nome, int capacidade) {
        SetorDto dto = new SetorDto();
        dto.setNome(nome);
        dto.setCapacidadeMaxima(capacidade);
        dto.setMetaTexto("Meta IT");
        dto.setMetaProducaoLeite(100.0);
        dto.setMetaArrobaAbate(20.0);
        dto.setTipo(EnTipoSetor.PASTO);
        return dto;
    }

    private EUsuario criarUsuario(EnPerfilUsuario perfil) {
        EUsuario u = new EUsuario();
        u.setNome("Usuario IT " + sufixo());
        u.setEmail("setor-" + sufixo() + "@it.local");
        u.setSenha("senha-it");
        u.setPerfil(perfil);
        u.setDataCadastro(LocalDateTime.now());
        return usuarioRepository.save(u);
    }

    private ESetor criarSetorDireto(String nome, int capacidade) {
        ESetor s = new ESetor();
        s.setNome(nome);
        s.setCapacidadeMaxima(capacidade);
        s.setTipo(EnTipoSetor.PASTO);
        s.setCriadoPor(admin);
        return setorRepository.save(s);
    }

    private ELote criarLoteDireto(ESetor setor) {
        EAnimal animal = new EAnimal();
        animal.setCodigoBrinco("BRINCO-" + sufixo());
        animal.setNome("Animal IT " + sufixo());
        animal.setCor("Preto");
        animal.setDataNascimento(LocalDateTime.now().minusYears(1));
        animal.setPesoAtual(300.0);
        animal.setRaca("Nelore");
        animal.setAlturaCernelha(1.30);
        animal.setPerimetroToracico(1.70);
        animal.setComprimentoCorporal(1.80);
        animal.setSexo(EnSexoAnimal.M);
        animal.setStatusAnimal(EnStatusAnimal.ATIVO);
        animal.setUsuario(admin);
        EAnimal animalSalvo = animalRepository.save(animal);

        ELote lote = new ELote();
        lote.setCodigo("LT-" + sufixo().substring(0, 3));
        lote.setDescricao("Lote IT " + sufixo());
        lote.setCorBrinco("Azul");
        lote.setDataCriacao(LocalDate.now());
        lote.setCriadoPor(admin);
        ELote loteSalvo = loteRepository.save(lote);

        ELoteSetor ls = new ELoteSetor();
        ls.setLote(loteSalvo);
        ls.setSetor(setor);
        ls.setAnimais(List.of(animalSalvo));
        loteSetorRepository.save(ls);

        return loteSalvo;
    }

    private static String sufixo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }
}
