package br.com.gado.application.services;

import br.com.gado.application.dto.OcorrenciaAnimalDTO;
import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.entities.EOcorrenciaAnimal;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.infrastructure.persistence.repositories.IAnimal;
import br.com.gado.infrastructure.persistence.repositories.IOcorrenciaAnimal;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SOcorrenciaAnimalTest {

    @InjectMocks
    private SOcorrenciaAnimal sOcorrenciaAnimal;

    @Mock
    private IOcorrenciaAnimal ocorrenciaAnimalInterface;

    @Mock
    private IAnimal animalInterface;

    private ModelMapper modelMapper;

    private EAnimal animalEntity;
    private EOcorrenciaAnimal ocorrenciaEntity;
    private OcorrenciaAnimalDTO dto;

    private final Long ID_GENERICO = 1L;
    private final String CODIGO_BRINCO = "BR123";

    @BeforeEach
    void setUp() {

        modelMapper = new ModelMapper();

        sOcorrenciaAnimal =
                new SOcorrenciaAnimal(
                        ocorrenciaAnimalInterface,
                        animalInterface,
                        modelMapper
                );

        animalEntity = new EAnimal();
        animalEntity.setId(ID_GENERICO);
        animalEntity.setCodigoBrinco(CODIGO_BRINCO);
        animalEntity.setStatus(EnStatus.A);

        ocorrenciaEntity = new EOcorrenciaAnimal();
        ocorrenciaEntity.setId(ID_GENERICO);
        ocorrenciaEntity.setIdAnimal(animalEntity);
        ocorrenciaEntity.setStatus(EnStatus.A);

        dto = new OcorrenciaAnimalDTO();

        EAnimal animalDto = new EAnimal();
        animalDto.setCodigoBrinco(CODIGO_BRINCO);
        animalDto.setStatus(EnStatus.A);

        dto.setId(ID_GENERICO);
        dto.setIdAnimal(animalDto);
        dto.setStatus(EnStatus.A);
    }

    @Nested
    class CriarOcorrenciaTests {

        @Test
        void criarOcorrencia_DeveSalvarComSucesso() {

            when(animalInterface.findByCodigoBrincoAndStatus(
                    CODIGO_BRINCO,
                    EnStatus.A
            )).thenReturn(Optional.of(animalEntity));

            when(ocorrenciaAnimalInterface.save(any(EOcorrenciaAnimal.class)))
                    .thenReturn(ocorrenciaEntity);

            OcorrenciaAnimalDTO response =
                    sOcorrenciaAnimal.criarOcorrenciaAnimal(dto);

            assertNotNull(response);
            assertEquals(ID_GENERICO, response.getId());

            verify(ocorrenciaAnimalInterface, times(1))
                    .save(any(EOcorrenciaAnimal.class));
        }

        @Test
        void criarOcorrencia_DeveLancarExcecao_QuandoAnimalNaoEncontrado() {

            when(animalInterface.findByCodigoBrincoAndStatus(
                    CODIGO_BRINCO,
                    EnStatus.A
            )).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sOcorrenciaAnimal.criarOcorrenciaAnimal(dto));
        }

        @Test
        void criarOcorrencia_DeveLancarExcecao_QuandoSaveFalhar() {

            when(animalInterface.findByCodigoBrincoAndStatus(
                    CODIGO_BRINCO,
                    EnStatus.A
            )).thenReturn(Optional.of(animalEntity));

            when(ocorrenciaAnimalInterface.save(any(EOcorrenciaAnimal.class)))
                    .thenThrow(new RuntimeException("Erro ao salvar"));

            assertThrows(RuntimeException.class,
                    () -> sOcorrenciaAnimal.criarOcorrenciaAnimal(dto));
        }
    }

    @Nested
    class BuscarOcorrenciaTests {

        @Test
        void buscarPorId_DeveRetornarOcorrencia() {

            when(ocorrenciaAnimalInterface.findById(ID_GENERICO))
                    .thenReturn(Optional.of(ocorrenciaEntity));

            OcorrenciaAnimalDTO response =
                    sOcorrenciaAnimal.encontrarOcorrenciaAnimalPorId(ID_GENERICO);

            assertNotNull(response);
            assertEquals(ID_GENERICO, response.getId());
        }

        @Test
        void buscarPorId_DeveLancarExcecao_QuandoNaoEncontrado() {

            when(ocorrenciaAnimalInterface.findById(ID_GENERICO))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sOcorrenciaAnimal.encontrarOcorrenciaAnimalPorId(ID_GENERICO));
        }
    }

    @Nested
    class AtualizarOcorrenciaTests {

        @Test
        void atualizarOcorrencia_DeveAtualizarComSucesso() {

            when(ocorrenciaAnimalInterface.findById(ID_GENERICO))
                    .thenReturn(Optional.of(ocorrenciaEntity));

            when(ocorrenciaAnimalInterface.save(any(EOcorrenciaAnimal.class)))
                    .thenReturn(ocorrenciaEntity);

            OcorrenciaAnimalDTO response =
                    sOcorrenciaAnimal.atualizarOcorrenciaAnimal(
                            ID_GENERICO,
                            dto
                    );

            assertNotNull(response);

            verify(ocorrenciaAnimalInterface, times(1))
                    .save(any(EOcorrenciaAnimal.class));
        }

        @Test
        void atualizarOcorrencia_DeveLancarExcecao_QuandoNaoEncontrado() {

            when(ocorrenciaAnimalInterface.findById(ID_GENERICO))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sOcorrenciaAnimal.atualizarOcorrenciaAnimal(
                            ID_GENERICO,
                            dto
                    ));
        }

        @Test
        void atualizarOcorrencia_DeveLancarExcecao_QuandoSaveFalhar() {

            when(ocorrenciaAnimalInterface.findById(ID_GENERICO))
                    .thenReturn(Optional.of(ocorrenciaEntity));

            when(ocorrenciaAnimalInterface.save(any(EOcorrenciaAnimal.class)))
                    .thenThrow(new RuntimeException("Erro ao atualizar"));

            assertThrows(RuntimeException.class,
                    () -> sOcorrenciaAnimal.atualizarOcorrenciaAnimal(
                            ID_GENERICO,
                            dto
                    ));
        }
    }

    @Nested
    class ExcluirOcorrenciaTests {

        @Test
        void excluirOcorrencia_DeveRealizarSoftDelete() {

            when(ocorrenciaAnimalInterface.findById(ID_GENERICO))
                    .thenReturn(Optional.of(ocorrenciaEntity));

            when(ocorrenciaAnimalInterface.save(any(EOcorrenciaAnimal.class)))
                    .thenReturn(ocorrenciaEntity);

            String response =
                    sOcorrenciaAnimal.excluirOcorrenciaAnimal(ID_GENERICO);

            assertEquals(
                    "ocorrência animal excluída com sucesso",
                    response
            );

            assertEquals(EnStatus.I, ocorrenciaEntity.getStatus());

            verify(ocorrenciaAnimalInterface, times(1))
                    .save(ocorrenciaEntity);
        }

        @Test
        void excluirOcorrencia_DeveRetornarMensagemErro_QuandoSaveFalhar() {

            when(ocorrenciaAnimalInterface.findById(ID_GENERICO))
                    .thenReturn(Optional.of(ocorrenciaEntity));

            when(ocorrenciaAnimalInterface.save(any(EOcorrenciaAnimal.class)))
                    .thenThrow(new RuntimeException());

            String response =
                    sOcorrenciaAnimal.excluirOcorrenciaAnimal(ID_GENERICO);

            assertEquals(
                    "erro ao excluir ocorrência animal",
                    response
            );
        }

        @Test
        void excluirOcorrencia_DeveLancarExcecao_QuandoNaoEncontrado() {

            when(ocorrenciaAnimalInterface.findById(ID_GENERICO))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sOcorrenciaAnimal.excluirOcorrenciaAnimal(ID_GENERICO));
        }
    }
}