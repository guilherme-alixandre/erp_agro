# Guia de Autenticação e Segurança — GADO

Este documento explica, de forma prática, os conceitos de segurança usados na correção de autenticação do sistema GADO: hashing (SHA-256), JWT e controle de acesso por perfil (RBAC). A ideia é que qualquer pessoa do time consiga entender **o que mudou e por quê**, mesmo sem experiência prévia em segurança.

---

## 1. O problema que motivou essa mudança

Antes desta correção, o GADO funcionava assim:

1. O usuário fazia login com e-mail e senha.
2. O backend conferia a senha e devolvia os dados do usuário (incluindo o perfil: `ADMINISTRADOR`, `GERENTE`, etc.).
3. O frontend guardava esses dados no navegador e, em **cada requisição seguinte**, reenviava o e-mail do usuário num header simples: `X-Usuario-Email: fulano@exemplo.com`.
4. O backend confiava nesse header para saber "quem está fazendo essa requisição" e decidir se a ação era permitida para aquele perfil.

O problema: esse header **não tem nenhuma proteção**. Qualquer pessoa com acesso ao navegador (aba de DevTools) ou usando uma ferramenta como Postman pode trocar o valor desse header para o e-mail de um ADMINISTRADOR e passar a agir como ele, sem saber a senha de ninguém. Não é uma falha sutil — é a ausência de um mecanismo básico de autenticação.

A correção troca esse header "de confiança" por um **token assinado (JWT)**, que o servidor consegue verificar matematicamente, e centraliza as regras de "quem pode fazer o quê" no backend, em vez de espalhar `if`s manuais por vários arquivos.

---

## 2. Hashing e SHA-256: por que senha não fica em texto puro

**Hashing** é uma função matemática que transforma qualquer texto em uma sequência de tamanho fixo (o "hash"), de forma que:
- É praticamente impossível reverter o hash de volta para o texto original.
- A mesma entrada sempre gera o mesmo hash.
- Uma pequena mudança na entrada gera um hash completamente diferente.

Por isso, sistemas nunca guardam a senha do usuário diretamente no banco — guardam o **hash** dela. No login, em vez de comparar "senha digitada == senha salva", o sistema compara "hash da senha digitada == hash salvo".

O GADO usava **SHA-256** para isso. O problema é que SHA-256 foi feito para ser **rápido** (ótimo para verificar integridade de arquivos, por exemplo), e rapidez é exatamente o que você **não** quer num hash de senha: se o banco de dados vazar, um invasor consegue testar bilhões de senhas por segundo contra hashes SHA-256 usando hardware comum (GPU), e "adivinhar" boa parte das senhas por força bruta (esse ataque é ainda mais fácil sem *salt* — um valor aleatório único por senha, que o GADO também não usava).

**BCrypt** (o algoritmo que passou a ser usado) resolve isso de duas formas:
- Tem um **salt** aleatório embutido em cada hash, então duas pessoas com a mesma senha geram hashes diferentes.
- É **deliberadamente lento** (e o quão lento é configurável), o que torna a força bruta inviável na prática, mesmo que o banco vaze.

No código, isso se traduz em usar `BCryptPasswordEncoder` do Spring Security em vez de calcular o SHA-256 manualmente.

---

## 3. JWT (JSON Web Token): como o servidor sabe quem você é, sem guardar sessão

JWT é um token que o servidor gera no login e devolve para o cliente (frontend), que passa a enviá-lo em toda requisição seguinte no header `Authorization: Bearer <token>`. Ele tem três partes, separadas por ponto, cada uma em Base64:

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmdWxhbm9AZXhlbXBsby5jb20iLCJwZXJmaWwiOiJBRE1JTklTVFJBRE9SIn0.4f8a...assinatura
   \_____ header _____/   \_______________ payload _______________/   \___ signature ___/
```

- **Header**: diz qual algoritmo de assinatura foi usado (ex.: HMAC-SHA256).
- **Payload**: os dados ("claims") — no GADO, isso inclui o e-mail do usuário, o perfil e a data de expiração do token. Importante: o payload **não é criptografado**, só está em Base64 (qualquer um consegue decodificar e ler). Por isso, nunca se coloca senha ou dado sensível no payload.
- **Signature**: uma assinatura calculada pelo servidor usando uma chave secreta que só ele conhece. É essa assinatura que torna o token confiável.

O ponto-chave: **o servidor não precisa guardar o token em lugar nenhum** (por isso "stateless" — sem estado de sessão). Quando uma requisição chega com um token, o servidor recalcula a assinatura usando sua chave secreta e compara com a assinatura do token. Se bater, ele sabe que:
1. O token foi realmente emitido por ele (ninguém consegue forjar a assinatura sem a chave secreta).
2. Os dados dentro do token (e-mail, perfil) não foram alterados no caminho — qualquer alteração no payload muda a assinatura esperada.

É exatamente isso que faltava no GADO: o header antigo (`X-Usuario-Email`) era um "payload" sem assinatura nenhuma — qualquer um podia escrever o que quisesse nele. O JWT resolve isso porque só quem tem a chave secreta do servidor consegue gerar um token válido.

Diferença para sessão tradicional: numa sessão, o servidor guarda um registro ("fulano está logado, id de sessão X") em memória ou banco. Com JWT, essa informação viaja dentro do próprio token, assinada — o servidor só precisa da chave secreta para validar, não de um banco de sessões.

---

## 4. Autenticação vs. Autorização (RBAC)

Dois conceitos que parecem a mesma coisa mas são diferentes:

- **Autenticação** = "quem é você?" — confirmado pelo login (senha certa) e, depois, pelo JWT válido em cada requisição.
- **Autorização** = "o que você tem permissão de fazer, já sabendo quem você é?" — no GADO, isso depende do `perfil` do usuário (`ADMINISTRADOR`, `GERENTE`, `CUIDADOR`, `CUIDADOR_CHEFE`, `FINANCEIRO`).

Esse modelo, em que permissões são atribuídas por "papel"/perfil em vez de usuário por usuário, se chama **RBAC** (Role-Based Access Control).

Um erro comum — que o GADO tinha — é implementar a autorização **só na tela**: esconder o botão ou o menu para quem não tem o perfil certo. Isso melhora a experiência de uso, mas não é segurança nenhuma, porque nada impede alguém de chamar a API diretamente (Postman, curl, ou até editando o JavaScript no navegador) e pular a tela inteira. A regra de autorização **precisa ser verificada no backend**, que é o único lugar que o usuário não controla.

No GADO, isso agora é feito com a anotação `@PreAuthorize` do Spring Security diretamente nos métodos do controller, por exemplo:

```java
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FINANCEIRO')")
@PostMapping
public ResponseEntity<...> registrarLancamentoManual(...) { ... }
```

Isso substitui os `if (!PERFIS_MODULO.contains(usuario.getPerfil()))` que antes ficavam espalhados dentro de cada service — regra de autorização centralizada, e aplicada antes mesmo do método ser executado, com base no perfil que veio (de forma confiável) dentro do JWT.

---

## 5. Outras práticas relacionadas (resumo rápido)

- **HTTPS**: criptografa a comunicação entre navegador e servidor. Sem HTTPS, mesmo um JWT bem feito pode ser interceptado em trânsito (ex.: numa rede Wi-Fi pública) e reutilizado por outra pessoa até expirar. Em produção, o GADO deve rodar atrás de HTTPS.
- **CORS** (Cross-Origin Resource Sharing): controla quais sites (origens) têm permissão de fazer requisições ao backend a partir do navegador. O GADO já restringe isso ao endereço do frontend (`http://localhost:5173` em desenvolvimento).
- **Expiração de token**: o JWT do GADO expira depois de um tempo (poucas horas), para que um token vazado não sirva para sempre. Depois de expirar, o usuário precisa logar de novo.

---

## 6. Como isso foi aplicado no GADO (resumo da mudança)

| Antes | Depois |
|---|---|
| Senha guardada com SHA-256 puro (sem salt) | Senha guardada com BCrypt (salt + custo ajustável) |
| "Autenticação" = header `X-Usuario-Email` sem assinatura, fácil de falsificar | Autenticação = JWT assinado, gerado no login, validado em toda requisição |
| Autorização feita com `if`s manuais espalhados em ~13 services, e ausente em outros ~11 | Autorização centralizada com `@PreAuthorize` por perfil, em todos os endpoints |
| Perfil restrito só escondia botão/menu no frontend — API continuava aberta | Perfil restrito é verificado no backend antes de executar qualquer ação |

Com isso, testar "o que cada perfil pode ou não fazer" passa a fazer sentido como QA — antes, qualquer resultado desse teste poderia ser contornado trocando um header, então não refletia a segurança real do sistema.
