package br.com.gado.infrastructure.persistence.repositories;

import br.com.gado.domain.entities.EParceiro;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IParceiro extends JpaRepository<EParceiro, Long> {
    Optional<EParceiro> findByCPF_CNPJ(String cpf_cnpj);
    boolean existsByCPF_CNPJ(String cpf_cnpj);

    @Modifying
    @Transactional
    void deleteByCPF_CNPJ(String cpf_cnpj);
}
