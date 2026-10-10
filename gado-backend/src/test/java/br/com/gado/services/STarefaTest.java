package br.com.gado.services;

import br.com.gado.dto.tarefaDto.TarefaAtribuirDto;
import br.com.gado.dto.tarefaDto.TarefaEdicaoDto;
import br.com.gado.dto.tarefaDto.TarefaRespostaDto;
import br.com.gado.entities.EListasTarefas;
import br.com.gado.entities.ETarefa;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IListasTarefas;
import br.com.gado.repositories.ITarefa;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class STarefaTest {

    @InjectMocks
    private STarefa sTarefa;

    @Mock
    private ITarefa tarefaInterface;

    @Mock
    private IListasTarefas listasTarefasInterface;

    @Mock
    private IUsuario usuarioInterface;

    private EUsuario atribuidor;
    private EUsuario destinatario;
    private EListasTarefas listaDestinatario;

    @BeforeEach
    void setUp() {
        atribuidor = new EUsuario();
        atribuidor.setId(1L);
        atribuidor.setEmail("atribuidor@fazenda.com");
        atribuidor.setNome("Atribuidor");

        destinatario = new EUsuario();
        destinatario.setId(2L);
        destinatario.setEmail("destinatario@fazenda.com");
        destinatario.setNome("Destinatario");

        listaDestinatario = new EListasTarefas();
        listaDestinatario.setId(10L);
        listaDestinatario.setUsuario(destinatario);
    }

    private ETarefa tarefaExistente(EListasTarefas lista, String atribuidoPorEmail) {
        ETarefa tarefa = new ETarefa();
        tarefa.setId(100L);
        tarefa.setDescricao("Alimentar o gado");
        tarefa.setStatusConclusao(false);
        tarefa.setStatus(EnStatus.A);
        tarefa.setListasTarefaId(lista);
        tarefa.setAtribuidoPorEmail(atribuidoPorEmail);
        return tarefa;
    }

    @Nested
    class AtribuirTarefaTests {

        @Test
        void deveCriarListaAutomaticamente_QuandoDestinatarioNaoTemListaAinda() {
            TarefaAtribuirDto dto = new TarefaAtribuirDto();
            dto.setDescricao("Vacinar lote 3");
            dto.setAtribuidoParaEmail(destinatario.getEmail());

            when(usuarioInterface.findByEmailAndStatus(atribuidor.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(atribuidor));
            when(usuarioInterface.findByEmailAndStatus(destinatario.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(destinatario));
            when(listasTarefasInterface.findByUsuario_Email(destinatario.getEmail()))
                    .thenReturn(Optional.empty());
            when(listasTarefasInterface.save(any(EListasTarefas.class))).thenReturn(listaDestinatario);
            when(tarefaInterface.save(any(ETarefa.class)))
                    .thenAnswer(inv -> tarefaExistente(listaDestinatario, atribuidor.getEmail()));

            TarefaRespostaDto resultado = sTarefa.atribuirTarefa(dto, atribuidor.getEmail());

            assertThat(resultado).isNotNull();
            assertThat(resultado.getDescricao()).isEqualTo("Alimentar o gado");
            verify(listasTarefasInterface, times(1)).save(any(EListasTarefas.class));
        }

        @Test
        void deveReutilizarListaExistente_QuandoDestinatarioJaTemLista() {
            TarefaAtribuirDto dto = new TarefaAtribuirDto();
            dto.setDescricao("Vacinar lote 3");
            dto.setAtribuidoParaEmail(destinatario.getEmail());

            when(usuarioInterface.findByEmailAndStatus(atribuidor.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(atribuidor));
            when(usuarioInterface.findByEmailAndStatus(destinatario.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(destinatario));
            when(listasTarefasInterface.findByUsuario_Email(destinatario.getEmail()))
                    .thenReturn(Optional.of(listaDestinatario));
            when(tarefaInterface.save(any(ETarefa.class)))
                    .thenAnswer(inv -> tarefaExistente(listaDestinatario, atribuidor.getEmail()));

            sTarefa.atribuirTarefa(dto, atribuidor.getEmail());

            verify(listasTarefasInterface, never()).save(any());
        }

        @Test
        void deveLancarExcecao_QuandoEmailDoAtribuidorEstaEmBranco() {
            TarefaAtribuirDto dto = new TarefaAtribuirDto();
            dto.setDescricao("Vacinar lote 3");
            dto.setAtribuidoParaEmail(destinatario.getEmail());

            assertThatThrownBy(() -> sTarefa.atribuirTarefa(dto, "  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("e-mail");
            verifyNoInteractions(tarefaInterface);
        }

        @Test
        void deveLancarExcecao_QuandoAtribuidorNaoEncontrado() {
            TarefaAtribuirDto dto = new TarefaAtribuirDto();
            dto.setDescricao("Vacinar lote 3");
            dto.setAtribuidoParaEmail(destinatario.getEmail());

            when(usuarioInterface.findByEmailAndStatus(atribuidor.getEmail(), EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sTarefa.atribuirTarefa(dto, atribuidor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Usuário não encontrado");
        }

        @Test
        void deveLancarExcecao_QuandoDestinatarioNaoEncontrado() {
            TarefaAtribuirDto dto = new TarefaAtribuirDto();
            dto.setDescricao("Vacinar lote 3");
            dto.setAtribuidoParaEmail("inexistente@fazenda.com");

            when(usuarioInterface.findByEmailAndStatus(atribuidor.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(atribuidor));
            when(usuarioInterface.findByEmailAndStatus("inexistente@fazenda.com", EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sTarefa.atribuirTarefa(dto, atribuidor.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Usuário não encontrado");
        }
    }

    @Nested
    class ListarMinhasTarefasTests {

        @Test
        void deveListarTarefasDoUsuarioOrdenadasPorDataLimite() {
            ETarefa tarefa = tarefaExistente(listaDestinatario, atribuidor.getEmail());
            when(usuarioInterface.findByEmailAndStatus(destinatario.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(destinatario));
            when(tarefaInterface.findByListasTarefaId_Usuario_EmailAndStatusOrderByDataLimiteAsc(
                    destinatario.getEmail(), EnStatus.A)).thenReturn(List.of(tarefa));
            when(usuarioInterface.findByEmailAndStatus(atribuidor.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(atribuidor));

            List<TarefaRespostaDto> resultado = sTarefa.listarMinhasTarefas(destinatario.getEmail());

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getAtribuidoParaEmail()).isEqualTo(destinatario.getEmail());
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioNaoEncontrado() {
            when(usuarioInterface.findByEmailAndStatus("fantasma@fazenda.com", EnStatus.A))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sTarefa.listarMinhasTarefas("fantasma@fazenda.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class ListarTarefasAtribuidasPorMimTests {

        @Test
        void deveListarTarefasAtribuidasPeloUsuario() {
            ETarefa tarefa = tarefaExistente(listaDestinatario, atribuidor.getEmail());
            when(usuarioInterface.findByEmailAndStatus(atribuidor.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(atribuidor));
            when(tarefaInterface.findByAtribuidoPorEmailIgnoreCaseAndStatusOrderByDataLimiteAsc(
                    atribuidor.getEmail(), EnStatus.A)).thenReturn(List.of(tarefa));

            List<TarefaRespostaDto> resultado = sTarefa.listarTarefasAtribuidasPorMim(atribuidor.getEmail());

            assertThat(resultado).hasSize(1);
        }
    }

    @Nested
    class EditarTarefaTests {

        @Test
        void deveAtualizarCampos_QuandoChamadorEhDonoDaLista() {
            ETarefa tarefa = tarefaExistente(listaDestinatario, atribuidor.getEmail());
            TarefaEdicaoDto dto = new TarefaEdicaoDto();
            dto.setStatusConclusao(true);

            when(usuarioInterface.findByEmailAndStatus(destinatario.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(destinatario));
            when(tarefaInterface.findById(100L)).thenReturn(Optional.of(tarefa));
            when(tarefaInterface.save(any(ETarefa.class))).thenAnswer(inv -> inv.getArgument(0));

            TarefaRespostaDto resultado = sTarefa.editarTarefa(100L, dto, destinatario.getEmail());

            assertThat(resultado.isStatusConclusao()).isTrue();
            assertThat(resultado.getDescricao()).isEqualTo("Alimentar o gado");
        }

        @Test
        void deveAtualizarCampos_QuandoChamadorEhQuemAtribuiu() {
            ETarefa tarefa = tarefaExistente(listaDestinatario, atribuidor.getEmail());
            TarefaEdicaoDto dto = new TarefaEdicaoDto();
            dto.setDescricao("Nova descrição");

            when(usuarioInterface.findByEmailAndStatus(atribuidor.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(atribuidor));
            when(tarefaInterface.findById(100L)).thenReturn(Optional.of(tarefa));
            when(tarefaInterface.save(any(ETarefa.class))).thenAnswer(inv -> inv.getArgument(0));

            TarefaRespostaDto resultado = sTarefa.editarTarefa(100L, dto, atribuidor.getEmail());

            assertThat(resultado.getDescricao()).isEqualTo("Nova descrição");
        }

        @Test
        void deveManterCamposNaoInformados_QuandoDtoParcial() {
            ETarefa tarefa = tarefaExistente(listaDestinatario, atribuidor.getEmail());
            Date dataOriginal = tarefa.getDataLimite();
            TarefaEdicaoDto dto = new TarefaEdicaoDto();
            dto.setStatusConclusao(true);

            when(usuarioInterface.findByEmailAndStatus(destinatario.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(destinatario));
            when(tarefaInterface.findById(100L)).thenReturn(Optional.of(tarefa));
            when(tarefaInterface.save(any(ETarefa.class))).thenAnswer(inv -> inv.getArgument(0));

            TarefaRespostaDto resultado = sTarefa.editarTarefa(100L, dto, destinatario.getEmail());

            assertThat(resultado.getDescricao()).isEqualTo("Alimentar o gado");
            assertThat(resultado.getDataLimite()).isEqualTo(dataOriginal);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoTarefaNaoExiste() {
            when(usuarioInterface.findByEmailAndStatus(destinatario.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(destinatario));
            when(tarefaInterface.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sTarefa.editarTarefa(999L, new TarefaEdicaoDto(), destinatario.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
            verify(tarefaInterface, never()).save(any());
        }

        @Test
        void deveLancarExcecao_QuandoChamadorNaoEhDonoNemAtribuidor() {
            ETarefa tarefa = tarefaExistente(listaDestinatario, atribuidor.getEmail());
            EUsuario terceiro = new EUsuario();
            terceiro.setEmail("terceiro@fazenda.com");

            when(usuarioInterface.findByEmailAndStatus(terceiro.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(terceiro));
            when(tarefaInterface.findById(100L)).thenReturn(Optional.of(tarefa));

            assertThatThrownBy(() -> sTarefa.editarTarefa(100L, new TarefaEdicaoDto(), terceiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Você só pode editar ou excluir");
            verify(tarefaInterface, never()).save(any());
        }
    }

    @Nested
    class ExcluirTarefaTests {

        @Test
        void deveInativarTarefa_QuandoChamadorTemPermissao() {
            ETarefa tarefa = tarefaExistente(listaDestinatario, atribuidor.getEmail());

            when(usuarioInterface.findByEmailAndStatus(destinatario.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(destinatario));
            when(tarefaInterface.findById(100L)).thenReturn(Optional.of(tarefa));
            when(tarefaInterface.save(any(ETarefa.class))).thenAnswer(inv -> inv.getArgument(0));

            String resultado = sTarefa.excluirTarefa(100L, destinatario.getEmail());

            assertThat(resultado).isEqualTo("Tarefa excluída com sucesso.");
            assertThat(tarefa.getStatus()).isEqualTo(EnStatus.I);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoTarefaNaoExiste() {
            when(usuarioInterface.findByEmailAndStatus(destinatario.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(destinatario));
            when(tarefaInterface.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sTarefa.excluirTarefa(999L, destinatario.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void deveLancarExcecao_QuandoChamadorSemPermissao() {
            ETarefa tarefa = tarefaExistente(listaDestinatario, atribuidor.getEmail());
            EUsuario terceiro = new EUsuario();
            terceiro.setEmail("terceiro@fazenda.com");

            when(usuarioInterface.findByEmailAndStatus(terceiro.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(terceiro));
            when(tarefaInterface.findById(100L)).thenReturn(Optional.of(tarefa));

            assertThatThrownBy(() -> sTarefa.excluirTarefa(100L, terceiro.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class);
            verify(tarefaInterface, never()).save(any());
        }
    }
}
