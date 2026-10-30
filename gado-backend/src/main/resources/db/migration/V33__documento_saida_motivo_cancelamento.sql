ALTER TABLE documento_saida ADD COLUMN motivo_cancelamento VARCHAR(20);

-- Liga cada venda_meta_lote ao item de venda de leite que a originou, para permitir reverter
-- (excluir) o registro de "vendido" na meta quando a venda correspondente for excluída.
ALTER TABLE venda_meta_lote ADD COLUMN venda_leite_item_id BIGINT;
ALTER TABLE venda_meta_lote ADD CONSTRAINT fk_venda_meta_lote_venda_leite_item
    FOREIGN KEY (venda_leite_item_id) REFERENCES venda_leite_item (id);
