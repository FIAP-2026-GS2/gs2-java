# SampaTech · Portal da Transparência da zeladoria de São Paulo

Backend Java da solução **SampaTech**, desenvolvida para a Global Solution 2026/2 da FIAP (tema **CitizenTech**).

As câmeras do programa Smart Sampa e um modelo de IA identificam problemas urbanos (buracos, calçadas danificadas, lixo acumulado, postes apagados) e registram cada ocorrência no banco. Este projeto expõe esses dados de forma aberta: uma **API REST** consumida pelo app Mobile, um **portal web** em Thymeleaf e um **assistente com IA generativa** que responde perguntas do cidadão consultando os dados reais.

| | Link |
|---|---|
| Portal web e API (deploy) | https://sampatech-portal.onrender.com |
| Swagger (deploy) | https://sampatech-portal.onrender.com/swagger-ui.html |
| Notification service (deploy) | https://gs2-java-ipgs.onrender.com |
| Repositório | https://github.com/FIAP-2026-GS2/gs2-java |
| Vídeo pitch | _adicionar link_ |

### Os dois deploys

| Deploy | O que é | Quando acessar |
|---|---|---|
| `sampatech-portal` | o **portal-service**: API REST consumida pelo app, portal web e assistente com IA | é o endereço usado no app, no navegador e no Swagger |
| `gs2-java-ipgs` | o **notification-service**: consumidor do RabbitMQ que envia o e-mail de boas-vindas e grava os logs e o histórico do chat | **não tem rotas web**, e o endereço responde 404. Ele existe só para ser **acordado** |

Os dois rodam no plano gratuito do Render, que coloca um serviço para dormir depois de 15 minutos sem receber requisições HTTP. O portal acorda sozinho quando alguém acessa (o primeiro acesso leva cerca de 50 segundos). O notification não recebe requisições HTTP, só mensagens do RabbitMQ, e mensagens não acordam o serviço.

**Antes de testar cadastro (e-mail de boas-vindas), buscas ou histórico do chat, abra https://gs2-java-ipgs.onrender.com e espere alguns segundos.** A página de erro 404 é esperada. Se ele estiver dormindo, nada se perde: as mensagens ficam guardadas nas filas e são processadas assim que ele acorda.

---

## Integrantes

| Nome | RM |
|---|---|
| Ana Carolina Pereira Fontes | 562145 |
| João Victor Nascimento Adão | 563409 |
| Johnny Dias Mathias Junior | 566516 |
| Luisa Ganasevici de Abreu | 563403 |
| Matheus Moya de Oliveira | 562822 |

---

## Sumário

1. [Como a solução colabora com o cidadão](#como-a-solução-colabora-com-o-cidadão)
2. [Arquitetura](#arquitetura)
3. [Requisito 5: opção escolhida](#requisito-5-opção-escolhida)
4. [Tecnologias](#tecnologias)
5. [Modelagem](#modelagem)
6. [Segurança](#segurança)
7. [Funcionalidade com Spring AI](#funcionalidade-com-spring-ai)
8. [Validação e tratamento de erros](#validação-e-tratamento-de-erros)
9. [Endpoints principais](#endpoints-principais)
10. [Portal web](#portal-web)
11. [Serviços externos](#serviços-externos)
12. [Como rodar](#como-rodar)
13. [Usuários de teste](#usuários-de-teste)
14. [Testes](#testes)
15. [Estrutura do projeto](#estrutura-do-projeto)
16. [Declaração de uso de IA](#declaração-de-uso-de-ia)

---

## Como a solução colabora com o cidadão

Problemas de zeladoria costumam depender de uma reclamação para entrar na fila de conserto, e quem reclama raramente fica sabendo se e quando o problema foi resolvido.

No SampaTech, o registro é automático (câmera + IA) e cada ocorrência fica pública, com status, local, gravidade, datas e histórico. Qualquer pessoa pode:

- consultar ocorrências por tipo, status, bairro, rua e período;
- acompanhar em que etapa cada conserto está e há quanto tempo ele espera solução;
- perguntar ao assistente em linguagem natural ("tem buraco aberto na Paulista?") e receber uma resposta baseada nos dados reais;
- com uma conta, manter o histórico das conversas com o assistente.

Gestores (perfil ADMIN) mantêm o catálogo de tipos de ocorrência, os órgãos municipais e os usuários.

O portal Java é um **portal de transparência**: ele mostra os **dados** de cada ocorrência (tipo, gravidade, status, local, datas e histórico) e **não exibe imagens nem vídeos das câmeras**. Nenhum dado pessoal de cidadãos aparece nas consultas públicas.

---

## Arquitetura

O backend é formado por **dois serviços Spring Boot** que se comunicam de forma assíncrona por **RabbitMQ**.

```mermaid
flowchart LR
    app[App Mobile] -- REST + JWT --> portal
    site[Navegador] -- Thymeleaf + sessão --> portal
    portal[portal-service<br/>API REST, portal web, Spring AI]
    portal -- consultas --> db[(Oracle)]
    portal -- ChatClient + tools --> llm[LLM Gemini<br/>API compatível com OpenAI]
    portal -- publica eventos --> mq{{RabbitMQ<br/>exchange sampatech.events}}
    mq -- consome --> notif[notification-service]
    notif -- grava logs --> db
    notif -- e-mail de boas-vindas --> smtp[SMTP]
```

| Serviço | Porta | Responsabilidade |
|---|---|---|
| `portal-service` (raiz do repositório) | 8080 | API REST, autenticação JWT, portal web, assistente com Spring AI, publicação de eventos |
| `notification-service` (pasta `notification-service/`) | 8081 | Consome os eventos, envia o e-mail de boas-vindas e grava os registros de notificação, buscas e conversas |

As tabelas de ocorrências são alimentadas pelo serviço de IA de outra disciplina. Este backend **não altera a estrutura do banco** (`ddl-auto: none`): lê as tabelas da IA (entidades `@Immutable`) e grava apenas nas tabelas de usuários, catálogos e logs.

---

## Requisito 5: opção escolhida

**Opção A: mensageria com RabbitMQ**, com eventos publicados pelo portal e consumidos de forma assíncrona pelo `notification-service`.

| Evento (routing key) | Publicado quando | Fila | O que o consumidor faz |
|---|---|---|---|
| `usuario.cadastrado` | um usuário se cadastra (API ou site) | `notification.usuario-cadastrado` | envia o e-mail de boas-vindas e grava o resultado em `NOTIFICATION_LOG` |
| `consulta.realizada` | uma busca com filtro é feita (app, site ou assistente) | `notification.consulta-realizada` | grava em `SEARCH_LOG`, com a origem `MOBILE`, `SITE` ou `CHAT` |
| `chat.mensagem` | um usuário logado conversa com o assistente | `notification.chat-mensagem` | grava pergunta e resposta em `CHAT_HISTORY` |

Detalhes da implementação:

- exchange do tipo **topic** (`sampatech.events`): o portal publica cada evento com uma routing key (`usuario.cadastrado`, `consulta.realizada`, `chat.mensagem`) e o RabbitMQ entrega a mensagem às filas cujo padrão de ligação combina com essa chave. Um novo consumidor pode assinar um evento (ou um grupo, como `usuario.*`) sem alterar o portal;
- filas **duráveis**: se o consumidor estiver fora do ar, as mensagens esperam na fila e são processadas quando ele volta;
- mensagens em JSON (`JacksonJsonMessageConverter`);
- consumidor com **retry** (3 tentativas, intervalo de 2 s);
- a publicação nunca derruba a requisição do usuário: se o broker estiver indisponível, o cadastro, a busca e o chat continuam funcionando e a falha é registrada em log.

---

## Tecnologias

- Java 21 e Spring Boot 4
- Spring Web MVC, Spring Data JPA (Hibernate), Bean Validation
- Spring Security com JWT (jjwt) para a API e sessão com form login para o portal web
- Spring AI (`ChatClient`, tool calling e memória de conversa) com o Gemini, pela API compatível com OpenAI
- Spring AMQP com RabbitMQ (CloudAMQP no deploy)
- Spring Mail (Brevo no deploy e Mailpit no ambiente local)
- Thymeleaf, Thymeleaf Extras Spring Security e Bootstrap 5
- springdoc-openapi (Swagger UI)
- Oracle Database
- Gradle, Lombok, Docker e Render

---

## Modelagem

```mermaid
erDiagram
    PLATFORM_USER ||--o{ ISSUE_REPORT : "registra"
    ISSUE_TYPE ||--o{ ISSUE_REPORT : "classifica"
    MONITORING_POINT ||--o{ ISSUE_REPORT : "localiza"
    ISSUE_REPORT ||--o{ ISSUE_HISTORY : "possui"
    PLATFORM_USER ||--o{ CHAT_HISTORY : "conversa"
    PLATFORM_USER ||--o{ SEARCH_LOG : "pesquisa"
    PLATFORM_USER ||--o{ NOTIFICATION_LOG : "recebe"
```

| Entidade | Tabela | Acesso no backend |
|---|---|---|
| `PlatformUser` | `PLATFORM_USER` | CRUD (cadastro, perfil, administração) |
| `IssueType` | `ISSUE_TYPE` | CRUD pelo ADMIN |
| `MunicipalAgency` | `MUNICIPAL_AGENCY` | CRUD pelo ADMIN |
| `IssueReport` | `ISSUE_REPORT` | leitura (gravada pela IA), com `@ManyToOne` para tipo, ponto de monitoramento e usuário |
| `IssueHistory` | `ISSUE_HISTORY` | leitura: linha do tempo de status de cada ocorrência |
| `MonitoringPoint` | `MONITORING_POINT` | leitura: rua e bairro da câmera |
| `ChatHistory` | `CHAT_HISTORY` | leitura no portal e gravação no `notification-service` |
| `SearchLog`, `NotificationLog` | `SEARCH_LOG`, `NOTIFICATION_LOG` | gravação no `notification-service` |

A exclusão é **lógica** (`IS_ACTIVE = 'N'`): um tipo de ocorrência desativado continua ligado às ocorrências antigas, o que preserva o histórico e a integridade referencial.

**Paginação e ordenação**: todas as listagens usam `Pageable` (`?page=0&size=10&sort=campo,asc`), com tamanho máximo de página de **50** e ordenação permitida apenas por campos de uma lista definida para cada recurso. Ordenar por qualquer outro campo retorna 400.

---

## Segurança

### Fluxo de autenticação (API)

```mermaid
sequenceDiagram
    participant C as Cliente (app ou Swagger)
    participant A as API
    participant DB as Oracle
    C->>A: POST /api/auth/login {email, senha}
    A->>DB: busca o usuário e confere a senha (BCrypt)
    A-->>C: 200 {token, type: "Bearer", user}
    C->>A: GET /api/users/me (Authorization: Bearer token)
    A->>A: JwtAuthFilter valida a assinatura e a expiração
    A->>DB: carrega o usuário pelo e-mail do token (sub)
    A-->>C: 200 com os dados, ou 401 se o token for inválido
```

- cada token é assinado com HMAC-SHA usando a chave `JWT_SECRET`, leva `sub` (e-mail), `name` e `role` e **expira 120 minutos depois de emitido**. A chave é fixa e não expira; o que expira é cada token gerado com ela;
- o papel usado na autorização vem do **banco**, e não do token: um usuário rebaixado ou desativado perde o acesso na próxima requisição;
- o cadastro também devolve o token, para o app entrar logado direto.

### Papéis

| Papel | Pode |
|---|---|
| Visitante | consultar ocorrências e tipos e usar o assistente |
| `CITIZEN` | o mesmo, mais perfil (ver, editar, trocar senha e desativar conta) e histórico de conversas |
| `ADMIN` (gestor) | o mesmo, mais administração de usuários, tipos de ocorrência e órgãos municipais |

O enum `UserType` também tem `AGENT`, mantido apenas para ler registros do banco compartilhado. No fluxo atual o status das ocorrências é definido pela câmera com IA, então esse papel não tem rotas próprias e não pode ser atribuído pela administração.

### Duas cadeias de segurança

| Cadeia | Rotas | Mecanismo |
|---|---|---|
| API | `/api/**` | stateless, JWT no cabeçalho `Authorization`, CSRF desligado, respostas 401 e 403 em JSON |
| Portal web | demais rotas | sessão com form login, CSRF ligado em todos os formulários, páginas 403 e 404 próprias |

### Outras proteções

- senhas com **BCrypt**;
- **bloqueio de login**: 5 senhas erradas para o mesmo e-mail bloqueiam novas tentativas por 15 minutos (429 na API e aviso no portal);
- dados pessoais só pelo token ou pela sessão: `/api/users/me` e `/profile` nunca recebem id na URL, então um usuário não consegue consultar dados de outro;
- o ADMIN não consegue alterar o próprio tipo nem desativar a própria conta;
- campos como `userType` enviados no cadastro ou na edição de perfil são ignorados;
- consultas JPQL parametrizadas e saída escapada pelo Thymeleaf;
- erros nunca expõem stacktrace (`include-stacktrace: never`);
- segredos fora do código, em variáveis de ambiente (`.env` no `.gitignore`).

---

## Funcionalidade com Spring AI

O assistente (`POST /api/chat`, a bolinha de chat do portal e a página `/chat`) responde perguntas sobre as ocorrências **consultando o banco em tempo real**. Ele não é um chat genérico: o modelo só fala com base no que as ferramentas devolvem.

**Como funciona:**

1. A pergunta chega ao `ChatService`, que monta a chamada no `ChatClient` com um prompt de sistema (regras de escopo, formato, tradução dos status e proibição de expor dados pessoais), a data do dia e o nome do usuário logado.
2. O modelo decide quais **tools** chamar (classe `IssueSearchTools`):
    - `listarTiposDeOcorrencia`: catálogo de tipos ativos;
    - `buscarOcorrencias`: busca por tipo, status, bairro ou rua, período e "apenas não resolvidas", com o total encontrado;
    - `detalharOcorrencia`: dados completos e linha do tempo de uma ocorrência.
3. As tools usam os mesmos services da API e normalizam a entrada do modelo (status em português, "Av."/"Rua" no nome do local, datas em DD/MM/AAAA).
4. Uma **memória de conversa** (`MessageWindowChatMemory`, últimas 10 mensagens) permite perguntas de seguimento, como "e qual dessas fica na Paulista?". A memória é por usuário logado, ou por sessão no portal.
5. Para usuários logados, a pergunta e a resposta são publicadas no RabbitMQ e gravadas no histórico pelo `notification-service`.

Exemplos de perguntas:

- "Quais ocorrências estão em andamento?"
- "Tem alguma pendente na Avenida Paulista?"
- "Detalhe a ocorrência 6."
- "Qual a diferença entre encaminhada e em andamento?"

O modelo é configurável por variável de ambiente (`LLM_BASE_URL`, `LLM_API_KEY`, `LLM_MODEL`). Se o provedor estiver indisponível, o usuário recebe uma mensagem amigável em vez de um erro.

---

## Validação e tratamento de erros

- DTOs de entrada são `record`s com Bean Validation (`@NotBlank`, `@Email`, `@Size`, `@Pattern`, `@NotNull`) e mensagens em português;
- `@RestControllerAdvice` (`GlobalExceptionHandler`) padroniza todas as respostas de erro da API;
- um segundo advice (`FallbackExceptionHandler`) cobre erros que acontecem antes do controller (rota inexistente, método errado, tipo de conteúdo errado).

Formato de erro:

```json
{
  "timestamp": "2026-11-19T02:33:59.885Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Dados inválidos",
  "path": "/api/auth/register",
  "fields": [
    { "field": "email", "message": "E-mail inválido" }
  ]
}
```

| Status | Quando |
|---|---|
| 400 | validação, JSON mal formatado, parâmetro com tipo errado, ordenação não permitida, período inválido |
| 401 | sem token, token inválido ou expirado, credenciais erradas, conta desativada |
| 403 | papel sem permissão para a rota |
| 404 | recurso ou rota inexistente |
| 405 | método HTTP não suportado na rota |
| 409 | conflito de negócio (e-mail já cadastrado, nome duplicado, alteração da própria conta pelo ADMIN) |
| 415 | corpo enviado em formato diferente de JSON |
| 429 | login bloqueado por excesso de tentativas |
| 500 | erro inesperado, com mensagem genérica e detalhes apenas no log |

---

## Endpoints principais

Documentação completa e testável no Swagger: `/swagger-ui.html`. Para as rotas protegidas, faça login, copie o `token` e clique em **Authorize**.

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/auth/register` | público | cadastro (sempre como CITIZEN), devolve o token |
| POST | `/api/auth/login` | público | login, devolve o token |
| GET | `/api/issues` | público | lista paginada com filtros `typeId`, `status`, `neighborhood`, `from`, `to` |
| GET | `/api/issues/{id}` | público | detalhe com localização e histórico |
| GET | `/api/issue-types` | público | tipos ativos |
| POST | `/api/chat` | público (com token, salva o histórico) | pergunta ao assistente |
| GET | `/api/users/me` | logado | perfil |
| PUT | `/api/users/me` | logado | atualiza nome e, opcionalmente, senha |
| DELETE | `/api/users/me` | logado | desativa a própria conta |
| GET | `/api/users/me/chat-history` | logado | histórico paginado do assistente |
| GET, PUT, DELETE | `/api/admin/users`, `/api/admin/users/{id}` | ADMIN | lista, altera tipo e situação, desativa |
| GET, POST, PUT, DELETE | `/api/admin/issue-types`, `/api/admin/issue-types/{id}` | ADMIN | CRUD de tipos de ocorrência |
| GET, POST, PUT, DELETE | `/api/admin/agencies`, `/api/admin/agencies/{id}` | ADMIN | CRUD de órgãos municipais |

Exemplo de login:

```bash
curl -X POST https://sampatech-portal.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"cidadao.demo@sampatech.com","password":"SampaTech@2026"}'
```

---

## Portal web

Mesma aplicação, servida em `/`:

| Página | Acesso |
|---|---|
| Início, com a explicação do projeto e números em tempo real | público |
| Ocorrências: lista com filtros e paginação, e detalhe com linha de andamento e histórico | público |
| Chat, em página própria e no botão flutuante disponível em todas as telas | público |
| Entrar e cadastro | público |
| Meu perfil e histórico do chat | logado |
| Administração de usuários, tipos e órgãos | ADMIN |

Também tem tema claro e escuro (escolha salva no navegador) e layout responsivo.

### Rotas do portal web

Todos os formulários (POST) exigem o token CSRF que o Thymeleaf inclui automaticamente. Um POST sem esse token recebe **403**.

| Método | Rota | Acesso | Resposta esperada | Casos de erro |
|---|---|---|---|---|
| GET | `/` | público | página inicial com números em tempo real | — |
| GET | `/login` | público | formulário de login | já logado: redireciona para `/` |
| POST | `/login` | público | entra e volta para a página que a pessoa tentou abrir, ou `/` | senha errada ou conta desativada: `/login?error`; 5 erros seguidos: `/login?locked` (bloqueio de 15 min) |
| POST | `/logout` | logado | sai e volta para `/?logout` | — |
| GET | `/register` | público | formulário de cadastro | já logado: redireciona para `/` |
| POST | `/register` | público | cadastra como CITIZEN e vai para `/login?registered` | campos inválidos ou e-mail já cadastrado: volta ao formulário com a mensagem em cada campo |
| GET | `/issues` | público | lista paginada; filtros `typeId`, `status`, `neighborhood`, `from`, `to` | `typeId` ou data com formato inválido: página de erro **400**; data inicial maior que a final: aviso na própria lista; ordenação não permitida é ignorada |
| GET | `/issues/{id}` | público | detalhe com andamento, local, gravidade e histórico | id inexistente: volta para `/issues` com o aviso "Ocorrência não encontrada"; id não numérico: página de erro **400** |
| GET | `/chat` | público | página do assistente | — |
| POST | `/chat` | público | envia a pergunta e mostra a resposta | pergunta vazia: volta para `/chat` sem enviar |
| POST | `/chat?clear` | público | inicia uma nova conversa | — |
| POST | `/chat/ask` | público | JSON `{"answer": "..."}`, usado pelo botão flutuante | pergunta vazia ou com mais de 1000 caracteres: **400** em JSON; sem CSRF: **403** |
| GET | `/chat/history` | público | JSON com a conversa da sessão | — |
| DELETE | `/chat/history` | público | apaga a conversa da sessão e a memória do assistente (**204**) | sem CSRF: **403** |
| GET | `/profile` | logado | perfil com edição de nome, troca de senha e desativação | sem login: redireciona para `/login` |
| POST | `/profile` | logado | salva o nome | nome vazio ou com mais de 120 caracteres: mensagem no campo |
| POST | `/profile/password` | logado | troca a senha | menos de 8 caracteres ou confirmação diferente: mensagem no campo |
| POST | `/profile/deactivate` | logado | desativa a conta, encerra a sessão e vai para `/?deactivated` | — |
| GET | `/profile/chat-history` | logado | histórico paginado das conversas salvas | sem login: redireciona para `/login` |
| GET | `/admin/users` | ADMIN | lista paginada de usuários | CITIZEN: página **403**; sem login: redireciona para `/login` |
| GET | `/admin/users/{id}/edit` | ADMIN | formulário de tipo e situação | id inexistente: página **404**; id não numérico: página **400** |
| POST | `/admin/users/{id}` | ADMIN | salva e volta para a lista com mensagem | editar a própria conta: aviso "Você não pode alterar o tipo nem desativar a própria conta"; tipo inválido (inclusive AGENT): mensagem no campo |
| GET | `/admin/issue-types` | ADMIN | lista paginada de tipos | CITIZEN: página **403** |
| GET | `/admin/issue-types/new` | ADMIN | formulário de novo tipo | — |
| POST | `/admin/issue-types` | ADMIN | cria e volta para a lista | nome vazio, categoria inválida ou nome já existente: mensagem no campo |
| GET | `/admin/issue-types/{id}/edit` | ADMIN | formulário de edição | id inexistente: página **404** |
| POST | `/admin/issue-types/{id}` | ADMIN | salva e volta para a lista | mesmas validações da criação; id inexistente: página **404** |
| POST | `/admin/issue-types/{id}/deactivate` | ADMIN | desativa (exclusão lógica) | id inexistente: página **404** |
| GET | `/admin/agencies` | ADMIN | lista paginada de órgãos | CITIZEN: página **403** |
| GET | `/admin/agencies/new` | ADMIN | formulário de novo órgão | — |
| POST | `/admin/agencies` | ADMIN | cria e volta para a lista | nome ou cidade vazios, UF fora do padrão (`SP`) ou órgão já existente: mensagem no campo |
| GET | `/admin/agencies/{id}/edit` | ADMIN | formulário de edição | id inexistente: página **404** |
| POST | `/admin/agencies/{id}` | ADMIN | salva e volta para a lista | mesmas validações da criação |
| POST | `/admin/agencies/{id}/deactivate` | ADMIN | desativa (exclusão lógica) | id inexistente: página **404** |
| GET | qualquer rota inexistente | — | — | sem login: redireciona para `/login`; logado: página **404** |

---

## Serviços externos

| Serviço | Uso no projeto |
|---|---|
| **Render** | hospeda os dois deploys a partir dos `Dockerfile`s do repositório (plano gratuito) |
| **CloudAMQP** | RabbitMQ gerenciado usado no deploy (plano gratuito "Little Lemur", região São Paulo). A conexão é feita pela variável `RABBITMQ_URL`, no formato `amqps://usuario:senha@host/vhost`. No ambiente local, o RabbitMQ roda pelo `docker-compose.yml` |
| **Brevo** | servidor SMTP usado no deploy para o e-mail de boas-vindas, com remetente verificado. Usamos a porta **2525** porque o plano gratuito do Render bloqueia as portas de SMTP padrão (25, 465 e 587). No ambiente local, os e-mails vão para o **Mailpit**, que mostra as mensagens em http://localhost:8025 sem enviar nada de verdade |
| **Google AI Studio (Gemini)** | provedor do modelo de linguagem do assistente, acessado pela API compatível com OpenAI |
| **Oracle (FIAP)** | banco de dados compartilhado com o serviço de IA |

---

## Como rodar

### Pré-requisitos

- Java 21
- Docker (para o RabbitMQ e o Mailpit locais)
- acesso a um banco Oracle com as tabelas do projeto
- uma chave de API do Gemini (gratuita em https://aistudio.google.com)

### 1. Variáveis de ambiente

Crie o arquivo `.env` na raiz a partir do modelo e preencha os valores em branco (banco, `JWT_SECRET` e chave do Gemini). O mesmo `.env` é lido pelos dois serviços. O modelo já vem com os valores do ambiente local (RabbitMQ e Mailpit do Docker).

```bash
cp .env.example .env
```

O `.env.example` não contém nenhum dado sensível, e o `.env` está no `.gitignore`: cada pessoa usa as próprias credenciais.

| Variável | Exemplo | Uso |
|---|---|---|
| `DB_URL` | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl` | banco |
| `DB_USER` / `DB_PASSWORD` | | credenciais do banco |
| `JWT_SECRET` | texto com pelo menos 32 caracteres | assinatura dos tokens |
| `RABBITMQ_URL` | `amqp://guest:guest@localhost:5672` | broker (local via Docker) |
| `MAIL_HOST` / `MAIL_PORT` | `localhost` / `1025` | SMTP (Mailpit local) |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | vazio no Mailpit | credenciais SMTP |
| `MAIL_SMTP_AUTH` / `MAIL_STARTTLS` | `false` / `false` | `true` / `true` em um SMTP real |
| `MAIL_FROM` | `nao-responda@sampatech.com.br` | remetente |
| `LLM_BASE_URL` | `https://generativelanguage.googleapis.com/v1beta/openai` | endpoint compatível com OpenAI |
| `LLM_API_KEY` | | chave do Gemini |
| `LLM_MODEL` | `gemini-3.1-flash-lite` | modelo |

### 2. RabbitMQ e Mailpit

```bash
docker compose up -d
```

- painel do RabbitMQ: http://localhost:15672 (usuário `guest`, senha `guest`)
- caixa de e-mails do Mailpit: http://localhost:8025

### 3. Portal (porta 8080)

Na raiz do repositório:

```bash
./gradlew bootRun
```

### 4. Notification (porta 8081)

Em outro terminal:

```bash
cd notification-service
./gradlew bootRun
```

No Windows, use `gradlew.bat bootRun`.

### 5. Acessar

| | URL |
|---|---|
| Portal web | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |

### Docker

Cada serviço tem um `Dockerfile` (build em duas etapas, JRE 21). É assim que o deploy roda no Render:

```bash
docker build -t sampatech-portal .
docker build -t sampatech-notification ./notification-service
```

---

## Usuários de teste

| Perfil | E-mail | Senha |
|---|---|---|
| ADMIN | `sampatechbrasil@gmail.com` | `Cherry2301` |
| CITIZEN | `cidadao.demo@sampatech.com` | `SampaTech@2026` |

Um novo cadastro pelo app, pelo site ou por `POST /api/auth/register` sempre cria um usuário CITIZEN.

---

## Testes

```bash
./gradlew build
```

Os testes unitários não dependem de banco nem de broker:

- `JwtServiceTest`: o token carrega o e-mail no `sub` e tokens adulterados ou assinados com outro segredo são recusados;
- `LoginAttemptServiceTest`: bloqueio após 5 falhas e liberação após login bem-sucedido;
- `SortRulesTest`: ordenação por campos sensíveis é recusada.

O teste de contexto (`contextLoads`) só roda quando `DB_URL` está definido como variável de ambiente, o que permite compilar e testar o projeto sem acesso ao banco.

---

## Estrutura do projeto

```
.
├── src/main/java/br/com/sampatech/portal
│   ├── ai/                  tools do assistente (Spring AI)
│   ├── config/              Swagger, paginação, RabbitMQ, BCrypt, regras de ordenação
│   ├── controller/api/      controllers REST (/api/**)
│   ├── controller/web/      controllers do portal Thymeleaf
│   ├── dto/request|response DTOs da API
│   ├── dto/form/            objetos de formulário do portal
│   ├── entity/              entidades JPA e enums
│   ├── exception/           exceções e tratamento global de erros
│   ├── messaging/           eventos e publicação no RabbitMQ
│   ├── repository/          Spring Data JPA
│   ├── security/            JWT, filtros, cadeias de segurança, bloqueio de login
│   └── service/             regras de negócio
├── src/main/resources
│   ├── templates/           páginas Thymeleaf
│   ├── static/              CSS, JS, ícones e logo
│   └── application.yaml
├── notification-service/    consumidor RabbitMQ (projeto Gradle separado)
├── docker-compose.yml       RabbitMQ e Mailpit para desenvolvimento
├── Dockerfile
└── .env.example
```

---

## Declaração de uso de IA

Usamos ferramentas de IA generativa neste projeto:

- **Claude (Anthropic)**: usado na revisão do código, apontando falhas de segurança e de validação para a equipe corrigir, e como apoio nos testes da API.
- **Gemini (Google)**: modelo de linguagem usado em tempo de execução pela funcionalidade de assistente, via Spring AI. O comportamento do assistente é definido por prompt de sistema e ferramentas que consultam o banco; não há treinamento de modelo.