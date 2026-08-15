package br.com.gado.services;

import br.com.gado.dto.SetorDto;
import br.com.gado.entities.ELoteSetor;
import br.com.gado.entities.ESetor;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.ILoteSetor;
import br.com.gado.repositories.ISetor;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SSetor {

    @Autowired
    private ISetor setorInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private ILoteSetor loteSetorInterface;

    public SetorDto procuraPorId(Long id) {
        ESetor setor = setorInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("nenhum setor encontrado"));
        return toDto(setor);
    }

    @Transactional(readOnly = true)
    public ArrayList<SetorDto> buscarTodos() {
        ArrayList<ESetor> setores = setorInterface.findAllByStatus(EnStatus.A);
        if (setores.isEmpty()) {
            log.warn("Nenhum setor ativo cadastrado.");
            return new ArrayList<>();
        }
        return setores.stream()
                .map(this::toDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional
    public SetorDto cadastra(SetorDto dto, String email) {
        EUsuario usuario = resolveUsuario(email);

        ESetor setor = new ESetor();
        setor.setNome(dto.getNome());
        setor.setCapacidadeMaxima(dto.getCapacidadeMaxima());
        setor.setMetaTexto(dto.getMetaTexto());
        setor.setMetaProducaoLeite(dto.getMetaProducaoLeite());
        setor.setMetaArrobaAbate(dto.getMetaArrobaAbate());
        setor.setTipo(dto.getTipo());
        setor.setCriadoPor(usuario);

        return toDto(setorInterface.save(setor));
    }

    @Transactional
    public void deleta(Long id) {
        ESetor setor = setorInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado ou inativo"));

        setor.setStatus(EnStatus.I);
        setorInterface.save(setor);
    }

    @Transactional
    public SetorDto altera(Long id, SetorDto dto, String email) {
        EUsuario usuario = resolveUsuario(email);

        ESetor setor = setorInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado"));

        setor.setNome(dto.getNome());
        setor.setCapacidadeMaxima(dto.getCapacidadeMaxima());
        setor.setTipo(dto.getTipo());
        setor.setMetaTexto(dto.getMetaTexto());
        setor.setAlteradoPor(usuario);

        return toDto(setorInterface.save(setor));
    }

    private EUsuario resolveUsuario(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return usuarioInterface.findByEmailAndStatus(email.trim(), EnStatus.A).orElse(null);
    }

    private SetorDto toDto(ESetor setor) {
        SetorDto dto = new SetorDto();
        dto.setId(setor.getId());
        dto.setNome(setor.getNome());
        dto.setCapacidadeMaxima(setor.getCapacidadeMaxima());
        dto.setMetaTexto(setor.getMetaTexto());
        dto.setMetaProducaoLeite(setor.getMetaProducaoLeite());
        dto.setMetaArrobaAbate(setor.getMetaArrobaAbate());
        dto.setTipo(setor.getTipo());
        dto.setStatus(setor.getStatus());

        if (setor.getCriadoPor() != null) {
            dto.setCriadoPorNome(setor.getCriadoPor().getNome());
            dto.setCriadoPorEmail(setor.getCriadoPor().getEmail());
        }
        if (setor.getAlteradoPor() != null) {
            dto.setAlteradoPorNome(setor.getAlteradoPor().getNome());
            dto.setAlteradoPorEmail(setor.getAlteradoPor().getEmail());
        }

        List<ELoteSetor> alocacoes = loteSetorInterface.findBySetor_Id(setor.getId());
        dto.setLotes(alocacoes.stream().map(ls -> {
            SetorDto.LoteResumoDto resumo = new SetorDto.LoteResumoDto();
            resumo.setLoteSectorId(ls.getId());
            resumo.setLoteId(ls.getLote().getId());
            resumo.setLoteCodigo(ls.getLote().getCodigo());
            resumo.setLoteCorBrinco(ls.getLote().getCorBrinco());
            resumo.setQuantidadeAnimais(ls.getAnimais().size());
            return resumo;
        }).collect(Collectors.toList()));

        return dto;
    }
}
