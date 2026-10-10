package br.com.gado.services;

import br.com.gado.dto.custoAnimalDto.CustoVidaAnimalDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.EConsumoInsumo;
import br.com.gado.entities.ECustoAnimal;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.ESetor;
import br.com.gado.entities.EVacinacaoAnimal;
import br.com.gado.entities.EVacinacaoAnimalItem;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoCustoAnimal;
import br.com.gado.repositories.IAnimal;
import br.com.gado.repositories.ICustoAnimal;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SCustoAnimalTest {

    @InjectMocks
    private SCustoAnimal sCustoAnimal;

    @Mock private ICustoAnimal custoAnimalInterface;
    @Mock private IAnimal animalInterface;

    private EAnimal animal;

    @BeforeEach
    void setUp() {
        animal = new EAnimal();
        animal.setId(100L);
        animal.setCodigoBrinco("NE0001");
    }

    @Nested
    class RegistrarOuAtualizarAquisicaoTests {

        @Test
        void deveCriarLancamento_QuandoValorAquisicaoInformado() {
            animal.setValorAquisicao(new BigDecimal("1500.00"));
            animal.setDataAquisicao(LocalDateTime.of(2026, 1, 10, 0, 0));
            when(custoAnimalInterface.findByOrigemTipoAndOrigemIdAndStatus("ANIMAL", 100L, EnStatus.A))
                    .thenReturn(List.of());

            sCustoAnimal.registrarOuAtualizarAquisicao(animal);

            ArgumentCaptor<ECustoAnimal> captor = ArgumentCaptor.forClass(ECustoAnimal.class);
            verify(custoAnimalInterface).save(captor.capture());
            ECustoAnimal salvo = captor.getValue();
            assertThat(salvo.getTipoCusto()).isEqualTo(EnTipoCustoAnimal.AQUISICAO);
            assertThat(salvo.getValor()).isEqualByComparingTo(new BigDecimal("1500.00"));
            assertThat(salvo.getAnimal()).isEqualTo(animal);
        }

        @Test
        void naoDeveCriarLancamento_QuandoValorAquisicaoAusente() {
            when(custoAnimalInterface.findByOrigemTipoAndOrigemIdAndStatus("ANIMAL", 100L, EnStatus.A))
                    .thenReturn(List.of());

            sCustoAnimal.registrarOuAtualizarAquisicao(animal);

            verify(custoAnimalInterface, never()).save(any());
        }

        @Test
        void deveEstornarLancamentoAnterior_AntesDeRecriar() {
            animal.setValorAquisicao(new BigDecimal("2000.00"));
            ECustoAnimal antigo = new ECustoAnimal();
            antigo.setStatus(EnStatus.A);
            when(custoAnimalInterface.findByOrigemTipoAndOrigemIdAndStatus("ANIMAL", 100L, EnStatus.A))
                    .thenReturn(List.of(antigo));

            sCustoAnimal.registrarOuAtualizarAquisicao(animal);

            assertThat(antigo.getStatus()).isEqualTo(EnStatus.I);
            verify(custoAnimalInterface).saveAll(List.of(antigo));
            verify(custoAnimalInterface).save(any(ECustoAnimal.class));
        }
    }

    @Nested
    class RegistrarCustoAlimentacaoTests {

        @Test
        void deveCriarUmLancamentoPorAnimal_ComPrecoFotografado() {
            when(custoAnimalInterface.findByOrigemTipoAndOrigemIdAndStatus("CONSUMO_INSUMO", 500L, EnStatus.A))
                    .thenReturn(List.of());

            ESetor setor = new ESetor();
            setor.setNome("Pasto 1");
            EInsumo insumo = new EInsumo();
            insumo.setNome("Ração Bovina");

            EConsumoInsumo consumo = new EConsumoInsumo();
            consumo.setId(500L);
            consumo.setSetor(setor);
            consumo.setInsumo(insumo);
            consumo.setConsumoPorAnimal(2.0);
            consumo.setDataConsumo(LocalDateTime.now());

            EAnimal animal2 = new EAnimal();
            animal2.setId(101L);

            sCustoAnimal.registrarCustoAlimentacao(consumo, List.of(animal, animal2), 10.0);

            ArgumentCaptor<ECustoAnimal> captor = ArgumentCaptor.forClass(ECustoAnimal.class);
            verify(custoAnimalInterface, times(2)).save(captor.capture());
            List<ECustoAnimal> salvos = captor.getAllValues();
            assertThat(salvos).hasSize(2);
            assertThat(salvos.get(0).getValor()).isEqualByComparingTo(new BigDecimal("20.00"));
            assertThat(salvos.get(0).getTipoCusto()).isEqualTo(EnTipoCustoAnimal.ALIMENTACAO);
        }

        @Test
        void naoDeveCriarLancamento_QuandoConsumoPorAnimalNulo() {
            when(custoAnimalInterface.findByOrigemTipoAndOrigemIdAndStatus("CONSUMO_INSUMO", 500L, EnStatus.A))
                    .thenReturn(List.of());

            EConsumoInsumo consumo = new EConsumoInsumo();
            consumo.setId(500L);
            consumo.setConsumoPorAnimal(null);

            sCustoAnimal.registrarCustoAlimentacao(consumo, List.of(animal), 10.0);

            verify(custoAnimalInterface, never()).save(any());
        }
    }

    @Nested
    class RegistrarCustoVacinacaoTests {

        @Test
        void deveCriarLancamentoComValorDaDoseVezesPreco() {
            EInsumo vacina = new EInsumo();
            vacina.setNome("Vacina Aftosa");

            EVacinacaoAnimal vacinacao = new EVacinacaoAnimal();
            vacinacao.setInsumo(vacina);
            vacinacao.setQuantidadePorAnimal(5.0);
            vacinacao.setQuantidadeBaixaPorAnimalUnidadePrimaria(5.0);
            vacinacao.setDataAplicacao(LocalDateTime.now());
            vacinacao.setCriadoPorEmail("cuidador@fazenda.com");

            EVacinacaoAnimalItem item = new EVacinacaoAnimalItem();
            item.setId(900L);
            item.setAnimal(animal);
            item.setVacinacao(vacinacao);

            sCustoAnimal.registrarCustoVacinacao(item, 2.0);

            ArgumentCaptor<ECustoAnimal> captor = ArgumentCaptor.forClass(ECustoAnimal.class);
            verify(custoAnimalInterface).save(captor.capture());
            ECustoAnimal salvo = captor.getValue();
            assertThat(salvo.getTipoCusto()).isEqualTo(EnTipoCustoAnimal.VACINACAO);
            assertThat(salvo.getValor()).isEqualByComparingTo(new BigDecimal("10.00"));
            assertThat(salvo.getAnimal()).isEqualTo(animal);
        }
    }

    @Nested
    class EstornoTests {

        @Test
        void estornarCustoAlimentacao_deveInativarLancamentosDaOrigem() {
            ECustoAnimal lancamento = new ECustoAnimal();
            lancamento.setStatus(EnStatus.A);
            when(custoAnimalInterface.findByOrigemTipoAndOrigemIdAndStatus("CONSUMO_INSUMO", 500L, EnStatus.A))
                    .thenReturn(List.of(lancamento));

            sCustoAnimal.estornarCustoAlimentacao(500L);

            assertThat(lancamento.getStatus()).isEqualTo(EnStatus.I);
            verify(custoAnimalInterface).saveAll(List.of(lancamento));
        }

        @Test
        void estornarCustoVacinacaoItem_deveInativarLancamentosDaOrigem() {
            ECustoAnimal lancamento = new ECustoAnimal();
            lancamento.setStatus(EnStatus.A);
            when(custoAnimalInterface.findByOrigemTipoAndOrigemIdAndStatus("VACINACAO_ITEM", 900L, EnStatus.A))
                    .thenReturn(List.of(lancamento));

            sCustoAnimal.estornarCustoVacinacaoItem(900L);

            assertThat(lancamento.getStatus()).isEqualTo(EnStatus.I);
        }
    }

    @Nested
    class CalcularCustoVidaTests {

        @Test
        void deveLancarEntityNotFoundException_QuandoAnimalNaoEncontrado() {
            when(animalInterface.findByIdAndStatus(100L, EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sCustoAnimal.calcularCustoVida(100L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void deveSomarCadaCategoriaSeparadamente() {
            when(animalInterface.findByIdAndStatus(100L, EnStatus.A)).thenReturn(Optional.of(animal));

            ECustoAnimal aquisicao = lancamento(EnTipoCustoAnimal.AQUISICAO, "1000.00");
            ECustoAnimal alimentacao1 = lancamento(EnTipoCustoAnimal.ALIMENTACAO, "20.00");
            ECustoAnimal alimentacao2 = lancamento(EnTipoCustoAnimal.ALIMENTACAO, "15.00");
            ECustoAnimal vacinacao = lancamento(EnTipoCustoAnimal.VACINACAO, "8.00");
            when(custoAnimalInterface.findByAnimal_IdAndStatusOrderByDataEventoDesc(100L, EnStatus.A))
                    .thenReturn(List.of(aquisicao, alimentacao1, alimentacao2, vacinacao));

            CustoVidaAnimalDto resultado = sCustoAnimal.calcularCustoVida(100L);

            assertThat(resultado.getTotalAquisicao()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(resultado.getTotalAlimentacao()).isEqualByComparingTo(new BigDecimal("35.00"));
            assertThat(resultado.getTotalVacinacao()).isEqualByComparingTo(new BigDecimal("8.00"));
            assertThat(resultado.getTotalGeral()).isEqualByComparingTo(new BigDecimal("1043.00"));
            assertThat(resultado.getEventos()).hasSize(4);
        }

        private ECustoAnimal lancamento(EnTipoCustoAnimal tipo, String valor) {
            ECustoAnimal e = new ECustoAnimal();
            e.setTipoCusto(tipo);
            e.setValor(new BigDecimal(valor));
            e.setDataEvento(LocalDateTime.now());
            e.setDescricao("evento");
            return e;
        }
    }

    @Nested
    class CalcularCustoTotalAnimaisTests {

        @Test
        void deveRetornarMapaVazio_QuandoListaVazia() {
            Map<Long, CustoVidaAnimalDto> resultado = sCustoAnimal.calcularCustoTotalAnimais(List.of());

            assertThat(resultado).isEmpty();
            verifyNoInteractions(custoAnimalInterface);
        }

        @Test
        void deveAgruparLancamentosPorAnimal() {
            EAnimal animal2 = new EAnimal();
            animal2.setId(101L);

            ECustoAnimal custoAnimal1 = new ECustoAnimal();
            custoAnimal1.setAnimal(animal);
            custoAnimal1.setTipoCusto(EnTipoCustoAnimal.ALIMENTACAO);
            custoAnimal1.setValor(new BigDecimal("10.00"));
            custoAnimal1.setDataEvento(LocalDateTime.now());

            ECustoAnimal custoAnimal2 = new ECustoAnimal();
            custoAnimal2.setAnimal(animal2);
            custoAnimal2.setTipoCusto(EnTipoCustoAnimal.VACINACAO);
            custoAnimal2.setValor(new BigDecimal("3.00"));
            custoAnimal2.setDataEvento(LocalDateTime.now());

            when(custoAnimalInterface.findByAnimal_IdInAndStatus(List.of(100L, 101L), EnStatus.A))
                    .thenReturn(List.of(custoAnimal1, custoAnimal2));

            Map<Long, CustoVidaAnimalDto> resultado = sCustoAnimal.calcularCustoTotalAnimais(List.of(100L, 101L));

            assertThat(resultado.get(100L).getTotalAlimentacao()).isEqualByComparingTo(new BigDecimal("10.00"));
            assertThat(resultado.get(101L).getTotalVacinacao()).isEqualByComparingTo(new BigDecimal("3.00"));
        }
    }
}
