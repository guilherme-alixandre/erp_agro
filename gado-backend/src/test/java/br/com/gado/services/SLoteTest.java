package br.com.gado.services;

import br.com.gado.dto.loteDto.CustoRacaoLoteDto;
import br.com.gado.dto.loteDto.LoteCadastroDto;
import br.com.gado.dto.loteDto.LoteDto;
import br.com.gado.dto.loteDto.LoteSetorCadastroDto;
import br.com.gado.dto.loteDto.PerdaAlimentacaoLoteDto;
import br.com.gado.dto.loteDto.TransferenciaAnimalDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.EConsumoInsumo;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.ELote;
import br.com.gado.entities.ELoteSetor;
import br.com.gado.entities.ESetor;
import br.com.gado.entities.ESobraAlimentacao;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnStatusAnimal;
import br.com.gado.repositories.IAnimal;
import br.com.gado.repositories.IConsumoInsumo;
import br.com.gado.repositories.ILote;
import br.com.gado.repositories.ILoteSetor;
import br.com.gado.repositories.ISetor;
import br.com.gado.repositories.ISobraAlimentacao;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SLoteTest {

    @InjectMocks
    private SLote sLote;

    @Mock private ILote loteInterface;
    @Mock private ILoteSetor loteSetorInterface;
    @Mock private ISetor setorInterface;
    @Mock private IAnimal animalInterface;
    @Mock private IUsuario usuarioInterface;
    @Mock private IConsumoInsumo consumoInsumoInterface;
    @Mock private ISobraAlimentacao sobraAlimentacaoInterface;

    private EUsuario admin;
    private EUsuario gerente;
    private EUsuario cuidador;

    @BeforeEach
    void setUp() {
        admin = new EUsuario();
        admin.setEmail("admin@fazenda.com");
        admin.setPerfil(EnPerfilUsuario.ADMINISTRADOR);

        gerente = new EUsuario();
        gerente.setEmail("gerente@fazenda.com");
        gerente.setPerfil(EnPerfilUsuario.GERENTE);

        cuidador = new EUsuario();
        cuidador.setEmail("cuidador@fazenda.com");
        cuidador.setPerfil(EnPerfilUsuario.CUIDADOR);
    }

    private void stubUsuario(EUsuario usuario) {
        when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A))
                .thenReturn(Optional.of(usuario));
    }

    @Nested
    class ValidacaoDePermissaoTests {

        @Test
        void validaPermissao_devePermitir_QuandoAdministradorOuGerente() {
            stubUsuario(admin);
            assertThatCode(() -> sLote.validaPermissao(admin.getEmail())).doesNotThrowAnyException();
        }

        @Test
        void validaPermissaoTransferencia_devePermitir_QuandoAdministrador() {
            stubUsuario(admin);
            assertThatCode(() -> sLote.validaPermissaoTransferencia(admin.getEmail())).doesNotThrowAnyException();
        }

        @Test
        void validaPermissao_deveNegar_QuandoCuidador() {
            stubUsuario(cuidador);
            assertThatThrownBy(() -> sLote.validaPermissao(cuidador.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Administradores e Gerentes");
        }

        @Test
        void validaPermissaoEdicao_devePermitir_QuandoCuidadorChefe() {
            EUsuario cuidadorChefe = new EUsuario();
            cuidadorChefe.setEmail("chefe@fazenda.com");
            cuidadorChefe.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);
            stubUsuario(cuidadorChefe);

            assertThatCode(() -> sLote.validaPermissaoEdicao(cuidadorChefe.getEmail())).doesNotThrowAnyException();
        }

        @Test
        void validaPermissaoEdicao_deveNegar_QuandoCuidadorComum() {
            stubUsuario(cuidador);
            assertThatThrownBy(() -> sLote.validaPermissaoEdicao(cuidador.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void validaPermissaoTransferencia_deveNegar_QuandoCuidadorComum() {
            stubUsuario(cuidador);
            assertThatThrownBy(() -> sLote.validaPermissaoTransferencia(cuidador.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class BuscaTests {

        @Test
        void buscaPorId_deveRetornarDto_QuandoLoteAtivoExiste() {
            ELote lote = new ELote();
            lote.setId(1L);
            lote.setCodigo("LOT001");
            when(loteInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(lote));
            when(loteSetorInterface.findByLote_Id(1L)).thenReturn(List.of());

            LoteDto dto = sLote.buscaPorId(1L);

            assertThat(dto.getCodigo()).isEqualTo("LOT001");
            assertThat(dto.getTotalAnimais()).isZero();
        }

        @Test
        void buscaPorId_deveLancarEntityNotFoundException_QuandoNaoExiste() {
            when(loteInterface.findByIdAndStatus(99L, EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sLote.buscaPorId(99L)).isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void listarTodos_deveRetornarTodosOsLotesAtivos() {
            ELote lote1 = new ELote();
            lote1.setId(1L);
            lote1.setCodigo("LOT001");
            ELote lote2 = new ELote();
            lote2.setId(2L);
            lote2.setCodigo("LOT002");
            when(loteInterface.findAllByStatus(EnStatus.A))
                    .thenReturn(new java.util.ArrayList<>(List.of(lote1, lote2)));
            when(loteSetorInterface.findByLote_Id(anyLong())).thenReturn(List.of());

            List<LoteDto> resultado = sLote.listarTodos();

            assertThat(resultado).hasSize(2).extracting(LoteDto::getCodigo)
                    .containsExactlyInAnyOrder("LOT001", "LOT002");
        }
    }

    @Nested
    class GerarProximoCodigoTests {

        @Test
        void deveGerarPrimeiroCodigo_QuandoNenhumLoteCadastrado() {
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.empty());
            assertThat(sLote.gerarProximoCodigo()).isEqualTo("LOT001");
        }

        @Test
        void deveIncrementarUltimoCodigo() {
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.of("LOT007"));
            assertThat(sLote.gerarProximoCodigo()).isEqualTo("LOT008");
        }
    }

    @Nested
    class CadastraTests {

        @Test
        void deveCadastrarLote_ComAlocacaoDeAnimais() {
            stubUsuario(admin);
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.empty());
            when(loteInterface.save(any(ELote.class))).thenAnswer(inv -> {
                ELote l = inv.getArgument(0);
                l.setId(1L);
                return l;
            });

            ESetor setor = new ESetor();
            setor.setId(10L);
            setor.setNome("Pasto 1");
            setor.setCapacidadeMaxima(50);
            when(setorInterface.findByIdAndStatus(10L, EnStatus.A)).thenReturn(Optional.of(setor));

            EAnimal animal = new EAnimal();
            animal.setId(100L);
            animal.setCodigoBrinco("BR-100");
            animal.setStatusAnimal(EnStatusAnimal.ATIVO);
            when(animalInterface.findAllById(List.of(100L))).thenReturn(List.of(animal));
            when(loteSetorInterface.findConflitosAtivos(eq(100L), any(), eq(EnStatus.A))).thenReturn(List.of());
            when(loteSetorInterface.findBySetor_Id(10L)).thenReturn(List.of());

            LoteSetorCadastroDto alocacao = new LoteSetorCadastroDto();
            alocacao.setSetorId(10L);
            alocacao.setAnimaisIds(List.of(100L));
            LoteCadastroDto dto = new LoteCadastroDto();
            dto.setCorBrinco("Azul");
            dto.setAlocacoes(List.of(alocacao));

            String resultado = sLote.cadastra(admin.getEmail(), dto);

            assertThat(resultado).contains("LOT001").contains("cadastrado com sucesso");
            verify(loteSetorInterface).save(any(ELoteSetor.class));
        }

        @Test
        void deveLancarExcecao_QuandoSetorDaAlocacaoNaoEncontrado() {
            stubUsuario(admin);
            when(loteInterface.findUltimoCodigoGerado()).thenReturn(Optional.empty());
            when(loteInterface.save(any(ELote.class))).thenAnswer(inv -> inv.getArgument(0));
            when(setorInterface.findByIdAndStatus(999L, EnStatus.A)).thenReturn(Optional.empty());

            LoteSetorCadastroDto alocacao = new LoteSetorCadastroDto();
            alocacao.setSetorId(999L);
            LoteCadastroDto dto = new LoteCadastroDto();
            dto.setCorBrinco("Azul");
            dto.setAlocacoes(List.of(alocacao));

            assertThatThrownBy(() -> sLote.cadastra(admin.getEmail(), dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Setor não encontrado");
        }

        @Test
        void deveNegarCadastro_QuandoUsuarioSemPermissao() {
            stubUsuario(cuidador);
            LoteCadastroDto dto = new LoteCadastroDto();
            dto.setCorBrinco("Azul");
            dto.setAlocacoes(List.of());

            assertThatThrownBy(() -> sLote.cadastra(cuidador.getEmail(), dto))
                    .isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(loteInterface);
        }
    }

    @Nested
    class DeletaTests {

        @Test
        void deveInativarLote_QuandoEncontrado() {
            stubUsuario(admin);
            ELote lote = new ELote();
            lote.setId(1L);
            lote.setCodigo("LOT001");
            when(loteInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(lote));
            when(loteInterface.save(any(ELote.class))).thenAnswer(inv -> inv.getArgument(0));

            String resultado = sLote.deleta(1L, admin.getEmail());

            assertThat(resultado).contains("LOT001").contains("excluído com sucesso");
            assertThat(lote.getStatus()).isEqualTo(EnStatus.I);
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoLoteNaoExiste() {
            stubUsuario(admin);
            when(loteInterface.findByIdAndStatus(99L, EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sLote.deleta(99L, admin.getEmail()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class TransferirAnimalTests {

        private EAnimal animal;
        private ELote loteOrigem;
        private ESetor setorOrigem;
        private ELoteSetor loteSetorOrigem;
        private ELote loteDestino;
        private ESetor setorDestino;
        private TransferenciaAnimalDto dto;

        @BeforeEach
        void setUpTransferencia() {
            animal = new EAnimal();
            animal.setId(100L);
            animal.setCodigoBrinco("BR-100");
            animal.setStatusAnimal(EnStatusAnimal.ATIVO);

            loteOrigem = new ELote();
            loteOrigem.setId(1L);
            loteOrigem.setCodigo("LOT001");

            setorOrigem = new ESetor();
            setorOrigem.setId(10L);
            setorOrigem.setNome("Pasto Origem");

            loteSetorOrigem = new ELoteSetor();
            loteSetorOrigem.setId(500L);
            loteSetorOrigem.setLote(loteOrigem);
            loteSetorOrigem.setSetor(setorOrigem);
            loteSetorOrigem.getAnimais().add(animal);

            loteDestino = new ELote();
            loteDestino.setId(2L);
            loteDestino.setCodigo("LOT002");

            setorDestino = new ESetor();
            setorDestino.setId(20L);
            setorDestino.setNome("Pasto Destino");
            setorDestino.setCapacidadeMaxima(0);

            dto = new TransferenciaAnimalDto();
            dto.setAnimalId(100L);
            dto.setLoteDestinoId(2L);
            dto.setSetorDestinoId(20L);
        }

        @Test
        void deveTransferirAnimal_ParaNovaAlocacaoNoDestino() {
            stubUsuario(gerente);
            when(animalInterface.findById(100L)).thenReturn(Optional.of(animal));
            when(loteSetorInterface.findByAnimalIdAndLoteAtivo(100L, EnStatus.A))
                    .thenReturn(List.of(loteSetorOrigem));
            when(loteInterface.findByIdAndStatus(2L, EnStatus.A)).thenReturn(Optional.of(loteDestino));
            when(setorInterface.findByIdAndStatus(20L, EnStatus.A)).thenReturn(Optional.of(setorDestino));
            when(loteSetorInterface.findBySetor_Id(20L)).thenReturn(List.of());
            when(loteSetorInterface.findByLote_Id(2L)).thenReturn(List.of());
            when(loteSetorInterface.save(any(ELoteSetor.class))).thenAnswer(inv -> inv.getArgument(0));

            String resultado = sLote.transferirAnimal(gerente.getEmail(), dto);

            assertThat(resultado).contains("LOT002").contains("Pasto Destino");
            assertThat(loteSetorOrigem.getAnimais()).isEmpty();
        }

        @Test
        void deveLancarExcecao_QuandoAnimalComStatusBloqueado() {
            stubUsuario(gerente);
            animal.setStatusAnimal(EnStatusAnimal.VENDIDO);
            when(animalInterface.findById(100L)).thenReturn(Optional.of(animal));

            assertThatThrownBy(() -> sLote.transferirAnimal(gerente.getEmail(), dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não pode ser movimentado");
        }

        @Test
        void deveLancarExcecao_QuandoAnimalNaoAlocadoEmLoteAtivo() {
            stubUsuario(gerente);
            when(animalInterface.findById(100L)).thenReturn(Optional.of(animal));
            when(loteSetorInterface.findByAnimalIdAndLoteAtivo(100L, EnStatus.A)).thenReturn(List.of());

            assertThatThrownBy(() -> sLote.transferirAnimal(gerente.getEmail(), dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não está alocado");
        }

        @Test
        void deveLancarExcecao_QuandoJaAlocadoNoLoteESetorInformados() {
            stubUsuario(gerente);
            dto.setLoteDestinoId(1L);
            dto.setSetorDestinoId(10L);
            when(animalInterface.findById(100L)).thenReturn(Optional.of(animal));
            when(loteSetorInterface.findByAnimalIdAndLoteAtivo(100L, EnStatus.A))
                    .thenReturn(List.of(loteSetorOrigem));
            when(loteInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(loteOrigem));
            when(setorInterface.findByIdAndStatus(10L, EnStatus.A)).thenReturn(Optional.of(setorOrigem));

            assertThatThrownBy(() -> sLote.transferirAnimal(gerente.getEmail(), dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("já está alocado");
        }

        @Test
        void deveLancarExcecao_QuandoSetorDestinoExcedeCapacidadeMaxima() {
            stubUsuario(gerente);
            setorDestino.setCapacidadeMaxima(1);
            EAnimal outroAnimalNoSetor = new EAnimal();
            outroAnimalNoSetor.setId(200L);
            ELoteSetor alocacaoExistenteNoDestino = new ELoteSetor();
            alocacaoExistenteNoDestino.getAnimais().add(outroAnimalNoSetor);

            when(animalInterface.findById(100L)).thenReturn(Optional.of(animal));
            when(loteSetorInterface.findByAnimalIdAndLoteAtivo(100L, EnStatus.A))
                    .thenReturn(List.of(loteSetorOrigem));
            when(loteInterface.findByIdAndStatus(2L, EnStatus.A)).thenReturn(Optional.of(loteDestino));
            when(setorInterface.findByIdAndStatus(20L, EnStatus.A)).thenReturn(Optional.of(setorDestino));
            when(loteSetorInterface.findBySetor_Id(20L)).thenReturn(List.of(alocacaoExistenteNoDestino));

            assertThatThrownBy(() -> sLote.transferirAnimal(gerente.getEmail(), dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("excede a capacidade máxima");
        }
    }

    @Nested
    class CalcularCustoRacaoAcumuladoTests {

        @Test
        void deveLancarEntityNotFoundException_QuandoLoteNaoEncontrado() {
            when(loteInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sLote.calcularCustoRacaoAcumulado(1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void deveCalcularCustoTotalECustoPorAnimal() {
            ELote lote = new ELote();
            lote.setId(1L);
            when(loteInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(lote));

            ESetor setor = new ESetor();
            setor.setId(10L);
            EAnimal animal = new EAnimal();
            animal.setId(100L);
            ELoteSetor alocacao = new ELoteSetor();
            alocacao.setSetor(setor);
            alocacao.getAnimais().add(animal);
            when(loteSetorInterface.findByLote_Id(1L)).thenReturn(List.of(alocacao));

            EInsumo insumo = new EInsumo();
            insumo.setPrecoCompraMedio(5.0);
            EConsumoInsumo consumo = new EConsumoInsumo();
            consumo.setInsumo(insumo);
            consumo.setConsumoPorAnimal(2.0);
            when(consumoInsumoInterface.findBySetor_IdAndStatusOrderByDataConsumoDesc(10L, EnStatus.A))
                    .thenReturn(List.of(consumo));

            CustoRacaoLoteDto resultado = sLote.calcularCustoRacaoAcumulado(1L);

            assertThat(resultado.getCustoTotalAcumulado()).isEqualByComparingTo(new BigDecimal("10.00"));
            assertThat(resultado.getCustoPorAnimal()).isEqualByComparingTo(new BigDecimal("10.00"));
        }

        @Test
        void deveIgnorarSetorSemAnimaisDoLote() {
            ELote lote = new ELote();
            lote.setId(1L);
            when(loteInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(lote));

            ELoteSetor alocacaoVazia = new ELoteSetor();
            alocacaoVazia.setSetor(new ESetor());
            when(loteSetorInterface.findByLote_Id(1L)).thenReturn(List.of(alocacaoVazia));

            CustoRacaoLoteDto resultado = sLote.calcularCustoRacaoAcumulado(1L);

            assertThat(resultado.getCustoTotalAcumulado()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(resultado.getCustoPorAnimal()).isEqualByComparingTo(BigDecimal.ZERO);
            verifyNoInteractions(consumoInsumoInterface);
        }

        @Test
        void deveIgnorarConsumoSemPrecoMedioCadastrado() {
            ELote lote = new ELote();
            lote.setId(1L);
            when(loteInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(lote));

            ESetor setor = new ESetor();
            setor.setId(10L);
            EAnimal animal = new EAnimal();
            animal.setId(100L);
            ELoteSetor alocacao = new ELoteSetor();
            alocacao.setSetor(setor);
            alocacao.getAnimais().add(animal);
            when(loteSetorInterface.findByLote_Id(1L)).thenReturn(List.of(alocacao));

            EInsumo insumoSemPreco = new EInsumo();
            EConsumoInsumo consumo = new EConsumoInsumo();
            consumo.setInsumo(insumoSemPreco);
            consumo.setConsumoPorAnimal(2.0);
            when(consumoInsumoInterface.findBySetor_IdAndStatusOrderByDataConsumoDesc(10L, EnStatus.A))
                    .thenReturn(List.of(consumo));

            CustoRacaoLoteDto resultado = sLote.calcularCustoRacaoAcumulado(1L);

            assertThat(resultado.getCustoTotalAcumulado()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    class CalcularPerdaAlimentacaoAcumuladaTests {

        @Test
        void deveLancarEntityNotFoundException_QuandoLoteNaoEncontrado() {
            when(loteInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sLote.calcularPerdaAlimentacaoAcumulada(1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void deveCalcularPerdaTotalEPorAnimal_QuandoSobraNaoReaproveitada() {
            ELote lote = new ELote();
            lote.setId(1L);
            when(loteInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(lote));

            ESetor setor = new ESetor();
            setor.setId(10L);
            EAnimal animal = new EAnimal();
            animal.setId(100L);
            ELoteSetor alocacao = new ELoteSetor();
            alocacao.setSetor(setor);
            alocacao.getAnimais().add(animal);
            when(loteSetorInterface.findByLote_Id(1L)).thenReturn(List.of(alocacao));

            EInsumo insumo = new EInsumo();
            insumo.setPrecoCompraMedio(4.0);
            EConsumoInsumo consumo = new EConsumoInsumo();
            consumo.setId(500L);
            consumo.setInsumo(insumo);
            consumo.setTotalAnimaisSetor(5);
            when(consumoInsumoInterface.findBySetor_IdAndStatusOrderByDataConsumoDesc(10L, EnStatus.A))
                    .thenReturn(List.of(consumo));

            ESobraAlimentacao sobra = new ESobraAlimentacao();
            sobra.setConsumoInsumo(consumo);
            sobra.setReaproveitado(false);
            sobra.setQuantidadeSobraUnidadePrimaria(10.0);
            when(sobraAlimentacaoInterface.findByConsumoInsumo_IdIn(List.of(500L))).thenReturn(List.of(sobra));

            PerdaAlimentacaoLoteDto resultado = sLote.calcularPerdaAlimentacaoAcumulada(1L);

            assertThat(resultado.getValorTotalPerdido()).isEqualByComparingTo(new BigDecimal("8.00"));
            assertThat(resultado.getValorPerdidoPorAnimal()).isEqualByComparingTo(new BigDecimal("8.00"));
        }

        @Test
        void deveIgnorarSobraReaproveitada() {
            ELote lote = new ELote();
            lote.setId(1L);
            when(loteInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(lote));

            ESetor setor = new ESetor();
            setor.setId(10L);
            EAnimal animal = new EAnimal();
            animal.setId(100L);
            ELoteSetor alocacao = new ELoteSetor();
            alocacao.setSetor(setor);
            alocacao.getAnimais().add(animal);
            when(loteSetorInterface.findByLote_Id(1L)).thenReturn(List.of(alocacao));

            EInsumo insumo = new EInsumo();
            insumo.setPrecoCompraMedio(4.0);
            EConsumoInsumo consumo = new EConsumoInsumo();
            consumo.setId(500L);
            consumo.setInsumo(insumo);
            consumo.setTotalAnimaisSetor(5);
            when(consumoInsumoInterface.findBySetor_IdAndStatusOrderByDataConsumoDesc(10L, EnStatus.A))
                    .thenReturn(List.of(consumo));

            ESobraAlimentacao sobraReaproveitada = new ESobraAlimentacao();
            sobraReaproveitada.setConsumoInsumo(consumo);
            sobraReaproveitada.setReaproveitado(true);
            sobraReaproveitada.setQuantidadeSobraUnidadePrimaria(10.0);
            when(sobraAlimentacaoInterface.findByConsumoInsumo_IdIn(List.of(500L)))
                    .thenReturn(List.of(sobraReaproveitada));

            PerdaAlimentacaoLoteDto resultado = sLote.calcularPerdaAlimentacaoAcumulada(1L);

            assertThat(resultado.getValorTotalPerdido()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        void deveIgnorarQuandoNenhumaSobraRegistradaParaOConsumo() {
            ELote lote = new ELote();
            lote.setId(1L);
            when(loteInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(lote));

            ESetor setor = new ESetor();
            setor.setId(10L);
            EAnimal animal = new EAnimal();
            animal.setId(100L);
            ELoteSetor alocacao = new ELoteSetor();
            alocacao.setSetor(setor);
            alocacao.getAnimais().add(animal);
            when(loteSetorInterface.findByLote_Id(1L)).thenReturn(List.of(alocacao));

            EConsumoInsumo consumo = new EConsumoInsumo();
            consumo.setId(500L);
            when(consumoInsumoInterface.findBySetor_IdAndStatusOrderByDataConsumoDesc(10L, EnStatus.A))
                    .thenReturn(List.of(consumo));
            when(sobraAlimentacaoInterface.findByConsumoInsumo_IdIn(List.of(500L))).thenReturn(List.of());

            PerdaAlimentacaoLoteDto resultado = sLote.calcularPerdaAlimentacaoAcumulada(1L);

            assertThat(resultado.getValorTotalPerdido()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}
