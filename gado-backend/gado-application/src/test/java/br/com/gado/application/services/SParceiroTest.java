package br.com.gado.application.services;

import br.com.gado.application.dto.parcerioDto.ParceiroCadastroDto;
import br.com.gado.application.dto.parcerioDto.ParceiroDto;
import br.com.gado.application.dto.parcerioDto.ParceiroPutDto;
import br.com.gado.domain.entities.EParceiro;
import br.com.gado.infrastructure.persistence.repositories.IParceiro;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SParceiroTest {

    @InjectMocks
    private SParceiro sParceiro;

    @Mock
    private IParceiro parceiroInterface;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private Configuration configuration;

    private EParceiro parceiroEntity;
    private ParceiroCadastroDto cadastroDto;
    private ParceiroPutDto putDto;
    private ParceiroDto parceiroDto;

    private final String CPF_CNPJ = "12345678900";

    @BeforeEach
    void setUp() {

        parceiroEntity = new EParceiro();
        parceiroEntity.setId(1L);
        parceiroEntity.setCpfCnpj(CPF_CNPJ);
        parceiroEntity.setDataCadastro(LocalDateTime.now());

        cadastroDto = new ParceiroCadastroDto();
        cadastroDto.setCPF_CNPJ(CPF_CNPJ);

        putDto = new ParceiroPutDto();

        parceiroDto = new ParceiroDto();
        parceiroDto.setCpfCnpj(CPF_CNPJ);
    }

    @Nested
    class BuscarParceiroTests {

        @Test
        void buscaPorCpfCnpj_DeveRetornarParceiro() {

            when(parceiroInterface.findByCpfCnpj(CPF_CNPJ))
                    .thenReturn(Optional.of(parceiroEntity));

            when(modelMapper.map(parceiroEntity, ParceiroDto.class))
                    .thenReturn(parceiroDto);

            ParceiroDto response =
                    sParceiro.buscaPorCPF_CNPJ(CPF_CNPJ);

            assertNotNull(response);
            assertEquals(CPF_CNPJ, response.getCpfCnpj());
        }

        @Test
        void buscaPorCpfCnpj_DeveLancarExcecao_QuandoNaoEncontrado() {

            when(parceiroInterface.findByCpfCnpj(CPF_CNPJ))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sParceiro.buscaPorCPF_CNPJ(CPF_CNPJ));
        }
    }

    @Nested
    class CadastroParceiroTests {

        @Test
        void cadastrar_DeveSalvarParceiroComSucesso() {

            when(parceiroInterface.existsByCpfCnpj(CPF_CNPJ))
                    .thenReturn(false);

            when(modelMapper.map(cadastroDto, EParceiro.class))
                    .thenReturn(parceiroEntity);

            when(parceiroInterface.save(any(EParceiro.class)))
                    .thenReturn(parceiroEntity);

            when(modelMapper.map(parceiroEntity, ParceiroDto.class))
                    .thenReturn(parceiroDto);

            ParceiroDto response =
                    sParceiro.cadastra(cadastroDto);

            assertNotNull(response);
            assertEquals(CPF_CNPJ, response.getCpfCnpj());

            verify(parceiroInterface, times(1))
                    .save(any(EParceiro.class));
        }

        @Test
        void cadastrar_DeveLancarExcecao_QuandoParceiroJaExiste() {

            when(parceiroInterface.existsByCpfCnpj(CPF_CNPJ))
                    .thenReturn(true);

            assertThrows(IllegalArgumentException.class,
                    () -> sParceiro.cadastra(cadastroDto));
        }
    }

    @Nested
    class DeletarParceiroTests {

        @Test
        void deletar_DeveRemoverParceiroComSucesso() {

            when(parceiroInterface.existsByCpfCnpj(CPF_CNPJ))
                    .thenReturn(true);

            String response =
                    sParceiro.deleta(CPF_CNPJ);

            assertEquals(
                    "Parceiro deletado com sucesso",
                    response
            );

            verify(parceiroInterface, times(1))
                    .deleteByCpfCnpj(CPF_CNPJ);
        }

        @Test
        void deletar_DeveRetornarMensagem_QuandoCpfCnpjNaoExiste() {

            when(parceiroInterface.existsByCpfCnpj(CPF_CNPJ))
                    .thenReturn(false);

            String response =
                    sParceiro.deleta(CPF_CNPJ);

            assertEquals(
                    "Esse cpf/cnpj não existe no banco de dados",
                    response
            );
        }

        @Test
        void deletar_DeveRetornarMensagemErro_QuandoDeleteFalhar() {

            when(parceiroInterface.existsByCpfCnpj(CPF_CNPJ))
                    .thenReturn(true);

            doThrow(new RuntimeException())
                    .when(parceiroInterface)
                    .deleteByCpfCnpj(CPF_CNPJ);

            String response =
                    sParceiro.deleta(CPF_CNPJ);

            assertEquals(
                    "Não é possível excluir ele pois ele possui vinculos com outras entidades",
                    response
            );
        }
    }

    @Nested
    class AlterarParceiroTests {

        @Test
        void alterar_DeveAtualizarParceiroComSucesso() {

            when(parceiroInterface.findByCpfCnpj(CPF_CNPJ))
                    .thenReturn(Optional.of(parceiroEntity));

            when(modelMapper.getConfiguration())
                    .thenReturn(configuration);

            when(configuration.setSkipNullEnabled(true))
                    .thenReturn(configuration);

            doNothing().when(modelMapper)
                    .map(any(ParceiroPutDto.class), any(EParceiro.class));

            when(parceiroInterface.save(any(EParceiro.class)))
                    .thenReturn(parceiroEntity);

            when(modelMapper.map(parceiroEntity, ParceiroDto.class))
                    .thenReturn(parceiroDto);

            ParceiroDto response =
                    sParceiro.altera(CPF_CNPJ, putDto);

            assertNotNull(response);

            verify(parceiroInterface, times(1))
                    .save(any(EParceiro.class));
        }

        @Test
        void alterar_DeveLancarExcecao_QuandoParceiroNaoEncontrado() {

            when(parceiroInterface.findByCpfCnpj(CPF_CNPJ))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> sParceiro.altera(CPF_CNPJ, putDto));
        }
    }
}