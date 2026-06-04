-- V7: Criação das tabelas de Metas de Setores e Medições
-- Segue o mesmo padrão das demais tabelas: id, status, created_at, updated_at + campos próprios.

CREATE TABLE IF NOT EXISTS meta_setor (
    id           BIGSERIAL PRIMARY KEY,
    status       VARCHAR(255),
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,
    setor_id     BIGINT        NOT NULL,
    data_inicial DATE          NOT NULL,
    data_final   DATE          NOT NULL,
    tipo_meta    VARCHAR(50)   NOT NULL,  -- 'LEITE' ou 'ARROBA'
    quantidade_esperada DOUBLE PRECISION NOT NULL,
    preco_medio  DOUBLE PRECISION NOT NULL,
    tipo_gado    VARCHAR(50),             -- NULL para metas LEITE
    CONSTRAINT fk_meta_setor_setor FOREIGN KEY (setor_id) REFERENCES setor (id)
);

CREATE TABLE IF NOT EXISTS medicao_meta (
    id                  BIGSERIAL PRIMARY KEY,
    status              VARCHAR(255),
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    meta_setor_id       BIGINT           NOT NULL,
    lote_id             BIGINT           NOT NULL,
    data_medicao        DATE             NOT NULL,
    quantidade_lancada  DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_medicao_meta_meta   FOREIGN KEY (meta_setor_id) REFERENCES meta_setor (id),
    CONSTRAINT fk_medicao_meta_lote   FOREIGN KEY (lote_id)       REFERENCES lote (id)
);
