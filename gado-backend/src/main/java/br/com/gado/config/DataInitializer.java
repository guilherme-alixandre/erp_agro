package br.com.gado.config;

import br.com.gado.dto.documentoEntradaDto.ReciboSimplesCadastroDto;
import br.com.gado.dto.documentoSaidaDto.VendaAnimalCadastroDto;
import br.com.gado.dto.documentoSaidaDto.VendaLeiteCadastroDto;
import br.com.gado.dto.documentoSaidaDto.VendaLeiteItemCadastroDto;
import br.com.gado.dto.metaSetorDto.MetaSetorCadastroDto;
import br.com.gado.dto.parcerioDto.ParceiroCadastroDto;
import br.com.gado.dto.parcerioDto.ParceiroDto;
import br.com.gado.dto.racaDto.RacaCadastroDto;
import br.com.gado.dto.racaDto.RacaRespostaDto;
import br.com.gado.entities.*;
import br.com.gado.enums.*;
import br.com.gado.repositories.*;
import br.com.gado.services.SDocumentoEntrada;
import br.com.gado.services.SDocumentoSaida;
import br.com.gado.services.SMetaSetor;
import br.com.gado.services.SParceiro;
import br.com.gado.services.SRaca;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final IRaca iRaca;
    private final IFuncionario iFuncionario;
    private final SRaca sRaca;
    private final SParceiro sParceiro;
    private final SMetaSetor sMetaSetor;
    private final SDocumentoEntrada sDocumentoEntrada;
    private final SDocumentoSaida sDocumentoSaida;

    public DataInitializer(
            IUsuario iUsuario,
            ISetor iSetor,
            IAnimal iAnimal,
            ILote iLote,
            ILoteSetor iLoteSetor,
            IUnidadeMedida iUnidadeMedida,
            IInsumo iInsumo,
            IGrupoProduto iGrupoProduto,
            IRaca iRaca,
            IFuncionario iFuncionario,
            SRaca sRaca,
            SParceiro sParceiro,
            SMetaSetor sMetaSetor,
            SDocumentoEntrada sDocumentoEntrada,
            SDocumentoSaida sDocumentoSaida) {
        this.iUsuario = iUsuario;
        this.iSetor = iSetor;
        this.iAnimal = iAnimal;
        this.iLote = iLote;
        this.iLoteSetor = iLoteSetor;
        this.iUnidadeMedida = iUnidadeMedida;
        this.iInsumo = iInsumo;
        this.iGrupoProduto = iGrupoProduto;
        this.iRaca = iRaca;
        this.iFuncionario = iFuncionario;
        this.sRaca = sRaca;
        this.sParceiro = sParceiro;
        this.sMetaSetor = sMetaSetor;
        this.sDocumentoEntrada = sDocumentoEntrada;
        this.sDocumentoSaida = sDocumentoSaida;
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

        EUsuario cuidadorChefe = new EUsuario();
        cuidadorChefe.setNome("Márcia Souza");
        cuidadorChefe.setEmail("marcia.chefe@gadoseed.com");
        cuidadorChefe.setSenha(sha256("Seed@1234"));
        cuidadorChefe.setPerfil(EnPerfilUsuario.CUIDADOR_CHEFE);
        cuidadorChefe.setDataCadastro(LocalDateTime.now());
        cuidadorChefe = iUsuario.save(cuidadorChefe);

        EUsuario financeiro = new EUsuario();
        financeiro.setNome("Roberto Lima");
        financeiro.setEmail("roberto.financeiro@gadoseed.com");
        financeiro.setSenha(sha256("Seed@1234"));
        financeiro.setPerfil(EnPerfilUsuario.FINANCEIRO);
        financeiro.setDataCadastro(LocalDateTime.now());
        financeiro = iUsuario.save(financeiro);

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

        ESetor setorPatio = new ESetor();
        setorPatio.setNome("Pátio de Triagem");
        setorPatio.setCapacidadeMaxima(20);
        setorPatio.setTipo(EnTipoSetor.PATIO);
        setorPatio.setCriadoPor(adminSeed);
        setorPatio.setAlteradoPor(adminSeed);
        setorPatio = iSetor.save(setorPatio);

        // ── 3. RAÇAS (Catálogo de Produtos + Animais) ────────────────────────────────
        // Cada raça gera automaticamente um Produto (EInsumo) "Gado {nome}" no grupo
        // "Animais" (semeado pela migration V23) — ver SRaca.criar.

        ERaca racaNelore = criarRacaSeed("Nelore", "NE", adminSeed.getEmail());
        ERaca racaAngus = criarRacaSeed("Angus", "AN", adminSeed.getEmail());
        ERaca racaBrangus = criarRacaSeed("Brangus", "BR", adminSeed.getEmail());
        ERaca racaGirolando = criarRacaSeed("Girolando", "GI", adminSeed.getEmail());
        ERaca racaHolandesa = criarRacaSeed("Holandesa", "HO", adminSeed.getEmail());

        // ── 4. PARCEIROS ─────────────────────────────────────────────────────────────

        ParceiroDto parceiroRacaoCentral = criarParceiroSeed(
                "Ração & Insumos Central Ltda", "12345678000195", "contato@racaocentral.com.br",
                "Rod. BR-060, Km 12 - Zona Rural, Anápolis/GO", "(62) 3333-1000",
                EnTipoParceiro.FORNECEDOR, adminSeed.getEmail());

        ParceiroDto parceiroVetFarma = criarParceiroSeed(
                "VetFarma Distribuidora", "11223344000186", "vendas@vetfarma.com.br",
                "Av. dos Veterinários, 450 - Centro, Anápolis/GO", "(62) 3333-2000",
                EnTipoParceiro.FORNECEDOR, adminSeed.getEmail());

        ParceiroDto parceiroAgroPecas = criarParceiroSeed(
                "AgroPeças Manutenção Ltda", "55443322000105", "contato@agropecas.com.br",
                "Rua das Ferramentas, 88 - Distrito Industrial, Anápolis/GO", "(62) 3333-3000",
                EnTipoParceiro.FORNECEDOR, adminSeed.getEmail());

        ParceiroDto parceiroFrigorifico = criarParceiroSeed(
                "Frigorífico Boa Carne S.A.", "99887766000105", "compras@boacarne.com.br",
                "Av. Frigorífico, 1200 - Distrito Industrial, Goiânia/GO", "(62) 3222-4000",
                EnTipoParceiro.COMPRADOR, adminSeed.getEmail());

        ParceiroDto parceiroJoseRicardo = criarParceiroSeed(
                "José Ricardo Almeida", "12345678909", "jose.ricardo.almeida@email.com",
                "Rua das Laranjeiras, 220 - Centro, Anápolis/GO", "(62) 99888-7766",
                EnTipoParceiro.COMPRADOR, adminSeed.getEmail());

        // ── 5. ANIMAIS (30 no total, ≥ 2 por status) ─────────────────────────────────

        EAnimal boi1 = criarAnimalSeed("NE0001", EnSexoAnimal.M, racaNelore, "Branca", 480.0, 1.45, 1.92, 1.55,
                LocalDateTime.of(2021, 3, 10, 0, 0), EnStatusAnimal.ATIVO, cuidador);
        EAnimal boi2 = criarAnimalSeed("AN0001", EnSexoAnimal.M, racaAngus, "Preta", 520.0, 1.50, 2.05, 1.60,
                LocalDateTime.of(2020, 8, 22, 0, 0), EnStatusAnimal.ATIVO, cuidador);
        EAnimal boi3 = criarAnimalSeed("BR0001", EnSexoAnimal.M, racaBrangus, "Cinza", 495.0, 1.47, 1.95, 1.57,
                LocalDateTime.of(2021, 1, 5, 0, 0), EnStatusAnimal.ATIVO, cuidador);
        EAnimal vaca1 = criarAnimalSeed("GI0001", EnSexoAnimal.F, racaGirolando, "Malhada", 380.0, 1.38, 1.80, 1.45,
                LocalDateTime.of(2019, 5, 14, 0, 0), EnStatusAnimal.OBSERVACAO, cuidador);
        EAnimal vaca2 = criarAnimalSeed("HO0001", EnSexoAnimal.F, racaHolandesa, "Branca e Preta", 410.0, 1.42, 1.88, 1.48,
                LocalDateTime.of(2020, 11, 30, 0, 0), EnStatusAnimal.ATIVO, cuidador);

        // Nelore
        EAnimal ne2 = criarAnimalSeed("NE0002", EnSexoAnimal.M, racaNelore, "Branca", 465.0, 1.44, 1.90, 1.53,
                LocalDateTime.of(2021, 4, 18, 0, 0), EnStatusAnimal.ATIVO, cuidador);
        EAnimal ne3 = criarAnimalSeed("NE0003", EnSexoAnimal.F, racaNelore, "Branca", 350.0, 1.35, 1.75, 1.40,
                LocalDateTime.of(2021, 6, 2, 0, 0), EnStatusAnimal.ATIVO, cuidador);
        EAnimal ne4 = criarAnimalSeed("NE0004", EnSexoAnimal.F, racaNelore, "Branca com manchas", 340.0, 1.33, 1.72, 1.38,
                LocalDateTime.of(2022, 1, 20, 0, 0), EnStatusAnimal.OBSERVACAO, cuidadorChefe);
        EAnimal ne5 = criarAnimalSeed("NE0005", EnSexoAnimal.M, racaNelore, "Branca", 500.0, 1.48, 1.98, 1.58,
                LocalDateTime.of(2020, 9, 9, 0, 0), EnStatusAnimal.ATIVO, cuidador);
        EAnimal ne6 = criarAnimalSeed("NE0006", EnSexoAnimal.M, racaNelore, "Branca", 510.0, 1.49, 2.00, 1.59,
                LocalDateTime.of(2020, 7, 15, 0, 0), EnStatusAnimal.ATIVO, cuidador);

        // Angus
        EAnimal an2 = criarAnimalSeed("AN0002", EnSexoAnimal.M, racaAngus, "Preta", 530.0, 1.51, 2.07, 1.61,
                LocalDateTime.of(2020, 10, 11, 0, 0), EnStatusAnimal.ATIVO, cuidador);
        EAnimal an3 = criarAnimalSeed("AN0003", EnSexoAnimal.F, racaAngus, "Preta", 400.0, 1.40, 1.85, 1.46,
                LocalDateTime.of(2021, 2, 27, 0, 0), EnStatusAnimal.ATIVO, cuidadorChefe);
        EAnimal an4 = criarAnimalSeed("AN0004", EnSexoAnimal.M, racaAngus, "Preta", 0.0, 0.0, 0.0, 0.0,
                LocalDateTime.of(2019, 12, 3, 0, 0), EnStatusAnimal.OBITO, cuidador);
        EAnimal an5 = criarAnimalSeed("AN0005", EnSexoAnimal.M, racaAngus, "Preta", 515.0, 1.50, 2.04, 1.60,
                LocalDateTime.of(2020, 6, 19, 0, 0), EnStatusAnimal.ATIVO, cuidador);
        EAnimal an6 = criarAnimalSeed("AN0006", EnSexoAnimal.F, racaAngus, "Preta e Branca", 405.0, 1.41, 1.86, 1.47,
                LocalDateTime.of(2021, 3, 30, 0, 0), EnStatusAnimal.ATIVO, cuidadorChefe);

        // Brangus
        EAnimal br2 = criarAnimalSeed("BR0002", EnSexoAnimal.M, racaBrangus, "Cinza Claro", 490.0, 1.46, 1.93, 1.56,
                LocalDateTime.of(2021, 5, 8, 0, 0), EnStatusAnimal.ATIVO, cuidador);
        EAnimal br3 = criarAnimalSeed("BR0003", EnSexoAnimal.F, racaBrangus, "Cinza", 345.0, 1.34, 1.73, 1.39,
                LocalDateTime.of(2022, 2, 14, 0, 0), EnStatusAnimal.OBSERVACAO, cuidadorChefe);
        EAnimal br4 = criarAnimalSeed("BR0004", EnSexoAnimal.M, racaBrangus, "Cinza", 485.0, 1.46, 1.92, 1.55,
                LocalDateTime.of(2021, 7, 25, 0, 0), EnStatusAnimal.ATIVO, cuidador);
        EAnimal br5 = criarAnimalSeed("BR0005", EnSexoAnimal.M, racaBrangus, "Cinza", 505.0, 1.48, 1.97, 1.58,
                LocalDateTime.of(2020, 8, 4, 0, 0), EnStatusAnimal.ATIVO, cuidador);
        EAnimal br6 = criarAnimalSeed("BR0006", EnSexoAnimal.M, racaBrangus, "Cinza Escuro", 498.0, 1.47, 1.94, 1.56,
                LocalDateTime.of(2021, 1, 29, 0, 0), EnStatusAnimal.ATIVO, cuidadorChefe);

        // Girolando
        EAnimal gi2 = criarAnimalSeed("GI0002", EnSexoAnimal.F, racaGirolando, "Malhada", 390.0, 1.39, 1.82, 1.46,
                LocalDateTime.of(2020, 4, 6, 0, 0), EnStatusAnimal.ATIVO, cuidador);
        EAnimal gi3 = criarAnimalSeed("GI0003", EnSexoAnimal.F, racaGirolando, "Malhada Clara", 375.0, 1.37, 1.79, 1.44,
                LocalDateTime.of(2020, 12, 17, 0, 0), EnStatusAnimal.ATIVO, cuidadorChefe);
        EAnimal gi4 = criarAnimalSeed("GI0004", EnSexoAnimal.F, racaGirolando, "Malhada", 0.0, 0.0, 0.0, 0.0,
                LocalDateTime.of(2018, 9, 23, 0, 0), EnStatusAnimal.OBITO, cuidador);
        EAnimal gi5 = criarAnimalSeed("GI0005", EnSexoAnimal.F, racaGirolando, "Malhada", 385.0, 1.38, 1.81, 1.45,
                LocalDateTime.of(2019, 11, 11, 0, 0), EnStatusAnimal.ATIVO, cuidador);
        EAnimal gi6 = criarAnimalSeed("GI0006", EnSexoAnimal.F, racaGirolando, "Malhada Escura", 370.0, 1.36, 1.78, 1.43,
                LocalDateTime.of(2021, 8, 1, 0, 0), EnStatusAnimal.OBSERVACAO, cuidadorChefe);

        // Holandesa
        EAnimal ho2 = criarAnimalSeed("HO0002", EnSexoAnimal.F, racaHolandesa, "Branca e Preta", 415.0, 1.43, 1.89, 1.49,
                LocalDateTime.of(2020, 3, 3, 0, 0), EnStatusAnimal.ATIVO, cuidador);
        EAnimal ho3 = criarAnimalSeed("HO0003", EnSexoAnimal.F, racaHolandesa, "Branca e Preta", 420.0, 1.44, 1.90, 1.50,
                LocalDateTime.of(2019, 10, 28, 0, 0), EnStatusAnimal.ATIVO, cuidadorChefe);
        EAnimal ho4 = criarAnimalSeed("HO0004", EnSexoAnimal.M, racaHolandesa, "Preta e Branca", 525.0, 1.51, 2.06, 1.62,
                LocalDateTime.of(2020, 5, 16, 0, 0), EnStatusAnimal.ATIVO, cuidador);
        EAnimal ho5 = criarAnimalSeed("HO0005", EnSexoAnimal.F, racaHolandesa, "Branca e Preta", 0.0, 0.0, 0.0, 0.0,
                LocalDateTime.of(2018, 7, 7, 0, 0), EnStatusAnimal.OBITO, cuidador);
        EAnimal ho6 = criarAnimalSeed("HO0006", EnSexoAnimal.F, racaHolandesa, "Branca", 408.0, 1.41, 1.87, 1.47,
                LocalDateTime.of(2021, 9, 12, 0, 0), EnStatusAnimal.ATIVO, cuidadorChefe);

        // ── 6. LOTES (5 no total) ─────────────────────────────────────────────────────

        ELote lote1 = criarLoteSeed("LOT001", "Lote de engorda — ciclo 2024/2025", "Nelore", "Amarelo",
                LocalDate.of(2024, 9, 1), gerente);
        ELote lote2 = criarLoteSeed("LOT002", "Lote leiteiro — Girolando/Holandesa", "Girolando", "Azul",
                LocalDate.of(2024, 10, 15), gerente);
        ELote lote3 = criarLoteSeed("LOT003", "Lote de recria mista — Nelore/Angus", "Nelore", "Verde",
                LocalDate.of(2025, 1, 10), cuidadorChefe);
        ELote lote4 = criarLoteSeed("LOT004", "Lote de engorda II — Brangus", "Brangus", "Vermelho",
                LocalDate.of(2025, 2, 20), gerente);
        ELote lote5 = criarLoteSeed("LOT005", "Lote de observação veterinária", "Misto", "Branco",
                LocalDate.of(2025, 3, 5), cuidadorChefe);

        // ── 7. ALOCAÇÕES LOTE-SETOR ───────────────────────────────────────────────────
        // Pasto Principal recebe 2 lotes diferentes (LOT002 + LOT003) — "lotes mistos no
        // setor" — usado pela meta de LEITE cadastrada na seção 8.

        criarAlocacaoSeed(lote1, setorConfinamento, List.of(boi1, boi2, boi3));
        criarAlocacaoSeed(lote1, setorGalpao, List.of(vaca1, vaca2));
        criarAlocacaoSeed(lote2, setorPasto, List.of(gi2, gi3, gi5, ho2, ho3));
        criarAlocacaoSeed(lote3, setorPasto, List.of(ne2, ne3, an2, an3));
        criarAlocacaoSeed(lote4, setorConfinamento, List.of(br2, br4, br6, an6));
        criarAlocacaoSeed(lote5, setorPatio, List.of(ne4, br3, gi6));

        // ── 8. GRUPOS DE PRODUTO (Catálogo de Insumos) ───────────────────────────────
        // O grupo "Animais" (prefixo 01) já vem semeado pela migration V23 — não recriar aqui
        // (a raça já o usa desde a seção 3).

        EGrupoProduto grupoVacinas = new EGrupoProduto();
        grupoVacinas.setNome("Vacinas");
        grupoVacinas.setCodigoPrefixo("02");
        grupoVacinas.setNaturezaFinanceira(EnNaturezaFinanceira.CUSTO);
        grupoVacinas = iGrupoProduto.save(grupoVacinas);

        EGrupoProduto grupoRacao = new EGrupoProduto();
        grupoRacao.setNome("Ração");
        grupoRacao.setCodigoPrefixo("03");
        grupoRacao.setNaturezaFinanceira(EnNaturezaFinanceira.CUSTO);
        grupoRacao = iGrupoProduto.save(grupoRacao);

        EGrupoProduto grupoOrigemAnimal = new EGrupoProduto();
        grupoOrigemAnimal.setNome("Origem Animal (Leite, etc)");
        grupoOrigemAnimal.setCodigoPrefixo("04");
        grupoOrigemAnimal.setNaturezaFinanceira(EnNaturezaFinanceira.CUSTO);
        grupoOrigemAnimal = iGrupoProduto.save(grupoOrigemAnimal);

        EGrupoProduto grupoManutencao = new EGrupoProduto();
        grupoManutencao.setNome("Manutenção");
        grupoManutencao.setCodigoPrefixo("05");
        grupoManutencao.setNaturezaFinanceira(EnNaturezaFinanceira.GASTO);
        grupoManutencao = iGrupoProduto.save(grupoManutencao);

        EGrupoProduto grupoPlantacoes = new EGrupoProduto();
        grupoPlantacoes.setNome("Plantações");
        grupoPlantacoes.setCodigoPrefixo("06");
        grupoPlantacoes.setNaturezaFinanceira(EnNaturezaFinanceira.CUSTO);
        grupoPlantacoes = iGrupoProduto.save(grupoPlantacoes);

        EGrupoProduto grupoInsumosGerais = new EGrupoProduto();
        grupoInsumosGerais.setNome("Insumos Gerais");
        grupoInsumosGerais.setCodigoPrefixo("07");
        grupoInsumosGerais.setNaturezaFinanceira(EnNaturezaFinanceira.GASTO);
        grupoInsumosGerais = iGrupoProduto.save(grupoInsumosGerais);

        // ── 9. UNIDADES DE MEDIDA ─────────────────────────────────────────────────────

        EUnidadeMedida unidadeSaca = new EUnidadeMedida();
        unidadeSaca.setUnidade("SACA");
        unidadeSaca = iUnidadeMedida.save(unidadeSaca);

        EUnidadeMedida unidadeKg = new EUnidadeMedida();
        unidadeKg.setUnidade("KG");
        unidadeKg = iUnidadeMedida.save(unidadeKg);

        EUnidadeMedida unidadeLitro = new EUnidadeMedida();
        unidadeLitro.setUnidade("LITRO");
        unidadeLitro = iUnidadeMedida.save(unidadeLitro);

        EUnidadeMedida unidadeDose = new EUnidadeMedida();
        unidadeDose.setUnidade("DOSE");
        unidadeDose = iUnidadeMedida.save(unidadeDose);

        EUnidadeMedida unidadeUnidade = new EUnidadeMedida();
        unidadeUnidade.setUnidade("UNIDADE");
        unidadeUnidade = iUnidadeMedida.save(unidadeUnidade);

        // ── 10. INSUMOS (≈ 3 por grupo) ───────────────────────────────────────────────

        EInsumo racao = criarInsumoSeed("Ração Engorda Premium", EnTipoInsumo.RACAO, grupoRacao, "000001",
                unidadeSaca, unidadeKg, 40.0, 10.0, 25.0, 120.0, 120.0, false);
        criarInsumoSeed("Ração Lactação Especial", EnTipoInsumo.RACAO, grupoRacao, "000002",
                unidadeSaca, unidadeKg, 40.0, 10.0, 30.0, 135.0, 135.0, false);
        EInsumo salMineral = criarInsumoSeed("Sal Mineral", EnTipoInsumo.RACAO, grupoRacao, "000003",
                unidadeSaca, null, null, 10.0, 5.0, 90.0, 90.0, false);

        EInsumo vacinaAftosa = criarInsumoSeed("Vacina Febre Aftosa", EnTipoInsumo.VACINA, grupoVacinas, "000001",
                unidadeDose, null, null, 10.0, 50.0, 8.0, 8.0, false);
        criarInsumoSeed("Vacina Brucelose", EnTipoInsumo.VACINA, grupoVacinas, "000002",
                unidadeDose, null, null, 10.0, 40.0, 12.0, 12.0, false);
        criarInsumoSeed("Vermífugo Injetável", EnTipoInsumo.MEDICAMENTO, grupoVacinas, "000003",
                unidadeDose, null, null, 10.0, 60.0, 6.5, 6.5, false);

        criarInsumoSeed("Sêmen Bovino (dose)", EnTipoInsumo.OUTROS, grupoOrigemAnimal, "000001",
                unidadeDose, null, null, 5.0, 15.0, 45.0, 45.0, false);
        criarInsumoSeed("Embriões Congelados", EnTipoInsumo.OUTROS, grupoOrigemAnimal, "000002",
                unidadeDose, null, null, 3.0, 5.0, 300.0, 300.0, false);
        criarInsumoSeed("Hormônio para IATF", EnTipoInsumo.MEDICAMENTO, grupoOrigemAnimal, "000003",
                unidadeDose, null, null, 10.0, 25.0, 18.0, 18.0, false);

        EInsumo arameFarpado = criarInsumoSeed("Arame Farpado", EnTipoInsumo.OUTROS, grupoManutencao, "000001",
                unidadeKg, null, null, 20.0, 80.0, 9.0, 9.0, false);
        criarInsumoSeed("Óleo Lubrificante para Trator", EnTipoInsumo.OUTROS, grupoManutencao, "000002",
                unidadeLitro, null, null, 15.0, 40.0, 22.0, 22.0, false);
        criarInsumoSeed("Material Elétrico Diversos", EnTipoInsumo.OUTROS, grupoManutencao, "000003",
                unidadeUnidade, null, null, 5.0, 15.0, 35.0, 35.0, false);

        criarInsumoSeed("Sementes de Milho (Silagem)", EnTipoInsumo.OUTROS, grupoPlantacoes, "000001",
                unidadeSaca, null, null, 10.0, 20.0, 180.0, 180.0, false);
        criarInsumoSeed("Fertilizante NPK", EnTipoInsumo.OUTROS, grupoPlantacoes, "000002",
                unidadeSaca, null, null, 10.0, 25.0, 95.0, 95.0, false);
        criarInsumoSeed("Herbicida Concentrado", EnTipoInsumo.OUTROS, grupoPlantacoes, "000003",
                unidadeLitro, null, null, 5.0, 10.0, 60.0, 60.0, false);

        criarInsumoSeed("Luvas Descartáveis (caixa)", EnTipoInsumo.OUTROS, grupoInsumosGerais, "000001",
                unidadeUnidade, null, null, 5.0, 12.0, 25.0, 25.0, false);
        EInsumo materialEscritorio = criarInsumoSeed("Material de Escritório", EnTipoInsumo.OUTROS, grupoInsumosGerais, "000002",
                unidadeUnidade, null, null, 5.0, 8.0, 15.0, 15.0, false);
        criarInsumoSeed("Ferramentas Diversas", EnTipoInsumo.OUTROS, grupoInsumosGerais, "000003",
                unidadeUnidade, null, null, 3.0, 6.0, 50.0, 50.0, false);

        // ── 11. METAS DE SETOR (3 no total: 2 ARROBA + 1 LEITE) ──────────────────────

        LocalDate inicioJanela = LocalDate.now().withDayOfMonth(1);
        LocalDate fimJanela = inicioJanela.plusMonths(1).minusDays(1);

        criarMetaSeed(setorPasto.getId(), inicioJanela, fimJanela, EnTipoMeta.LEITE, 3000.0, 2.50, null);
        criarMetaSeed(setorConfinamento.getId(), inicioJanela, fimJanela, EnTipoMeta.ARROBA, 600.0, 280.0,
                EnTipoGado.CONFINAMENTO_56);
        criarMetaSeed(setorGalpao.getId(), inicioJanela, fimJanela, EnTipoMeta.ARROBA, 200.0, 260.0,
                EnTipoGado.NOVILHA_DESCARTE_47_5);

        // ── 12. DOCUMENTOS DE ENTRADA (recibos simulados) ────────────────────────────
        // 3 aprovados (geram custo/gasto no mês) + 2 deixados pendentes de aprovação.

        LocalDate dataCompraRacao = LocalDate.now().minusDays(10);
        Long idReciboRacao = criarReciboSeed("Compra de ração para o trimestre", dataCompraRacao,
                parceiroRacaoCentral.getId(), racao.getId(), new BigDecimal("50"), new BigDecimal("6000.00"),
                EnNaturezaFinanceira.CUSTO, adminSeed.getEmail());
        sDocumentoEntrada.aprovarDocumento(idReciboRacao, adminSeed.getEmail());

        Long idReciboVacina = criarReciboSeed("Compra de vacinas contra aftosa", dataCompraRacao.plusDays(1),
                parceiroVetFarma.getId(), vacinaAftosa.getId(), new BigDecimal("200"), new BigDecimal("1600.00"),
                EnNaturezaFinanceira.CUSTO, adminSeed.getEmail());
        sDocumentoEntrada.aprovarDocumento(idReciboVacina, adminSeed.getEmail());

        Long idReciboManutencao = criarReciboSeed("Manutenção de cercas e curral", dataCompraRacao.plusDays(2),
                parceiroAgroPecas.getId(), arameFarpado.getId(), new BigDecimal("100"), new BigDecimal("1200.00"),
                EnNaturezaFinanceira.GASTO, adminSeed.getEmail());
        sDocumentoEntrada.aprovarDocumento(idReciboManutencao, adminSeed.getEmail());

        // Pendentes de aprovação — aparecem no módulo de Aprovações.
        criarReciboSeed("Compra de sal mineral", LocalDate.now().minusDays(2),
                parceiroRacaoCentral.getId(), salMineral.getId(), new BigDecimal("30"), new BigDecimal("900.00"),
                EnNaturezaFinanceira.CUSTO, adminSeed.getEmail());
        criarReciboSeed("Material de escritório para o setor administrativo", LocalDate.now().minusDays(1),
                parceiroAgroPecas.getId(), materialEscritorio.getId(), new BigDecimal("10"), new BigDecimal("350.00"),
                EnNaturezaFinanceira.GASTO, adminSeed.getEmail());

        // Entrada retroativa de estoque de cabeças (produto "Gado {raça}") para cada raça, cobrindo
        // a aquisição histórica do rebanho — pré-requisito para as vendas/abates da seção 13
        // conseguirem dar baixa (SInsumo.baixarEstoque exige saldo suficiente no produto da raça).
        // Datada há 2 anos para não distorcer o custo/gasto do mês corrente no Resumo/Financeiro.
        LocalDate dataAquisicaoRebanho = LocalDate.now().minusYears(2);
        criarEntradaCabecasSeed(racaNelore, 6, dataAquisicaoRebanho, new BigDecimal("2800.00"), adminSeed.getEmail());
        criarEntradaCabecasSeed(racaAngus, 6, dataAquisicaoRebanho, new BigDecimal("3200.00"), adminSeed.getEmail());
        criarEntradaCabecasSeed(racaBrangus, 6, dataAquisicaoRebanho, new BigDecimal("3000.00"), adminSeed.getEmail());
        criarEntradaCabecasSeed(racaGirolando, 6, dataAquisicaoRebanho, new BigDecimal("2200.00"), adminSeed.getEmail());
        criarEntradaCabecasSeed(racaHolandesa, 6, dataAquisicaoRebanho, new BigDecimal("2500.00"), adminSeed.getEmail());

        // ── 13. DOCUMENTOS DE SAÍDA (vendas simuladas) ───────────────────────────────
        // Geram receita e — no caso de venda/abate de animais — dão baixa automática de
        // status (VENDIDO/ABATIDO) nos animais indicados abaixo.

        VendaLeiteCadastroDto vendaLeite = new VendaLeiteCadastroDto();
        vendaLeite.setDataEmissao(LocalDate.now().minusDays(5));
        vendaLeite.setCompradorId(parceiroJoseRicardo.getId());
        vendaLeite.setPrecoLitro(new BigDecimal("2.60"));
        VendaLeiteItemCadastroDto itemLeite = new VendaLeiteItemCadastroDto();
        itemLeite.setLoteId(lote2.getId());
        itemLeite.setLitros(new BigDecimal("1500"));
        vendaLeite.setItens(List.of(itemLeite));
        sDocumentoSaida.cadastrarVendaLeite(vendaLeite, adminSeed.getEmail());

        VendaAnimalCadastroDto vendaAnimais = new VendaAnimalCadastroDto();
        vendaAnimais.setDataEmissao(LocalDate.now().minusDays(3));
        vendaAnimais.setDestino(EnStatusAnimal.VENDIDO);
        vendaAnimais.setAnimalIds(List.of(ne5.getId(), an5.getId(), gi5.getId()));
        vendaAnimais.setValorTotal(new BigDecimal("9000.00"));
        vendaAnimais.setCompradorId(parceiroFrigorifico.getId());
        sDocumentoSaida.cadastrarVendaAnimal(vendaAnimais, adminSeed.getEmail());

        VendaAnimalCadastroDto abateAnimais = new VendaAnimalCadastroDto();
        abateAnimais.setDataEmissao(LocalDate.now().minusDays(2));
        abateAnimais.setDestino(EnStatusAnimal.ABATIDO);
        abateAnimais.setAnimalIds(List.of(ne6.getId(), br5.getId(), ho4.getId()));
        abateAnimais.setValorTotal(new BigDecimal("10500.00"));
        abateAnimais.setCompradorId(parceiroFrigorifico.getId());
        sDocumentoSaida.cadastrarVendaAnimal(abateAnimais, adminSeed.getEmail());

        // ── 14. FUNCIONÁRIOS (cadastro dos perfis já semeados) ───────────────────────

        criarFuncionarioSeed(gerente, "Carlos Mendes", "11111111111", "GERENTE",
                LocalDate.of(2022, 3, 1), new BigDecimal("8000.00"), EnNaturezaFinanceira.CUSTO);
        criarFuncionarioSeed(cuidador, "Ana Silva", "22222222222", "CUIDADOR",
                LocalDate.of(2023, 6, 12), new BigDecimal("2200.00"), EnNaturezaFinanceira.CUSTO);
        criarFuncionarioSeed(adminSeed, "João Administrador", "33333333333", "ADMINISTRADOR",
                LocalDate.of(2021, 1, 10), new BigDecimal("9500.00"), EnNaturezaFinanceira.GASTO);
        criarFuncionarioSeed(cuidadorChefe, "Márcia Souza", "44444444444", "CUIDADOR_CHEFE",
                LocalDate.of(2022, 9, 5), new BigDecimal("3200.00"), EnNaturezaFinanceira.CUSTO);
        criarFuncionarioSeed(financeiro, "Roberto Lima", "55555555555", "FINANCEIRO",
                LocalDate.of(2023, 2, 20), new BigDecimal("6500.00"), EnNaturezaFinanceira.GASTO);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────────

    private ERaca criarRacaSeed(String nome, String sigla, String emailUsuario) {
        RacaCadastroDto dto = new RacaCadastroDto();
        dto.setNome(nome);
        dto.setSigla(sigla);
        RacaRespostaDto resposta = sRaca.criar(dto, emailUsuario);
        return iRaca.findById(resposta.getId())
                .orElseThrow(() -> new IllegalStateException("Falha ao semear a raça " + nome));
    }

    private ParceiroDto criarParceiroSeed(String nome, String cpfCnpj, String email, String endereco,
                                           String telefone, EnTipoParceiro tipo, String emailUsuario) {
        ParceiroCadastroDto dto = new ParceiroCadastroDto();
        dto.setNome(nome);
        dto.setCPF_CNPJ(cpfCnpj);
        dto.setEmail(email);
        dto.setEndereco(endereco);
        dto.setTelefone(telefone);
        dto.setTipo(tipo);
        return sParceiro.cadastra(dto, emailUsuario);
    }

    private EAnimal criarAnimalSeed(String codigoBrinco, EnSexoAnimal sexo, ERaca raca, String cor, double peso,
                                     double alturaCernelha, double perimetroToracico, double comprimentoCorporal,
                                     LocalDateTime dataNascimento, EnStatusAnimal status, EUsuario usuario) {
        EAnimal animal = new EAnimal();
        animal.setCodigoBrinco(codigoBrinco);
        animal.setSexo(sexo);
        animal.setRaca(raca);
        animal.setCor(cor);
        animal.setPesoAtual(peso);
        animal.setAlturaCernelha(alturaCernelha);
        animal.setPerimetroToracico(perimetroToracico);
        animal.setComprimentoCorporal(comprimentoCorporal);
        animal.setDataNascimento(dataNascimento);
        animal.setStatusAnimal(status);
        animal.setUsuario(usuario);
        return iAnimal.save(animal);
    }

    private ELote criarLoteSeed(String codigo, String descricao, String racaPredominante, String corBrinco,
                                 LocalDate dataCriacao, EUsuario responsavel) {
        ELote lote = new ELote();
        lote.setCodigo(codigo);
        lote.setDescricao(descricao);
        lote.setRacaPredominante(racaPredominante);
        lote.setCorBrinco(corBrinco);
        lote.setDataCriacao(dataCriacao);
        lote.setCriadoPor(responsavel);
        lote.setAlteradoPor(responsavel);
        return iLote.save(lote);
    }

    private void criarAlocacaoSeed(ELote lote, ESetor setor, List<EAnimal> animais) {
        ELoteSetor alocacao = new ELoteSetor();
        alocacao.setLote(lote);
        alocacao.setSetor(setor);
        alocacao.setAnimais(new ArrayList<>(animais));
        iLoteSetor.save(alocacao);
    }

    private EInsumo criarInsumoSeed(String nome, EnTipoInsumo tipo, EGrupoProduto grupoProduto, String sufixoCodigo,
                                     EUnidadeMedida unidadePrimaria, EUnidadeMedida unidadeSecundaria,
                                     Double fatorConversao, Double estoqueMinimo, Double saldoAtual,
                                     Double precoCompraMedio, Double precoUltimaCompra, boolean pendente) {
        EInsumo insumo = new EInsumo();
        insumo.setNome(nome);
        insumo.setTipo(tipo);
        insumo.setGrupoProduto(grupoProduto);
        insumo.setCodigoProduto(grupoProduto.getCodigoPrefixo() + sufixoCodigo);
        insumo.setUnidadeMedidaPrimaria(unidadePrimaria);
        insumo.setUnidadeMedidaSecundaria(unidadeSecundaria);
        insumo.setFatorConversao(fatorConversao);
        insumo.setEstoqueMinimo(estoqueMinimo);
        insumo.setSaldoAtual(saldoAtual);
        insumo.setPrecoCompraMedio(precoCompraMedio);
        insumo.setPrecoUltimaCompra(precoUltimaCompra);
        insumo.setPendente(pendente);
        return iInsumo.save(insumo);
    }

    private void criarMetaSeed(Long setorId, LocalDate dataInicial, LocalDate dataFinal, EnTipoMeta tipoMeta,
                                Double quantidadeEsperada, Double precoMedio, EnTipoGado tipoGado) {
        MetaSetorCadastroDto dto = new MetaSetorCadastroDto();
        dto.setSetorId(setorId);
        dto.setDataInicial(dataInicial);
        dto.setDataFinal(dataFinal);
        dto.setTipoMeta(tipoMeta);
        dto.setQuantidadeEsperada(quantidadeEsperada);
        dto.setPrecoMedio(precoMedio);
        dto.setTipoGado(tipoGado);
        sMetaSetor.cadastrar(dto);
    }

    private Long criarReciboSeed(String descricao, LocalDate dataEmissao, Long fornecedorId, Long produtoId,
                                  BigDecimal quantidade, BigDecimal valorTotal, EnNaturezaFinanceira naturezaFinanceira,
                                  String emailUsuario) {
        ReciboSimplesCadastroDto dto = new ReciboSimplesCadastroDto();
        dto.setDescricao(descricao);
        dto.setDataEmissao(dataEmissao);
        dto.setFornecedorId(fornecedorId);
        dto.setProdutoId(produtoId);
        dto.setQuantidade(quantidade);
        dto.setValorTotal(valorTotal);
        dto.setNaturezaFinanceira(naturezaFinanceira);
        return sDocumentoEntrada.cadastrarReciboSimples(dto, emailUsuario).getId();
    }

    private void criarEntradaCabecasSeed(ERaca raca, int quantidadeCabecas, LocalDate dataEmissao,
                                          BigDecimal valorPorCabeca, String emailUsuario) {
        BigDecimal quantidade = BigDecimal.valueOf(quantidadeCabecas);
        BigDecimal valorTotal = valorPorCabeca.multiply(quantidade).setScale(2, java.math.RoundingMode.HALF_UP);
        Long idRecibo = criarReciboSeed("Aquisição histórica do rebanho — " + raca.getNome(), dataEmissao,
                null, raca.getProduto().getId(), quantidade, valorTotal, EnNaturezaFinanceira.CUSTO, emailUsuario);
        sDocumentoEntrada.aprovarDocumento(idRecibo, emailUsuario);
    }

    private void criarFuncionarioSeed(EUsuario usuario, String nomeCompleto, String cpf, String cargo,
                                       LocalDate dataAdmissao, BigDecimal salarioBase, EnNaturezaFinanceira natureza) {
        EFuncionario funcionario = new EFuncionario();
        funcionario.setUsuario(usuario);
        funcionario.setNomeCompleto(nomeCompleto);
        funcionario.setCpf(cpf);
        funcionario.setCargo(cargo);
        funcionario.setDataAdmissao(dataAdmissao);
        funcionario.setSalarioBase(salarioBase);
        funcionario.setPercentualInss(new BigDecimal("9.00"));
        funcionario.setPercentualFgts(new BigDecimal("8.00"));
        funcionario.setValorValeTransporte(new BigDecimal("220.00"));
        funcionario.setValorValeAlimentacao(new BigDecimal("400.00"));
        funcionario.setNaturezaFinanceira(natureza);
        iFuncionario.save(funcionario);
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
