package br.com.gado.repositories;

import br.com.gado.entities.EParceiro;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IParceiro extends JpaRepository<EParceiro, Long> {
    Optional<EParceiro> findByCpfCnpj(String cpfCnpj);
    boolean existsByCpfCnpj(String cpfCnpj);

    @Modifying
    @Transactional
    void deleteByCpfCnpj(String cpfCnpj);
}
