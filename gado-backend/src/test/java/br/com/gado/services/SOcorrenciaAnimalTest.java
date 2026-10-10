package br.com.gado.services;

import br.com.gado.dto.ocorrenciaAnimalDto.OcorrenciaAnimalAtualizacaoDto;
import br.com.gado.dto.ocorrenciaAnimalDto.OcorrenciaAnimalCadastroDto;
import br.com.gado.dto.ocorrenciaAnimalDto.OcorrenciaAnimalRespostaDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.EOcorrenciaAnimal;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoOcorrencia;
import br.com.gado.repositories.IAnimal;
import br.com.gado.repositories.IOcorrenciaAnimal;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SOcorrenciaAnimalTest {

    @Mock private IOcorrenciaAnimal ocorrenciaAnimalInterface;
    @Mock private IAnimal animalInterface;

    private SOcorrenciaAnimal sOcorrenciaAnimal;

    @BeforeEach
    void setUp() {
        sOcorrenciaAnimal = new SOcorrenciaAnimal(ocorrenciaAnimalInterface, animalInterface);
    }

    @Nested
    class ListarPorAnimalTests {

        @Test
        void deveListarOcorrenciasAtivasDoAnimal() {
            EOcorrenciaAnimal ocorrencia = new EOcorrenciaAnimal();
            ocorrencia.setId(1L);
            ocorrencia.setTipoOcorrencia(EnTipoOcorrencia.DOENCA);
            when(ocorrenciaAnimalInterface.findByIdAnimal_IdAndStatusOrderByDataOcorrenciaDesc(10L, EnStatus.A))
                    .thenReturn(List.of(ocorrencia));

            List<OcorrenciaAnimalRespostaDto> resultado = sOcorrenciaAnimal.listarPorAnimal(10L);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getTipoOcorrencia()).isEqualTo(EnTipoOcorrencia.DOENCA);
        }
    }

    @Nested
    class CriarOcorrenciaTests {

        @Test
        void deveCriarOcorrencia_QuandoAnimalEncontrado() {
            EAnimal animal = new EAnimal();
            animal.setId(10L);
            when(animalInterface.findByIdAndStatus(10L, EnStatus.A)).thenReturn(Optional.of(animal));
            when(ocorrenciaAnimalInterface.save(any(EOcorrenciaAnimal.class))).thenAnswer(inv -> {
                EOcorrenciaAnimal o = inv.getArgument(0);
                o.setId(1L);
                return o;
            });

            OcorrenciaAnimalCadastroDto dto = new OcorrenciaAnimalCadastroDto();
            dto.setAnimalId(10L);
            dto.setTipoOcorrencia(EnTipoOcorrencia.PESAGEM);
            dto.setDataOcorrencia(new Date());
            dto.setObservacao("Peso registrado");

            OcorrenciaAnimalRespostaDto resultado = sOcorrenciaAnimal.criarOcorrenciaPorAnimalId(dto);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getTipoOcorrencia()).isEqualTo(EnTipoOcorrencia.PESAGEM);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoAnimalNaoEncontrado() {
            when(animalInterface.findByIdAndStatus(99L, EnStatus.A)).thenReturn(Optional.empty());

            OcorrenciaAnimalCadastroDto dto = new OcorrenciaAnimalCadastroDto();
            dto.setAnimalId(99L);

            assertThatThrownBy(() -> sOcorrenciaAnimal.criarOcorrenciaPorAnimalId(dto))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class AtualizarOcorrenciaTests {

        @Test
        void deveAtualizarCampos() {
            EOcorrenciaAnimal ocorrencia = new EOcorrenciaAnimal();
            ocorrencia.setId(1L);
            when(ocorrenciaAnimalInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(ocorrencia));
            when(ocorrenciaAnimalInterface.save(any(EOcorrenciaAnimal.class))).thenAnswer(inv -> inv.getArgument(0));

            OcorrenciaAnimalAtualizacaoDto dto = new OcorrenciaAnimalAtualizacaoDto();
            dto.setTipoOcorrencia(EnTipoOcorrencia.OBITO);
            dto.setDataOcorrencia(new Date());
            dto.setObservacao("Atualizado");

            OcorrenciaAnimalRespostaDto resultado = sOcorrenciaAnimal.atualizarOcorrencia(1L, dto);

            assertThat(resultado.getTipoOcorrencia()).isEqualTo(EnTipoOcorrencia.OBITO);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoOcorrenciaNaoExiste() {
            when(ocorrenciaAnimalInterface.findByIdAndStatus(99L, EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sOcorrenciaAnimal.atualizarOcorrencia(99L, new OcorrenciaAnimalAtualizacaoDto()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class ExcluirOcorrenciaTests {

        @Test
        void deveInativarOcorrencia() {
            EOcorrenciaAnimal ocorrencia = new EOcorrenciaAnimal();
            ocorrencia.setId(1L);
            when(ocorrenciaAnimalInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(ocorrencia));
            when(ocorrenciaAnimalInterface.save(any(EOcorrenciaAnimal.class))).thenAnswer(inv -> inv.getArgument(0));

            sOcorrenciaAnimal.excluirOcorrencia(1L);

            assertThat(ocorrencia.getStatus()).isEqualTo(EnStatus.I);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoOcorrenciaNaoExiste() {
            when(ocorrenciaAnimalInterface.findByIdAndStatus(99L, EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sOcorrenciaAnimal.excluirOcorrencia(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
