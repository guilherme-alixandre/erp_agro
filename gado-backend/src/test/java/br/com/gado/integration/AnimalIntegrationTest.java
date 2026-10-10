package br.com.gado.integration;

import br.com.gado.GadoApplication;
import br.com.gado.dto.AnimalDto;
import br.com.gado.dto.ocorrenciaAnimalDto.OcorrenciaAnimalCadastroDto;
import br.com.gado.dto.ocorrenciaAnimalDto.OcorrenciaAnimalRespostaDto;
import br.com.gado.dto.racaDto.RacaCadastroDto;
import br.com.gado.dto.racaDto.RacaRespostaDto;
import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalCadastroDto;
import br.com.gado.dto.vacinacaoAnimalDto.VacinacaoAnimalRespostaDto;
import br.com.gado.entities.EGrupoProduto;
import br.com.gado.entities.EInsumo;
import br.com.gado.entities.EUnidadeMedida;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnNaturezaFinanceira;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnTipoInsumo;
import br.com.gado.enums.EnTipoOcorrencia;
import br.com.gado.repositories.IGrupoProduto;
import br.com.gado.repositories.IInsumo;
import br.com.gado.repositories.IUnidadeMedida;
import br.com.gado.repositories.IUsuario;
import br.com.gado.services.SAnimal;
import br.com.gado.services.SOcorrenciaAnimal;
import br.com.gado.services.SRaca;
import br.com.gado.services.SVacinacaoAnimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fluxo completo (sem mocks, contra H2) de cadastro de raça (com produto de catálogo associado)
 * -> cadastro de animal com código de brinco sequencial -> vacinação com baixa de estoque ->
 * registro de ocorrência.
 */
@SpringBootTest(classes = GadoApplication.class)
@ActiveProfiles("test")
@Transactional
class AnimalIntegrationTest {

    @Autowired private SRaca racaService;
    @Autowired private SAnimal animalService;
    @Autowired private SVacinacaoAnimal vacinacaoAnimalService;
    @Autowired private SOcorrenciaAnimal ocorrenciaAnimalService;

    @Autowired private IUsuario usuarioRepository;
    @Autowired private IGrupoProduto grupoProdutoRepository;
    @Autowired private IUnidadeMedida unidadeMedidaRepository;
    @Autowired private IInsumo insumoRepository;

    private EUsuario gerente;
    private EUsuario cuidador;

    @BeforeEach
    void setUp() {
        gerente = new EUsuario();
        gerente.setNome("Gerente IT");
        gerente.setEmail("gerente-it-" + System.nanoTime() + "@fazenda.com");
        gerente.setSenha("hash");
        gerente.setPerfil(EnPerfilUsuario.GERENTE);
        gerente = usuarioRepository.save(gerente);

        cuidador = new EUsuario();
        cuidador.setNome("Cuidador IT");
        cuidador.setEmail("cuidador-it-" + System.nanoTime() + "@fazenda.com");
        cuidador.setSenha("hash");
        cuidador.setPerfil(EnPerfilUsuario.CUIDADOR);
        cuidador = usuarioRepository.save(cuidador);

        EGrupoProduto grupoAnimais = new EGrupoProduto();
        grupoAnimais.setNome("Animais");
        grupoAnimais.setCodigoPrefixo("99");
        grupoAnimais.setCategoriaGrupo(EnTipoInsumo.ANIMAL);
        grupoAnimais.setNaturezaFinanceira(EnNaturezaFinanceira.CUSTO);
        grupoProdutoRepository.save(grupoAnimais);

        EUnidadeMedida unidadeCabeca = new EUnidadeMedida();
        unidadeCabeca.setUnidade("CABECA");
        unidadeMedidaRepository.save(unidadeCabeca);
    }

    @Test
    void deveCadastrarRacaAnimalEVacinacaoEOcorrencia_FluxoCompleto() {
        RacaCadastroDto racaDto = new RacaCadastroDto();
        racaDto.setNome("Nelore IT " + System.nanoTime());
        racaDto.setSigla("NI");
        RacaRespostaDto raca = racaService.criar(racaDto, gerente.getEmail());

        assertThat(raca.getProdutoId()).isNotNull();

        AnimalDto animalDto = new AnimalDto();
        animalDto.setRacaId(raca.getId());
        animalDto.setPesoAtual(220.0);
        AnimalDto animal;
        try {
            animal = animalService.cadastraAnimal(cuidador.getEmail(), animalDto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThat(animal.getCodigoBrinco()).startsWith("NI");
        assertThat(animal.getCodigoBrinco()).endsWith("0001");

        EUnidadeMedida unidadeMl = new EUnidadeMedida();
        unidadeMl.setUnidade("ML");
        unidadeMl = unidadeMedidaRepository.save(unidadeMl);

        EInsumo vacina = new EInsumo();
        vacina.setNome("Vacina Aftosa IT");
        vacina.setTipo(EnTipoInsumo.VACINA);
        vacina.setSaldoAtual(1000.0);
        vacina.setUnidadeMedidaPrimaria(unidadeMl);
        vacina = insumoRepository.save(vacina);

        VacinacaoAnimalCadastroDto vacinacaoDto = new VacinacaoAnimalCadastroDto();
        vacinacaoDto.setInsumoId(vacina.getId());
        vacinacaoDto.setQuantidadePorAnimal(5.0);
        vacinacaoDto.setAnimalIds(List.of(buscarIdDoAnimal(animal.getCodigoBrinco())));

        VacinacaoAnimalRespostaDto vacinacaoResposta =
                vacinacaoAnimalService.registrarAplicacao(vacinacaoDto, cuidador.getEmail());

        assertThat(vacinacaoResposta.getQuantidadeTotalBaixaUnidadePrimaria()).isEqualTo(5.0);
        EInsumo vacinaAtualizada = insumoRepository.findById(vacina.getId()).orElseThrow();
        assertThat(vacinaAtualizada.getSaldoAtual()).isEqualTo(995.0);

        OcorrenciaAnimalCadastroDto ocorrenciaDto = new OcorrenciaAnimalCadastroDto();
        ocorrenciaDto.setAnimalId(buscarIdDoAnimal(animal.getCodigoBrinco()));
        ocorrenciaDto.setTipoOcorrencia(EnTipoOcorrencia.PESAGEM);
        ocorrenciaDto.setDataOcorrencia(new Date());
        ocorrenciaDto.setObservacao("Pesagem mensal");

        OcorrenciaAnimalRespostaDto ocorrencia = ocorrenciaAnimalService.criarOcorrenciaPorAnimalId(ocorrenciaDto);
        assertThat(ocorrencia.getId()).isNotNull();

        List<OcorrenciaAnimalRespostaDto> ocorrencias =
                ocorrenciaAnimalService.listarPorAnimal(buscarIdDoAnimal(animal.getCodigoBrinco()));
        assertThat(ocorrencias).hasSize(1);
        assertThat(ocorrencias.get(0).getTipoOcorrencia()).isEqualTo(EnTipoOcorrencia.PESAGEM);
    }

    private Long buscarIdDoAnimal(String codigoBrinco) {
        return animalService.buscarPorBrinco(codigoBrinco).getId();
    }
}
