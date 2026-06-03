ALTER TABLE lote ADD COLUMN setor_id BIGINT;

ALTER TABLE lote ADD CONSTRAINT fk_lote_setor
FOREIGN KEY (setor_id) REFERENCES setor (id);