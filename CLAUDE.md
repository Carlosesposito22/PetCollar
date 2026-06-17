# CLAUDE.md — Guia de Padrão de Código do petCollar

> Este arquivo define **o padrão de código obrigatório** deste projeto. Toda
> nova funcionalidade, refatoração ou correção DEVE seguir as convenções aqui
> descritas. O objetivo é que o código novo seja indistinguível do bom código
> existente. Quando houver dúvida, **imite o slice do contexto `RecepcaoTriagem`
> (`Triagem` / `Tutor` / `Paciente`)** — é a referência canônica de domínio.
>
> A **arquitetura** segue o modelo DDD + Arquitetura Limpa já validado pelo
> professor; as **convenções de nomenclatura** são as do próprio petCollar
> (repositórios `IXxxRepositorio`, VOs de Id com `gerar()`/`de()`, DTOs
> `XxxDTO` com `de(...)`).

---

## 1. Visão Geral

**petCollar** é um ecossistema de **gestão clínica veterinária** construído com
**Domain-Driven Design + Arquitetura Limpa**, organizado como um projeto
**Maven multi-módulo**. O domínio é Java puro (sem framework) e coberto por
**testes BDD com Cucumber em português**.

O sistema cobre o fluxo da clínica em torno da **segurança do paciente**: da
recepção inteligente e triagem por risco, passando pela medicina preventiva
(ciclo vacinal), até a inteligência farmacológica. São **12 funcionalidades
(F-01 a F-12)** distribuídas em três personas:

| Persona | Funcionalidades |
|---|---|
| **Recepcionista** | F-01 Prontuário Unificado com análise epidemiológica · F-02 Triagem com classificação de risco · F-03 Protocolo de tutor inacessível |
| **Tutor** | F-04 Programa de indicação · F-05 Agendamento de retorno · F-06 Ciclo vacinal · F-07 Assinatura e financeiro · F-08 Carência de benefícios · F-09 Gamificação |
| **Médico** | F-10 Relatório clínico evolutivo · F-11 Gestão nutricional (NEM) · F-12 Central de farmacovigilância |

- **Linguagem do código e do domínio:** Português (classes, métodos, variáveis,
  mensagens de erro, testes). Mantenha SEMPRE o português — inclusive em nomes
  como `buscarPorId`, `salvar`, `listarOrdenada`, `finalizar`.
- **Linguagem onipresente:** use os termos do glossário do domínio — `Tutor`,
  `Paciente`, `Triagem`, `CorDeRisco`, `PesoTotal`, `Fila de Espera Dinâmica`,
  `Ticket de Benefício`, `Status da Conta`, `NEM`, `Blindagem de Dosagem`,
  `Plano Nutricional`, `Cronograma de Transição`, `Ração`, `Comorbidade`,
  `Prescrição Farmacológica`, `Tag Clínica`, `Cobrança`, `Status da Prescrição`.
- **Java 17** · **Spring Boot 4.0.5** · **Spring Security + JWT (HS256)** ·
  **Spring Data JPA + PostgreSQL** (camada-alvo de persistência) ·
  **Cucumber 7 + JUnit 5 + Mockito** · **Lombok** (disponível, mas o domínio
  evita usá-lo).

---

## 2. Estrutura de Módulos e Regra de Dependência

Diferente de um monólito, **cada bounded context é seu próprio módulo Maven**
`dominio-<Contexto>`:

```
petCollar-pai (pom · groupId br.com.cesar)
├── dominio-compartilhado          ← Shared Kernel: VOs de Id e enums compartilhados (Java puro)
├── dominio-Notificacao            ← alertas e notificações (Java puro)
├── dominio-IdentidadeAcesso       ← acesso, perfis, status da conta (Java puro)
├── dominio-RelacaoTutor           ← tutor, responsáveis, indicação (Java puro)
├── dominio-RecepcaoTriagem        ← Core: prontuário, triagem, fila por risco (Java puro)
├── dominio-GestaoFluxo            ← fila dinâmica, SLA, chamada (Java puro)
├── dominio-SaudePreventiva        ← ciclo vacinal, carteira de vacinação (Java puro)
├── dominio-AtendimentoClinico     ← relatório clínico, plano nutricional + NEM, catálogo de rações, evolução (Java puro)
├── dominio-AgendamentoClinico     ← agenda, consultas e retornos (Java puro)
├── dominio-ProtocoloInacessibilidade ← protocolo de tutor inacessível, SLA de contato (Java puro)
├── dominio-AssinaturaFaturamento  ← contratação de plano, cobrança, juros de mora (Java puro)
├── dominio-BeneficiosPlano        ← carência e tickets de benefício (Java puro)
├── dominio-Gamificacao            ← conquistas e indicações (Java puro)
├── dominio-Farmacovigilancia      ← catálogo de medicamentos, prescrição, matriz de interação (Java puro)
├── aplicacao                      ← Casos de uso (orquestra os Services do domínio)
├── infraestrutura                 ← Persistência: JPA, entidades, adapters, @Configuration
├── apresentacao-frontend          ← Camada web (fora do escopo deste guia)
└── apresentacao-backend           ← API REST (Spring Boot, Controllers, DTOs, segurança, bootstrap)
```

> Todos os bounded contexts acima estão **ativos** no `<modules>` do pom-pai e
> têm seu próprio agregado raiz, repositório e wiring de beans na infra.

**Regra de dependência (NUNCA inverter):**

```
apresentacao-backend ──▶ aplicacao ──▶ dominio-<Contexto> ──▶ dominio-compartilhado
infraestrutura ──▶ aplicacao + dominio-*        (implementa as interfaces IXxxRepositorio)
apresentacao-backend ──▶ infraestrutura (scope: runtime)   (só para subir os beans)
```

- O domínio **não conhece** Spring, JPA ou qualquer framework. Sem `import
  org.springframework.*` ou `jakarta.persistence.*` em `dominio-*`.
- Todos os módulos herdam do parent `br.com.cesar:petCollar-pai`.
- **Pacote raiz canônico: `br.com.cesar.petCollar`**, seguido da camada e do
  contexto. Ex.: `br.com.cesar.petCollar.dominio.RecepcaoTriagem.triagem`,
  `br.com.cesar.petCollar.apresentacao.IdentidadeAcesso`.
  > ⚠️ Parte do código de domínio ainda usa o pacote legado `petcollar.dominio.*`
  > (minúsculo). **Código novo deve usar `br.com.cesar.petCollar.*`**; ao tocar
  > arquivos no pacote antigo, prefira migrá-los para o canônico.

---

## 3. Convenções de Nomenclatura por Camada

Para um agregado `Xxx`, os arquivos seguem este mapa (referência:
`RecepcaoTriagem`):

| Camada | Tipo | Convenção de nome | Exemplo |
|---|---|---|---|
| domínio | Entidade / Agregado | substantivo do domínio | `Triagem`, `Paciente`, `Tutor` |
| domínio | Value Object | substantivo | `PesoTotal`, `RespostaSintoma`, `Endereco`, `CPF` |
| domínio | VO de identidade | `XxxId` | `TutorId`, `PacienteId`, `TriagemId` |
| domínio | Enum | substantivo | `CorDeRisco`, `StatusTriagem`, `StatusConta` |
| domínio | Interface de repositório | **`IXxxRepositorio`** | `ITutorRepositorio`, `IFilaAtendimentoRepositorio` |
| domínio | Serviço de domínio | `XxxService` | `ClassificacaoDeRiscoService`, `GestaoFilaAtendimentoService` |
| aplicação | Caso de uso | `VerboXxxUseCase` | `FinalizarTriagemUseCase` |
| infraestrutura | Entidade JPA | `XxxJpa` | `TriagemJpa`, `TutorJpa` |
| infraestrutura | Repositório Spring Data | `XxxJpaRepository` | `TriagemJpaRepository` |
| infraestrutura | Adapter (impl. da interface) | `XxxRepositorioJpa` | `TriagemRepositorioJpa` |
| infraestrutura | Impl. em memória (provisória) | `XxxRepositorioEmMemoria` | `FilaAtendimentoRepositorioEmMemoria` |
| infraestrutura | Wiring de beans | `XxxConfig` | `RecepcaoTriagemConfig` |
| apresentação | Controller REST | `XxxController` | `FilaAtendimentoController` |
| apresentação | DTO de entrada | `RequisicaoXxxDTO` (record) | `RequisicaoFilaDTO` |
| apresentação | DTO de saída | `XxxDTO` (record) + `de(...)` | `FilaItemDTO` |

**Organização de pacotes:** por **contexto e depois agregado**, nunca por tipo
técnico. Ex.: `dominio.RecepcaoTriagem.triagem`, não `dominio.entidades`.

---

## 4. Camada de Domínio (`dominio-*`) — Java puro

Esta é a camada mais importante. Regras:

### 4.1 Entidades / Agregados
- **Identidade por VO** (`XxxId`), **recebida pelo construtor** (não é gerada
  dentro do agregado — quem cria gera com `XxxId.gerar()`):
  ```java
  public Triagem(TriagemId id, PacienteId pacienteId) {
      if (id == null)
          throw new IllegalArgumentException("Id da triagem não pode ser nulo.");
      if (pacienteId == null)
          throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
      this.id = id;
      this.pacienteId = pacienteId;
      this.status = StatusTriagem.EM_ELABORACAO;   // estado inicial coerente
      this.criadoEm = LocalDateTime.now();
  }
  ```
- **Validação no construtor**, lançando `IllegalArgumentException` com mensagem
  em português para cada invariante violada.
- **Comportamento rico + máquina de estados**: as regras ficam em métodos da
  entidade (`adicionarResposta(...)`, `definirCorDeRisco(...)`, `finalizar()`,
  `bloquear()`), que **protegem as transições** com `IllegalStateException`:
  ```java
  public void finalizar() {
      if (this.status != StatusTriagem.EM_ELABORACAO)
          throw new IllegalStateException("Só é possível finalizar triagens com status EM_ELABORACAO.");
      this.status = StatusTriagem.FINALIZADA;
      this.finalizadaEm = LocalDateTime.now();
  }
  ```
  **Não** crie setters públicos; a mutação acontece por métodos de negócio
  nomeados.
- **Construtor de reconstituição**: um construtor adicional **com todos os
  campos**, comentado como `// Construtor de RECONSTRUÇÃO`, usado pela infra
  para recriar a entidade a partir do banco sem reexecutar efeitos colaterais.
- **Imutabilidade defensiva**: campos `final` quando o valor não muda; coleções
  retornadas como `Collections.unmodifiableList(...)` e copiadas na entrada
  (`new ArrayList<>(respostas)`).
- **Constantes / limiares de regra** como parâmetros ou `static final`, nunca
  números mágicos espalhados (ex.: limiares de risco Verde `< 5`, Amarelo `5–9`,
  Vermelho `>= 10`; juros simples `0,033%` ao dia).

### 4.2 Value Objects de Identidade (`XxxId`)
- `final class`, construtor **privado**, com **dois factories estáticos**:
  - `gerar()` → cria um novo (`UUID.randomUUID().toString()` por baixo);
  - `de(String valor)` → reconstrói a partir de um valor existente, validando.
  - `getValor()` devolve `String`; `equals`/`hashCode`/`toString` por valor.
  ```java
  public final class TutorId {
      private final String valor;
      private TutorId(String valor) { this.valor = valor; }
      public static TutorId gerar() { return new TutorId(UUID.randomUUID().toString()); }
      public static TutorId de(String valor) {
          if (valor == null || valor.isBlank())
              throw new IllegalArgumentException("TutorId não pode ser vazio.");
          return new TutorId(valor);
      }
      public String getValor() { return valor; }
      // equals / hashCode / toString por valor (Objects.equals / Objects.hash)
  }
  ```
- VOs de Id compartilhados entre contextos moram em **`dominio-compartilhado`**
  (`TutorId`, `PacienteId`, `MedicoId`, `PlanoId`, `AtendimentoId`). Outros VOs
  (`PesoTotal`, `RespostaSintoma`, `CPF`, `Endereco`) ficam no contexto dono.

### 4.3 Enums
- Simples quando bastam (`CorDeRisco { VERMELHO, AMARELO, VERDE }`,
  `StatusTriagem { EM_ELABORACAO, FINALIZADA, BLOQUEADA }`).
- Carregam dados/comportamento quando a regra pede (ex.: `NivelDeAtividade` com
  fator multiplicador da NEM; `StatusConta` ditando acesso).

### 4.4 Interfaces de Repositório (`IXxxRepositorio`)
- Definidas **no domínio**, prefixadas com **`I`**, em português, minimalistas.
  Recebem/retornam **tipos de domínio** (incluindo VOs de Id). Verbos padrão:
  `salvar`, `buscarPorId` (retorna `Optional<T>`), `listar...`, `remover`, e
  finders específicos do agregado.
  ```java
  public interface ITutorRepositorio {
      void salvar(Tutor tutor);
      Optional<Tutor> buscarPorId(TutorId id);
      Optional<Tutor> buscarPorCpf(CPF cpf);
      void remover(TutorId id);
  }
  ```

### 4.5 Serviços de Domínio (`XxxService`)
- Recebem as interfaces `IXxxRepositorio` (e outras dependências) **por
  construtor** e validam que não são nulas.
- Orquestram regras que cruzam entidades/repositórios; podem ser **stateless**
  (ex.: `ClassificacaoDeRiscoService.calcular(...)` só computa o `PesoTotal`).
- Convenção de erros (igual em todo o sistema):
  - **Pré-condição / argumento inválido** → `IllegalArgumentException`.
  - **Estado / conflito de regra de negócio** → `IllegalStateException`.
  ```java
  public List<PosicaoFila> inserirNaFila(Triagem triagem) {
      if (triagem == null)
          throw new IllegalArgumentException("Triagem não pode ser nula.");
      if (triagem.getStatus() != StatusTriagem.FINALIZADA)
          throw new IllegalStateException("Só é possível inserir na fila triagens com status FINALIZADA.");
      ...
  }
  ```
- **Sem anotações Spring** no domínio. A instância vira bean via `@Configuration`
  na infra (ver §6.5).

---

## 5. Camada de Aplicação (`aplicacao`)

> Módulo ainda em scaffold — implemente os casos de uso aqui ao construir as
> funcionalidades.

- Casos de uso finos: classe `VerboXxxUseCase` com um método público
  **`executar(...)`** que delega ao(s) `Service` do domínio. Recebe dependências
  por construtor. **Sem anotações Spring.**
  ```java
  public class FinalizarTriagemUseCase {
      private final FinalizacaoTriagemService finalizacaoService;
      private final GestaoFilaAtendimentoService gestaoFila;
      public FinalizarTriagemUseCase(FinalizacaoTriagemService f, GestaoFilaAtendimentoService g) {
          this.finalizacaoService = f; this.gestaoFila = g;
      }
      public List<PosicaoFila> executar(Triagem triagem) {
          triagem.finalizar();
          return gestaoFila.inserirNaFila(triagem);
      }
  }
  ```
- Use este módulo para orquestração **entre subdomínios**. Casos simples podem ir
  direto do Controller ao `Service` do domínio.

---

## 6. Camada de Infraestrutura (`infraestrutura`)

> Módulo ainda em scaffold. A persistência-alvo é **Spring Data JPA +
> PostgreSQL** (atualmente desligada — ver §9). Enquanto o banco não está ligado,
> implementações **`XxxRepositorioEmMemoria`** servem de stand-in (ex.:
> `FilaAtendimentoRepositorioEmMemoria` no backend). Ao ligar o banco, troque-as
> pelos adapters JPA abaixo, sem tocar no domínio.

Para cada agregado, quatro tipos de arquivo:

### 6.1 Entidade JPA (`XxxJpa`)
- `@Entity` + `@Table(name = "plural_snake_case")` (ex.: `triagens`, `tutores`,
  `pacientes`).
- **`@Id` é o valor `String` do VO de Id** do domínio (não use `@GeneratedValue`;
  o Id nasce no domínio via `XxxId.gerar()`).
- **Construtor `protected` sem-args** exigido pelo JPA.
- **Mapeamento manual domínio ↔ entidade**:
  - `public static XxxJpa fromDomain(Xxx d)` — domínio → entidade (grava
    `id.getValor()`, `enum.name()`, etc.).
  - `public Xxx toDomain()` — entidade → domínio, usando o **construtor de
    reconstituição** e `XxxId.de(...)`.
- **Enums e VOs persistidos como `String`/primitivo**: grave `enum.name()` /
  `id.getValor()` e releia com `Enum.valueOf(...)` / `XxxId.de(...)`. Não use
  `@Enumerated`.
  ```java
  @Entity @Table(name = "triagens")
  public class TriagemJpa {
      @Id private String id;
      @Column(nullable = false) private String pacienteId;
      @Column(nullable = false) private String status;          // StatusTriagem.name()
      private String corDeRisco;                                 // CorDeRisco.name() (pode ser nulo)
      private LocalDateTime criadoEm;
      private LocalDateTime finalizadaEm;
      protected TriagemJpa() {}
      public static TriagemJpa fromDomain(Triagem t) { ... }
      public Triagem toDomain() { /* construtor de reconstituição + XxxId.de(...) */ }
  }
  ```

### 6.2 Relações entre tabelas — regra de DDD
- **Dentro de um mesmo agregado** (filhos realmente "donos"): use relação JPA
  com cascade — `@OneToMany(cascade = ALL, orphanRemoval = true)` +
  `@JoinColumn`. Ex.: `TriagemJpa` → `RespostaSintomaJpa`.
- **Entre agregados diferentes**: NÃO use `@ManyToOne`/`@OneToMany`. Guarde
  **apenas o `String` do Id de referência** (ex.: `TriagemJpa.pacienteId`) e
  **monte o objeto de domínio no Adapter**, buscando o outro agregado pelo seu
  próprio repositório. Mantém os agregados desacoplados.

### 6.3 Repositório Spring Data (`XxxJpaRepository`)
- `interface XxxJpaRepository extends JpaRepository<XxxJpa, String>`.
- Finders por convenção de nome (`findByPacienteId`, `findByCpf`,
  `existsByPacienteIdAndStatus`, `deleteByTriagemId`).
- Consultas complexas com **`@Query`** (JPQL); relatórios/agregações pesadas com
  **`@Query(nativeQuery = true)`** usando text blocks (`"""..."""`) e `@Param`;
  mutações em massa com `@Modifying` + `@Transactional`.

### 6.4 Adapter (`XxxRepositorioJpa`)
- Anotado com **`@Repository`**, recebe o `XxxJpaRepository` por construtor e
  **implementa a interface do domínio `IXxxRepositorio`**, traduzindo com
  `fromDomain`/`toDomain`:
  ```java
  @Repository
  public class TriagemRepositorioJpa implements ITriagemRepositorio {
      private final TriagemJpaRepository jpa;
      public TriagemRepositorioJpa(TriagemJpaRepository jpa) { this.jpa = jpa; }
      @Override public void salvar(Triagem t) { jpa.save(TriagemJpa.fromDomain(t)); }
      @Override public Optional<Triagem> buscarPorId(TriagemId id) {
          return jpa.findById(id.getValor()).map(TriagemJpa::toDomain);
      }
      @Override public void remover(TriagemId id) { jpa.deleteById(id.getValor()); }
  }
  ```
- Operações que tocam várias tabelas levam **`@Transactional`** no método do
  Adapter.

### 6.5 Configuração de beans (`XxxConfig`)
- Classe `@Configuration` que monta os **Services do domínio como `@Bean`**,
  injetando as interfaces `IXxxRepositorio` (que o Spring resolve para os
  Adapters):
  ```java
  @Configuration
  public class RecepcaoTriagemConfig {
      @Bean public GestaoFilaAtendimentoService gestaoFilaAtendimentoService(IFilaAtendimentoRepositorio r) {
          return new GestaoFilaAtendimentoService(r);
      }
      @Bean public ClassificacaoDeRiscoService classificacaoDeRiscoService() {
          return new ClassificacaoDeRiscoService();
      }
  }
  ```
- É aqui que se faz a **wiring de padrões de projeto** (ex.: registrar
  observadores de `Notificacao` num Service antes de devolver o bean).
- **Seeds de dados** ficam em `@Bean CommandLineRunner` dentro do `Config`.

---

## 7. Camada de Apresentação (`apresentacao-backend`)

### 7.1 Controllers
- `@RestController` + `@RequestMapping("/api/recurso")` (ex.:
  `/api/recepcao/fila`). **Rotas sempre sob `/api`.**
- Dependências (Services do domínio, Repositórios, UseCases) **por construtor**.
- Verbos HTTP: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`
  (transições de estado), `@DeleteMapping`. Use `ResponseEntity` para controlar
  status (`ResponseEntity.ok(...)`, `.noContent().build()`) ou
  `@ResponseStatus`.
- O controller **traduz DTO ↔ domínio** (reconstrói VOs com `XxxId.de(...)`,
  enums com `Enum.valueOf(...)`) e delega ao Service. **Não** coloque regra de
  negócio no controller.
  ```java
  @PostMapping
  public ResponseEntity<List<FilaItemDTO>> inserir(@RequestBody RequisicaoFilaDTO req) {
      Triagem triagem = new Triagem(TriagemId.de(req.triagemId()), PacienteId.de(req.pacienteId()));
      triagem.definirCorDeRisco(CorDeRisco.valueOf(req.corDeRisco()));
      triagem.finalizar();
      return ResponseEntity.ok(gestaoFila.inserirNaFila(triagem).stream().map(FilaItemDTO::de).toList());
  }
  ```

### 7.2 DTOs (`record`)
- Sempre **`record`**. Entrada: **`RequisicaoXxxDTO`**; saída: **`XxxDTO`** com um
  **factory estático `de(EntidadeOuVO)`** que monta o DTO a partir do domínio.
  DTOs simples podem ser **records aninhados** dentro do Controller.
  ```java
  record FilaItemDTO(String pacienteId, String triagemId, String corDeRisco, LocalDateTime finalizadaEm) {
      static FilaItemDTO de(PosicaoFila p) {
          return new FilaItemDTO(p.getPacienteId().getValor(), p.getTriagemId().getValor(),
                                 p.getCorDeRisco().name(), p.getFinalizadaEm());
      }
  }
  ```
- DTOs trafegam **tipos primitivos/String** (Id como `String`, enum como
  `String`, datas ISO), nunca expõem objetos de domínio diretamente.

### 7.3 Tratamento de erros
- Prefira **centralizar** num `@RestControllerAdvice` global que mapeie:
  - `IllegalArgumentException` → **400 Bad Request**
  - `IllegalStateException` → **409 Conflict**
  - exceções de domínio específicas (`ContaSuspensaException`,
    `CredenciaisInvalidasException`, `PagamentoPendenteException`) → o status
    adequado (401/402/403/409).
  - Resposta padronizada: `record ErroResponse(String mensagem)`.
- Lance as exceções normalmente no domínio; o handler cuida do HTTP.

### 7.4 Segurança (Spring Security + JWT)
- Autenticação por **JWT HS256** (`JwtService`, `JwtAuthFiltro`,
  `SecurityConfig`, `JwtProperties`), no contexto `IdentidadeAcesso`.
- Configuração em `application.yml` sob `petcollar.security.jwt`
  (`secret`/`issuer`/`expiracao-minutos`) — **segredo nunca commitado**; troque
  por variável de ambiente/secret manager em produção.
- Perfis de usuário via enum `Perfil`; estado da conta via `StatusConta`.

### 7.5 Bootstrap
- `PetCollarApplication` (`@SpringBootApplication`) é o entrypoint, no pacote
  `br.com.cesar.petCollar.apresentacao`. CORS é configurado a partir de
  `petcollar.security.cors.origens-permitidas` (patterns `http://localhost:*`
  em dev; origens fixas em produção).

---

## 8. Padrões de Projeto em Uso (replicar quando aplicável)

| Padrão | Onde | Como |
|---|---|---|
| **Repository** | todo o sistema | interface `IXxxRepositorio` no domínio + impl. na infra |
| **Adapter** | `*RepositorioJpa` / `*RepositorioEmMemoria` | adapta a tecnologia → interface do domínio |
| **Factory Method** | `XxxId.gerar()`/`XxxId.de()`, construtor de reconstituição, `XxxDTO.de()` | criação/recriação controlada de objetos |
| **Decorator** | F-07 `Cobranca` (juros), F-11 `CalculadoraNEM` (peso metabólico → atividade → comorbidade), F-12 `CalculadoraDoseMaximaSegura` (base → tag clínica → alergia) | camadas cumulativas que transformam um cálculo; cada decorator empilha sobre o anterior preservando a interface comum |
| **Strategy** | F-02 `ClassificacaoDeRiscoService`, F-11 `RecomendacaoRacaoService` (3 strategies pontuam cada ração por comorbidade/faixa etária/porte e o service agrega) | cálculo/ranqueamento por critério intercambiável; novas strategies entram via `List<EstrategiaXxx>` no Config |
| **State** | F-02 `Triagem`/`StatusTriagem`, F-07 `StatusConta`, F-11 `PlanoNutricional`/`StatusPlanoNutricional` (RASCUNHO→FINALIZADO→SUBSTITUIDO), F-12 `Prescricao`/`StatusPrescricao` (FINALIZADA→SUBSTITUIDA) | transições guardadas (`verificarRascunho()`, `marcarComoSubstituido()`) que bloqueiam operações inválidas e preservam imutabilidade após assinatura |
| **Observer** | `Notificacao` | Services notificam observadores (alertas de SLA, vacina, estoque) — wiring no `Config` |
| **Service Layer** | `dominio-*` | regras que cruzam entidades/repositórios |

- Documente o padrão com um **Javadoc curto** explicando a intenção.
- Para sobrepor um bean (ex.: cache/proxy na frente de um adapter), use
  `@Primary`.

---

## 9. Persistência / Banco de Dados

- **Alvo:** PostgreSQL + Spring Data JPA, com `spring.jpa.hibernate.ddl-auto=update`
  (schema derivado das entidades, sem migrations manuais).
- **Estado atual:** DataSource/JPA estão **desligados** no `application.yml`
  (`spring.autoconfigure.exclude`) enquanto o domínio não é mapeado ao banco — o
  login usa usuários **em memória**. Ao ligar a persistência, remova essas
  exclusões e adicione as credenciais.
- **Configuração via `application.yml`** (não `.properties`). Segredos (JWT,
  banco) ficam fora do versionamento (env/secret manager). **Nunca** commite
  segredos reais — o valor atual do JWT é apenas placeholder de dev.
- **Chaves primárias são o `String` do VO de Id** gerado no domínio
  (`XxxId.gerar()`), não auto-incremento.
- Tabelas em **snake_case plural**; colunas seguem o nome do campo Java, com
  `@Column(nullable=false)` para obrigatórios e `columnDefinition="TEXT"` para
  textos longos.

---

## 10. Testes (BDD com Cucumber, em português)

- Testes vivem **em cada módulo de domínio** (`dominio-<Contexto>/src/test`),
  validando regras de negócio em isolamento — **sem banco, sem Spring**.
  Dependências (`IXxxRepositorio`) são **mockadas com Mockito**.
- Estrutura por contexto, três peças:
  - **`src/test/resources/features/<nome>.feature`**: Gherkin em **português**
    (`# language: pt`, `Funcionalidade`, `Cenário`, `Dado`, `Quando`, `Então`,
    `E`). Ex.: `triagem_clinica.feature`, `prontuario_unificado.feature`.
  - **`CucumberRunnerTest.java`**: suíte JUnit 5 Platform que liga o Cucumber:
    ```java
    @Suite @IncludeEngines("cucumber")
    @SelectClasspathResource("features")
    @ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "br.com.cesar.petCollar.dominio.RecepcaoTriagem.bdd")
    @ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
    public class CucumberRunnerTest {}
    ```
  - **Step definitions** no pacote `...<Contexto>.bdd`, com
    `io.cucumber.java.pt.{Dado,Quando,Então}`, montando entidades reais +
    repositórios mockados, e asserções com JUnit. Compartilhe estado entre steps
    por um `ContextoCenario`. Capture exceções para validar no `Então`.
- Rodar tudo: `mvn test`. Build completo: `mvn clean install`. Subir API:
  `cd apresentacao-backend && mvn spring-boot:run`.

---

## 11. Checklist para Criar uma Nova Funcionalidade

Siga esta ordem (de dentro para fora), espelhando `RecepcaoTriagem`:

1. **Domínio** (`dominio-<Contexto>`, pacote `br.com.cesar.petCollar.dominio.<Contexto>.<agregado>`):
   - [ ] Entidade(s)/agregado com `XxxId` recebido no construtor, validação,
         métodos de negócio com transições guardadas e construtor de
         reconstituição.
   - [ ] VOs/enums necessários (VOs de Id compartilhados em `dominio-compartilhado`).
   - [ ] Interface `IXxxRepositorio` (verbos em português, `Optional` em buscas,
         tipos de domínio).
   - [ ] `XxxService` com regras, validando dependências no construtor, lançando
         `IllegalArgumentException`/`IllegalStateException`.
2. **Testes BDD** do domínio: `features/*.feature` (pt) + `CucumberRunnerTest` +
   steps no pacote `.bdd` com mocks.
3. **Infra** (`infraestrutura`, pacote do contexto):
   - [ ] `XxxJpa` (`@Entity`/`@Table`, ctor `protected`, `fromDomain`/`toDomain`,
         Id/enum como String, referências entre agregados como String).
   - [ ] `XxxJpaRepository extends JpaRepository<XxxJpa, String>`.
   - [ ] `XxxRepositorioJpa implements IXxxRepositorio` (`@Repository`,
         `@Transactional` se multi-tabela). [ou `XxxRepositorioEmMemoria` provisório]
   - [ ] `XxxConfig` (`@Configuration`) expondo o(s) `Service` como `@Bean` e
         fazendo wiring de padrões/seeds.
4. **Apresentação** (`apresentacao-backend`, pacote do contexto):
   - [ ] `RequisicaoXxxDTO`/`XxxDTO` (`record`, `XxxDTO.de(...)`).
   - [ ] `XxxController` (`@RestController`, rotas `/api/...`, injeção por
         construtor, sem regra de negócio, deixa as exceções subirem para o
         handler global).

**Regras de ouro:**
- Domínio é sagrado: **zero dependência de framework** lá dentro.
- Tudo em **português**, fiel à **linguagem onipresente**; mensagens de erro
  descritivas e específicas.
- **`IllegalArgumentException`** = entrada/argumento inválido (vira 400);
  **`IllegalStateException`** = conflito de regra de negócio (vira 409).
- Identidade sempre por **VO `XxxId`** (`gerar()` ao criar, `de(String)` ao
  reconstituir); persiste como `String`.
- Conversão domínio↔persistência só via `fromDomain`/`toDomain` + construtor de
  reconstituição; conversão domínio↔API só via `record` DTO + `de(...)`.
- Pacote raiz **`br.com.cesar.petCollar`**; organize por **contexto/agregado**,
  injete por **construtor**, prefira **imutabilidade**.
