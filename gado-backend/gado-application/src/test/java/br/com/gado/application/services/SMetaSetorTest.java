package br.com.gado.application.services;

import br.com.gado.application.dto.metaSetorDto.MedicaoMetaCadastroDto;
import br.com.gado.application.dto.metaSetorDto.MetaSetorCadastroDto;
import br.com.gado.application.dto.metaSetorDto.MetaSetorPutDto;
import br.com.gado.application.dto.metaSetorDto.MetaSetorRespostaDto;
import br.com.gado.domain.entities.EMedicaoMeta;
import br.com.gado.domain.entities.EMetaSetor;
import br.com.gado.domain.entities.ESetor;
import br.com.gado.domain.entities.ELote;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnPerfilUsuario;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.domain.enums.EnTipoMeta;
import br.com.gado.domain.enums.EnTipoGado;
import br.com.gado.infrastructure.persistence.repositories.IMedicaoMeta;
import br.com.gado.infrastructure.persistence.repositories.IMetaSetor;
import br.com.gado.infrastructure.persistence.repositories.ISetor;
import br.com.gado.infrastructure.persistence.repositories.ILote;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SMetaSetorTest {

    @InjectMocks
    private SMetaSetor sMetaSetor;

    @Mock
    private IMetaSetor metaSetorInterface;
    @Mock
    private IMedicaoMeta medicaoMetaInterface;
    @Mock
    private ISetor setorInterface;
    @Mock
    private ILote loteInterface;
    @Mock
    private IUsuario usuarioInterface;

    private EUsuario usuarioAdmin;
    private EUsuario usuarioCaseiro;
    private ESetor setorEntity;
    private ELote loteEntity;
    private EMetaSetor metaLeite;
    private EMetaSetor metaArroba;
    private EMedicaoMeta medicaoLeite;
    private EMedicaoMeta medicaoArroba;

    private MetaSetorCadastroDto cadastroDto;
    private MetaSetorPutDto putDto;
    private MedicaoMetaCadastroDto medicaoDto;

    private final String EMAIL_ADMIN = "admin@gado.com";
    private final String EMAIL_CASEIRO = "caseiro@gado.com";
    private final Long ID_GENERICO = 1L;

    @BeforeEach
    void setUp() {
        usuarioAdmin = new EUsuario();
        usuarioAdmin.setEmail(EMAIL_ADMIN);
        usuarioAdmin.setPerfil(EnPerfilUsuario.ADMINISTRADOR);
        usuarioAdmin.setStatus(EnStatus.A);

        usuarioCaseiro = new EUsuario();
        usuarioCaseiro.setEmail(EMAIL_CASEIRO);
        usuarioCaseiro.setPerfil(EnPerfilUsuario.CUIDADOR);
        usuarioCaseiro.setStatus(EnStatus.A);

        setorEntity = new ESetor();
        setorEntity.setId(ID_GENERICO);
        setorEntity.setNome("Setor Norte");

        loteEntity = new ELote();
        loteEntity.setId(ID_GENERICO);
        loteEntity.setDescricao("Lote 01");

        metaLeite = new EMetaSetor();
        metaLeite.setId(10L);
        metaLeite.setSetor(setorEntity);
        metaLeite.setTipoMeta(EnTipoMeta.LEITE);
        metaLeite.setQuantidadeEsperada(1000.0); // 1000 Litros
        metaLeite.setPrecoMedio(2.50);
        metaLeite.setDataInicial(LocalDate.now());
        metaLeite.setDataFinal(LocalDate.now().plusDays(30));

        metaArroba = new EMetaSetor();
        metaArroba.setId(11L);
        metaArroba.setSetor(setorEntity);
        metaArroba.setTipoMeta(EnTipoMeta.ARROBA);
        metaArroba.setTipoGado(EnTipoGado.values()[0]); // Pega dinamicamente o primeiro Enum
        metaArroba.setQuantidadeEsperada(50.0);
        metaArroba.setPrecoMedio(300.0);
        metaArroba.setDataInicial(LocalDate.now());
        metaArroba.setDataFinal(LocalDate.now().plusDays(30));

        medicaoLeite = new EMedicaoMeta();
        medicaoLeite.setId(20L);
        medicaoLeite.setMetaSetor(metaLeite);
        medicaoLeite.setLote(loteEntity);
        medicaoLeite.setQuantidadeLancada(250.0); // 250 Litros

        medicaoArroba = new EMedicaoMeta();
        medicaoArroba.setId(21L);
        medicaoArroba.setMetaSetor(metaArroba);
        medicaoArroba.setLote(loteEntity);
        medicaoArroba.setQuantidadeLancada(600.0); // 600 Kg Peso Vivo

        cadastroDto = new MetaSetorCadastroDto();
        cadastroDto.setSetorId(ID_GENERICO);
        cadastroDto.setTipoMeta(EnTipoMeta.LEITE);
        cadastroDto.setDataInicial(LocalDate.now());
        cadastroDto.setDataFinal(LocalDate.now().plusDays(10));
        cadastroDto.setQuantidadeEsperada(500.0);
        cadastroDto.setPrecoMedio(3.0);

        putDto = new MetaSetorPutDto();
        putDto.setDataInicial(LocalDate.now());
        putDto.setDataFinal(LocalDate.now().plusDays(20));
        putDto.setQuantidadeEsperada(800.0);
        putDto.setPrecoMedio(3.2);

        medicaoDto = new MedicaoMetaCadastroDto();
        medicaoDto.setMetaSetorId(10L);
        medicaoDto.setLoteId(ID_GENERICO);
        medicaoDto.setDataMedicao(LocalDate.now());
        medicaoDto.setQuantidadeLancada(100.0);
    }

    @Nested
    class ValidacoesDeAcessoTests {
        @Test
        void validaAdminOuGerente_DeveLancarExcecao_QuandoEmailNuloOuVazio() {
            assertThrows(IllegalArgumentException.class, () -> sMetaSetor.validaAdminOuGerente(null));
            assertThrows(IllegalArgumentException.class, () -> sMetaSetor.validaAdminOuGerente("   "));
        }

        @Test
        void validaAdminOuGerente_DeveLancarExcecao_QuandoUsuarioNaoEncontrado() {
            when(usuarioInterface.findByEmailAndStatus("inexistente@gado.com", EnStatus.A)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> sMetaSetor.validaAdminOuGerente("inexistente@gado.com"));
        }

        @Test
        void validaAdminOuGerente_DeveLancarExcecao_QuandoPerfilInvalido() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_CASEIRO, EnStatus.A)).thenReturn(Optional.of(usuarioCaseiro));
            assertThrows(IllegalArgumentException.class, () -> sMetaSetor.validaAdminOuGerente(EMAIL_CASEIRO));
        }

        @Test
        void validaAdminOuGerente_DevePassar_QuandoAdmin() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_ADMIN, EnStatus.A)).thenReturn(Optional.of(usuarioAdmin));
            assertDoesNotThrow(() -> sMetaSetor.validaAdminOuGerente(EMAIL_ADMIN));
        }

        @Test
        void validaQualquerPerfil_DeveLancarExcecao_QuandoEmailInvalido() {
            assertThrows(IllegalArgumentException.class, () -> sMetaSetor.validaQualquerPerfil(null));
        }

        @Test
        void validaQualquerPerfil_DevePassar_QuandoUsuarioExiste() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_CASEIRO, EnStatus.A)).thenReturn(Optional.of(usuarioCaseiro));
            assertDoesNotThrow(() -> sMetaSetor.validaQualquerPerfil(EMAIL_CASEIRO));
        }
    }

    @Nested
    class MetaSetorCadastrarEAlterarTests {
        @Test
        void cadastrar_DeveLancarExcecao_QuandoDatasInvalidas() {
            cadastroDto.setDataInicial(LocalDate.now());
            cadastroDto.setDataFinal(LocalDate.now().minusDays(1)); // Data Final menor
            assertThrows(IllegalArgumentException.class, () -> sMetaSetor.cadastrar(cadastroDto));
        }

        @Test
        void cadastrar_DeveLancarExcecao_QuandoMetaArrobaSemTipoGado() {
            cadastroDto.setTipoMeta(EnTipoMeta.ARROBA);
            cadastroDto.setTipoGado(null);
            assertThrows(IllegalArgumentException.class, () -> sMetaSetor.cadastrar(cadastroDto));
        }

        @Test
        void cadastrar_DeveLancarExcecao_QuandoSetorNaoEncontrado() {
            when(setorInterface.findById(ID_GENERICO)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> sMetaSetor.cadastrar(cadastroDto));
        }

        @Test
        void cadastrar_DeveSalvarMetaLeite_EAnularTipoGado() {
            when(setorInterface.findById(ID_GENERICO)).thenReturn(Optional.of(setorEntity));
            cadastroDto.setTipoMeta(EnTipoMeta.LEITE);
            cadastroDto.setTipoGado(EnTipoGado.values()[0]); // Coloca um tipo gado que deve ser ignorado/anulado

            String msg = sMetaSetor.cadastrar(cadastroDto);

            assertEquals("Meta do setor cadastrada com sucesso.", msg);
            verify(metaSetorInterface, times(1)).save(argThat(meta -> meta.getTipoGado() == null));
        }

        @Test
        void cadastrar_DeveSalvarMetaArroba_ComTipoGado() {
            when(setorInterface.findById(ID_GENERICO)).thenReturn(Optional.of(setorEntity));
            cadastroDto.setTipoMeta(EnTipoMeta.ARROBA);
            cadastroDto.setTipoGado(EnTipoGado.values()[0]);

            String msg = sMetaSetor.cadastrar(cadastroDto);

            assertEquals("Meta do setor cadastrada com sucesso.", msg);
            verify(metaSetorInterface, times(1)).save(argThat(meta -> meta.getTipoGado() != null));
        }

        @Test
        void alterar_DeveLancarExcecao_QuandoMetaNaoEncontrada() {
            when(metaSetorInterface.findById(10L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> sMetaSetor.alterar(10L, putDto));
        }

        @Test
        void alterar_DeveLancarExcecao_QuandoTentarPorTipoGadoEmMetaLeite() {
            when(metaSetorInterface.findById(10L)).thenReturn(Optional.of(metaLeite));
            putDto.setTipoGado(EnTipoGado.values()[0]);

            assertThrows(IllegalArgumentException.class, () -> sMetaSetor.alterar(10L, putDto));
        }

        @Test
        void alterar_DeveLancarExcecao_QuandoCruzamentoDeDatasFicarInvalido() {
            when(metaSetorInterface.findById(11L)).thenReturn(Optional.of(metaArroba));
            putDto.setDataInicial(LocalDate.now().plusDays(10));
            putDto.setDataFinal(LocalDate.now().minusDays(5)); // Força data final menor

            assertThrows(IllegalArgumentException.class, () -> sMetaSetor.alterar(11L, putDto));
        }

        @Test
        void alterar_DeveAtualizarMetaArroba_ComSucesso() {
            when(metaSetorInterface.findById(11L)).thenReturn(Optional.of(metaArroba));
            putDto.setTipoGado(EnTipoGado.values()[0]);

            String msg = sMetaSetor.alterar(11L, putDto);

            assertEquals("Meta do setor atualizada com sucesso.", msg);
            verify(metaSetorInterface, times(1)).save(metaArroba);
        }
    }

    @Nested
    class ListagemEConversaoDeMedidasTests {
        @Test
        void listarPorSetor_DeveRetornarListagemCorreta() {
            when(metaSetorInterface.findBySetor_Id(ID_GENERICO)).thenReturn(List.of(metaLeite));
            when(medicaoMetaInterface.findByMetaSetor_Id(10L)).thenReturn(List.of());

            List<MetaSetorRespostaDto> lista = sMetaSetor.listarPorSetor(ID_GENERICO);

            assertFalse(lista.isEmpty());
            assertEquals(1, lista.size());
        }

        @Test
        void buscarPorId_DeveLancarExcecao_QuandoNaoEncontrado() {
            when(metaSetorInterface.findById(99L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> sMetaSetor.buscarPorId(99L));
        }

        @Test
        void buscarPorId_DeveCalcularCorretamente_MetaLeite() {
            when(metaSetorInterface.findById(10L)).thenReturn(Optional.of(metaLeite));
            when(medicaoMetaInterface.findByMetaSetor_Id(10L)).thenReturn(List.of(medicaoLeite));

            MetaSetorRespostaDto dto = sMetaSetor.buscarPorId(10L);

            // A quantidade lançada de leite (250) deve permanecer intacta
            assertEquals(250.0, dto.getQuantidadeRealizada());
            assertEquals(25.0, dto.getPercentualProgresso()); // 250 de 1000 = 25%
            assertEquals(2500.0, dto.getValorEsperado()); // 1000 * 2.50
            assertEquals(625.0, dto.getValorRealizado()); // 250 * 2.50
        }

        @Test
        void buscarPorId_DeveCalcularCorretamente_MetaArroba() {
            when(metaSetorInterface.findById(11L)).thenReturn(Optional.of(metaArroba));
            when(medicaoMetaInterface.findByMetaSetor_Id(11L)).thenReturn(List.of(medicaoArroba));

            MetaSetorRespostaDto dto = sMetaSetor.buscarPorId(11L);

            // Cálculo dinâmico para garantir que o teste passe com qualquer Enum
            double taxa = metaArroba.getTipoGado().getTaxaRendimento();
            double conversaoEsperada = (600.0 * taxa) / 15.0; // medicao de 600kg
            double percentualEsperado = (conversaoEsperada / 50.0) * 100.0; // meta de 50 arrobas

            // Verificamos com uma margem de erro pequena para arredondamentos do BigDecimal (delta 0.01)
            assertEquals(conversaoEsperada, dto.getQuantidadeRealizada(), 0.01);
            assertEquals(percentualEsperado, dto.getPercentualProgresso(), 0.01);
        }

        @Test
        void buscarPorId_DeveRetornarZeroNoPercentual_QuandoMetaEsperadaForZero() {
            metaLeite.setQuantidadeEsperada(0.0); // Forçando divisor zero
            when(metaSetorInterface.findById(10L)).thenReturn(Optional.of(metaLeite));
            when(medicaoMetaInterface.findByMetaSetor_Id(10L)).thenReturn(List.of(medicaoLeite));

            MetaSetorRespostaDto dto = sMetaSetor.buscarPorId(10L);
            assertEquals(0.0, dto.getPercentualProgresso()); // Validando a segurança contra divisão por zero
        }
    }

    @Nested
    class DelecaoTests {
        @Test
        void deletarMeta_NaoEncontrada_RetornaMensagem() {
            when(metaSetorInterface.existsById(10L)).thenReturn(false);
            assertEquals("Meta não encontrada para o ID: 10", sMetaSetor.deletar(10L));
        }

        @Test
        void deletarMeta_Encontrada_RemoveComSucesso() {
            when(metaSetorInterface.existsById(10L)).thenReturn(true);
            assertEquals("Meta do setor removida com sucesso.", sMetaSetor.deletar(10L));
            verify(metaSetorInterface, times(1)).deleteById(10L);
        }
    }

    @Nested
    class MedicaoMetaTests {
        @Test
        void cadastrarMedicao_LancaExcecao_MetaNaoEncontrada() {
            when(metaSetorInterface.findById(10L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> sMetaSetor.cadastrarMedicao(medicaoDto, "usuario@teste.com"));
        }

        @Test
        void cadastrarMedicao_LancaExcecao_LoteNaoEncontrado() {
            when(metaSetorInterface.findById(10L)).thenReturn(Optional.of(metaLeite));
            when(loteInterface.findById(ID_GENERICO)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> sMetaSetor.cadastrarMedicao(medicaoDto, "usuario@teste.com"));
        }

        @Test
        void cadastrarMedicao_SalvaComSucesso() {
            when(metaSetorInterface.findById(10L)).thenReturn(Optional.of(metaLeite));
            when(loteInterface.findById(ID_GENERICO)).thenReturn(Optional.of(loteEntity));

            String msg = sMetaSetor.cadastrarMedicao(medicaoDto, "usuario@teste.com");

            assertEquals("Medição cadastrada com sucesso.", msg);
            verify(medicaoMetaInterface, times(1)).save(any(EMedicaoMeta.class));
        }

        @Test
        void deletarMedicao_NaoEncontrada_RetornaMensagem() {
            when(medicaoMetaInterface.existsById(20L)).thenReturn(false);
            assertEquals("Medição não encontrada para o ID: 20", sMetaSetor.deletarMedicao(20L));
        }

        @Test
        void deletarMedicao_Encontrada_RemoveComSucesso() {
            when(medicaoMetaInterface.existsById(20L)).thenReturn(true);
            assertEquals("Medição removida com sucesso.", sMetaSetor.deletarMedicao(20L));
            verify(medicaoMetaInterface, times(1)).deleteById(20L);
        }
    }
}