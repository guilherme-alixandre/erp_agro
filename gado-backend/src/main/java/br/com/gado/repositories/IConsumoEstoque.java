package br.com.gado.repositories;

import br.com.gado.entities.EConsumoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IConsumoEstoque extends JpaRepository<EConsumoEstoque, Long> {

    List<EConsumoEstoque> findAllByOrderByDataConsumoDesc();
}
