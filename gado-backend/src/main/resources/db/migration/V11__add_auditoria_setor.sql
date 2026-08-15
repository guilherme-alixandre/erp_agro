ALTER TABLE setor ADD COLUMN criado_por_id BIGINT;
ALTER TABLE setor ADD COLUMN alterado_por_id BIGINT;

ALTER TABLE setor ADD CONSTRAINT fk_setor_criado_por FOREIGN KEY (criado_por_id) REFERENCES usuario (id);
ALTER TABLE setor ADD CONSTRAINT fk_setor_alterado_por FOREIGN KEY (alterado_por_id) REFERENCES usuario (id);
