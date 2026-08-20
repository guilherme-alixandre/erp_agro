package br.com.gado.config;

import br.com.gado.entities.*;
import br.com.gado.enums.*;
import br.com.gado.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Popula o banco com dados de teste ao iniciar em ambiente de desenvolvimento.
 *
 * Ativação: já vem ligado por padrão via spring.profiles.active=dev em application.properties.
 * Para desativar (ex: ambiente de produção), remova essa linha ou sobrescreva o perfil
 * ativo por variável de ambiente/VM option.
 *
 * O guard count() == 1 garante que o seed só roda uma vez, pois a migration
 * Flyway já cria exatamente 1 usuário administrador padrão no banco vazio.
 */
@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final IUsuario iUsuario;
    private final ISetor iSetor;
    private final IAnimal iAnimal;
    private final ILote iLote;
    private final ILoteSetor iLoteSetor;
    private final IUnidadeMedida iUnidadeMedida;
    private final IInsumo iInsumo;
    private final IGrupoProduto iGrupoProduto;

    public DataInitializer(
            IUsuario iUsuario,
            ISetor iSetor,
            IAnimal iAnimal,
            ILote iLote,
            ILoteSetor iLoteSetor,
            IUnidadeMedida iUnidadeMedida,
            IInsumo iInsumo,
            IGrupoProduto iGrupoProduto) {
        this.iUsuario = iUsuario;
        this.iSetor = iSetor;
        this.iAnimal = iAnimal;
        this.iLote = iLote;
        this.iLoteSetor = iLoteSetor;
        this.iUnidadeMedida = iUnidadeMedida;
        this.iInsumo = iInsumo;
        this.iGrupoProduto = iGrupoProduto;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (iUsuario.count() != 1) {
            return;
        }
        seed();
    }

    private void seed() {

        // ── 1. USUÁRIOS ──────────────────────────────────────────────────────────────
        // Senha padrão para todos os usuários de teste: Seed@1234

        EUsuario gerente = new EUsuario();
        gerente.setNome("Carlos Mendes");
        gerente.setEmail("carlos.gerente@gadoseed.com");
        gerente.setSenha(sha256("Seed@1234"));
        gerente.setPerfil(EnPerfilUsuario.GERENTE);
        gerente.setDataCadastro(LocalDateTime.now());
        gerente = iUsuario.save(gerente);

        EUsuario cuidador = new EUsuario();
        cuidador.setNome("Ana Silva");
        cuidador.setEmail("ana.cuidadora@gadoseed.com");
        cuidador.setSenha(sha256("Seed@1234"));
        cuidador.setPerfil(EnPerfilUsuario.CUIDADOR);
        cuidador.setDataCadastro(LocalDateTime.now());
        cuidador = iUsuario.save(cuidador);

        EUsuario adminSeed = new EUsuario();
        adminSeed.setNome("João Administrador");
        adminSeed.setEmail("joao.admin@gadoseed.com");
        adminSeed.setSenha(sha256("Seed@1234"));
        adminSeed.setPerfil(EnPerfilUsuario.ADMINISTRADOR);
        adminSeed.setDataCadastro(LocalDateTime.now());
        adminSeed = iUsuario.save(adminSeed);

        // ── 2. SETORES ────────────────────────────────────────────────────────────────

        ESetor setorPasto = new ESetor();
        setorPasto.setNome("Pasto Principal");
        setorPasto.setCapacidadeMaxima(50);
        setorPasto.setTipo(EnTipoSetor.PASTO);
        setorPasto.setMetaProducaoLeite(200.0);
        setorPasto.setCriadoPor(gerente);
        setorPasto.setAlteradoPor(gerente);
        setorPasto = iSetor.save(setorPasto);

        ESetor setorConfinamento = new ESetor();
        setorConfinamento.setNome("Confinamento Engorda");
        setorConfinamento.setCapacidadeMaxima(30);
        setorConfinamento.setTipo(EnTipoSetor.CONFINAMENTO);
        setorConfinamento.setMetaArrobaAbate(15.0);
        setorConfinamento.setMetaTexto("Setor de engorda intensiva para abate. Dieta de alto concentrado com silagem de milho.");
        setorConfinamento.setCriadoPor(gerente);
        setorConfinamento.setAlteradoPor(adminSeed);
        setorConfinamento = iSetor.save(setorConfinamento);

        ESetor setorGalpao = new ESetor();
        setorGalpao.setNome("Galpão Maternidade");
        setorGalpao.setCapacidadeMaxima(15);
        setorGalpao.setTipo(EnTipoSetor.GALPAO);
        setorGalpao.setMetaTexto("Área reservada para vacas em período de gestação e amamentação. Monitoramento diário obrigatório.");
        setorGalpao.setCriadoPor(adminSeed);
        setorGalpao.setAlteradoPor(adminSeed);
        setorGalpao = iSetor.save(setorGalpao);

        // Setor salvo mas sem alocação neste seed (demonstra capacidade extra no sistema)
        ESetor setorPatio = new ESetor();
        setorPatio.setNome("Pátio de Triagem");
        setorPatio.setCapacidadeMaxima(20);
        setorPatio.setTipo(EnTipoSetor.PATIO);
        setorPatio.setCriadoPor(adminSeed);
        setorPatio.setAlteradoPor(adminSeed);
        iSetor.save(setorPatio);

        // ── 3. ANIMAIS ────────────────────────────────────────────────────────────────

        EAnimal boi1 = new EAnimal();
        boi1.setCodigoBrinco("BR001");
        boi1.setNome("Trovão");
        boi1.setSexo(EnSexoAnimal.M);
        boi1.setRaca("Nelore");
        boi1.setCor("Branca");
        boi1.setPesoAtual(480.0);
        boi1.setAlturaCernelha(1.45);
        boi1.setPerimetroToracico(1.92);
        boi1.setComprimentoCorporal(1.55);
        boi1.setDataNascimento(LocalDateTime.of(2021, 3, 10, 0, 0));
        boi1.setStatusAnimal(EnStatusAnimal.ATIVO);
        boi1.setUsuario(cuidador);
        boi1 = iAnimal.save(boi1);

        EAnimal boi2 = new EAnimal();
        boi2.setCodigoBrinco("BR002");
        boi2.setNome("Tempestade");
        boi2.setSexo(EnSexoAnimal.M);
        boi2.setRaca("Angus");
        boi2.setCor("Preta");
        boi2.setPesoAtual(520.0);
        boi2.setAlturaCernelha(1.50);
        boi2.setPerimetroToracico(2.05);
        boi2.setComprimentoCorporal(1.60);
        boi2.setDataNascimento(LocalDateTime.of(2020, 8, 22, 0, 0));
        boi2.setStatusAnimal(EnStatusAnimal.ATIVO);
        boi2.setUsuario(cuidador);
        boi2 = iAnimal.save(boi2);

        EAnimal boi3 = new EAnimal();
        boi3.setCodigoBrinco("BR003");
        boi3.setNome("Furacão");
        boi3.setSexo(EnSexoAnimal.M);
        boi3.setRaca("Brangus");
        boi3.setCor("Cinza");
        boi3.setPesoAtual(495.0);
        boi3.setAlturaCernelha(1.47);
        boi3.setDataNascimento(LocalDateTime.of(2021, 1, 5, 0, 0));
        boi3.setStatusAnimal(EnStatusAnimal.ATIVO);
        boi3.setUsuario(cuidador);
        boi3 = iAnimal.save(boi3);

        EAnimal vaca1 = new EAnimal();
        vaca1.setCodigoBrinco("BR004");
        vaca1.setNome("Mimosa");
        vaca1.setSexo(EnSexoAnimal.F);
        vaca1.setRaca("Girolando");
        vaca1.setCor("Malhada");
        vaca1.setPesoAtual(380.0);
        vaca1.setAlturaCernelha(1.38);
        vaca1.setPerimetroToracico(1.80);
        vaca1.setDataNascimento(LocalDateTime.of(2019, 5, 14, 0, 0));
        vaca1.setStatusAnimal(EnStatusAnimal.OBSERVACAO);
        vaca1.setUsuario(cuidador);
        vaca1 = iAnimal.save(vaca1);

        EAnimal vaca2 = new EAnimal();
        vaca2.setCodigoBrinco("BR005");
        vaca2.setNome("Estrela");
        vaca2.setSexo(EnSexoAnimal.F);
        vaca2.setRaca("Holandesa");
        vaca2.setCor("Branca e Preta");
        vaca2.setPesoAtual(410.0);
        vaca2.setAlturaCernelha(1.42);
        vaca2.setPerimetroToracico(1.88);
        vaca2.setComprimentoCorporal(1.48);
        vaca2.setDataNascimento(LocalDateTime.of(2020, 11, 30, 0, 0));
        vaca2.setStatusAnimal(EnStatusAnimal.ATIVO);
        vaca2.setUsuario(cuidador);
        vaca2 = iAnimal.save(vaca2);

        // ── 4. LOTE ──────────────────────────────────────────────────────────────────

        ELote lote = new ELote();
        lote.setCodigo("LOT001");
        lote.setDescricao("Lote de engorda — ciclo 2024/2025");
        lote.setRacaPredominante("Nelore");
        lote.setCorBrinco("Amarelo");
        lote.setDataCriacao(LocalDate.of(2024, 9, 1));
        lote.setCriadoPor(gerente);
        lote.setAlteradoPor(gerente);
        lote = iLote.save(lote);

        // ── 5. ALOCAÇÕES LOTE-SETOR (com animais distribuídos) ───────────────────────
        //
        // Estrutura: ELote ──< ELoteSetor >── ESetor
        //                            │
        //                        List<EAnimal>  (join table: lote_setor_animal)
        //
        // Cada ELoteSetor representa "quais animais deste lote estão neste setor".

        // Alocação 1 — bois de engorda alocados no Confinamento
        ELoteSetor alocacaoConfinamento = new ELoteSetor();
        alocacaoConfinamento.setLote(lote);
        alocacaoConfinamento.setSetor(setorConfinamento);
        alocacaoConfinamento.setAnimais(List.of(boi1, boi2, boi3));
        iLoteSetor.save(alocacaoConfinamento);

        // Alocação 2 — vacas em gestação alocadas no Galpão Maternidade
        ELoteSetor alocacaoGalpao = new ELoteSetor();
        alocacaoGalpao.setLote(lote);
        alocacaoGalpao.setSetor(setorGalpao);
        alocacaoGalpao.setAnimais(List.of(vaca1, vaca2));
        iLoteSetor.save(alocacaoGalpao);

        // ── 6. GRUPOS DE PRODUTO (Catálogo de Insumos) ───────────────────────────────

        EGrupoProduto grupoAnimais = new EGrupoProduto();
        grupoAnimais.setNome("Animais");
        grupoAnimais.setCodigoPrefixo("01");
        grupoAnimais.setNaturezaFinanceira(EnNaturezaFinanceira.CUSTO);
        iGrupoProduto.save(grupoAnimais);

        EGrupoProduto grupoVacinas = new EGrupoProduto();
        grupoVacinas.setNome("Vacinas");
        grupoVacinas.setCodigoPrefixo("02");
        grupoVacinas.setNaturezaFinanceira(EnNaturezaFinanceira.CUSTO);
        iGrupoProduto.save(grupoVacinas);

        EGrupoProduto grupoRacao = new EGrupoProduto();
        grupoRacao.setNome("Ração");
        grupoRacao.setCodigoPrefixo("03");
        grupoRacao.setNaturezaFinanceira(EnNaturezaFinanceira.CUSTO);
        grupoRacao = iGrupoProduto.save(grupoRacao);

        EGrupoProduto grupoOrigemAnimal = new EGrupoProduto();
        grupoOrigemAnimal.setNome("Origem Animal (Leite, etc)");
        grupoOrigemAnimal.setCodigoPrefixo("04");
        grupoOrigemAnimal.setNaturezaFinanceira(EnNaturezaFinanceira.CUSTO);
        iGrupoProduto.save(grupoOrigemAnimal);

        EGrupoProduto grupoManutencao = new EGrupoProduto();
        grupoManutencao.setNome("Manutenção");
        grupoManutencao.setCodigoPrefixo("05");
        grupoManutencao.setNaturezaFinanceira(EnNaturezaFinanceira.GASTO);
        iGrupoProduto.save(grupoManutencao);

        EGrupoProduto grupoPlantacoes = new EGrupoProduto();
        grupoPlantacoes.setNome("Plantações");
        grupoPlantacoes.setCodigoPrefixo("06");
        grupoPlantacoes.setNaturezaFinanceira(EnNaturezaFinanceira.CUSTO);
        iGrupoProduto.save(grupoPlantacoes);

        EGrupoProduto grupoInsumosGerais = new EGrupoProduto();
        grupoInsumosGerais.setNome("Insumos Gerais");
        grupoInsumosGerais.setCodigoPrefixo("07");
        grupoInsumosGerais.setNaturezaFinanceira(EnNaturezaFinanceira.GASTO);
        iGrupoProduto.save(grupoInsumosGerais);

        // ── 7. UNIDADES DE MEDIDA E INSUMO DE EXEMPLO (módulo de Estoque) ────────────

        EUnidadeMedida unidadeSaca = new EUnidadeMedida();
        unidadeSaca.setUnidade("SACA");
        unidadeSaca = iUnidadeMedida.save(unidadeSaca);

        EUnidadeMedida unidadeKg = new EUnidadeMedida();
        unidadeKg.setUnidade("KG");
        unidadeKg = iUnidadeMedida.save(unidadeKg);

        EUnidadeMedida unidadeLitro = new EUnidadeMedida();
        unidadeLitro.setUnidade("LITRO");
        iUnidadeMedida.save(unidadeLitro);

        EUnidadeMedida unidadeDose = new EUnidadeMedida();
        unidadeDose.setUnidade("DOSE");
        iUnidadeMedida.save(unidadeDose);

        EInsumo racao = new EInsumo();
        racao.setNome("Ração Engorda Premium");
        racao.setTipo(EnTipoInsumo.RACAO);
        racao.setGrupoProduto(grupoRacao);
        racao.setCodigoProduto(grupoRacao.getCodigoPrefixo() + "000001");
        racao.setUnidadeMedidaPrimaria(unidadeSaca);
        racao.setUnidadeMedidaSecundaria(unidadeKg);
        racao.setFatorConversao(40.0); // 1 SACA = 40 KG
        racao.setEstoqueMinimo(10.0);
        racao.setSaldoAtual(25.0);
        racao.setPrecoCompraMedio(120.0);
        racao.setPrecoUltimaCompra(120.0);
        racao.setPendente(Boolean.FALSE);
        iInsumo.save(racao);
    }

    // Replica o mesmo algoritmo usado em SUsuario para garantir compatibilidade de login
    private String sha256(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao aplicar SHA-256 no DataInitializer", e);
        }
    }
}
