-- Módulo de Insumos (Estoque) e funcionalidade "Alimentar Lote"

ALTER TABLE insumo
    ADD COLUMN IF NOT EXISTS unidade_medida_primaria_id BIGINT,
    ADD COLUMN IF NOT EXISTS unidade_medida_secundaria_id BIGINT,
    ADD COLUMN IF NOT EXISTS fator_conversao DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS preco_compra_medio DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS preco_ultima_compra DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS numero_nf VARCHAR(50),
    ADD COLUMN IF NOT EXISTS chave_acesso_nf VARCHAR(44);

ALTER TABLE insumo
    ADD CONSTRAINT fk_insumo_unidade_primaria FOREIGN KEY (unidade_medida_primaria_id) REFERENCES unidade_medida (id),
    ADD CONSTRAINT fk_insumo_unidade_secundaria FOREIGN KEY (unidade_medida_secundaria_id) REFERENCES unidade_medida (id);

CREATE TABLE IF NOT EXISTS consumo_insumo (
    id                                BIGSERIAL PRIMARY KEY,
    status                            VARCHAR(255),
    created_at                        TIMESTAMP,
    updated_at                        TIMESTAMP,
    insumo_id                         BIGINT           NOT NULL,
    setor_id                          BIGINT           NOT NULL,
    quantidade_registrada             DOUBLE PRECISION NOT NULL,
    unidade_registro_id               BIGINT           NOT NULL,
    quantidade_baixa_unidade_primaria DOUBLE PRECISION NOT NULL,
    total_animais_setor               INTEGER          NOT NULL,
    consumo_por_animal                DOUBLE PRECISION,
    data_consumo                      TIMESTAMP        NOT NULL,
    registrado_por_email              VARCHAR(255),
    CONSTRAINT fk_consumo_insumo_insumo FOREIGN KEY (insumo_id) REFERENCES insumo (id),
    CONSTRAINT fk_consumo_insumo_setor FOREIGN KEY (setor_id) REFERENCES setor (id),
    CONSTRAINT fk_consumo_insumo_unidade FOREIGN KEY (unidade_registro_id) REFERENCES unidade_medida (id)
);
