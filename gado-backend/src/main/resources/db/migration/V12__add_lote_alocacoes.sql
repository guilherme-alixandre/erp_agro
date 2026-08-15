-- Remove a relação simples e não utilizada lote->setor (nunca foi exposta por nenhum DTO)
ALTER TABLE lote DROP CONSTRAINT IF EXISTS fk_lote_setor;
ALTER TABLE lote DROP COLUMN IF EXISTS setor_id;

ALTER TABLE lote ADD COLUMN codigo VARCHAR(10);
ALTER TABLE lote ADD COLUMN cor_brinco VARCHAR(255);
ALTER TABLE lote ADD COLUMN data_criacao DATE;
ALTER TABLE lote ADD COLUMN criado_por_id BIGINT;
ALTER TABLE lote ADD COLUMN alterado_por_id BIGINT;

ALTER TABLE lote ADD CONSTRAINT fk_lote_criado_por FOREIGN KEY (criado_por_id) REFERENCES usuario (id);
ALTER TABLE lote ADD CONSTRAINT fk_lote_alterado_por FOREIGN KEY (alterado_por_id) REFERENCES usuario (id);

-- Backfill para eventuais lotes já existentes, para satisfazer as constraints abaixo
UPDATE lote SET codigo = 'LOT' || LPAD(id::text, 3, '0') WHERE codigo IS NULL;
UPDATE lote SET data_criacao = COALESCE(created_at::date, CURRENT_DATE) WHERE data_criacao IS NULL;

ALTER TABLE lote ALTER COLUMN codigo SET NOT NULL;
ALTER TABLE lote ALTER COLUMN data_criacao SET NOT NULL;
ALTER TABLE lote ADD CONSTRAINT uq_lote_codigo UNIQUE (codigo);

-- Alocações de um lote em setores (um lote pode estar em vários setores ao mesmo tempo)
CREATE TABLE lote_setor (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    lote_id BIGINT NOT NULL,
    setor_id BIGINT NOT NULL,
    CONSTRAINT fk_lote_setor_alocacao_lote FOREIGN KEY (lote_id) REFERENCES lote (id),
    CONSTRAINT fk_lote_setor_alocacao_setor FOREIGN KEY (setor_id) REFERENCES setor (id),
    CONSTRAINT uq_lote_setor_alocacao UNIQUE (lote_id, setor_id)
);

-- Animais alocados em cada divisão (lote x setor)
CREATE TABLE lote_setor_animal (
    lote_setor_id BIGINT NOT NULL,
    animal_id BIGINT NOT NULL,
    PRIMARY KEY (lote_setor_id, animal_id),
    CONSTRAINT fk_lote_setor_animal_alocacao FOREIGN KEY (lote_setor_id) REFERENCES lote_setor (id),
    CONSTRAINT fk_lote_setor_animal_animal FOREIGN KEY (animal_id) REFERENCES animal (id)
);
