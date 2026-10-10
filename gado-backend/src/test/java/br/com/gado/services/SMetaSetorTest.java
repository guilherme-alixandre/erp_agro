package br.com.gado.services;

import br.com.gado.dto.metaSetorDto.MedicaoMetaCadastroDto;
import br.com.gado.dto.metaSetorDto.MedicaoMetaPutDto;
import br.com.gado.dto.metaSetorDto.MetaSetorCadastroDto;
import br.com.gado.dto.metaSetorDto.MetaSetorPutDto;
import br.com.gado.dto.metaSetorDto.MetaSetorRespostaDto;
import br.com.gado.entities.EMedicaoMeta;
import br.com.gado.entities.EMetaSetor;
import br.com.gado.entities.ELote;
import br.com.gado.entities.ESetor;
import br.com.gado.entities.EUsuario;
import br.com.gado.entities.EVendaMetaLote;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoGado;
import br.com.gado.enums.EnTipoMeta;
import br.com.gado.repositories.ILote;
import br.com.gado.repositories.IMedicaoMeta;
import br.com.gado.repositories.IMetaSetor;
import br.com.gado.repositories.ISetor;
import br.com.gado.repositories.IUsuario;
import br.com.gado.repositories.IVendaMetaLote;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SMetaSetorTest {

    @InjectMocks
    private SMetaSetor sMetaSetor;

    @Mock private IMetaSetor metaSetorInterface;
    @Mock private IMedicaoMeta medicaoMetaInterface;
    @Mock private ISetor setorInterface;
    @Mock private ILote loteInterface;
    @Mock private IUsuario usuarioInterface;
    @Mock private IVendaMetaLote vendaMetaLoteInterface;

    private EUsuario admin;
    private EUsuario cuidador;
    private EUsuario cuidadorChefe;
    private ESetor setor;

    @BeforeEach
    void setUp() {
        admin = new EUsuario();
        admin.setEmail("admin@fazenda.com");
        admin.setPerfil(EnPerfilUsuario.ADMINISTRADOR);

        cuidador = new EUsuario();
        cuidador.setEmail("cuidador@fazenda.com");
        cuidador.setPerfil(EnPerfilUsuario.CUIDADOR);

        cuidadorChefe = new EUsuario();
        cuidadorChefe.setEmail("chefe@fazenda.com");
        cuidadorChefe.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);

        setor = new ESetor();
        setor.setId(1L);
        setor.setNome("Pasto 1");
    }

    private void stubUsuario(EUsuario usuario) {
        when(usuarioInterface.findByEmailAndStatus(usuario.getEmail(), EnStatus.A))
                .thenReturn(Optional.of(usuario));
    }

    @Nested
    class ValidacaoDePermissaoTests {

        @Test
        void validaAdminOuGerente_devePermitirAdministrador() {
            stubUsuario(admin);
            sMetaSetor.validaAdminOuGerente(admin.getEmail());
        }

        @Test
        void validaAdminOuGerente_deveNegarCuidador() {
            stubUsuario(cuidador);
            assertThatThrownBy(() -> sMetaSetor.validaAdminOuGerente(cuidador.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void validaEdicaoMedicao_devePermitirAdministrador_SempreQueForACriacao() {
            stubUsuario(admin);
            EMedicaoMeta medicao = new EMedicaoMeta();
            medicao.setCriadoPorEmail(cuidador.getEmail());

            sMetaSetor.validaEdicaoMedicao(admin.getEmail(), medicao);
        }

        @Test
        void validaEdicaoMedicao_deveNegarCuidadorChefe_QuandoCriadaPorGerente() {
            stubUsuario(cuidadorChefe);
            EUsuario gerente = new EUsuario();
            gerente.setEmail("gerente@fazenda.com");
            gerente.setPerfil(EnPerfilUsuario.GERENTE);
            when(usuarioInterface.findByEmailAndStatus(gerente.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(gerente));

            EMedicaoMeta medicao = new EMedicaoMeta();
            medicao.setCriadoPorEmail(gerente.getEmail());

            assertThatThrownBy(() -> sMetaSetor.validaEdicaoMedicao(cuidadorChefe.getEmail(), medicao))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não podem alterar medições criadas por Administradores ou Gerentes");
        }

        @Test
        void validaEdicaoMedicao_devePermitirCuidadorChefe_QuandoCriadaPorCuidador() {
            stubUsuario(cuidadorChefe);
            when(usuarioInterface.findByEmailAndStatus(cuidador.getEmail(), EnStatus.A))
                    .thenReturn(Optional.of(cuidador));

            EMedicaoMeta medicao = new EMedicaoMeta();
            medicao.setCriadoPorEmail(cuidador.getEmail());

            sMetaSetor.validaEdicaoMedicao(cuidadorChefe.getEmail(), medicao);
        }

        @Test
        void validaEdicaoMedicao_devePermitirCuidador_QuandoCriouAMedicao() {
            stubUsuario(cuidador);
            EMedicaoMeta medicao = new EMedicaoMeta();
            medicao.setCriadoPorEmail(cuidador.getEmail());

            sMetaSetor.validaEdicaoMedicao(cuidador.getEmail(), medicao);
        }

        @Test
        void validaEdicaoMedicao_deveNegarCuidador_QuandoNaoCriouAMedicao() {
            stubUsuario(cuidador);
            EMedicaoMeta medicao = new EMedicaoMeta();
            medicao.setCriadoPorEmail("outro@fazenda.com");

            assertThatThrownBy(() -> sMetaSetor.validaEdicaoMedicao(cuidador.getEmail(), medicao))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("só pode editar medições que você mesmo criou");
        }
    }

    @Nested
    class CadastrarTests {

        private MetaSetorCadastroDto dtoLeite() {
            MetaSetorCadastroDto dto = new MetaSetorCadastroDto();
            dto.setSetorId(1L);
            dto.setDataInicial(LocalDate.of(2026, 1, 1));
            dto.setDataFinal(LocalDate.of(2026, 1, 31));
            dto.setTipoMeta(EnTipoMeta.LEITE);
            dto.setQuantidadeEsperada(1000.0);
            dto.setPrecoMedio(2.5);
            return dto;
        }

        @Test
        void deveCadastrarMetaLeite_ForcandoTipoGadoNulo() {
            when(setorInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(setor));
            when(metaSetorInterface.save(any(EMetaSetor.class))).thenAnswer(inv -> inv.getArgument(0));

            String resultado = sMetaSetor.cadastrar(dtoLeite());

            assertThat(resultado).contains("cadastrada com sucesso");
            verify(metaSetorInterface).save(argThat(m -> m.getTipoGado() == null));
        }

        @Test
        void deveLancarExcecao_QuandoDataFinalAnteriorAInicial() {
            MetaSetorCadastroDto dto = dtoLeite();
            dto.setDataFinal(LocalDate.of(2025, 12, 1));

            assertThatThrownBy(() -> sMetaSetor.cadastrar(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("data final não pode ser anterior");
        }

        @Test
        void deveLancarExcecao_QuandoArrobaSemTipoGado() {
            MetaSetorCadastroDto dto = dtoLeite();
            dto.setTipoMeta(EnTipoMeta.ARROBA);

            assertThatThrownBy(() -> sMetaSetor.cadastrar(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("tipo de gado é obrigatório");
        }

        @Test
        void deveLancarExcecao_QuandoSetorNaoEncontrado() {
            when(setorInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sMetaSetor.cadastrar(dtoLeite()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Setor não encontrado");
        }
    }

    @Nested
    class AlterarTests {

        private EMetaSetor metaLeite() {
            EMetaSetor meta = new EMetaSetor();
            meta.setId(1L);
            meta.setSetor(setor);
            meta.setDataInicial(LocalDate.of(2026, 1, 1));
            meta.setDataFinal(LocalDate.of(2026, 1, 31));
            meta.setTipoMeta(EnTipoMeta.LEITE);
            meta.setQuantidadeEsperada(1000.0);
            meta.setPrecoMedio(2.5);
            return meta;
        }

        @Test
        void deveAtualizarQuantidadeEsperada() {
            EMetaSetor meta = metaLeite();
            when(metaSetorInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(meta));
            when(metaSetorInterface.save(any(EMetaSetor.class))).thenAnswer(inv -> inv.getArgument(0));

            MetaSetorPutDto dto = new MetaSetorPutDto();
            dto.setQuantidadeEsperada(2000.0);

            String resultado = sMetaSetor.alterar(1L, dto);

            assertThat(resultado).contains("atualizada com sucesso");
            assertThat(meta.getQuantidadeEsperada()).isEqualTo(2000.0);
        }

        @Test
        void deveLancarExcecao_QuandoTipoGadoInformadoParaMetaLeite() {
            EMetaSetor meta = metaLeite();
            when(metaSetorInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(meta));

            MetaSetorPutDto dto = new MetaSetorPutDto();
            dto.setTipoGado(EnTipoGado.BOVINO_JOVEM_50);

            assertThatThrownBy(() -> sMetaSetor.alterar(1L, dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não possuem tipo de gado");
        }

        @Test
        void deveLancarEntityNotFoundException_QuandoMetaNaoExiste() {
            when(metaSetorInterface.findByIdAndStatus(99L, EnStatus.A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sMetaSetor.alterar(99L, new MetaSetorPutDto()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class DeletarTests {

        @Test
        void deveInativarMeta() {
            EMetaSetor meta = new EMetaSetor();
            meta.setId(1L);
            when(metaSetorInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(meta));
            when(metaSetorInterface.save(any(EMetaSetor.class))).thenAnswer(inv -> inv.getArgument(0));

            String resultado = sMetaSetor.deletar(1L);

            assertThat(resultado).contains("removida com sucesso");
            assertThat(meta.getStatus()).isEqualTo(EnStatus.I);
        }
    }

    @Nested
    class MedicaoTests {

        @Test
        void cadastrarMedicao_deveSalvarComSucesso() {
            EMetaSetor meta = new EMetaSetor();
            meta.setId(1L);
            ELote lote = new ELote();
            lote.setId(1L);
            when(metaSetorInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(meta));
            when(loteInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(lote));
            when(medicaoMetaInterface.save(any(EMedicaoMeta.class))).thenAnswer(inv -> inv.getArgument(0));

            MedicaoMetaCadastroDto dto = new MedicaoMetaCadastroDto();
            dto.setMetaSetorId(1L);
            dto.setLoteId(1L);
            dto.setDataMedicao(LocalDate.now());
            dto.setQuantidadeLancada(100.0);

            String resultado = sMetaSetor.cadastrarMedicao(dto, cuidador.getEmail());

            assertThat(resultado).contains("cadastrada com sucesso");
        }

        @Test
        void cadastrarMedicao_deveLancarExcecao_QuandoMetaNaoEncontrada() {
            when(metaSetorInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.empty());

            MedicaoMetaCadastroDto dto = new MedicaoMetaCadastroDto();
            dto.setMetaSetorId(1L);
            dto.setLoteId(1L);

            assertThatThrownBy(() -> sMetaSetor.cadastrarMedicao(dto, cuidador.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Meta não encontrada");
        }

        @Test
        void validarEAtualizarMedicao_deveNegar_QuandoUsuarioSemPermissao() {
            stubUsuario(cuidador);
            EMedicaoMeta medicao = new EMedicaoMeta();
            medicao.setId(1L);
            medicao.setCriadoPorEmail("outro@fazenda.com");
            when(medicaoMetaInterface.findById(1L)).thenReturn(Optional.of(medicao));

            assertThatThrownBy(() -> sMetaSetor.validarEAtualizarMedicao(1L, new MedicaoMetaPutDto(), cuidador.getEmail()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void validarEDeletarMedicao_devePermitirAutor() {
            stubUsuario(cuidador);
            EMedicaoMeta medicao = new EMedicaoMeta();
            medicao.setId(1L);
            medicao.setCriadoPorEmail(cuidador.getEmail());
            when(medicaoMetaInterface.findById(1L)).thenReturn(Optional.of(medicao));

            String resultado = sMetaSetor.validarEDeletarMedicao(1L, cuidador.getEmail());

            assertThat(resultado).contains("removida com sucesso");
            verify(medicaoMetaInterface).deleteById(1L);
        }
    }

    @Nested
    class ProgressoTests {

        @Test
        void buscarPorId_deveCalcularProgressoEValoresParaMetaLeite() {
            EMetaSetor meta = new EMetaSetor();
            meta.setId(1L);
            meta.setSetor(setor);
            meta.setTipoMeta(EnTipoMeta.LEITE);
            meta.setQuantidadeEsperada(1000.0);
            meta.setPrecoMedio(2.0);
            when(metaSetorInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(meta));

            ELote lote = new ELote();
            lote.setId(1L);
            lote.setCodigo("LOT001");
            EMedicaoMeta medicao = new EMedicaoMeta();
            medicao.setId(1L);
            medicao.setLote(lote);
            medicao.setQuantidadeLancada(400.0);
            when(medicaoMetaInterface.findByMetaSetor_Id(1L)).thenReturn(List.of(medicao));

            EVendaMetaLote venda = new EVendaMetaLote();
            venda.setLitrosVendidos(200.0);
            when(vendaMetaLoteInterface.findByMetaSetor_Id(1L)).thenReturn(List.of(venda));

            MetaSetorRespostaDto resultado = sMetaSetor.buscarPorId(1L);

            assertThat(resultado.getQuantidadeRealizada()).isEqualTo(400.0);
            assertThat(resultado.getPercentualProgresso()).isEqualTo(40.0);
            assertThat(resultado.getValorRealizado()).isEqualTo(800.0);
            assertThat(resultado.getValorEsperado()).isEqualTo(2000.0);
            assertThat(resultado.getQuantidadeVendida()).isEqualTo(200.0);
            assertThat(resultado.getPercentualVendido()).isEqualTo(20.0);
        }

        @Test
        void buscarPorId_deveConverterKgParaArroba_QuandoMetaArroba() {
            EMetaSetor meta = new EMetaSetor();
            meta.setId(1L);
            meta.setSetor(setor);
            meta.setTipoMeta(EnTipoMeta.ARROBA);
            meta.setTipoGado(EnTipoGado.BOVINO_JOVEM_50);
            meta.setQuantidadeEsperada(100.0);
            meta.setPrecoMedio(300.0);
            when(metaSetorInterface.findByIdAndStatus(1L, EnStatus.A)).thenReturn(Optional.of(meta));

            ELote lote = new ELote();
            lote.setId(1L);
            lote.setCodigo("LOT001");
            EMedicaoMeta medicao = new EMedicaoMeta();
            medicao.setId(1L);
            medicao.setLote(lote);
            // 450 kg * 0.50 (taxa) / 15 (kg por arroba) = 15 arrobas
            medicao.setQuantidadeLancada(450.0);
            when(medicaoMetaInterface.findByMetaSetor_Id(1L)).thenReturn(List.of(medicao));

            MetaSetorRespostaDto resultado = sMetaSetor.buscarPorId(1L);

            assertThat(resultado.getQuantidadeRealizada()).isEqualTo(15.0);
        }
    }
}
