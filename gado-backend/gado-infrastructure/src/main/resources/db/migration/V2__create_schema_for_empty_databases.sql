CREATE TABLE IF NOT EXISTS categoria (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    categoria VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS parceiro (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    nome VARCHAR(255),
    cpf_cnpj VARCHAR(255),
    endereco VARCHAR(255),
    telefone VARCHAR(255),
    data_cadastro TIMESTAMP,
    tipo VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS setor (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    nome VARCHAR(255),
    capacidade_maxima INTEGER,
    meta_texto VARCHAR(255),
    meta_producao_leite DOUBLE PRECISION,
    meta_arroba_abate DOUBLE PRECISION,
    setor VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS unidade_medida (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    unidade VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS usuario (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    nome VARCHAR(255),
    email VARCHAR(255),
    senha VARCHAR(255),
    perfil VARCHAR(255),
    data_cadastro TIMESTAMP
);

CREATE TABLE IF NOT EXISTS listas_tarefas (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    nome_lista VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS animal (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    codigo_brinco VARCHAR(255),
    nome VARCHAR(255),
    data_nascimento TIMESTAMP,
    peso_atual DOUBLE PRECISION,
    raca VARCHAR(255),
    cor VARCHAR(255),
    tamanho VARCHAR(255),
    sexo VARCHAR(255),
    status_animal VARCHAR(255),
    pessoa_id BIGINT,
    CONSTRAINT fk_animal_usuario FOREIGN KEY (pessoa_id) REFERENCES usuario (id)
);

CREATE TABLE IF NOT EXISTS lote (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    descricao VARCHAR(255),
    raca_predominante VARCHAR(255),
    usuario_id BIGINT,
    CONSTRAINT fk_lote_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

CREATE TABLE IF NOT EXISTS insumo (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    nome VARCHAR(255),
    estoque_minimo DOUBLE PRECISION,
    saldo_atual DOUBLE PRECISION,
    parceiro_id BIGINT,
    tipo VARCHAR(255),
    CONSTRAINT fk_insumo_parceiro FOREIGN KEY (parceiro_id) REFERENCES parceiro (id)
);

CREATE TABLE IF NOT EXISTS ocorrencia_animal (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    tipo_ocorrencia VARCHAR(255),
    data_ocorrencia TIMESTAMP,
    observacao VARCHAR(255),
    id_animal_id BIGINT,
    CONSTRAINT fk_ocorrencia_animal_animal FOREIGN KEY (id_animal_id) REFERENCES animal (id)
);

CREATE TABLE IF NOT EXISTS registro_financeiro (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    descricao VARCHAR(255),
    tipo_despesa VARCHAR(255),
    valor DOUBLE PRECISION,
    data_vencimento TIMESTAMP,
    data_pagamento TIMESTAMP,
    status_despesa VARCHAR(255),
    categoria_id BIGINT,
    pessoa_id BIGINT,
    CONSTRAINT fk_registro_financeiro_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
    CONSTRAINT fk_registro_financeiro_usuario FOREIGN KEY (pessoa_id) REFERENCES usuario (id)
);

CREATE TABLE IF NOT EXISTS tarefa (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    descricao VARCHAR(255),
    data_limite TIMESTAMP,
    status_conclusao BOOLEAN,
    lista_tarefa_id BIGINT,
    CONSTRAINT fk_tarefa_lista FOREIGN KEY (lista_tarefa_id) REFERENCES listas_tarefas (id)
);

CREATE TABLE IF NOT EXISTS transacao (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    data TIMESTAMP,
    valor DOUBLE PRECISION,
    parceiro_id BIGINT,
    lote_id BIGINT,
    CONSTRAINT fk_transacao_parceiro FOREIGN KEY (parceiro_id) REFERENCES parceiro (id),
    CONSTRAINT fk_transacao_lote FOREIGN KEY (lote_id) REFERENCES lote (id)
);

CREATE TABLE IF NOT EXISTS vacinacao (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    data_ocorrencia TIMESTAMP,
    usuario_id_id BIGINT,
    lote_id_id BIGINT,
    insumo_id_id BIGINT,
    CONSTRAINT fk_vacinacao_usuario FOREIGN KEY (usuario_id_id) REFERENCES usuario (id),
    CONSTRAINT fk_vacinacao_lote FOREIGN KEY (lote_id_id) REFERENCES lote (id),
    CONSTRAINT fk_vacinacao_insumo FOREIGN KEY (insumo_id_id) REFERENCES insumo (id)
);

CREATE TABLE IF NOT EXISTS movimentacao_estoque (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    en_tipo_movimentacao_estoque VARCHAR(255),
    quantidade DOUBLE PRECISION,
    valor_unitario DOUBLE PRECISION,
    data_movimentacao TIMESTAMP,
    lote_id BIGINT,
    setor_id_id BIGINT,
    parceiro_id_id BIGINT,
    insumo_id_id BIGINT,
    animal_id_id BIGINT,
    CONSTRAINT fk_movimentacao_lote FOREIGN KEY (lote_id) REFERENCES lote (id),
    CONSTRAINT fk_movimentacao_setor FOREIGN KEY (setor_id_id) REFERENCES setor (id),
    CONSTRAINT fk_movimentacao_parceiro FOREIGN KEY (parceiro_id_id) REFERENCES parceiro (id),
    CONSTRAINT fk_movimentacao_insumo FOREIGN KEY (insumo_id_id) REFERENCES insumo (id),
    CONSTRAINT fk_movimentacao_animal FOREIGN KEY (animal_id_id) REFERENCES animal (id)
);
