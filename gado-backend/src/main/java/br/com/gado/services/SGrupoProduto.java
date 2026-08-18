package br.com.gado.services;

import br.com.gado.dto.grupoProdutoDto.GrupoProdutoCadastroDto;
import br.com.gado.dto.grupoProdutoDto.GrupoProdutoPutDto;
import br.com.gado.dto.grupoProdutoDto.GrupoProdutoRespostaDto;
import br.com.gado.entities.EGrupoProduto;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IGrupoProduto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SGrupoProduto {

    @Autowired
    private IGrupoProduto grupoProdutoInterface;

    @Transactional
    public List<GrupoProdutoRespostaDto> listar(String busca) {
        String termo = busca == null ? "" : busca.trim();
        List<EGrupoProduto> grupos = termo.isBlank()
                ? grupoProdutoInterface.findByStatusOrderByNomeAsc(EnStatus.A)
                : grupoProdutoInterface.findByStatusAndNomeContainingIgnoreCaseOrderByNomeAsc(EnStatus.A, termo);

        return grupos.stream().map(this::toRespostaDto).collect(Collectors.toList());
    }

    public GrupoProdutoRespostaDto buscarPorId(Long id) {
        EGrupoProduto grupo = grupoProdutoInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Grupo de produto não encontrado."));
        return toRespostaDto(grupo);
    }

    @Transactional
    public GrupoProdutoRespostaDto criar(GrupoProdutoCadastroDto dto) {
        String nome = dto.getNome().trim();
        String prefixo = dto.getCodigoPrefixo().trim();

        if (grupoProdutoInterface.findFirstByNomeIgnoreCase(nome).isPresent()) {
            throw new IllegalArgumentException("Já existe um grupo de produto com esse nome.");
        }
        if (grupoProdutoInterface.findFirstByCodigoPrefixo(prefixo).isPresent()) {
            throw new IllegalArgumentException("Já existe um grupo de produto com esse prefixo.");
        }

        EGrupoProduto grupo = new EGrupoProduto();
        grupo.setNome(nome);
        grupo.setCodigoPrefixo(prefixo);

        return toRespostaDto(grupoProdutoInterface.save(grupo));
    }

    @Transactional
    public GrupoProdutoRespostaDto atualizar(Long id, GrupoProdutoPutDto dto) {
        EGrupoProduto grupo = grupoProdutoInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Grupo de produto não encontrado."));

        if (dto.getNome() != null) {
            String nome = dto.getNome().trim();
            if (nome.isBlank()) {
                throw new IllegalArgumentException("O nome do grupo não pode ser vazio.");
            }
            grupoProdutoInterface.findFirstByNomeIgnoreCase(nome)
                    .filter(existente -> !existente.getId().equals(id))
                    .ifPresent(existente -> {
                        throw new IllegalArgumentException("Já existe um grupo de produto com esse nome.");
                    });
            grupo.setNome(nome);
        }

        if (dto.getCodigoPrefixo() != null) {
            String prefixo = dto.getCodigoPrefixo().trim();
            grupoProdutoInterface.findFirstByCodigoPrefixo(prefixo)
                    .filter(existente -> !existente.getId().equals(id))
                    .ifPresent(existente -> {
                        throw new IllegalArgumentException("Já existe um grupo de produto com esse prefixo.");
                    });
            grupo.setCodigoPrefixo(prefixo);
        }

        return toRespostaDto(grupoProdutoInterface.save(grupo));
    }

    /** Inativação lógica — produtos já vinculados ao grupo (e seus códigos) continuam intactos. */
    @Transactional
    public String inativar(Long id) {
        EGrupoProduto grupo = grupoProdutoInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Grupo de produto não encontrado."));
        grupo.setStatus(EnStatus.I);
        grupoProdutoInterface.save(grupo);
        return "Grupo de produto inativado com sucesso";
    }

    private GrupoProdutoRespostaDto toRespostaDto(EGrupoProduto grupo) {
        GrupoProdutoRespostaDto dto = new GrupoProdutoRespostaDto();
        dto.setId(grupo.getId());
        dto.setNome(grupo.getNome());
        dto.setCodigoPrefixo(grupo.getCodigoPrefixo());
        return dto;
    }
}
