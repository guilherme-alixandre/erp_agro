-- Adiciona a coluna descricao à tabela setor
ALTER TABLE setor ADD COLUMN IF NOT EXISTS descricao VARCHAR(255);

-- Adiciona a coluna usuario_id à tabela setor para o relacionamento ManyToOne
ALTER TABLE setor ADD COLUMN IF NOT EXISTS usuario_id BIGINT;

-- Adiciona a constraint de chave estrangeira para a tabela usuario (assumindo que a tabela se chama 'usuario')
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_setor_usuario') THEN
        ALTER TABLE setor ADD CONSTRAINT fk_setor_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id);
    END IF;
END $$;
