-- Venda/abate de animal passa a debitar 1 cabeça do produto da raça do animal vendido.
ALTER TABLE venda_animal_item ADD COLUMN IF NOT EXISTS produto_id BIGINT;
ALTER TABLE venda_animal_item ADD CONSTRAINT fk_venda_animal_item_produto FOREIGN KEY (produto_id) REFERENCES insumo (id);
