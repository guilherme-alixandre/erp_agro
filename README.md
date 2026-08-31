# GADO — Gerenciamento de Agronegócio Digital e Organizado

O GADO é um ERP web para a gestão de pequenas e médias fazendas, com foco atual em gado de corte. O sistema reúne rebanho, lotes, setores, insumos, tarefas, metas e financeiro em uma interface responsiva, pensada primeiro para o uso no celular e também adequada ao acompanhamento em desktop.

O projeto foi desenvolvido pela Fábrica de Software Acadêmica do IFG — Câmpus Inhumas com base na metodologia PRAXIS.

## O que o sistema oferece

- **Resumo da fazenda:** indicadores do rebanho, lotes, setores, resultado financeiro, alertas de estoque e tarefas pendentes.
- **Animais e raças:** cadastro, busca, filtros, medidas zootécnicas, ocorrências, status e exportação CSV.
- **Lotes e setores:** agrupamento de animais, capacidade, alocação, transferência e relatórios em PDF.
- **Metas:** objetivos por setor, medições, acompanhamento de progresso e relatório em PDF.
- **Insumos:** catálogo, unidades, estoque, entradas, alimentação de setores, consumo, vacinação e histórico de movimentações.
- **Financeiro:** notas fiscais, recibos, vendas, aprovações, parceiros, funcionários, pagamentos e dashboard.
- **Tarefas:** distribuição, acompanhamento e conclusão das atividades da equipe.
- **Usuários e perfis:** autenticação JWT, edição de perfil e controle de acesso por função.

O escopo atual não inclui lavoura nem integração direta com balanças ou outros equipamentos.

## Estrutura do repositório

```text
erp_agro/
├── gado-backend/       API REST em Java/Spring Boot
├── gado-frontend/      aplicação web em React/Vite
├── Arquivos Projeto/   requisitos, diagramas, backlog e documentação acadêmica
└── README.md            este guia
```

### Tecnologias

| Camada | Tecnologias principais |
|---|---|
| Front-end | React 18, Vite 5, CSS responsivo e Recharts |
| Backend | Java 21, Spring Boot 4, Spring Security e Spring Data JPA |
| Banco | PostgreSQL 15 e Flyway |
| Segurança | JWT e autorização baseada em perfis (RBAC) |
| Relatórios | OpenPDF |

## Pré-requisitos

Instale as ferramentas abaixo antes de iniciar:

- Java JDK 21;
- Node.js 20 ou superior e npm;
- Docker Desktop com Docker Compose, ou PostgreSQL 15 configurado manualmente;
- Git.

Confirme a instalação com:

```bash
java -version
node --version
npm --version
docker --version
```

## Execução local

### 1. Obtenha o projeto

```bash
git clone <URL_DO_REPOSITORIO>
cd erp_agro
```

### 2. Inicie o PostgreSQL

O Compose cria o banco `gado_db`, usuário `admin`, senha `12345` e publica o PostgreSQL na porta `5433`.

```bash
cd gado-backend
docker compose up -d
```

Para confirmar que o contêiner iniciou:

```bash
docker compose ps
```

Os dados ficam no volume Docker `gado-db-data`. As migrações de `gado-backend/src/main/resources/db/migration` são aplicadas automaticamente pelo Flyway quando a API inicia.

### 3. Inicie o backend

No Windows PowerShell:

```powershell
cd gado-backend
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```bash
cd gado-backend
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:8080/api`.

### 4. Inicie o front-end

Em outro terminal:

```bash
cd gado-frontend
npm ci
npm run dev
```

Abra `http://localhost:5173`. Durante o desenvolvimento, o Vite encaminha chamadas de `/api` para `http://localhost:8080`, evitando problemas de CORS.

### 5. Primeiro acesso

Em um banco novo, a migração inicial cria uma conta administrativa:

- **E-mail:** `admin@erp.com`
- **Senha inicial:** `admin123`

Altere a senha após o primeiro acesso. O administrador pode criar os demais usuários pela tela **Equipe**.

## Configuração

### Front-end

O endereço da API é lido de `VITE_API_BASE_URL`.

```bash
# gado-frontend/.env.local
VITE_API_BASE_URL=https://api.exemplo.com/api
```

Sem essa variável, o front-end usa `/api`, que é encaminhado ao backend local pelo proxy do Vite.

Comandos disponíveis em `gado-frontend`:

```bash
npm run dev       # servidor de desenvolvimento
npm run build     # build otimizado em dist/
npm run preview   # visualização local do build
npm run lint      # análise estática do código
```

### Backend

A configuração local atual fica em `gado-backend/src/main/resources/application.properties`:

| Propriedade | Valor local |
|---|---|
| Banco | `jdbc:postgresql://localhost:5433/gado_db` |
| Usuário | `admin` |
| Senha | `12345` |
| Perfil Spring | `dev` |
| Duração do JWT | 8 horas |

Esses valores são apenas de desenvolvimento. Antes de publicar, externalize as credenciais e a chave JWT em variáveis de ambiente ou em um gerenciador de segredos.

## Perfis e acesso

| Perfil | Uso esperado |
|---|---|
| `ADMINISTRADOR` | acesso completo, inclusive usuários e permissões |
| `GERENTE` | operação e supervisão da fazenda, incluindo financeiro |
| `FINANCEIRO` | documentos, lançamentos, parceiros e folha de pagamento |
| `CUIDADOR_CHEFE` | rotina operacional e coordenação dos cuidadores |
| `CUIDADOR` | atividades operacionais autorizadas |

O front-end oculta módulos incompatíveis com o perfil para simplificar a navegação. A autorização efetiva também é verificada no backend; ocultar um botão não é usado como mecanismo de segurança.

## Organização do front-end

```text
gado-frontend/src/
├── components/shared/  estrutura visual e componentes reutilizáveis
├── features/           módulos separados por domínio
│   └── <modulo>/
│       ├── components/
│       ├── integration/
│       ├── pages/
│       └── styles/
├── integration/        cliente HTTP, token e erros comuns
├── styles/             sistema visual compartilhado
├── App.jsx              sessão, autorização de telas e navegação
└── main.jsx             inicialização do React
```

Ao alterar uma tela:

1. preserve os nomes e formatos usados em `integration/`, pois eles representam o contrato com a API;
2. reutilize as classes e variáveis do sistema visual antes de criar novos estilos;
3. mantenha alvos de toque com pelo menos 44 px e teste a largura de 320 px;
4. mantenha busca, filtros e ação principal no início da tela;
5. mostre mensagens de carregamento, vazio, sucesso e erro;
6. não use somente cor para comunicar status.

## Organização do backend

```text
gado-backend/src/main/
├── java/br/com/gado/
│   ├── controllers/    endpoints REST e regras de acesso
│   ├── services/       regras de negócio
│   ├── repositories/   persistência JPA
│   ├── entities/       modelo de dados
│   ├── dtos/           entrada e saída da API
│   ├── security/       JWT e autenticação
│   └── config/         CORS, erros globais e inicialização
└── resources/
    ├── db/migration/   evolução versionada do banco
    └── application.properties
```

A API usa o prefixo `/api`. Os principais recursos são `/usuarios`, `/resumo`, `/animais`, `/racas`, `/lotes`, `/setores`, `/metas-setor`, `/tarefas`, `/insumos`, `/consumo-estoque`, `/vacinacoes-animal`, `/documentos-entrada`, `/documentos-saida`, `/financeiro/lancamentos`, `/parceiros` e `/folha-pagamento`.

## Validação antes de entregar

Front-end:

```bash
cd gado-frontend
npm run lint
npm run build
```

Backend:

```bash
cd gado-backend
./mvnw test
```

No Windows, substitua o último comando por `.\mvnw.cmd test`.

Também faça uma verificação manual nos seguintes tamanhos:

- celular compacto: 320 × 568;
- celular comum: 390 × 844;
- tablet: 768 × 1024;
- desktop: 1440 × 900.

Confira login, troca de módulos, busca, filtros, tabelas com rolagem, abertura e fechamento de modais, operações de cadastro/edição e restrições por perfil.

## Solução de problemas

### O front-end informa que não encontrou o servidor

- confirme que a API está na porta `8080`;
- confirme que o banco está ativo com `docker compose ps`;
- verifique se `VITE_API_BASE_URL` termina com `/api` quando uma URL externa é usada;
- reinicie o Vite depois de alterar um arquivo `.env`.

### O backend não conecta ao PostgreSQL

- confirme que a porta `5433` está livre;
- confira banco, usuário e senha em `application.properties` e `docker-compose.yml`;
- veja os logs com `docker compose logs postgres-gado`.

### Uma migração Flyway falhou

Não edite uma migração já aplicada. Corrija o problema em uma nova migração versionada e confira o histórico do schema antes de reiniciar a API.

### A sessão expirou

O token local expira em 8 horas. Entre novamente. Respostas `401` removem a sessão do navegador automaticamente.

## Documentação complementar

Na pasta `Arquivos Projeto` estão o backlog, DER, casos de uso, documentos PRAXIS e o guia de autenticação e segurança. Esses arquivos registram decisões acadêmicas e de domínio; este README é o ponto de entrada para execução e desenvolvimento.

## Equipe

- Pedro Américo Rocha
- Guilherme Alixandre Gonçalves Silva
- Gabriel Araujo de Ataides
- Pedro Henrique Portela de Souza
- João Antônio André Barbosa Camilo

**Instituição:** Instituto Federal de Goiás (IFG) — Câmpus Inhumas.
