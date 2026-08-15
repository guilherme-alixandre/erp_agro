package br.com.gado.repositories;

import br.com.gado.entities.EMedicaoMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IMedicaoMeta extends JpaRepository<EMedicaoMeta, Long> {

    List<EMedicaoMeta> findByMetaSetor_Id(Long metaSetorId);
}
