-- Garante (idempotente) o Grupo de Produto "Vacinas" fora do ambiente de dev, no mesmo
-- espírito da V23 para "Animais": hoje só existia via DataInitializer, que só roda com o
-- banco vazio. Prefixo "02" reservado para este grupo desde a concepção do catálogo.
INSERT INTO grupo_produto (status, created_at, updated_at, nome, codigo_prefixo, categoria_grupo, natureza_financeira)
SELECT 'A', NOW(), NOW(), 'Vacinas', '02', 'VACINA','CUSTO'
WHERE NOT EXISTS (SELECT 1 FROM grupo_produto WHERE nome = 'Vacinas')
  AND NOT EXISTS (SELECT 1 FROM grupo_produto WHERE codigo_prefixo = '02');
