ALTER TABLE parceiro ADD COLUMN email VARCHAR(255);
ALTER TABLE parceiro ADD CONSTRAINT uq_parceiro_cpf_cnpj UNIQUE (cpf_cnpj);
