-- Custo de vida do animal: ledger histórico de custo (aquisição, alimentação e vacinação) com o
-- preço fotografado no momento de cada evento. Pertence ao animal (não ao lote), para que o custo
-- acumulado "viaje" com ele quando troca de lote — ver SCustoAnimal/ECustoAnimal.

ALTER TABLE animal ADD COLUMN IF NOT EXISTS valor_aquisicao NUMERIC(14,2);
ALTER TABLE animal ADD COLUMN IF NOT EXISTS data_aquisicao TIMESTAMP;

CREATE TABLE IF NOT EXISTS custo_animal (
    id                   BIGSERIAL PRIMARY KEY,
    status               VARCHAR(255),
    created_at           TIMESTAMP,
    updated_at           TIMESTAMP,
    animal_id            BIGINT        NOT NULL,
    tipo_custo           VARCHAR(20)   NOT NULL,
    valor                NUMERIC(14,2) NOT NULL,
    quantidade           DOUBLE PRECISION,
    data_evento          TIMESTAMP     NOT NULL,
    origem_tipo          VARCHAR(30),
    origem_id            BIGINT,
    descricao            VARCHAR(500),
    registrado_por_email VARCHAR(255),
    CONSTRAINT fk_custo_animal_animal FOREIGN KEY (animal_id) REFERENCES animal (id)
);

CREATE INDEX IF NOT EXISTS idx_custo_animal_animal ON custo_animal (animal_id);
CREATE INDEX IF NOT EXISTS idx_custo_animal_origem ON custo_animal (origem_tipo, origem_id);
