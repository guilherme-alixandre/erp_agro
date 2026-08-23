ALTER TABLE documento_saida ADD COLUMN comprador_id BIGINT;
ALTER TABLE documento_saida ADD CONSTRAINT fk_documento_saida_comprador FOREIGN KEY (comprador_id) REFERENCES parceiro (id);
