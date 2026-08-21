-- Módulo Financeiro: Documentos de Entrada (NF-e/recibos), Folha de Pagamento e o
-- livro-razão (lancamento_financeiro) que alimenta o resumo mensal (DRE).

CREATE TABLE IF NOT EXISTS documento_entrada (
    id                      BIGSERIAL PRIMARY KEY,
    status                  VARCHAR(255),
    created_at              TIMESTAMP,
    updated_at              TIMESTAMP,
    tipo_documento          VARCHAR(20)   NOT NULL,
    status_aprovacao        VARCHAR(25)   NOT NULL,
    numero_documento        VARCHAR(50),
    serie                   VARCHAR(10),
    chave_acesso_nfe        VARCHAR(44),
    data_emissao            DATE          NOT NULL,
    data_entrada            DATE          NOT NULL,
    fornecedor_id           BIGINT,
    valor_total             NUMERIC(15,2) NOT NULL,
    xml_original            TEXT,
    justificativa_recusa    VARCHAR(255),
    criado_por_email        VARCHAR(255)  NOT NULL,
    aprovado_por_email      VARCHAR(255),
    aprovado_em             TIMESTAMP,
    ultima_edicao_por_email VARCHAR(255),
    ultima_edicao_em        TIMESTAMP,
    CONSTRAINT uq_documento_entrada_chave_acesso_nfe UNIQUE (chave_acesso_nfe),
    CONSTRAINT fk_documento_entrada_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES parceiro (id)
);

CREATE TABLE IF NOT EXISTS documento_entrada_item (
    id                   BIGSERIAL PRIMARY KEY,
    status               VARCHAR(255),
    created_at           TIMESTAMP,
    updated_at           TIMESTAMP,
    documento_entrada_id BIGINT           NOT NULL,
    descricao_xml        VARCHAR(255)     NOT NULL,
    codigo_xml           VARCHAR(60),
    produto_id           BIGINT,
    quantidade           NUMERIC(15,3)    NOT NULL,
    valor_unitario       NUMERIC(15,4)    NOT NULL,
    valor_total          NUMERIC(15,2)    NOT NULL,
    vinculado            BOOLEAN          NOT NULL DEFAULT FALSE,
    vinculado_por_email  VARCHAR(255),
    vinculado_em         TIMESTAMP,
    natureza_financeira  VARCHAR(20),
    CONSTRAINT fk_documento_entrada_item_documento FOREIGN KEY (documento_entrada_id) REFERENCES documento_entrada (id),
    CONSTRAINT fk_documento_entrada_item_produto FOREIGN KEY (produto_id) REFERENCES insumo (id)
);

CREATE TABLE IF NOT EXISTS funcionario (
    id                     BIGSERIAL PRIMARY KEY,
    status                 VARCHAR(255),
    created_at             TIMESTAMP,
    updated_at             TIMESTAMP,
    usuario_id             BIGINT,
    nome_completo          VARCHAR(255)  NOT NULL,
    cpf                    VARCHAR(14)   NOT NULL,
    cargo                  VARCHAR(255)  NOT NULL,
    data_admissao          DATE          NOT NULL,
    data_demissao          DATE,
    salario_base           NUMERIC(15,2) NOT NULL,
    percentual_inss        NUMERIC(5,2)  NOT NULL,
    percentual_fgts        NUMERIC(5,2)  NOT NULL,
    valor_vale_transporte  NUMERIC(15,2),
    valor_vale_alimentacao NUMERIC(15,2),
    valor_plano_saude      NUMERIC(15,2),
    natureza_financeira    VARCHAR(20)   NOT NULL,
    CONSTRAINT uq_funcionario_cpf UNIQUE (cpf),
    CONSTRAINT uq_funcionario_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_funcionario_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

CREATE TABLE IF NOT EXISTS pagamento_funcionario (
    id                            BIGSERIAL PRIMARY KEY,
    status                        VARCHAR(255),
    created_at                    TIMESTAMP,
    updated_at                    TIMESTAMP,
    funcionario_id                BIGINT        NOT NULL,
    ano_referencia                INTEGER       NOT NULL,
    mes_referencia                INTEGER       NOT NULL,
    data_pagamento                DATE          NOT NULL,
    status_pagamento              VARCHAR(20)   NOT NULL,
    valor_bruto                   NUMERIC(15,2) NOT NULL,
    desconto_inss                 NUMERIC(15,2) NOT NULL,
    desconto_outros               NUMERIC(15,2),
    encargo_fgts                  NUMERIC(15,2) NOT NULL,
    valor_beneficios              NUMERIC(15,2),
    valor_liquido                 NUMERIC(15,2) NOT NULL,
    natureza_financeira_snapshot  VARCHAR(20)   NOT NULL,
    CONSTRAINT fk_pagamento_funcionario_funcionario FOREIGN KEY (funcionario_id) REFERENCES funcionario (id)
);

CREATE TABLE IF NOT EXISTS lancamento_financeiro (
    id                   BIGSERIAL PRIMARY KEY,
    status               VARCHAR(255),
    created_at           TIMESTAMP,
    updated_at           TIMESTAMP,
    tipo_movimento       VARCHAR(10)   NOT NULL,
    natureza_financeira  VARCHAR(20),
    origem               VARCHAR(25)   NOT NULL,
    origem_id            BIGINT,
    descricao            VARCHAR(255)  NOT NULL,
    valor                NUMERIC(15,2) NOT NULL,
    data_competencia     DATE          NOT NULL,
    ano_competencia      INTEGER       NOT NULL,
    mes_competencia      INTEGER       NOT NULL,
    virtual              BOOLEAN       NOT NULL DEFAULT FALSE,
    criado_por_email     VARCHAR(255)  NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_lancamento_financeiro_bloco ON lancamento_financeiro (ano_competencia, mes_competencia);
CREATE INDEX IF NOT EXISTS idx_lancamento_financeiro_origem ON lancamento_financeiro (origem, origem_id);
