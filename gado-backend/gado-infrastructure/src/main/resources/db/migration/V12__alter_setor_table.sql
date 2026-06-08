-- 1. Adiciona as colunas de auditoria na tabela setor
ALTER TABLE setor
    ADD COLUMN criado_por_id BIGINT,
ADD COLUMN alterado_por_id BIGINT;

-- 2. Cria as restrições de chave estrangeira apontando para a tabela usuario
ALTER TABLE setor
    ADD CONSTRAINT fk_setor_criado_por
        FOREIGN KEY (criado_por_id) REFERENCES usuario (id);

ALTER TABLE setor
    ADD CONSTRAINT fk_setor_alterado_por
        FOREIGN KEY (alterado_por_id) REFERENCES usuario (id);