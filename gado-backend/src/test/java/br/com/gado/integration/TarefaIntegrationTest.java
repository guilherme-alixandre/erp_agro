package br.com.gado.integration;

import br.com.gado.GadoApplication;
import br.com.gado.dto.tarefaDto.TarefaAtribuirDto;
import br.com.gado.dto.tarefaDto.TarefaEdicaoDto;
import br.com.gado.dto.tarefaDto.TarefaRespostaDto;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.repositories.IListasTarefas;
import br.com.gado.repositories.IUsuario;
import br.com.gado.services.STarefa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Fluxo completo (sem mocks, contra H2) de atribuir uma tarefa a um usuário que ainda não tem
 * lista própria -> lista é criada automaticamente -> terceiro não pode editar -> dono/atribuidor
 * conseguem excluir.
 */
@SpringBootTest(classes = GadoApplication.class)
@ActiveProfiles("test")
@Transactional
class TarefaIntegrationTest {

    @Autowired private STarefa tarefaService;
    @Autowired private IUsuario usuarioRepository;
    @Autowired private IListasTarefas listasTarefasRepository;

    private EUsuario atribuidor;
    private EUsuario destinatario;

    @BeforeEach
    void setUp() {
        atribuidor = new EUsuario();
        atribuidor.setNome("Atribuidor IT");
        atribuidor.setEmail("atribuidor-it-" + System.nanoTime() + "@fazenda.com");
        atribuidor.setSenha("hash");
        atribuidor.setPerfil(EnPerfilUsuario.GERENTE);
        atribuidor = usuarioRepository.save(atribuidor);

        destinatario = new EUsuario();
        destinatario.setNome("Destinatario IT");
        destinatario.setEmail("destinatario-it-" + System.nanoTime() + "@fazenda.com");
        destinatario.setSenha("hash");
        destinatario.setPerfil(EnPerfilUsuario.CUIDADOR);
        destinatario = usuarioRepository.save(destinatario);
    }

    @Test
    void deveAtribuirTarefa_CriandoListaAutomaticamente_EPermitirEdicaoEExclusaoPeloDono() {
        assertThat(listasTarefasRepository.findByUsuario_Email(destinatario.getEmail())).isEmpty();

        TarefaAtribuirDto dto = new TarefaAtribuirDto();
        dto.setDescricao("Vacinar lote 5");
        dto.setAtribuidoParaEmail(destinatario.getEmail());
        TarefaRespostaDto criada = tarefaService.atribuirTarefa(dto, atribuidor.getEmail());

        assertThat(listasTarefasRepository.findByUsuario_Email(destinatario.getEmail())).isPresent();

        List<TarefaRespostaDto> minhasTarefas = tarefaService.listarMinhasTarefas(destinatario.getEmail());
        assertThat(minhasTarefas).extracting(TarefaRespostaDto::getId).contains(criada.getId());

        TarefaEdicaoDto edicao = new TarefaEdicaoDto();
        edicao.setStatusConclusao(true);
        TarefaRespostaDto editada = tarefaService.editarTarefa(criada.getId(), edicao, destinatario.getEmail());
        assertThat(editada.isStatusConclusao()).isTrue();

        String resultado = tarefaService.excluirTarefa(criada.getId(), destinatario.getEmail());
        assertThat(resultado).isEqualTo("Tarefa excluída com sucesso.");
        assertThat(tarefaService.listarMinhasTarefas(destinatario.getEmail())).isEmpty();
    }

    @Test
    void naoDevePermitirEdicao_PorUsuarioQueNaoEhDonoNemAtribuidor() {
        TarefaAtribuirDto dto = new TarefaAtribuirDto();
        dto.setDescricao("Vacinar lote 5");
        dto.setAtribuidoParaEmail(destinatario.getEmail());
        TarefaRespostaDto criada = tarefaService.atribuirTarefa(dto, atribuidor.getEmail());

        EUsuario terceiro = new EUsuario();
        terceiro.setNome("Terceiro IT");
        terceiro.setEmail("terceiro-it-" + System.nanoTime() + "@fazenda.com");
        terceiro.setSenha("hash");
        terceiro.setPerfil(EnPerfilUsuario.CUIDADOR);
        terceiro = usuarioRepository.save(terceiro);

        TarefaEdicaoDto edicao = new TarefaEdicaoDto();
        edicao.setStatusConclusao(true);
        String emailTerceiro = terceiro.getEmail();
        Long tarefaId = criada.getId();

        assertThatThrownBy(() -> tarefaService.editarTarefa(tarefaId, edicao, emailTerceiro))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveReutilizarMesmaLista_QuandoDestinatarioRecebeSegundaTarefa() {
        TarefaAtribuirDto primeira = new TarefaAtribuirDto();
        primeira.setDescricao("Primeira tarefa");
        primeira.setAtribuidoParaEmail(destinatario.getEmail());
        tarefaService.atribuirTarefa(primeira, atribuidor.getEmail());

        TarefaAtribuirDto segunda = new TarefaAtribuirDto();
        segunda.setDescricao("Segunda tarefa");
        segunda.setAtribuidoParaEmail(destinatario.getEmail());
        tarefaService.atribuirTarefa(segunda, atribuidor.getEmail());

        assertThat(listasTarefasRepository.findByUsuario_Email(destinatario.getEmail())).isPresent();
        assertThat(tarefaService.listarMinhasTarefas(destinatario.getEmail())).hasSize(2);
    }
}
