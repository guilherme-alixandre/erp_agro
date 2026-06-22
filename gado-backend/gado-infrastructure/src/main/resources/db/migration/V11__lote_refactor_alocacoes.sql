-- V11: Módulo de Lotes refatorado
-- Adiciona os campos novos na tabela lote existente e cria as tabelas de alocação.

-- ── 1. Adicionar colunas novas à tabela lote ──────────────────────────────────
ALTER TABLE lote
    ADD COLUMN IF NOT EXISTS codigo            VARCHAR(6)   UNIQUE,
    ADD COLUMN IF NOT EXISTS descricao         VARCHAR(255),
    ADD COLUMN IF NOT EXISTS raca_predominante VARCHAR(255),
    ADD COLUMN IF NOT EXISTS cor_brinco        VARCHAR(255),
    ADD COLUMN IF NOT EXISTS data_criacao      DATE,
    ADD COLUMN IF NOT EXISTS criado_por_id     BIGINT,
    ADD COLUMN IF NOT EXISTS alterado_por_id   BIGINT;

ALTER TABLE lote
    ADD CONSTRAINT fk_lote_criado_por
    FOREIGN KEY (criado_por_id) REFERENCES usuario (id),
    ADD CONSTRAINT fk_lote_alterado_por
    FOREIGN KEY (alterado_por_id) REFERENCES usuario (id);

-- ── 2. Criar tabela lote_setor ────────────────────────────────────────────────
-- Representa a alocação de um lote num setor físico.
CREATE TABLE IF NOT EXISTS lote_setor (
    id         BIGSERIAL PRIMARY KEY,
    status     VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    lote_id    BIGINT NOT NULL,
    setor_id   BIGINT NOT NULL,
    CONSTRAINT uq_lote_setor   UNIQUE (lote_id, setor_id),
    CONSTRAINT fk_ls_lote      FOREIGN KEY (lote_id)  REFERENCES lote  (id),
    CONSTRAINT fk_ls_setor     FOREIGN KEY (setor_id) REFERENCES setor (id)
    );

-- ── 3. Criar tabela de junção lote_setor_animal ───────────────────────────────
-- Associa animais específicos a cada alocação (lote_setor).
CREATE TABLE IF NOT EXISTS lote_setor_animal (
                                                 lote_setor_id BIGINT NOT NULL,
                                                 animal_id     BIGINT NOT NULL,
                                                 PRIMARY KEY (lote_setor_id, animal_id),
    CONSTRAINT fk_lsa_lote_setor FOREIGN KEY (lote_setor_id) REFERENCES lote_setor (id),
    CONSTRAINT fk_lsa_animal     FOREIGN KEY (animal_id)     REFERENCES animal      (id)
    );

-- ── 4. Popular codigo nos lotes já existentes (backfill) ─────────────────────
-- Gera um código LOTxxx para os lotes que existiam antes desta migration.
DO $$
DECLARE
rec   RECORD;
    seq   INT := 1;
BEGIN
FOR rec IN
SELECT id FROM lote WHERE codigo IS NULL ORDER BY id
    LOOP
UPDATE lote
SET codigo = 'LOT' || LPAD(seq::TEXT, 3, '0')
WHERE id = rec.id;
seq := seq + 1;
END LOOP;
END $$;

-- Agora que todos os registros têm valor, tornamos a coluna NOT NULL
ALTER TABLE lote ALTER COLUMN codigo SET NOT NULL;

