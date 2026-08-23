package br.com.gado.services;

import br.com.gado.dto.unidadeMedidaDto.UnidadeMedidaCadastroDto;
import br.com.gado.dto.unidadeMedidaDto.UnidadeMedidaPutDto;
import br.com.gado.dto.unidadeMedidaDto.UnidadeMedidaRespostaDto;
import br.com.gado.entities.EUnidadeMedida;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IUnidadeMedida;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SUnidadeMedida {

    @Autowired
    private IUnidadeMedida unidadeMedidaInterface;

    /**
     * Lista unidades de medida (ativas e inativas, para permitir reativação).
     * @param status opcional: "A" (apenas ativas), "I" (apenas inativas), nulo/vazio = todas.
     */
    @Transactional
    public List<UnidadeMedidaRespostaDto> listar(String busca, String status) {
        String termo = busca == null ? "" : busca.trim();
        EnStatus filtroStatus = parseStatus(status);

        List<EUnidadeMedida> unidades;
        if (filtroStatus != null) {
            unidades = termo.isBlank()
                    ? unidadeMedidaInterface.findByStatusOrderByUnidadeAsc(filtroStatus)
                    : unidadeMedidaInterface.findByStatusAndUnidadeContainingIgnoreCaseOrderByUnidadeAsc(filtroStatus, termo);
        } else {
            unidades = termo.isBlank()
                    ? unidadeMedidaInterface.findAllByOrderByUnidadeAsc()
                    : unidadeMedidaInterface.findByUnidadeContainingIgnoreCaseOrderByUnidadeAsc(termo);
        }

        return unidades.stream().map(this::toRespostaDto).collect(Collectors.toList());
    }

    private EnStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return EnStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido. Use 'A' (ativo) ou 'I' (inativo).");
        }
    }

    public UnidadeMedidaRespostaDto buscarPorId(Long id) {
        EUnidadeMedida unidade = unidadeMedidaInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Unidade de medida não encontrada."));
        return toRespostaDto(unidade);
    }

    @Transactional
    public UnidadeMedidaRespostaDto criar(UnidadeMedidaCadastroDto dto) {
        String unidade = dto.getUnidade().trim().toUpperCase();

        if (unidadeMedidaInterface.findFirstByUnidadeIgnoreCase(unidade).isPresent()) {
            throw new IllegalArgumentException("Já existe uma unidade de medida com esse nome.");
        }

        EUnidadeMedida nova = new EUnidadeMedida();
        nova.setUnidade(unidade);

        return toRespostaDto(unidadeMedidaInterface.save(nova));
    }

    @Transactional
    public UnidadeMedidaRespostaDto atualizar(Long id, UnidadeMedidaPutDto dto) {
        EUnidadeMedida existente = unidadeMedidaInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Unidade de medida não encontrada."));

        if (dto.getUnidade() != null) {
            String unidade = dto.getUnidade().trim().toUpperCase();
            if (unidade.isBlank()) {
                throw new IllegalArgumentException("A unidade não pode ser vazia.");
            }
            unidadeMedidaInterface.findFirstByUnidadeIgnoreCase(unidade)
                    .filter(outra -> !outra.getId().equals(id))
                    .ifPresent(outra -> {
                        throw new IllegalArgumentException("Já existe uma unidade de medida com esse nome.");
                    });
            existente.setUnidade(unidade);
        }

        return toRespostaDto(unidadeMedidaInterface.save(existente));
    }

    /** Inativação lógica — produtos já vinculados à unidade continuam intactos. */
    @Transactional
    public String inativar(Long id) {
        EUnidadeMedida unidade = unidadeMedidaInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Unidade de medida não encontrada."));
        unidade.setStatus(EnStatus.I);
        unidadeMedidaInterface.save(unidade);
        return "Unidade de medida inativada com sucesso";
    }

    @Transactional
    public String reativar(Long id) {
        EUnidadeMedida unidade = unidadeMedidaInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Unidade de medida não encontrada."));
        unidade.setStatus(EnStatus.A);
        unidadeMedidaInterface.save(unidade);
        return "Unidade de medida reativada com sucesso";
    }

    private UnidadeMedidaRespostaDto toRespostaDto(EUnidadeMedida unidade) {
        UnidadeMedidaRespostaDto dto = new UnidadeMedidaRespostaDto();
        dto.setId(unidade.getId());
        dto.setUnidade(unidade.getUnidade());
        dto.setStatus(unidade.getStatus());
        return dto;
    }
}
