-- Sobras de alimentação: quanto sobrou de uma alimentação já registrada em "Alimentar Setores"
-- (Consumo de Insumo), se foi reaproveitada, e a recomendação/perda resultante.

CREATE TABLE IF NOT EXISTS sobra_alimentacao (
    id                                BIGSERIAL PRIMARY KEY,
    status                            VARCHAR(255),
    created_at                        TIMESTAMP,
    updated_at                        TIMESTAMP,
    consumo_insumo_id                 BIGINT           NOT NULL,
    quantidade_sobra_registrada       DOUBLE PRECISION NOT NULL,
    unidade_registro_id               BIGINT           NOT NULL,
    quantidade_sobra_unidade_primaria DOUBLE PRECISION NOT NULL,
    percentual_sobra                  DOUBLE PRECISION NOT NULL,
    reaproveitado                     BOOLEAN          NOT NULL,
    status_faixa                      VARCHAR(10)      NOT NULL,
    quantidade_ajuste_recomendada     DOUBLE PRECISION,
    mensagem                          VARCHAR(500),
    data_registro                     TIMESTAMP        NOT NULL,
    registrado_por_email              VARCHAR(255),
    CONSTRAINT uq_sobra_alimentacao_consumo UNIQUE (consumo_insumo_id),
    CONSTRAINT fk_sobra_alimentacao_consumo FOREIGN KEY (consumo_insumo_id) REFERENCES consumo_insumo (id),
    CONSTRAINT fk_sobra_alimentacao_unidade FOREIGN KEY (unidade_registro_id) REFERENCES unidade_medida (id)
);
