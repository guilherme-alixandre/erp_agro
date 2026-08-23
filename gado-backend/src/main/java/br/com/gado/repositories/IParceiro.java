package br.com.gado.repositories;

import br.com.gado.entities.EParceiro;
import br.com.gado.enums.EnStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IParceiro extends JpaRepository<EParceiro, Long> {
    Optional<EParceiro> findByCpfCnpj(String cpfCnpj);
    boolean existsByCpfCnpj(String cpfCnpj);
    List<EParceiro> findByStatus(EnStatus status);

    @Modifying
    @Transactional
    void deleteByCpfCnpj(String cpfCnpj);
}
