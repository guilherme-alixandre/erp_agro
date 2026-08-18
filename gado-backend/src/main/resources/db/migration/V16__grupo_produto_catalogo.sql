-- Catálogo de Produtos: Grupos de Produto e código sequencial do Insumo

CREATE TABLE IF NOT EXISTS grupo_produto (
    id             BIGSERIAL PRIMARY KEY,
    status         VARCHAR(255),
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,
    nome           VARCHAR(255) NOT NULL,
    codigo_prefixo VARCHAR(2)   NOT NULL,
    CONSTRAINT uk_grupo_produto_codigo_prefixo UNIQUE (codigo_prefixo)
);

ALTER TABLE insumo
    ADD COLUMN IF NOT EXISTS grupo_produto_id BIGINT,
    ADD COLUMN IF NOT EXISTS codigo_produto VARCHAR(8);

ALTER TABLE insumo
    ADD CONSTRAINT fk_insumo_grupo_produto FOREIGN KEY (grupo_produto_id) REFERENCES grupo_produto (id),
    ADD CONSTRAINT uk_insumo_codigo_produto UNIQUE (codigo_produto);
