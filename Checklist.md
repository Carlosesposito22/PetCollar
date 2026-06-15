# Checklist do Projeto petCollar

> Referência: diretrizes do professor (texto e slides "Trabalho em grupo.pdf"),
> CLAUDE.md deste projeto e o projeto cinema-frame como modelo de código aprovado.

---

## Parte 1 — Entregáveis do Projeto (nível global)

Estes itens valem para o projeto inteiro, não por funcionalidade.

### Documentação

- [ ] **Descrição do domínio** com linguagem onipresente (glossário de termos:
      `Tutor`, `Paciente`, `Triagem`, `CorDeRisco`, `Fila de Espera Dinâmica`,
      `Ticket de Benefício`, `NEM`, `Blindagem de Dosagem`, etc.)
- [ ] **Mapa de histórias do usuário** cobrindo as 12 funcionalidades
- [ ] **Protótipos** de baixa ou alta fidelidade para cada funcionalidade
- [ ] **Arquivo CML** (`petCollar.cml`) com o modelo dos subdomínios no
      Context Mapper (bounded contexts, aggregates, entities, services,
      context map com relacionamentos)

### GitHub

- [ ] Repositório público **ou** privado com acesso ao usuário `@profsauloaraujo`
- [ ] Todos os artefatos commitados: código, docs, CML, features BDD, protótipos
- [ ] Commits identificáveis por integrante (use o e-mail do GitHub configurado)
- [ ] README com instruções de como rodar o projeto localmente

### Build

- [ ] `mvn clean install` passa **sem erros** (incluindo todos os testes BDD)
- [ ] `cd apresentacao-backend && mvn spring-boot:run` sobe a API sem exceções
- [ ] Banco PostgreSQL conectado via Docker (`docker-compose up -d` na raiz)

---

## Parte 2 — Requisitos de Arquitetura (todo o código)

Estes requisitos valem para CADA linha de código produzida.

### DDD — Níveis obrigatórios

- [ ] **Preliminar:** linguagem onipresente aplicada em todo o código (nomes em
      português, termos do domínio)
- [ ] **Estratégico:** bounded contexts separados em módulos Maven distintos
      (`dominio-<Contexto>`); Context Map no CML
- [ ] **Tático:** entidades com identidade por VO (`XxxId`), value objects,
      aggregates, domain services, repository interfaces no domínio
- [ ] **Operacional:** casos de uso no módulo `aplicacao`, sem regra de negócio
      no controller

### Arquitetura Limpa — regra de dependência

```
apresentacao-backend → aplicacao → dominio-* → dominio-compartilhado
infraestrutura       → aplicacao + dominio-*   (implements IXxxRepositorio)
apresentacao-backend → infraestrutura           (scope: runtime apenas)
```

- [ ] Zero `import org.springframework.*` ou `jakarta.persistence.*` nos módulos
      `dominio-*`
- [ ] Interfaces `IXxxRepositorio` definidas **no domínio**, implementadas **na
      infraestrutura**
- [ ] Controllers não chamam services do domínio diretamente — passam pelo
      `UseCase` ou pelo `Service` via injeção por construtor
- [ ] Nenhuma regra de negócio nos controllers (só tradução DTO ↔ domínio)

### Padrões de projeto (2.ª entrega)

Mínimo de 6 padrões distintos, 1 por integrante, dentre:
Iterator · Decorator · Observer · Proxy · Strategy · Template Method

- [ ] Cada padrão implementado em **código Java real** (não só citado na doc)
- [ ] Wiring dos padrões feito nos `@Configuration` da infraestrutura
- [ ] O padrão resolve um problema real do domínio (não é artificial)

### Persistência JPA

- [ ] `@Entity` **NUNCA** nos módulos `dominio-*` — apenas em `infraestrutura`
- [ ] Cada entidade JPA (`XxxJpa`) tem `fromDomain(Xxx)` e `toDomain()`
- [ ] Id persistido como `String` (`id.getValor()`) — sem `@GeneratedValue`
- [ ] Enums persistidos como `String` (`enum.name()`) — sem `@Enumerated`
- [ ] Referências entre agregados distintos: guardar apenas o `String` do Id,
      nunca `@ManyToOne` cruzando agregados
- [ ] Tabelas criadas automaticamente pelo Hibernate (`ddl-auto=update`)

---

## Parte 3 — Checklist por Funcionalidade

Aplique este bloco às **12 funcionalidades (F-01 a F-12)**. Uma funcionalidade
está 100% implementada apenas quando **todos** os itens abaixo estiverem marcados.

> Substitua `[F-XX]` pelo identificador da funcionalidade sendo verificada.

---

### [F-XX] — Nome da Funcionalidade

#### 3.1 Domínio (`dominio-<Contexto>`)

- [ ] **Entidade / Agregado** criado com:
  - [ ] `XxxId` recebido pelo construtor (gerado fora: `XxxId.gerar()`)
  - [ ] Validação no construtor (`IllegalArgumentException` com mensagem em PT)
  - [ ] Métodos de negócio com nome do domínio (sem setters públicos)
  - [ ] Transições de estado protegidas (`IllegalStateException` se inválida)
  - [ ] Construtor de reconstituição (comentado como `// Construtor de RECONSTRUÇÃO`)
  - [ ] Coleções retornadas como `unmodifiableList`; copiadas na entrada
- [ ] **Value Objects / Enums** necessários criados (VOs de Id compartilhados
      em `dominio-compartilhado`)
- [ ] **Interface `IXxxRepositorio`** no domínio com verbos em português
      (`salvar`, `buscarPorId` → `Optional<T>`, `listar...`, `remover`)
- [ ] **`XxxService`** com regras de negócio que cruzam entidades, validando
      dependências no construtor, sem anotações Spring

#### 3.2 Testes BDD (Cucumber, português)

- [ ] Arquivo **`src/test/resources/features/<funcionalidade>.feature`** com
      `# language: pt` e cenários em Gherkin PT (`Dado / Quando / Então`)
- [ ] Pelo menos **3 cenários** cobrindo: caminho feliz, regra de negócio
      bloqueante e caso de borda relevante
- [ ] **`CucumberRunnerTest.java`** configurado para esta feature
- [ ] **Step definitions** no pacote `...<Contexto>.bdd`:
  - [ ] Usa mocks Mockito para `IXxxRepositorio`
  - [ ] Compartilha estado entre steps via `ContextoCenario`
  - [ ] Captura exceções para validar nos steps `Então`
- [ ] `mvn test` no módulo do domínio passa **verde** com todos os cenários

#### 3.3 Infraestrutura (`infraestrutura`)

- [ ] **`XxxJpa`** (`@Entity`, `@Table(name="plural_snake_case")`):
  - [ ] Construtor `protected` sem argumentos
  - [ ] `fromDomain(Xxx d)` — domínio → JPA
  - [ ] `toDomain()` — JPA → domínio (usa construtor de reconstituição)
  - [ ] Id como `String`, enums como `String`, refs entre agregados como `String`
- [ ] **`XxxJpaRepository extends JpaRepository<XxxJpa, String>`** com finders
      necessários
- [ ] **`XxxRepositorioJpa implements IXxxRepositorio`** (`@Repository`,
      `@Transactional` se multi-tabela)  
      _ou_ `XxxRepositorioEmMemoria` provisório enquanto o banco não está ligado
- [ ] **`XxxConfig`** (`@Configuration`) expondo o(s) `Service` como `@Bean`
      e fazendo wiring do padrão de projeto (Observer, Proxy, etc.) se aplicável
- [ ] **Padrão de projeto** da funcionalidade implementado nesta camada
      (ex.: Proxy no lugar do Adapter, Observer registrado no Config)

#### 3.4 Camada de Aplicação (`aplicacao`)

- [ ] **`VerboXxxUseCase`** com método `executar(...)` que orquestra o(s)
      `Service`(s) do domínio (sem anotações Spring; dependências por construtor)
- [ ] Registrado como `@Bean` no `XxxConfig` da infraestrutura

#### 3.5 Apresentação — Backend (`apresentacao-backend`)

- [ ] **DTOs** (`record`):
  - [ ] Entrada: `RequisicaoXxxDTO` com campos primitivos/String
  - [ ] Saída: `XxxDTO` com factory estático `de(EntidadeOuVO)`
- [ ] **`XxxController`** (`@RestController`, rotas `/api/...`):
  - [ ] Injeção por construtor (UseCase ou Service)
  - [ ] Traduz DTO → domínio (reconstrói VOs com `XxxId.de(...)`)
  - [ ] Delega ao UseCase/Service e mapeia resposta via `XxxDTO.de(...)`
  - [ ] Não contém regra de negócio
  - [ ] Deixa exceções subirem para o handler global
- [ ] **`GlobalExceptionHandler`** (`@RestControllerAdvice`) mapeando:
  - [ ] `IllegalArgumentException` → 400 Bad Request
  - [ ] `IllegalStateException` → 409 Conflict
  - [ ] Resposta padronizada `record ErroResponse(String mensagem)`
- [ ] Endpoints testados manualmente (curl / Postman / Swagger):
  - [ ] Caminho feliz retorna status correto e payload esperado
  - [ ] Violação de regra de negócio retorna 409 com mensagem legível
  - [ ] Argumento inválido retorna 400 com mensagem legível

#### 3.6 Apresentação — Frontend

- [ ] Pelo menos **1 tela funcional** para a funcionalidade
- [ ] A tela exercita o caminho feliz completo (criar / atualizar / visualizar)
- [ ] Erros da API (400/409) são exibidos ao usuário de forma legível
- [ ] A tela foi testada manualmente no browser com o backend rodando

---

## Parte 4 — Verificação Final antes da Entrega

### Contagem de funcionalidades

| ID | Nome | Domínio ✓ | BDD ✓ | Infra ✓ | UseCase ✓ | Controller ✓ | Frontend ✓ |
|----|------|-----------|-------|---------|-----------|--------------|------------|
| F-01 | Prontuário Unificado + Análise Epidemiológica | | | | | | |
| F-02 | Triagem Clínica com Classificação de Risco | | | | | | |
| F-03 | Protocolo de Tutor Inacessível | | | | | | |
| F-04 | Programa de Indicação | | | | | | |
| F-05 | Agendamento de consulta inicial e de Retorno | | | | | | |
| F-06 | Ciclo Vacinal Inteligente | | | | | | |
| F-07 | Assinatura de Plano e Gestão Financeira | | | | | | |
| F-08 | Carência de Benefícios do Plano | | | | | | |
| F-09 | Gamificação e Conquistas | | | | | | |
| F-10 | Relatório Clínico Evolutivo | | | | | | |
| F-11 | Gestão Nutricional (NEM) | | | | | | |
| F-12 | Central de Farmacovigilância | | | | | | |

### Padrões de projeto

| Padrão | Integrante | Funcionalidade | Implementado ✓ |
|--------|-----------|----------------|----------------|
| | | | |
| | | | |
| | | | |
| | | | |
| | | | |
| | | | |

> Mínimo: 6 padrões distintos (Iterator · Decorator · Observer · Proxy · Strategy · Template Method)

### Checklist global

- [ ] 12 funcionalidades não triviais implementadas (com escrita + regras de negócio)
- [ ] Código em Java com Spring Boot + JPA
- [ ] DDD em 4 níveis: preliminar, estratégico, tático, operacional
- [ ] Arquitetura limpa: zero dependência de framework no domínio
- [ ] Mínimo 6 padrões de projeto em código Java real
- [ ] 1 arquivo `.feature` por funcionalidade (12 no total), cenários em PT
- [ ] `mvn clean install` verde (todos os testes BDD passando)
- [ ] JPA: tabelas criadas automaticamente, dados persistidos no PostgreSQL
- [ ] Camada web: pelo menos 1 tela por funcionalidade
- [ ] CML com modelo dos subdomínios commitado
- [ ] Protótipos (baixa ou alta fidelidade) commitados
- [ ] Descrição do domínio com linguagem onipresente commitada
- [ ] Mapa de histórias do usuário commitado
- [ ] Repositório acessível ao professor (`@profsauloaraujo`)
- [ ] README com instruções de execução
