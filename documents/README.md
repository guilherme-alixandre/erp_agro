# GADO - Gerenciamento de Agronegócio Digital e Organizado

## Sobre o Projeto
O **GADO** é um Sistema de ERP para o Agronegócio desenvolvido para gerenciar e integrar os processos de uma fazenda. O foco principal desta versão é a gestão de **gado de corte** (cria, recria e engorda) e o **controle financeiro**.
Este projeto foi documentado e desenvolvido pela **Fábrica de Software Acadêmica** do IFG - Câmpus Inhumas, utilizando a metodologia PRAXIS.

---

## Funcionalidades Principais
Conforme definido na especificação do projeto, o sistema abrange os seguintes módulos:

- **Gestão de Animais:** Cadastro completo (vacinas, peso, lote) e rastreabilidade.
- **Controle de Estoque/Insumos:** Registro de entrada e saída de insumos (medicamentos, ração).
- **Financeiro:** Contas a pagar, contas a receber e fluxo de caixa.
- **Planejamento:** Lista de atividades diárias e metas por setor.
- **Dashboard:** Visão geral com indicadores de vendas, gastos e alertas de estoque.

> **Nota:** Nesta versão, o sistema não contempla a gestão de plantio (lavoura) ou integração com balanças de hardware.

---

## Autores
Projeto desenvolvido pelos discentes:
* Pedro Américo Rocha
* Guilherme Alixandre Gonçalves Silva
* Gabriel Araujo de Ataides
* Pedro Henrique Portela de Souza
* João Antônio André Barbosa Camilo

---

## Como Baixar o Projeto

Você pode obter o código-fonte de duas maneiras: via linha de comando ou usando uma interface visual.

### Opção 1: Clonando via GitHub Desktop
Se você não gosta de usar o terminal, o **GitHub Desktop** facilita o processo:

1.  Baixe e instale o [GitHub Desktop](https://desktop.github.com/).
2.  Abra o aplicativo e faça login na sua conta do GitHub.
3.  Vá em **File** > **Clone repository...**
4.  Selecione a aba **URL**.
5.  No campo "Repository URL", cole o link abaixo:
    ```
    [https://github.com/SEU_USUARIO/nome-do-repositorio.git](https://github.com/SEU_USUARIO/nome-do-repositorio.git)
    ```
6.  Escolha a pasta onde quer salvar no seu computador (Local path) e clique em **Clone**.

### Opção 2: Clonando via Terminal (Git Bash / CMD)
Se você já tem o Git instalado:

1.  Abra o seu terminal.
2.  Navegue até a pasta onde deseja salvar o projeto.
3.  Execute o comando:
    ```bash
    git clone [https://github.com/SEU_USUARIO/nome-do-repositorio.git](https://github.com/SEU_USUARIO/nome-do-repositorio.git)
    ```

### Obs: Pode adquirir o link da url em "Code"

<img width="1300" height="763" alt="image" src="https://github.com/user-attachments/assets/d10b9bf9-35bf-4391-89ef-05032500c32d" />

---

## Como Executar

Este projeto utiliza **Java** e **Maven**. Para rodar a aplicação:

1.  Abra o projeto na sua IDE (IntelliJ IDEA é recomendada).
2.  Aguarde o Maven baixar as dependências.
3.  Localize a classe principal na pasta `src/main/java` (geralmente `GadoApplication.java`).
4.  Clique com o botão direito e selecione **Run**.

---
**Instituição:** Instituto Federal de Goiás (IFG) - Câmpus Inhumas.
