package br.com.gado.services;

import br.com.gado.dto.CategoriaDTO;
import br.com.gado.entities.ECategoria;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.ICategoria;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SCategoria {

    private static final Logger log = LoggerFactory.getLogger(SCategoria.class);

    @Autowired
    private ICategoria categoriaInterface;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public CategoriaDTO criarCategoria(CategoriaDTO nomeCategoria) {
        ECategoria novaCategoria = modelMapper.map(nomeCategoria, ECategoria.class);

        ECategoria categoriaSalva = categoriaInterface.save(novaCategoria);
        return modelMapper.map(categoriaSalva, CategoriaDTO.class);
    }

    @Transactional(readOnly = true)
    public CategoriaDTO buscarCategoriaPorId(Long categoriaId) {
        ECategoria categoriaEntity = this.categoriaInterface
                .findByIdAndStatus(categoriaId, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada ou está inativa."));
        return modelMapper.map(categoriaEntity, CategoriaDTO.class);
    }

    @Transactional
    public CategoriaDTO atualizarCategoriaPorId(CategoriaDTO categoriaParaAtualizar, Long categoriaId) {
        ECategoria existingEntity = this.categoriaInterface
                .findById(categoriaId)
                .orElseThrow(EntityNotFoundException::new);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        this.modelMapper.map(categoriaParaAtualizar, existingEntity);

        try {
            ECategoria categoriaSalva = this.categoriaInterface.save(existingEntity);
            return modelMapper.map(categoriaSalva, CategoriaDTO.class);
        } catch (Exception e) {
            log.error("Erro ao atualizar nome da categoria: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public String excluirCategoria(Long categoriaId) {
        ECategoria categoriaParaExcluir = this.categoriaInterface
                .findByIdAndStatus(categoriaId, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada, ou já inativa com o ID: " + categoriaId));

        categoriaParaExcluir.setStatus(EnStatus.I);

        try {
            this.categoriaInterface.save(categoriaParaExcluir);
            return "Categoria excluida com sucesso";
        } catch (Exception e) {
            log.error("Erro ao excluir categoria: {}", e.getMessage(), e);
            return "Erro ao excluir categoria";
        }
    }
}
