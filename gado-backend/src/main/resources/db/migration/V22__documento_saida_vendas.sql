CREATE TABLE IF NOT EXISTS documento_saida (
    id                  BIGSERIAL PRIMARY KEY,
    status              VARCHAR(255),
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    tipo_documento      VARCHAR(20)   NOT NULL,
    numero_documento    VARCHAR(50),
    chave_acesso        VARCHAR(44),
    data_emissao        DATE          NOT NULL,
    valor_total         NUMERIC(15,2) NOT NULL,
    criado_por_email    VARCHAR(255)  NOT NULL
);

CREATE TABLE IF NOT EXISTS venda_leite_item (
    id                  BIGSERIAL PRIMARY KEY,
    status              VARCHAR(255),
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    documento_saida_id  BIGINT        NOT NULL,
    lote_id             BIGINT        NOT NULL,
    litros              NUMERIC(15,3) NOT NULL,
    preco_litro         NUMERIC(15,4) NOT NULL,
    valor_total         NUMERIC(15,2) NOT NULL,
    CONSTRAINT fk_venda_leite_item_documento FOREIGN KEY (documento_saida_id) REFERENCES documento_saida (id),
    CONSTRAINT fk_venda_leite_item_lote FOREIGN KEY (lote_id) REFERENCES lote (id)
);

CREATE TABLE IF NOT EXISTS venda_animal_item (
    id                  BIGSERIAL PRIMARY KEY,
    status              VARCHAR(255),
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    documento_saida_id  BIGINT        NOT NULL,
    animal_id           BIGINT        NOT NULL,
    destino             VARCHAR(20)   NOT NULL,
    valor_venda         NUMERIC(15,2) NOT NULL,
    CONSTRAINT fk_venda_animal_item_documento FOREIGN KEY (documento_saida_id) REFERENCES documento_saida (id),
    CONSTRAINT fk_venda_animal_item_animal FOREIGN KEY (animal_id) REFERENCES animal (id)
);

CREATE TABLE IF NOT EXISTS venda_meta_lote (
    id                  BIGSERIAL PRIMARY KEY,
    status              VARCHAR(255),
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    meta_setor_id       BIGINT        NOT NULL,
    lote_id             BIGINT        NOT NULL,
    data_venda          DATE          NOT NULL,
    litros_vendidos     DOUBLE PRECISION NOT NULL,
    criado_por_email    VARCHAR(255),
    CONSTRAINT fk_venda_meta_lote_meta FOREIGN KEY (meta_setor_id) REFERENCES meta_setor (id),
    CONSTRAINT fk_venda_meta_lote_lote FOREIGN KEY (lote_id) REFERENCES lote (id)
);
