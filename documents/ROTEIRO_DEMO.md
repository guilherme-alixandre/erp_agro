# Roteiro de Demonstração — ERP Agro (GADO)

**Objetivo:** Apresentar ao cliente todas as funcionalidades do sistema percorrendo os quatro perfis de acesso em uma narrativa de uso real.

**Duração estimada:** 25–35 minutos  
**Dados sugeridos para criar antes da demo:** nenhum — tudo é criado ao vivo durante o roteiro.

---

## Contexto narrativo

> "A Fazenda Modelo tem um Administrador responsável pela operação geral, um Gerente que planeja as metas produtivas, um Cuidador Chefe que supervisiona o campo e dois Cuidadores que fazem o trabalho diário. Vamos acompanhar um dia completo de operação."

---

## PARTE 1 — Administração e cadastro de usuários
**Perfil em uso: ADMINISTRADOR**  
**Tempo estimado: 5 min**

### 1.1 Login
- Acesse o sistema e faça login com a conta do Administrador.
- Mostre a tela inicial de **Animais** — destaque o menu lateral (ou barra superior no mobile) com todos os módulos disponíveis: Animais, Lotes, Setores, Metas, Insumos, Financeiro, Perfil e **⚙ Configurações** (exclusivo do Administrador).

### 1.2 Configurações — Visão geral dos usuários
- Navegue até **⚙ Configurações**.
- Mostre a tabela de usuários já cadastrados.
- Use o filtro por perfil para exibir cada grupo separadamente.

### 1.3 Criar novos usuários
Crie os seguintes usuários (ou mostre os já existentes):

| Nome          | E-mail                       | Perfil          |
|---------------|------------------------------|-----------------|
| Ana Gerente   | ana@fazendamodelo.com        | Gerente         |
| Carlos Chefe  | carlos@fazendamodelo.com     | Cuidador Chefe  |
| Joao Cuidador | joao@fazendamodelo.com       | Cuidador        |

- Clique em **+ Novo Usuário**, preencha nome, e-mail, perfil e senha.
- Mostre que o Administrador pode alterar o perfil de qualquer usuário na edição.

> **Ponto de venda:** O Administrador controla quem acessa o quê, sem precisar de suporte técnico.

---

## PARTE 2 — Estrutura da fazenda: Setores e Lotes
**Perfil em uso: ADMINISTRADOR (ou GERENTE)**  
**Tempo estimado: 6 min**

### 2.1 Criar Setores
- Navegue até **Setores**.
- Crie os setores abaixo clicando em **+ Novo Setor**:

| Nome              | Tipo           | Capacidade |
|-------------------|----------------|------------|
| Pasto Principal   | Pastagem       | 50         |
| Pasto Secundário  | Pastagem       | 30         |
| Curral            | Confinamento   | 20         |
| Enfermaria        | Quarentena     | 10         |

- Mostre a tabela de setores com colunas de capacidade e lotes vinculados.
- Abra os **Detalhes** de um setor para mostrar os lotes que serão associados a ele.

### 2.2 Criar Lotes
- Navegue até **Lotes**.
- Crie dois lotes clicando em **+ Novo Lote**:

**Lote 1 — Engorda**
- Descrição: Lote Engorda 2025
- Cor do Brinco: Amarelo
- Raça Predominante: Nelore
- Setores: Pasto Principal (50 vagas), Curral (20 vagas)

**Lote 2 — Desmama**
- Descrição: Lote Desmama 2025
- Cor do Brinco: Azul
- Raça Predominante: Angus
- Setores: Pasto Secundário (30 vagas)

- Mostre a tabela de lotes e abra os **Detalhes** do Lote Engorda para visualizar as alocações por setor.

> **Ponto de venda:** A estrutura de setores e lotes reflete exatamente o mapa físico da fazenda. Qualquer reorganização é feita em segundos.

---

## PARTE 3 — Cadastro de animais
**Perfil em uso: CUIDADOR (Joao)**  
**Tempo estimado: 6 min**

### 3.1 Login como Cuidador
- Faça logout e entre com a conta do João.
- **Destaque:** o menu não tem Financeiro nem Configurações — o Cuidador só vê o que precisa para o seu trabalho.

### 3.2 Cadastrar animais
- Vá para **Animais** e clique em **+ Novo Animal**.
- **Destaque:** o vínculo com um lote e setor é **obrigatório** no cadastro — o sistema garante que todo animal entra na fazenda já alocado.
- Cadastre três animais:

**Animal 1 — Touro Adulto**
- Código Brinco: `BR-001`
- Nome: Trovão
- Raça: Nelore, Sexo: Macho
- Peso: 520 kg, Nascimento: (data ~5 anos atrás)
- Status: Ativo
- Vacinas: Febre Aftosa (data atual)
- Lote obrigatório: Lote Engorda → Pasto Principal

**Animal 2 — Vaca Leiteira**
- Código Brinco: `BR-002`
- Nome: Estrela
- Raça: Girolando, Sexo: Fêmea
- Peso: 380 kg, Nascimento: (data ~3 anos atrás)
- Status: Ativo
- Vacinar: Brucelose (data atual)
- Lote obrigatório: Lote Engorda → Pasto Principal

**Animal 3 — Bezerro**
- Código Brinco: `BR-003`
- Nome: Faísca
- Raça: Angus, Sexo: Macho
- Peso: 120 kg, Nascimento: (data ~8 meses atrás)
- Status: Ativo
- Sem vacinas
- Lote obrigatório: Lote Desmama → Pasto Secundário

### 3.3 Buscar e filtrar animais
- Busque por `"Trovão"` na barra de pesquisa.
- Limpe a busca e use o filtro **Sexo = Fêmea** para mostrar apenas a Estrela.
- Use o filtro de **Nascimento até** para mostrar animais nascidos antes do ano atual.
- Mostre o **Exportar CSV** — abra o arquivo para o cliente ver os dados.

### 3.4 Limitações do Cuidador
- Tente editar um animal criado por outro usuário — o botão **Editar** não aparece.
- Mostre que o Cuidador só edita e exclui animais que ele mesmo cadastrou.

> **Ponto de venda:** O campo registra o trabalho de cada funcionário. O sistema garante responsabilidade sem bloquear o fluxo diário.

---

## PARTE 4 — Supervisão de campo
**Perfil em uso: CUIDADOR CHEFE (Carlos)**  
**Tempo estimado: 5 min**

### 4.1 Login como Cuidador Chefe
- Faça logout e entre com a conta do Carlos.

### 4.2 Gestão ampliada de animais
- Em **Animais**, mostre que o Cuidador Chefe vê e **edita qualquer animal**, independente de quem cadastrou.
- Edite o Animal `BR-003` (Faísca): atualize o peso para 135 kg.

### 4.3 Gerenciar Lotes (transferência de animais)
- Vá para **Lotes** e abra os Detalhes do **Lote Engorda**.
- Mostre a lista de animais alocados por setor.
- Clique em **Editar Lote** e demonstre a transferência: mova o animal `BR-002` (Estrela) do Pasto Principal para o Curral.
- Salve e mostre a atualização imediata nas alocações.

### 4.4 Criar novo setor
- Vá para **Setores** e crie um novo setor:
  - Nome: Área de Ordenha
  - Tipo: Confinamento
  - Capacidade: 15
- Mostre que o Cuidador Chefe **não pode excluir** setores — apenas Administrador e Gerente têm essa permissão.

> **Ponto de venda:** A hierarquia funciona na prática. O supervisor tem autonomia operacional sem risco de apagar estrutura crítica da fazenda.

---

## PARTE 5 — Metas produtivas
**Perfil em uso: GERENTE (Ana)**  
**Tempo estimado: 5 min**

### 5.1 Login como Gerente
- Faça logout e entre com a conta da Ana.
- Mostre que o Gerente tem acesso ao **Financeiro** no menu, mas não às Configurações.

### 5.2 Criar metas de produção
- Vá para **Metas** e selecione o setor **Pasto Principal** no filtro.
- Clique em **+ Nova Meta** e crie:

**Meta 1 — Produção de Leite**
- Tipo: Leite
- Valor alvo: 5.000 litros
- Data início: primeiro dia do mês atual
- Data fim: último dia do mês atual

**Meta 2 — Engorda**
- Tipo: Arroba
- Valor alvo: 200 arrobas
- Data início: primeiro dia do mês atual
- Data fim: último dia do trimestre

### 5.3 Registrar medições
- Na **Meta de Leite**, clique em **+ Registrar Medição**.
  - Valor: 1.200 litros, Data: hoje
- Mostre a **barra de progresso** atualizando (24% da meta).
- Expanda o histórico de medições clicando em **Ver medições**.

### 5.4 Permissões nas medições
- Mostre que o Gerente pode **editar e excluir qualquer medição**.
- Explique: Cuidador edita só as próprias; Cuidador Chefe edita as do Cuidador e do Cuidador Chefe; Gerente e Admin editam todas.

> **Ponto de venda:** A fazenda tem visibilidade em tempo real do quanto está produzindo contra o que foi planejado, com histórico rastreável.

---

## PARTE 6 — Insumos e vacinas
**Perfil em uso: GERENTE (Ana) ou ADMINISTRADOR**  
**Tempo estimado: 3 min**

### 6.1 Navegar para Insumos
- Vá para **Insumos**.
- Mostre a aba **Vacinas** com as vacinas cadastradas automaticamente durante o cadastro dos animais.

### 6.2 Cadastrar nova vacina
- Clique em **+ Nova Vacina** e cadastre:
  - Nome: Raiva Bovina
  - Status: Pendente (aguardando aplicação)
- Mostre o **badge amarelo "PENDENTE"** na lista.

### 6.3 Confirmar aplicação
- Abra os detalhes da vacina pendente e clique em **Confirmar** para marcar como aplicada.
- O badge muda de status.

> **Ponto de venda:** O controle sanitário fica centralizado. Sem planilhas paralelas ou fichas de papel.

---

## PARTE 7 — Perfil pessoal
**Perfil em uso: qualquer (demonstrar com CUIDADOR)**  
**Tempo estimado: 2 min**

### 7.1 Editar nome e senha
- Navegue até **Perfil**.
- Mostre as informações da sessão: nome, e-mail e perfil atual.
- Clique em **Editar perfil** e altere o nome.
- Acesse a seção de senha e demonstre a troca (campos: senha atual, nova senha, confirmação).
- Mostre o **toggle de visibilidade** da senha (mostrar/ocultar).

> **Nota:** Somente o Administrador pode alterar o próprio perfil de acesso. Os demais usuários apenas editam nome e senha.

---

## PARTE 8 — Encerramento: volta ao Administrador
**Perfil em uso: ADMINISTRADOR**  
**Tempo estimado: 2 min**

### 8.1 Visão geral do sistema
- Faça logout e entre novamente como Administrador.
- Percorra rapidamente todos os módulos mostrando os dados criados durante a demo:
  - **Animais** → 3 animais cadastrados, filtros funcionando
  - **Lotes** → 2 lotes com animais alocados nos setores corretos
  - **Setores** → 5 setores, incluindo o criado pelo Cuidador Chefe
  - **Metas** → barra de progresso com medição registrada
  - **Insumos** → vacinas registradas e confirmadas
  - **Configurações** → 4 usuários ativos com diferentes perfis

### 8.2 Editar perfil de usuário
- Nas Configurações, edite o usuário **Joao Cuidador** e promova-o para **Cuidador Chefe**.
- Mostre que a mudança é imediata — na próxima vez que João logar, terá as permissões expandidas.

---

## Tabela resumo de permissões (para deixar na tela)

| Funcionalidade                     | Admin | Gerente | Cuid. Chefe | Cuidador |
|------------------------------------|:-----:|:-------:|:-----------:|:--------:|
| ⚙ Configurações (usuários)         | ✅    | ❌      | ❌          | ❌       |
| 💰 Financeiro                      | ✅    | ✅      | ❌          | ❌       |
| Criar / excluir Lotes              | ✅    | ✅      | ❌          | ❌       |
| Editar Lotes / transferir animais  | ✅    | ✅      | ✅          | ❌       |
| Criar / editar Setores             | ✅    | ✅      | ✅          | ❌       |
| Excluir Setores                    | ✅    | ✅      | ❌          | ❌       |
| Cadastrar Animais                  | ✅    | ✅      | ✅          | ✅       |
| Editar qualquer Animal             | ✅    | ✅      | ✅          | ❌ (só próprios) |
| Excluir qualquer Animal            | ✅    | ✅      | ❌ (só próprios) | ❌ (só próprios) |
| Criar / excluir Metas              | ✅    | ✅      | ❌          | ❌       |
| Registrar Medições                 | ✅    | ✅      | ✅          | ✅       |
| Editar qualquer Medição            | ✅    | ✅      | ✅ (exceto admin/gerente) | ❌ (só próprias) |
| Alterar próprio perfil de acesso   | ✅    | ❌      | ❌          | ❌       |
| Exportar CSV                       | ✅    | ✅      | ✅          | ✅       |

---

## Dicas para a apresentação

- **Tenha os dados pré-carregados** se o tempo for curto: crie um ambiente de demo com usuários e estrutura básica pronta, e use o roteiro apenas para as ações mais visuais (filtros, metas, transferências).
- **Use dois dispositivos em paralelo** para mostrar a diferença de menus entre Administrador e Cuidador lado a lado.
- **Foque nas histórias**, não nos campos: "o João cadastrou o bezerro no campo usando o celular" é mais impactante do que "o campo Código Brinco aceita alfanumérico".
- **Perguntas frequentes de clientes:**
  - *"E se o funcionário sair da empresa?"* → Administrador edita o perfil ou desativa o usuário nas Configurações.
  - *"Posso ver o histórico de quem fez o quê?"* → Cada animal, lote e medição registra o e-mail de quem criou.
  - *"Funciona no celular?"* → Sim, o sistema é responsivo.
