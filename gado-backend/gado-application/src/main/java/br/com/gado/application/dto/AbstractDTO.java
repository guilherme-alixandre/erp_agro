package br.com.gado.application.dto;

import br.com.gado.domain.enums.EnStatus;
import javax.persistence.MappedSuperclass;
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
