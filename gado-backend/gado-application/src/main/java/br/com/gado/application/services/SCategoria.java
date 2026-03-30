package br.com.gado.application.services;

import br.com.gado.domain.entities.ECategoria;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.application.dto.CategoriaDTO;
import br.com.gado.infrastructure.persistence.repositories.ICategoria;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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
    public CategoriaDTO criarCategoria (CategoriaDTO nomeCategoria) {

        ECategoria novaCategoria = new ECategoria();
        novaCategoria.setCategoria(nomeCategoria.getCategoria());

        ECategoria categoriaSalva = categoriaInterface.save(novaCategoria);

        return modelMapper.map(categoriaSalva, CategoriaDTO.class);
    }

    @Transactional
    public CategoriaDTO buscarCategoriaPorId(Long categoriaId) {
        ECategoria categoriaEntitye = (ECategoria) this.categoriaInterface
                .findById(categoriaId)
                .orElseThrow(EntityNotFoundException::new);
        return modelMapper.map(categoriaEntitye, CategoriaDTO.class);
    }

    @Transactional
    public CategoriaDTO atualizarCategoriaPorId(CategoriaDTO categoriaParaAtualizar, Long categoriaId) {
        ECategoria existingEntitye = (ECategoria) this.categoriaInterface
                .findById(categoriaId)
                .orElseThrow(EntityNotFoundException::new);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);

        this.modelMapper.map(categoriaParaAtualizar, existingEntitye);
        ECategoria categoriaParaSalvar = existingEntitye;

        try {

            ECategoria categoriaSalva = this.categoriaInterface.save(categoriaParaSalvar);
            return modelMapper.map(categoriaSalva, CategoriaDTO.class);

        } catch (Exception e){
            log.error("Erro ao atualizar nome da categoria: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public boolean excluirCategoria(Long categoriaId) {
        // 1. Usa o findById padrão do JPA
        ECategoria categoriaParaExcluir = this.categoriaInterface
                .findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com o ID: " + categoriaId));

        // 2. Inativa a categoria (Exclusão Lógica)
        categoriaParaExcluir.setStatus(EnStatus.I);

        try {
            this.categoriaInterface.save(categoriaParaExcluir);
            return true;
        } catch (Exception e) {
            // O log.error é excelente para debugar no console do IntelliJ
            log.error("Erro ao excluir categoria: {}", e.getMessage(), e);
            return false;
        }
    }
}
