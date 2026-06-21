package br.com.gado.application.services;

import br.com.gado.application.dto.metaSetorDto.MedicaoMetaCadastroDto;
import br.com.gado.application.dto.metaSetorDto.MedicaoMetaPutDto;
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
import org.mockito.ArgumentCaptor;
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

        @Test
        void validaQualquerPerfil_DeveLancarExcecao_QuandoEmailBlank() {
            assertThrows(IllegalArgumentException.class, () -> sMetaSetor.validaQualquerPerfil("   "));
        }

        @Test
        void validaQualquerPerfil_DeveLancarExcecao_QuandoUsuarioNaoEncontrado() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_CASEIRO, EnStatus.A)).thenReturn(Optional.empty());
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sMetaSetor.validaQualquerPerfil(EMAIL_CASEIRO));
            assertEquals("Usuário não encontrado.", ex.getMessage());
        }

        @Test
        void validaAdminOuGerente_DevePassar_QuandoGerente() {
            usuarioAdmin.setPerfil(EnPerfilUsuario.GERENTE);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_ADMIN, EnStatus.A)).thenReturn(Optional.of(usuarioAdmin));
            assertDoesNotThrow(() -> sMetaSetor.validaAdminOuGerente(EMAIL_ADMIN));
        }

        @Test
        void validaEdicaoMedicao_DeveLancarExcecao_QuandoEmailNuloOuBlank() {
            assertThrows(IllegalArgumentException.class, () -> sMetaSetor.validaEdicaoMedicao(null, medicaoLeite));
            assertThrows(IllegalArgumentException.class, () -> sMetaSetor.validaEdicaoMedicao("   ", medicaoLeite));
        }

        @Test
        void validaEdicaoMedicao_DeveLancarExcecao_QuandoUsuarioNaoEncontrado() {
            when(usuarioInterface.findByEmailAndStatus(EMAIL_ADMIN, EnStatus.A)).thenReturn(Optional.empty());
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sMetaSetor.validaEdicaoMedicao(EMAIL_ADMIN, medicaoLeite));
            assertEquals("Usuário não encontrado.", ex.getMessage());
        }

        @Test
        void validaEdicaoMedicao_DevePassar_QuandoGerenteOuCuidadorChefe() {
            EUsuario gerente = new EUsuario();
            gerente.setEmail("gerente@gado.com");
            gerente.setPerfil(EnPerfilUsuario.GERENTE);
            when(usuarioInterface.findByEmailAndStatus("gerente@gado.com", EnStatus.A)).thenReturn(Optional.of(gerente));
            assertDoesNotThrow(() -> sMetaSetor.validaEdicaoMedicao("gerente@gado.com", medicaoLeite));

            EUsuario cuidadorChefe = new EUsuario();
            cuidadorChefe.setEmail("chefe@gado.com");
            cuidadorChefe.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);
            when(usuarioInterface.findByEmailAndStatus("chefe@gado.com", EnStatus.A)).thenReturn(Optional.of(cuidadorChefe));
            assertDoesNotThrow(() -> sMetaSetor.validaEdicaoMedicao("chefe@gado.com", medicaoLeite));
        }

        @Test
        void validaEdicaoMedicao_DeveLancarExcecao_QuandoCuidadorEMedicaoSemCriadoPorEmail() {
            medicaoLeite.setCriadoPorEmail(null);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_CASEIRO, EnStatus.A)).thenReturn(Optional.of(usuarioCaseiro));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sMetaSetor.validaEdicaoMedicao(EMAIL_CASEIRO, medicaoLeite));
            assertEquals("Você só pode editar medições que você mesmo criou.", ex.getMessage());
        }

        @Test
        void validaEdicaoMedicao_DeveLancarExcecao_QuandoPerfilNaoPermitido() {
            EUsuario financeiro = new EUsuario();
            financeiro.setEmail("fin@gado.com");
            financeiro.setPerfil(EnPerfilUsuario.FINANCEIRO);
            when(usuarioInterface.findByEmailAndStatus("fin@gado.com", EnStatus.A)).thenReturn(Optional.of(financeiro));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sMetaSetor.validaEdicaoMedicao("fin@gado.com", medicaoLeite));
            assertEquals("Seu perfil não permite editar medições.", ex.getMessage());
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

        @Test
        void alterar_NaoDeveAlterarNenhumCampo_QuandoDtoForVazio() {
            when(metaSetorInterface.findById(11L)).thenReturn(Optional.of(metaArroba));

            LocalDate dataInicialOriginal = metaArroba.getDataInicial();
            LocalDate dataFinalOriginal = metaArroba.getDataFinal();
            Double quantidadeOriginal = metaArroba.getQuantidadeEsperada();
            Double precoOriginal = metaArroba.getPrecoMedio();
            var tipoGadoOriginal = metaArroba.getTipoGado();

            String msg = sMetaSetor.alterar(11L, new MetaSetorPutDto());

            assertEquals("Meta do setor atualizada com sucesso.", msg);
            assertEquals(dataInicialOriginal, metaArroba.getDataInicial());
            assertEquals(dataFinalOriginal, metaArroba.getDataFinal());
            assertEquals(quantidadeOriginal, metaArroba.getQuantidadeEsperada());
            assertEquals(precoOriginal, metaArroba.getPrecoMedio());
            assertEquals(tipoGadoOriginal, metaArroba.getTipoGado());
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

        @Test
        void buscarPorId_DeveIncluirNomeDoCriador_QuandoMedicaoTemCriadoPorEmail() {
            medicaoLeite.setCriadoPorEmail(EMAIL_CASEIRO);
            usuarioCaseiro.setNome("Caseiro Teste");
            when(metaSetorInterface.findById(10L)).thenReturn(Optional.of(metaLeite));
            when(medicaoMetaInterface.findByMetaSetor_Id(10L)).thenReturn(List.of(medicaoLeite));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_CASEIRO, EnStatus.A)).thenReturn(Optional.of(usuarioCaseiro));

            MetaSetorRespostaDto dto = sMetaSetor.buscarPorId(10L);

            assertEquals("Caseiro Teste", dto.getMedicoes().get(0).getCriadoPorNome());
        }

        @Test
        void buscarPorId_DeveRetornarNomeNulo_QuandoMedicaoSemCriadoPorEmail() {
            when(metaSetorInterface.findById(10L)).thenReturn(Optional.of(metaLeite));
            when(medicaoMetaInterface.findByMetaSetor_Id(10L)).thenReturn(List.of(medicaoLeite));

            MetaSetorRespostaDto dto = sMetaSetor.buscarPorId(10L);

            assertNull(dto.getMedicoes().get(0).getCriadoPorNome());
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
        void cadastrarMedicao_DeveSalvarComCriadoPorEmailNulo_QuandoEmailCriadorForNulo() {
            when(metaSetorInterface.findById(10L)).thenReturn(Optional.of(metaLeite));
            when(loteInterface.findById(ID_GENERICO)).thenReturn(Optional.of(loteEntity));

            ArgumentCaptor<EMedicaoMeta> captor = ArgumentCaptor.forClass(EMedicaoMeta.class);

            String msg = sMetaSetor.cadastrarMedicao(medicaoDto, null);

            assertEquals("Medição cadastrada com sucesso.", msg);
            verify(medicaoMetaInterface).save(captor.capture());
            assertNull(captor.getValue().getCriadoPorEmail());
        }

        @Test
        void deletarMedicao_LancaExcecao_QuandoNaoEncontrada() {
            when(medicaoMetaInterface.findById(20L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class,
                    () -> sMetaSetor.validarEDeletarMedicao(20L, EMAIL_ADMIN));
        }

        @Test
        void deletarMedicao_RemoveComSucesso_QuandoAdmin() {
            medicaoLeite.setCriadoPorEmail(EMAIL_CASEIRO);
            when(medicaoMetaInterface.findById(20L)).thenReturn(Optional.of(medicaoLeite));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_ADMIN, EnStatus.A)).thenReturn(Optional.of(usuarioAdmin));

            String msg = sMetaSetor.validarEDeletarMedicao(20L, EMAIL_ADMIN);

            assertEquals("Medição removida com sucesso.", msg);
            verify(medicaoMetaInterface, times(1)).deleteById(20L);
        }

        @Test
        void deletarMedicao_RemoveComSucesso_QuandoCuidadorExcluiPropriaMedicao() {
            medicaoLeite.setCriadoPorEmail(EMAIL_CASEIRO);
            when(medicaoMetaInterface.findById(20L)).thenReturn(Optional.of(medicaoLeite));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_CASEIRO, EnStatus.A)).thenReturn(Optional.of(usuarioCaseiro));

            String msg = sMetaSetor.validarEDeletarMedicao(20L, EMAIL_CASEIRO);

            assertEquals("Medição removida com sucesso.", msg);
            verify(medicaoMetaInterface, times(1)).deleteById(20L);
        }

        @Test
        void deletarMedicao_LancaExcecao_QuandoCuidadorTentaExcluirMedicaoDeOutro() {
            medicaoLeite.setCriadoPorEmail(EMAIL_ADMIN);
            when(medicaoMetaInterface.findById(20L)).thenReturn(Optional.of(medicaoLeite));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_CASEIRO, EnStatus.A)).thenReturn(Optional.of(usuarioCaseiro));

            assertThrows(IllegalArgumentException.class,
                    () -> sMetaSetor.validarEDeletarMedicao(20L, EMAIL_CASEIRO));
            verify(medicaoMetaInterface, never()).deleteById(any());
        }
    }

    @Nested
    class ValidaEdicaoMedicaoAdicionaisTests {

        @Test
        void validaEdicaoMedicao_DeveLancarExcecao_QuandoCuidadorChefeAlteraMedicaoCriadaPorAdmin() {
            EUsuario cuidadorChefe = new EUsuario();
            cuidadorChefe.setEmail("chefe@gado.com");
            cuidadorChefe.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);

            EUsuario adminCriador = new EUsuario();
            adminCriador.setEmail(EMAIL_ADMIN);
            adminCriador.setPerfil(EnPerfilUsuario.ADMINISTRADOR);

            medicaoLeite.setCriadoPorEmail(EMAIL_ADMIN);

            when(usuarioInterface.findByEmailAndStatus("chefe@gado.com", EnStatus.A))
                    .thenReturn(Optional.of(cuidadorChefe));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_ADMIN, EnStatus.A))
                    .thenReturn(Optional.of(adminCriador));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sMetaSetor.validaEdicaoMedicao("chefe@gado.com", medicaoLeite));
            assertEquals("Cuidadores Chefe não podem alterar medições criadas por Administradores ou Gerentes.", ex.getMessage());
        }

        @Test
        void validaEdicaoMedicao_DeveLancarExcecao_QuandoCuidadorChefeAlteraMedicaoCriadaPorGerente() {
            EUsuario cuidadorChefe = new EUsuario();
            cuidadorChefe.setEmail("chefe@gado.com");
            cuidadorChefe.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);

            EUsuario gerente = new EUsuario();
            gerente.setEmail("gerente@gado.com");
            gerente.setPerfil(EnPerfilUsuario.GERENTE);

            medicaoLeite.setCriadoPorEmail("gerente@gado.com");

            when(usuarioInterface.findByEmailAndStatus("chefe@gado.com", EnStatus.A))
                    .thenReturn(Optional.of(cuidadorChefe));
            when(usuarioInterface.findByEmailAndStatus("gerente@gado.com", EnStatus.A))
                    .thenReturn(Optional.of(gerente));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sMetaSetor.validaEdicaoMedicao("chefe@gado.com", medicaoLeite));
            assertEquals("Cuidadores Chefe não podem alterar medições criadas por Administradores ou Gerentes.", ex.getMessage());
        }

        @Test
        void validaEdicaoMedicao_DevePermitir_QuandoCuidadorChefeAlteraMedicaoCriadaPorCuidador() {
            EUsuario cuidadorChefe = new EUsuario();
            cuidadorChefe.setEmail("chefe@gado.com");
            cuidadorChefe.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);

            EUsuario cuidador = new EUsuario();
            cuidador.setEmail("cuidador@gado.com");
            cuidador.setPerfil(EnPerfilUsuario.CUIDADOR);

            medicaoLeite.setCriadoPorEmail("cuidador@gado.com");

            when(usuarioInterface.findByEmailAndStatus("chefe@gado.com", EnStatus.A))
                    .thenReturn(Optional.of(cuidadorChefe));
            when(usuarioInterface.findByEmailAndStatus("cuidador@gado.com", EnStatus.A))
                    .thenReturn(Optional.of(cuidador));

            assertDoesNotThrow(() -> sMetaSetor.validaEdicaoMedicao("chefe@gado.com", medicaoLeite));
        }

        @Test
        void validaEdicaoMedicao_DevePermitir_QuandoCuidadorChefeECriadoPorEmailNuloOuSemPerfil() {
            // resolverPerfilPorEmail returns null when email is null => not ADMIN/GERENTE => allowed
            EUsuario cuidadorChefe = new EUsuario();
            cuidadorChefe.setEmail("chefe@gado.com");
            cuidadorChefe.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);

            medicaoLeite.setCriadoPorEmail(null);

            when(usuarioInterface.findByEmailAndStatus("chefe@gado.com", EnStatus.A))
                    .thenReturn(Optional.of(cuidadorChefe));

            // null email => resolverPerfilPorEmail returns null => no throw
            assertDoesNotThrow(() -> sMetaSetor.validaEdicaoMedicao("chefe@gado.com", medicaoLeite));
        }

        @Test
        void validaEdicaoMedicao_DevePermitirCuidador_QuandoEmailConfere() {
            // CUIDADOR + email matches criadoPorEmail => ok
            medicaoLeite.setCriadoPorEmail(EMAIL_CASEIRO);
            when(usuarioInterface.findByEmailAndStatus(EMAIL_CASEIRO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioCaseiro));

            assertDoesNotThrow(() -> sMetaSetor.validaEdicaoMedicao(EMAIL_CASEIRO, medicaoLeite));
        }
    }

    @Nested
    class ResolverNomePorEmailAdicionaisTests {

        @Test
        void buscarPorId_DeveRetornarNomeNulo_QuandoEmailNaoEncontradoNoBanco() {
            // email presente na medicao mas usuario nao existe => resolverNomePorEmail retorna null
            medicaoLeite.setCriadoPorEmail("naoexiste@gado.com");
            when(metaSetorInterface.findById(10L)).thenReturn(Optional.of(metaLeite));
            when(medicaoMetaInterface.findByMetaSetor_Id(10L)).thenReturn(List.of(medicaoLeite));
            when(usuarioInterface.findByEmailAndStatus("naoexiste@gado.com", EnStatus.A))
                    .thenReturn(Optional.empty());

            MetaSetorRespostaDto dto = sMetaSetor.buscarPorId(10L);

            assertNull(dto.getMedicoes().get(0).getCriadoPorNome());
            assertNull(dto.getMedicoes().get(0).getCriadoPorPerfil());
        }

        @Test
        void buscarPorId_DeveRetornarPerfilNulo_QuandoCriadoPorEmailForBlank() {
            // criadoPorEmail blank => perfisPorEmail key is "", isBlank => returns null
            medicaoLeite.setCriadoPorEmail("");
            when(metaSetorInterface.findById(10L)).thenReturn(Optional.of(metaLeite));
            when(medicaoMetaInterface.findByMetaSetor_Id(10L)).thenReturn(List.of(medicaoLeite));

            MetaSetorRespostaDto dto = sMetaSetor.buscarPorId(10L);

            assertNull(dto.getMedicoes().get(0).getCriadoPorPerfil());
        }

        @Test
        void buscarPorId_DeveIncluirPerfilDoCriador_QuandoUsuarioEncontrado() {
            medicaoLeite.setCriadoPorEmail(EMAIL_CASEIRO);
            usuarioCaseiro.setNome("Caseiro");
            when(metaSetorInterface.findById(10L)).thenReturn(Optional.of(metaLeite));
            when(medicaoMetaInterface.findByMetaSetor_Id(10L)).thenReturn(List.of(medicaoLeite));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_CASEIRO, EnStatus.A))
                    .thenReturn(Optional.of(usuarioCaseiro));

            MetaSetorRespostaDto dto = sMetaSetor.buscarPorId(10L);

            assertEquals(EnPerfilUsuario.CUIDADOR.name(), dto.getMedicoes().get(0).getCriadoPorPerfil());
        }

        @Test
        void alterar_DeveAtualizarSomenteDataFinal_QuandoApenasDataFinalInformada() {
            when(metaSetorInterface.findById(11L)).thenReturn(Optional.of(metaArroba));
            MetaSetorPutDto dto = new MetaSetorPutDto();
            LocalDate novaDataFinal = LocalDate.now().plusDays(60);
            dto.setDataFinal(novaDataFinal);

            String msg = sMetaSetor.alterar(11L, dto);

            assertEquals("Meta do setor atualizada com sucesso.", msg);
            assertEquals(novaDataFinal, metaArroba.getDataFinal());
        }

        @Test
        void alterar_NaoDeveLancarExcecao_QuandoDataInicialForNulaAposEdicao() {
            // meta with dataInicial = null => second && short-circuits => no exception
            metaArroba.setDataInicial(null);
            when(metaSetorInterface.findById(11L)).thenReturn(Optional.of(metaArroba));
            MetaSetorPutDto dto = new MetaSetorPutDto();
            dto.setDataFinal(LocalDate.now().plusDays(10));

            String msg = sMetaSetor.alterar(11L, dto);

            assertEquals("Meta do setor atualizada com sucesso.", msg);
        }

        @Test
        void alterar_NaoDeveLancarExcecao_QuandoDataFinalForNulaAposEdicao() {
            // meta with dataFinal = null => first && short-circuits => no exception
            metaArroba.setDataFinal(null);
            when(metaSetorInterface.findById(11L)).thenReturn(Optional.of(metaArroba));

            String msg = sMetaSetor.alterar(11L, new MetaSetorPutDto());

            assertEquals("Meta do setor atualizada com sucesso.", msg);
        }

        @Test
        void buscarPorId_ComEmailBlank_DeveRetornarPerfilNuloViaCriadoPorEmailBlank() {
            // resolverPerfilPorEmail with blank email returns null => criadoPorPerfil = null
            medicaoLeite.setCriadoPorEmail("   ");
            when(metaSetorInterface.findById(10L)).thenReturn(Optional.of(metaLeite));
            when(medicaoMetaInterface.findByMetaSetor_Id(10L)).thenReturn(List.of(medicaoLeite));

            MetaSetorRespostaDto dto = sMetaSetor.buscarPorId(10L);

            assertNull(dto.getMedicoes().get(0).getCriadoPorPerfil());
        }

        @Test
        void validaEdicaoMedicao_DevePermitir_QuandoCuidadorChefeECriadoPorEmailBlank() {
            // resolverPerfilPorEmail called directly with blank email => return null => not ADMIN/GERENTE => allowed
            EUsuario cuidadorChefe = new EUsuario();
            cuidadorChefe.setEmail("chefe@gado.com");
            cuidadorChefe.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);

            medicaoLeite.setCriadoPorEmail("   "); // blank email

            when(usuarioInterface.findByEmailAndStatus("chefe@gado.com", EnStatus.A))
                    .thenReturn(Optional.of(cuidadorChefe));

            // resolverPerfilPorEmail("   ") => isBlank => return null => not ADMIN/GERENTE => no throw
            assertDoesNotThrow(() -> sMetaSetor.validaEdicaoMedicao("chefe@gado.com", medicaoLeite));
        }
    }

    @Nested
    class ValidarEAtualizarMedicaoTests {
        @Test
        void deveLancarExcecao_QuandoMedicaoNaoEncontrada() {
            when(medicaoMetaInterface.findById(20L)).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sMetaSetor.validarEAtualizarMedicao(20L, new MedicaoMetaPutDto(), EMAIL_ADMIN));
            assertEquals("Medição não encontrada para o ID: 20", ex.getMessage());
        }

        @Test
        void deveLancarExcecao_QuandoUsuarioSemPermissao() {
            medicaoLeite.setCriadoPorEmail(EMAIL_ADMIN);
            when(medicaoMetaInterface.findById(20L)).thenReturn(Optional.of(medicaoLeite));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_CASEIRO, EnStatus.A)).thenReturn(Optional.of(usuarioCaseiro));

            assertThrows(IllegalArgumentException.class,
                    () -> sMetaSetor.validarEAtualizarMedicao(20L, new MedicaoMetaPutDto(), EMAIL_CASEIRO));
            verify(medicaoMetaInterface, never()).save(any());
        }

        @Test
        void deveLancarExcecao_QuandoNovoLoteNaoEncontrado() {
            when(medicaoMetaInterface.findById(20L)).thenReturn(Optional.of(medicaoLeite));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_ADMIN, EnStatus.A)).thenReturn(Optional.of(usuarioAdmin));
            when(loteInterface.findById(99L)).thenReturn(Optional.empty());

            MedicaoMetaPutDto dto = new MedicaoMetaPutDto();
            dto.setLoteId(99L);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> sMetaSetor.validarEAtualizarMedicao(20L, dto, EMAIL_ADMIN));
            assertEquals("Lote não encontrado para o ID: 99", ex.getMessage());
        }

        @Test
        void deveAtualizarTodosOsCampos_QuandoInformados() {
            ELote novoLote = new ELote();
            novoLote.setId(77L);

            when(medicaoMetaInterface.findById(20L)).thenReturn(Optional.of(medicaoLeite));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_ADMIN, EnStatus.A)).thenReturn(Optional.of(usuarioAdmin));
            when(loteInterface.findById(77L)).thenReturn(Optional.of(novoLote));

            MedicaoMetaPutDto dto = new MedicaoMetaPutDto();
            dto.setLoteId(77L);
            LocalDate novaData = LocalDate.now().plusDays(5);
            dto.setDataMedicao(novaData);
            dto.setQuantidadeLancada(123.0);

            String msg = sMetaSetor.validarEAtualizarMedicao(20L, dto, EMAIL_ADMIN);

            assertEquals("Medição atualizada com sucesso.", msg);
            assertEquals(novoLote, medicaoLeite.getLote());
            assertEquals(novaData, medicaoLeite.getDataMedicao());
            assertEquals(123.0, medicaoLeite.getQuantidadeLancada());
            verify(medicaoMetaInterface, times(1)).save(medicaoLeite);
        }

        @Test
        void naoDeveAlterarCampos_QuandoDtoForVazio() {
            when(medicaoMetaInterface.findById(20L)).thenReturn(Optional.of(medicaoLeite));
            when(usuarioInterface.findByEmailAndStatus(EMAIL_ADMIN, EnStatus.A)).thenReturn(Optional.of(usuarioAdmin));

            ELote loteOriginal = medicaoLeite.getLote();
            Double quantidadeOriginal = medicaoLeite.getQuantidadeLancada();

            String msg = sMetaSetor.validarEAtualizarMedicao(20L, new MedicaoMetaPutDto(), EMAIL_ADMIN);

            assertEquals("Medição atualizada com sucesso.", msg);
            assertEquals(loteOriginal, medicaoLeite.getLote());
            assertNull(medicaoLeite.getDataMedicao());
            assertEquals(quantidadeOriginal, medicaoLeite.getQuantidadeLancada());
            verify(loteInterface, never()).findById(any());
        }
    }
}