package br.com.gado.dto;

import br.com.gado.domain.enums.EnStatus;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

import java.util.Date;

@MappedSuperclass
@Data
public class AbstractDTO {
    private Long id;
    private EnStatus status;
    private Date createdAt;
    private Date updatedAt;
}
