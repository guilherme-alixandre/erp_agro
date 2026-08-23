-- Garante (idempotente) o Grupo de Produto "Animais" e a unidade de medida "CABECA" fora do
-- ambiente de dev (hoje só existiam via DataInitializer, que só roda com o banco vazio).
INSERT INTO grupo_produto (status, created_at, updated_at, nome, codigo_prefixo, natureza_financeira)
SELECT 'A', NOW(), NOW(), 'Animais', '01', 'CUSTO'
WHERE NOT EXISTS (SELECT 1 FROM grupo_produto WHERE nome = 'Animais');

INSERT INTO unidade_medida (status, created_at, updated_at, unidade)
SELECT 'A', NOW(), NOW(), 'CABECA'
WHERE NOT EXISTS (SELECT 1 FROM unidade_medida WHERE unidade = 'CABECA');

-- Cadastro de Raças: cada raça tem uma sigla única (usada no código do brinco do animal) e um
-- produto (insumo) vinculado, criado automaticamente, usado nas NFs de compra/venda de animais.
CREATE TABLE IF NOT EXISTS raca (
    id         BIGSERIAL PRIMARY KEY,
    status     VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    nome       VARCHAR(255) NOT NULL,
    sigla      VARCHAR(4)   NOT NULL,
    produto_id BIGINT       NOT NULL,
    CONSTRAINT uk_raca_nome UNIQUE (nome),
    CONSTRAINT uk_raca_sigla UNIQUE (sigla),
    CONSTRAINT fk_raca_produto FOREIGN KEY (produto_id) REFERENCES insumo (id)
);

-- Animal passa a referenciar Raça (em vez de texto livre) e deixa de ter nome — é identificado
-- só pelo código do brinco, agora gerado automaticamente (sigla da raça + sequencial) e único de
-- verdade no banco.
ALTER TABLE animal ADD COLUMN IF NOT EXISTS raca_id BIGINT;
ALTER TABLE animal ADD CONSTRAINT fk_animal_raca FOREIGN KEY (raca_id) REFERENCES raca (id);
ALTER TABLE animal DROP COLUMN IF EXISTS raca;
ALTER TABLE animal DROP COLUMN IF EXISTS nome;
ALTER TABLE animal ADD CONSTRAINT uq_animal_codigo_brinco UNIQUE (codigo_brinco);
