-- Adiciona coluna que identifica o lote padrão do sistema
ALTER TABLE lote ADD COLUMN padrao BOOLEAN NOT NULL DEFAULT FALSE;

-- Insere o lote padrão usando o admin como criador
INSERT INTO lote (status, created_at, updated_at, codigo, descricao, cor_brinco, data_criacao, padrao, criado_por_id)
SELECT 'A', NOW(), NOW(), 'PADRAO', 'Lote Padrão do Sistema', 'N/A', CURRENT_DATE, TRUE, u.id
FROM usuario u
WHERE u.perfil = 'ADMINISTRADOR'
LIMIT 1;
