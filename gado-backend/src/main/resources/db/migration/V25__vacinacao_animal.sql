-- Funcionalidade "Vacinar Animais": aplicação de um insumo (tipicamente
-- tipo VACINA) em um ou mais animais individuais ou em um lote inteiro,
-- com baixa automática de estoque (dose por animal x nº de animais).
-- Espelha a estrutura de cabeçalho + itens de consumo_estoque (V17), com
-- suporte a cancelamento (com justificativa) e edição.

CREATE TABLE IF NOT EXISTS vacinacao_animal (
    id                                             BIGSERIAL PRIMARY KEY,
    status                                         VARCHAR(255),
    created_at                                     TIMESTAMP,
    updated_at                                     TIMESTAMP,
    insumo_id                                      BIGINT           NOT NULL,
    quantidade_por_animal                          DOUBLE PRECISION NOT NULL,
    unidade_registro_id                            BIGINT           NOT NULL,
    quantidade_baixa_por_animal_unidade_primaria   DOUBLE PRECISION NOT NULL,
    quantidade_total_baixa_unidade_primaria        DOUBLE PRECISION NOT NULL,
    lote_id                                        BIGINT,
    data_aplicacao                                 TIMESTAMP        NOT NULL,
    criado_por_email                               VARCHAR(255)     NOT NULL,
    cancelado                                      BOOLEAN          NOT NULL DEFAULT FALSE,
    motivo_cancelamento                            VARCHAR(255),
    cancelado_por_email                            VARCHAR(255),
    cancelado_em                                   TIMESTAMP,
    CONSTRAINT fk_vacinacao_animal_insumo FOREIGN KEY (insumo_id) REFERENCES insumo (id),
    CONSTRAINT fk_vacinacao_animal_unidade FOREIGN KEY (unidade_registro_id) REFERENCES unidade_medida (id),
    CONSTRAINT fk_vacinacao_animal_lote FOREIGN KEY (lote_id) REFERENCES lote (id)
);

CREATE TABLE IF NOT EXISTS vacinacao_animal_item (
    id            BIGSERIAL PRIMARY KEY,
    status        VARCHAR(255),
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    vacinacao_id  BIGINT NOT NULL,
    animal_id     BIGINT NOT NULL,
    CONSTRAINT fk_vacinacao_animal_item_vacinacao FOREIGN KEY (vacinacao_id) REFERENCES vacinacao_animal (id),
    CONSTRAINT fk_vacinacao_animal_item_animal FOREIGN KEY (animal_id) REFERENCES animal (id)
);
