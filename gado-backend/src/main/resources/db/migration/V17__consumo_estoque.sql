-- Funcionalidade "Consumo de Estoque": baixa do próprio estoque por qualquer usuário ativo,
-- com motivo/finalidade obrigatório e cancelamento (com justificativa) restrito ao autor
-- da movimentação ou a Administradores.

CREATE TABLE IF NOT EXISTS consumo_estoque (
    id                     BIGSERIAL PRIMARY KEY,
    status                 VARCHAR(255),
    created_at             TIMESTAMP,
    updated_at             TIMESTAMP,
    motivo                 VARCHAR(255) NOT NULL,
    data_consumo           TIMESTAMP    NOT NULL,
    criado_por_email       VARCHAR(255) NOT NULL,
    cancelado              BOOLEAN      NOT NULL DEFAULT FALSE,
    motivo_cancelamento    VARCHAR(255),
    cancelado_por_email    VARCHAR(255),
    cancelado_em           TIMESTAMP
);

CREATE TABLE IF NOT EXISTS consumo_estoque_item (
    id                                BIGSERIAL PRIMARY KEY,
    status                            VARCHAR(255),
    created_at                        TIMESTAMP,
    updated_at                        TIMESTAMP,
    consumo_estoque_id                BIGINT           NOT NULL,
    insumo_id                         BIGINT           NOT NULL,
    quantidade_registrada             DOUBLE PRECISION NOT NULL,
    unidade_registro_id               BIGINT           NOT NULL,
    quantidade_baixa_unidade_primaria DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_consumo_estoque_item_consumo FOREIGN KEY (consumo_estoque_id) REFERENCES consumo_estoque (id),
    CONSTRAINT fk_consumo_estoque_item_insumo FOREIGN KEY (insumo_id) REFERENCES insumo (id),
    CONSTRAINT fk_consumo_estoque_item_unidade FOREIGN KEY (unidade_registro_id) REFERENCES unidade_medida (id)
);
