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
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cada usuário tem uma lista de tarefas própria, criada automaticamente na primeira vez que
 * alguém (inclusive ele mesmo) atribui uma tarefa a ele. Só o dono da lista (a quem a tarefa foi
 * atribuída) ou quem atribuiu podem editar/excluir uma tarefa.
 */
@Service
public class STarefa {

    @Autowired
    private ITarefa tarefaInterface;

    @Autowired
    private IListasTarefas listasTarefasInterface;

    @Autowired
    private IUsuario usuarioInterface;

    private EUsuario resolveUsuarioAtivo(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do usuário responsável pela operação.");
        }
        return usuarioInterface.findByEmailAndStatus(email.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    private EListasTarefas resolveOuCriarListaDoUsuario(EUsuario usuario) {
        return listasTarefasInterface.findByUsuario_Email(usuario.getEmail())
                .orElseGet(() -> {
                    EListasTarefas lista = new EListasTarefas();
                    lista.setNomeLista("Tarefas de " + usuario.getNome());
                    lista.setUsuario(usuario);
                    return listasTarefasInterface.save(lista);
                });
    }

    @Transactional
    public TarefaRespostaDto atribuirTarefa(TarefaAtribuirDto dto, String emailAtribuidor) {
        resolveUsuarioAtivo(emailAtribuidor);
        EUsuario destinatario = resolveUsuarioAtivo(dto.getAtribuidoParaEmail());
        EListasTarefas lista = resolveOuCriarListaDoUsuario(destinatario);

        ETarefa tarefa = new ETarefa();
        tarefa.setDescricao(dto.getDescricao().trim());
        tarefa.setDataLimite(dto.getDataLimite());
        tarefa.setStatusConclusao(false);
        tarefa.setAtribuidoPorEmail(emailAtribuidor.trim());
        tarefa.setListasTarefaId(lista);

        return toRespostaDto(tarefaInterface.save(tarefa));
    }

    @Transactional
    public List<TarefaRespostaDto> listarMinhasTarefas(String email) {
        resolveUsuarioAtivo(email);
        return tarefaInterface
                .findByListasTarefaId_Usuario_EmailAndStatusOrderByDataLimiteAsc(email, EnStatus.A)
                .stream()
                .map(this::toRespostaDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional
    public TarefaRespostaDto editarTarefa(Long tarefaId, TarefaEdicaoDto dto, String email) {
        EUsuario chamador = resolveUsuarioAtivo(email);
        ETarefa tarefa = tarefaInterface.findById(tarefaId)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada."));

        validarPermissao(chamador, tarefa);

        if (dto.getDescricao() != null && !dto.getDescricao().isBlank()) {
            tarefa.setDescricao(dto.getDescricao().trim());
        }
        if (dto.getDataLimite() != null) {
            tarefa.setDataLimite(dto.getDataLimite());
        }
        if (dto.getStatusConclusao() != null) {
            tarefa.setStatusConclusao(dto.getStatusConclusao());
        }

        return toRespostaDto(tarefaInterface.save(tarefa));
    }

    @Transactional
    public String excluirTarefa(Long tarefaId, String email) {
        EUsuario chamador = resolveUsuarioAtivo(email);
        ETarefa tarefa = tarefaInterface.findById(tarefaId)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada."));

        validarPermissao(chamador, tarefa);

        tarefa.setStatus(EnStatus.I);
        tarefaInterface.save(tarefa);
        return "Tarefa excluída com sucesso.";
    }

    private void validarPermissao(EUsuario chamador, ETarefa tarefa) {
        String donoLista = tarefa.getListasTarefaId().getUsuario() != null
                ? tarefa.getListasTarefaId().getUsuario().getEmail()
                : null;
        boolean ehDono = chamador.getEmail().equalsIgnoreCase(donoLista);
        boolean ehAtribuidor = chamador.getEmail().equalsIgnoreCase(tarefa.getAtribuidoPorEmail());

        if (!ehDono && !ehAtribuidor) {
            throw new IllegalArgumentException("Você só pode editar ou excluir tarefas atribuídas a você ou por você.");
        }
    }

    private TarefaRespostaDto toRespostaDto(ETarefa tarefa) {
        TarefaRespostaDto dto = new TarefaRespostaDto();
        dto.setId(tarefa.getId());
        dto.setDescricao(tarefa.getDescricao());
        dto.setDataLimite(tarefa.getDataLimite());
        dto.setStatusConclusao(tarefa.isStatusConclusao());
        dto.setAtribuidoPorEmail(tarefa.getAtribuidoPorEmail());
        dto.setCreatedAt(tarefa.getCreatedAt());

        usuarioInterface.findByEmailAndStatus(tarefa.getAtribuidoPorEmail(), EnStatus.A)
                .ifPresent(u -> dto.setAtribuidoPorNome(u.getNome()));

        EUsuario dono = tarefa.getListasTarefaId().getUsuario();
        if (dono != null) {
            dto.setAtribuidoParaEmail(dono.getEmail());
            dto.setAtribuidoParaNome(dono.getNome());
        }

        return dto;
    }
}
