package br.com.gado.services;

import br.com.gado.dto.SetorDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.ELote;
import br.com.gado.entities.ELoteSetor;
import br.com.gado.entities.ESetor;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoSetor;
import br.com.gado.repositories.ILoteSetor;
import br.com.gado.repositories.ISetor;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SSetorTest {

    @InjectMocks
    private SSetor sSetor;

    @Mock private ISetor setorInterface;
    @Mock private IUsuario usuarioInterface;
    @Mock private ILoteSetor loteSetorInterface;

    private EUsuario usuario;
    private ESetor setor;

    @BeforeEach
    void setUp() {
        usuario = new EUsuario();
        usuario.setNome("Admin");
        usuario.setEmail("admin@fazenda.com");

        setor = new ESetor();
        setor.setId(1L);
        setor.setNome("Pasto Principal");
        setor.setCapacidadeMaxima(50);
    }

    @Nested
    class ProcuraPorIdTests {

        @Test
        void deveRetornarSetor_QuandoEncontrado() {
            ESetor setor = new ESetor();
            setor.setId(1L);
            setor.setNome("Pasto 1");
            when(setorInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(setor));
            when(loteSetorInterface.findBySetor_Id(1L)).thenReturn(List.of());

            SetorDto resultado = sSetor.procuraPorId(1L);

            assertThat(resultado.getNome()).isEqualTo("Pasto 1");
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoNaoEncontrado() {
            when(setorInterface.findByIdAndStatus(99L, EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sSetor.procuraPorId(99L)).isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void deveIgnorarAlocacoesDeLotesInativados() {
            when(setorInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(setor));

            ELote loteAtivo = new ELote();
            loteAtivo.setId(10L);
            loteAtivo.setCodigo("LOT001");
            loteAtivo.setStatus(EnStatus.A);
            ELoteSetor alocacaoAtiva = new ELoteSetor();
            alocacaoAtiva.setId(100L);
            alocacaoAtiva.setLote(loteAtivo);
            alocacaoAtiva.setSetor(setor);
            alocacaoAtiva.setAnimais(List.of(new EAnimal(), new EAnimal()));

            ELote loteInativo = new ELote();
            loteInativo.setId(11L);
            loteInativo.setCodigo("LOT002");
            loteInativo.setStatus(EnStatus.I);
            ELoteSetor alocacaoInativa = new ELoteSetor();
            alocacaoInativa.setId(101L);
            alocacaoInativa.setLote(loteInativo);
            alocacaoInativa.setSetor(setor);
            alocacaoInativa.setAnimais(List.of(new EAnimal()));

            when(loteSetorInterface.findBySetor_Id(1L)).thenReturn(List.of(alocacaoAtiva, alocacaoInativa));

            SetorDto resultado = sSetor.procuraPorId(1L);

            assertThat(resultado.getLotes()).hasSize(1);
            assertThat(resultado.getLotes().get(0).getLoteCodigo()).isEqualTo("LOT001");
            assertThat(resultado.getLotes().get(0).getQuantidadeAnimais()).isEqualTo(2);
        }

        @Test
        void deveRetornarListaVazia_QuandoSetorSemAlocacoes() {
            when(setorInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(setor));
            when(loteSetorInterface.findBySetor_Id(1L)).thenReturn(List.of());

            SetorDto resultado = sSetor.procuraPorId(1L);

            assertThat(resultado.getLotes()).isEmpty();
        }
    }

    @Nested
    class BuscarTodosTests {

        @Test
        void deveRetornarListaVazia_QuandoNenhumSetorAtivo() {
            when(setorInterface.findAllByStatus(EnStatus.A)).thenReturn(new ArrayList<>());

            assertThat(sSetor.buscarTodos()).isEmpty();
        }

        @Test
        void deveListarTodosOsSetoresAtivos() {
            ESetor setor = new ESetor();
            setor.setId(1L);
            setor.setNome("Pasto 1");
            ArrayList<ESetor> setores = new ArrayList<>(List.of(setor));
            when(setorInterface.findAllByStatus(EnStatus.A)).thenReturn(setores);
            when(loteSetorInterface.findBySetor_Id(1L)).thenReturn(List.of());

            assertThat(sSetor.buscarTodos()).hasSize(1);
        }
    }

    @Nested
    class CadastraTests {

        @Test
        void deveCadastrarSetor() {
            when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(usuario));
            when(setorInterface.save(any(ESetor.class))).thenAnswer(inv -> {
                ESetor s = inv.getArgument(0);
                s.setId(1L);
                return s;
            });
            when(loteSetorInterface.findBySetor_Id(1L)).thenReturn(List.of());

            SetorDto dto = new SetorDto();
            dto.setNome("Pasto Novo");
            dto.setCapacidadeMaxima(20);
            dto.setTipo(EnTipoSetor.PASTO);

            SetorDto resultado = sSetor.cadastra(dto, usuario.getEmail());

            assertThat(resultado.getNome()).isEqualTo("Pasto Novo");
            assertThat(resultado.getCriadoPorEmail()).isEqualTo(usuario.getEmail());
        }
    }

    @Nested
    class DeletaTests {

        @Test
        void deveInativarSetor() {
            ESetor setor = new ESetor();
            setor.setId(1L);
            when(setorInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(setor));
            when(setorInterface.save(any(ESetor.class))).thenAnswer(inv -> inv.getArgument(0));

            sSetor.deleta(1L);

            assertThat(setor.getStatus()).isEqualTo(EnStatus.I);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoSetorNaoExiste() {
            when(setorInterface.findByIdAndStatus(99L, EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sSetor.deleta(99L)).isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class AlteraTests {

        @Test
        void deveAlterarSetor() {
            ESetor setor = new ESetor();
            setor.setId(1L);
            setor.setNome("Nome antigo");
            when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(usuario));
            when(setorInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(setor));
            when(setorInterface.save(any(ESetor.class))).thenAnswer(inv -> inv.getArgument(0));
            when(loteSetorInterface.findBySetor_Id(1L)).thenReturn(List.of());

            SetorDto dto = new SetorDto();
            dto.setNome("Nome novo");
            dto.setCapacidadeMaxima(30);
            dto.setTipo(EnTipoSetor.GALPAO);

            SetorDto resultado = sSetor.altera(1L, dto, usuario.getEmail());

            assertThat(resultado.getNome()).isEqualTo("Nome novo");
            assertThat(resultado.getAlteradoPorEmail()).isEqualTo(usuario.getEmail());
        }
    }
}
