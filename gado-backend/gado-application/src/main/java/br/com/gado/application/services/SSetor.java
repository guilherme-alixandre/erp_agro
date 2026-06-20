package br.com.gado.application.services;

import br.com.gado.application.dto.SetorDto;
import br.com.gado.domain.entities.ELoteSetor;
import br.com.gado.domain.entities.ESetor;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnPerfilUsuario;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.ILoteSetor;
import br.com.gado.infrastructure.persistence.repositories.ISetor;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;

import java.util.List;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SSetor {

    @Autowired
    private ISetor setorInterface;

    @Autowired
    private ILoteSetor loteSetorInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private ModelMapper modelMapper;

    public SetorDto procuraPorId(Long id) {
        ESetor setor = setorInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("nenhum setor encontrado"));
        return toSetorDto(setor);
    }

    @Transactional(readOnly = true)
    public ArrayList<SetorDto> buscarTodos() {
        return setorInterface.findAllByStatus(EnStatus.A).stream()
                .map(this::toSetorDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional
    public SetorDto cadastra(SetorDto dto, String email) {
        validaPermissaoCriarEditar(email);
        EUsuario usuario = resolveUsuario(email);

        ESetor setor = new ESetor();
        setor.setNome(dto.getNome());
        setor.setCapacidadeMaxima(dto.getCapacidadeMaxima());
        setor.setMetaTexto(dto.getMetaTexto());
        setor.setMetaProducaoLeite(dto.getMetaProducaoLeite());
        setor.setMetaArrobaAbate(dto.getMetaArrobaAbate());
        setor.setTipo(dto.getTipo());
        setor.setCriadoPor(usuario);

        return toSetorDto(setorInterface.save(setor));
    }

    @Transactional
    public void deleta(Long id, String email) {
        validaPermissaoExcluir(email);
        ESetor setor = setorInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado ou inativo"));

        setor.setStatus(EnStatus.I);
        setorInterface.save(setor);
    }

    @Transactional
    public SetorDto altera(Long id, SetorDto dto, String email) {
        validaPermissaoCriarEditar(email);
        EUsuario usuario = resolveUsuario(email);

        ESetor setor = setorInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado"));

        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            setor.setNome(dto.getNome());
        }
        if (dto.getCapacidadeMaxima() > 0) {
            setor.setCapacidadeMaxima(dto.getCapacidadeMaxima());
        }
        if (dto.getTipo() != null) {
            setor.setTipo(dto.getTipo());
        }
        setor.setMetaTexto(dto.getMetaTexto());
        setor.setAlteradoPor(usuario);

        return toSetorDto(setorInterface.save(setor));
    }

    private void validaPermissaoCriarEditar(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do usuário responsável pela operação.");
        }
        EUsuario u = usuarioInterface.findByEmailAndStatus(email.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        EnPerfilUsuario p = u.getPerfil();
        if (p != EnPerfilUsuario.ADMINISTRADOR && p != EnPerfilUsuario.GERENTE && p != EnPerfilUsuario.CUIDADOR_CHEFE) {
            throw new IllegalArgumentException("Apenas Administradores, Gerentes e Cuidadores Chefe podem criar ou editar setores.");
        }
    }

    private void validaPermissaoExcluir(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do usuário responsável pela operação.");
        }
        EUsuario u = usuarioInterface.findByEmailAndStatus(email.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        EnPerfilUsuario p = u.getPerfil();
        if (p != EnPerfilUsuario.ADMINISTRADOR && p != EnPerfilUsuario.GERENTE) {
            throw new IllegalArgumentException("Apenas Administradores e Gerentes podem excluir setores.");
        }
    }

    private EUsuario resolveUsuario(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return usuarioInterface.findByEmailAndStatus(email.trim(), EnStatus.A)
                .orElse(null);
    }

    private SetorDto toSetorDto(ESetor setor) {
        SetorDto dto = new SetorDto();
        dto.setId(setor.getId());
        dto.setStatus(setor.getStatus());
        dto.setCreatedAt(setor.getCreatedAt());
        dto.setUpdatedAt(setor.getUpdatedAt());
        dto.setNome(setor.getNome());
        dto.setCapacidadeMaxima(setor.getCapacidadeMaxima());
        dto.setMetaTexto(setor.getMetaTexto());
        dto.setMetaProducaoLeite(setor.getMetaProducaoLeite());
        dto.setMetaArrobaAbate(setor.getMetaArrobaAbate());
        dto.setTipo(setor.getTipo());

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
            SetorDto.LoteResumoDto l = new SetorDto.LoteResumoDto();
            l.setLoteSectorId(ls.getId());
            l.setLoteId(ls.getLote().getId());
            l.setLoteCodigo(ls.getLote().getCodigo());
            l.setLoteCorBrinco(ls.getLote().getCorBrinco());
            l.setQuantidadeAnimais(ls.getAnimais().size());
            return l;
        }).toList());

        return dto;
    }
}
