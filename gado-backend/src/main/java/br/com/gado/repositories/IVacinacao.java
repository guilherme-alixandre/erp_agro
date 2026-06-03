package br.com.gado.repositories;

import br.com.gado.entities.EVacinacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IVacinacao extends JpaRepository<EVacinacao, Long> {
    Optional<EVacinacao> findById(Long vacinacaoId);
    void deleteById(Long vacinacaoId);

    List<EVacinacao> findByAnimalRelacionado_Id(Long animalId);
}
