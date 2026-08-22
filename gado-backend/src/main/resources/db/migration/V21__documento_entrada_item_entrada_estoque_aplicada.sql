ALTER TABLE documento_entrada_item
    ADD COLUMN IF NOT EXISTS entrada_estoque_aplicada BOOLEAN NOT NULL DEFAULT FALSE;
