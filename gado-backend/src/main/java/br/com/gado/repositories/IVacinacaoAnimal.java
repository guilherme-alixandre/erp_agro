package br.com.gado.repositories;

import br.com.gado.entities.EVacinacaoAnimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IVacinacaoAnimal extends JpaRepository<EVacinacaoAnimal, Long> {

    List<EVacinacaoAnimal> findAllByOrderByDataAplicacaoDesc();

    List<EVacinacaoAnimal> findByDataAplicacaoBetweenOrderByDataAplicacaoDesc(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT DISTINCT v FROM EVacinacaoAnimal v JOIN v.itens i "
            + "WHERE i.animal.id = :animalId ORDER BY v.dataAplicacao DESC")
    List<EVacinacaoAnimal> findByAnimalId(@Param("animalId") Long animalId);
}
