-- Natureza financeira do grupo de produto: CUSTO (gera retorno financeiro em produtos
-- vendidos) ou GASTO (não gera retorno direto).

ALTER TABLE grupo_produto
    ADD COLUMN IF NOT EXISTS natureza_financeira VARCHAR(20) NOT NULL DEFAULT 'GASTO';

ALTER TABLE grupo_produto
    ALTER COLUMN natureza_financeira DROP DEFAULT;
