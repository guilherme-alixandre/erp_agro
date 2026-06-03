-- Cria um administrador inicial caso ainda nao exista nenhum.
-- Senha padrao: admin123 (SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9)
-- ALTERAR a senha apos o primeiro login.

INSERT INTO usuario (status, created_at, updated_at, nome, email, senha, perfil, data_cadastro)
SELECT 'A', NOW(), NOW(),
       'Administrador',
       'admin@erp.com',
       '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
       'ADMINISTRADOR',
       NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM usuario WHERE perfil = 'ADMINISTRADOR'
);
