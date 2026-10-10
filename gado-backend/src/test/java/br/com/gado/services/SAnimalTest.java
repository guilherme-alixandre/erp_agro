package br.com.gado.services;

import br.com.gado.dto.AnimalDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.ERaca;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IAnimal;
import br.com.gado.repositories.IRaca;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SAnimalTest {

    @InjectMocks
    private SAnimal sAnimal;

    @Mock private IAnimal animalInterface;
    @Mock private IUsuario usuarioInterface;
    @Mock private IRaca racaInterface;

    @org.mockito.Spy
    private ModelMapper modelMapper = new ModelMapper();

    private EUsuario usuario;
    private ERaca raca;

    @BeforeEach
    void setUp() {
        usuario = new EUsuario();
        usuario.setEmail("cuidador@fazenda.com");

        raca = new ERaca();
        raca.setId(1L);
        raca.setNome("Nelore");
        raca.setSigla("NE");
        raca.setStatus(EnStatus.A);
    }

    @Nested
    class BuscarTests {

        @Test
        void buscarPorBrinco_deveRetornarAnimal_QuandoEncontrado() {
            EAnimal animal = new EAnimal();
            animal.setId(1L);
            animal.setCodigoBrinco("NE0001");
            when(animalInterface.findByCodigoBrincoAndStatus("NE0001", EnStatus.A)).thenReturn(Optional.of(animal));

            AnimalDto resultado = sAnimal.buscarPorBrinco("NE0001");

            assertThat(resultado.getCodigoBrinco()).isEqualTo("NE0001");
        }

        @Test
        void buscarPorBrinco_deveLancarEntityNotFoundException_QuandoNaoEncontrado() {
            when(animalInterface.findByCodigoBrincoAndStatus("XX9999", EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sAnimal.buscarPorBrinco("XX9999")).isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void buscarTodosAnimais_semTermo_deveListarTodosAtivos() {
            EAnimal animal = new EAnimal();
            animal.setCodigoBrinco("NE0001");
            when(animalInterface.findAllByStatus(EnStatus.A)).thenReturn(Optional.of(new ArrayList<>(List.of(animal))));

            ArrayList<AnimalDto> resultado = sAnimal.buscarTodosAnimais(null);

            assertThat(resultado).hasSize(1);
        }

        @Test
        void buscarTodosAnimais_comTermo_deveFiltrar() {
            EAnimal animal = new EAnimal();
            animal.setCodigoBrinco("NE0001");
            when(animalInterface.buscarPorTermo(EnStatus.A, "NE00")).thenReturn(List.of(animal));

            ArrayList<AnimalDto> resultado = sAnimal.buscarTodosAnimais("NE00");

            assertThat(resultado).hasSize(1);
        }
    }

    @Nested
    class CadastraAnimalTests {

        @Test
        void deveCadastrarAnimal_GerandoPrimeiroCodigoDaRaca() throws Exception {
            when(racaInterface.findById(1L)).thenReturn(Optional.of(raca));
            when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A)).thenReturn(Optional.of(usuario));
            when(animalInterface.findFirstByCodigoBrincoStartingWithOrderByCodigoBrincoDesc("NE"))
                    .thenReturn(Optional.empty());
            when(animalInterface.saveAndFlush(any(EAnimal.class))).thenAnswer(inv -> inv.getArgument(0));

            AnimalDto dto = new AnimalDto();
            dto.setRacaId(1L);
            dto.setPesoAtual(200.0);

            AnimalDto resultado = sAnimal.cadastraAnimal(usuario.getEmail(), dto);

            assertThat(resultado.getCodigoBrinco()).isEqualTo("NE0001");
            assertThat(resultado.getRacaNome()).isEqualTo("Nelore");
        }

        @Test
        void deveIncrementarUltimoCodigo_QuandoJaExistemAnimaisDaRaca() throws Exception {
            when(racaInterface.findById(1L)).thenReturn(Optional.of(raca));
            when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A)).thenReturn(Optional.of(usuario));
            EAnimal ultimo = new EAnimal();
            ultimo.setCodigoBrinco("NE0007");
            when(animalInterface.findFirstByCodigoBrincoStartingWithOrderByCodigoBrincoDesc("NE"))
                    .thenReturn(Optional.of(ultimo));
            when(animalInterface.saveAndFlush(any(EAnimal.class))).thenAnswer(inv -> inv.getArgument(0));

            AnimalDto dto = new AnimalDto();
            dto.setRacaId(1L);
            dto.setPesoAtual(200.0);

            AnimalDto resultado = sAnimal.cadastraAnimal(usuario.getEmail(), dto);

            assertThat(resultado.getCodigoBrinco()).isEqualTo("NE0008");
        }

        @Test
        void deveLancarExcecao_QuandoRacaIdAusente() {
            AnimalDto dto = new AnimalDto();

            assertThatThrownBy(() -> sAnimal.cadastraAnimal(usuario.getEmail(), dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("raça");
        }

        @Test
        void deveLancarExcecao_QuandoRacaNaoEncontradaOuInativa() {
            when(racaInterface.findById(1L)).thenReturn(Optional.empty());

            AnimalDto dto = new AnimalDto();
            dto.setRacaId(1L);

            assertThatThrownBy(() -> sAnimal.cadastraAnimal(usuario.getEmail(), dto))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoUsuarioNaoEncontrado() {
            when(racaInterface.findById(1L)).thenReturn(Optional.of(raca));
            when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A)).thenReturn(Optional.empty());

            AnimalDto dto = new AnimalDto();
            dto.setRacaId(1L);

            assertThatThrownBy(() -> sAnimal.cadastraAnimal(usuario.getEmail(), dto))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class DeletaAnimalTests {

        @Test
        void deveInativarAnimal_QuandoEncontrado() {
            EAnimal animal = new EAnimal();
            animal.setCodigoBrinco("NE0001");
            animal.setStatus(EnStatus.A);
            when(animalInterface.findByCodigoBrincoAndStatus("NE0001", EnStatus.A)).thenReturn(Optional.of(animal));

            String resultado = sAnimal.deletaAnimal("NE0001");

            assertThat(resultado).isEqualTo("Animal deletado com sucesso");
            assertThat(animal.getStatus()).isEqualTo(EnStatus.I);
        }

        @Test
        void deveRetornarMensagem_QuandoNaoEncontrado() {
            when(animalInterface.findByCodigoBrincoAndStatus("XX9999", EnStatus.A)).thenReturn(Optional.empty());

            String resultado = sAnimal.deletaAnimal("XX9999");

            assertThat(resultado).isEqualTo("animal não encontrado");
        }
    }

    @Nested
    class AlteraAnimalTests {

        @Test
        void deveAtualizarCampos_SemAlterarCodigoBrinco() {
            EAnimal animal = new EAnimal();
            animal.setCodigoBrinco("NE0001");
            animal.setPesoAtual(200.0);
            when(animalInterface.findByCodigoBrincoAndStatus("NE0001", EnStatus.A)).thenReturn(Optional.of(animal));
            when(animalInterface.save(any(EAnimal.class))).thenAnswer(inv -> inv.getArgument(0));

            AnimalDto dto = new AnimalDto();
            dto.setPesoAtual(250.0);
            dto.setCodigoBrinco("HACKED9999");

            AnimalDto resultado = sAnimal.alteraAnimal("NE0001", dto);

            assertThat(resultado.getCodigoBrinco()).isEqualTo("NE0001");
            assertThat(resultado.getPesoAtual()).isEqualTo(250.0);
        }

        @Test
        void deveTrocarRaca_QuandoRacaIdInformadaEValida() {
            EAnimal animal = new EAnimal();
            animal.setCodigoBrinco("NE0001");
            when(animalInterface.findByCodigoBrincoAndStatus("NE0001", EnStatus.A)).thenReturn(Optional.of(animal));
            when(racaInterface.findById(1L)).thenReturn(Optional.of(raca));
            when(animalInterface.save(any(EAnimal.class))).thenAnswer(inv -> inv.getArgument(0));

            AnimalDto dto = new AnimalDto();
            dto.setRacaId(1L);

            AnimalDto resultado = sAnimal.alteraAnimal("NE0001", dto);

            assertThat(resultado.getRacaNome()).isEqualTo("Nelore");
        }

        @Test
        void deveLancarExcecao_QuandoNovaRacaInvalida() {
            EAnimal animal = new EAnimal();
            animal.setCodigoBrinco("NE0001");
            when(animalInterface.findByCodigoBrincoAndStatus("NE0001", EnStatus.A)).thenReturn(Optional.of(animal));
            when(racaInterface.findById(99L)).thenReturn(Optional.empty());

            AnimalDto dto = new AnimalDto();
            dto.setRacaId(99L);

            assertThatThrownBy(() -> sAnimal.alteraAnimal("NE0001", dto))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoAnimalNaoExiste() {
            when(animalInterface.findByCodigoBrincoAndStatus("XX9999", EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sAnimal.alteraAnimal("XX9999", new AnimalDto()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
