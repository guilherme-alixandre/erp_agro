package br.com.gado.repositories;

import br.com.gado.entities.EFuncionario;
import br.com.gado.enums.EnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IFuncionario extends JpaRepository<EFuncionario, Long> {
    Optional<EFuncionario> findByCpf(String cpf);
    List<EFuncionario> findByStatusOrderByNomeCompletoAsc(EnStatus status);
}
