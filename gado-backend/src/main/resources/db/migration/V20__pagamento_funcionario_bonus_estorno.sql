ALTER TABLE pagamento_funcionario
    ADD COLUMN IF NOT EXISTS valor_bonus         NUMERIC(15,2),
    ADD COLUMN IF NOT EXISTS estornado           BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS motivo_estorno       VARCHAR(500),
    ADD COLUMN IF NOT EXISTS estornado_por_email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS estornado_em        TIMESTAMP;
