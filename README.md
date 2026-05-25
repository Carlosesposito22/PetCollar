## 🐾 **Sobre o Projeto**

O **Sistema de Inteligência Clínica e Gestão Veterinária** é uma plataforma desenvolvida para modernizar e proteger o fluxo de atendimento em clínicas veterinárias. O sistema tem como motor central a **segurança do paciente** e a **gravidade médica**, garantindo que casos críticos sejam sempre priorizados — desde a recepção até a emissão da receita.

---

## ⭐ **Funcionalidades Principais**

### **🔍 Recepção Inteligente**

- **Busca com Varredura Epidemiológica:** Localização ágil do tutor com análise automática do histórico clínico dos animais. Pacientes com doenças infectocontagiosas nos últimos 40 dias recebem a tag "Alerta Epidemiológico" e são movidos automaticamente para o topo da fila, garantindo a biossegurança da clínica.
- **Cadastro com tagueamento automático**: cálculo de idade em tempo real e aplicação de tags visuais como *Braquiocefálico* e *Geriátrico* com base em raça e idade.
- **Registro de queixa e alertas comportamentais** (ex: animal agressivo ou estressado) para preparar a equipe clínica antes do atendimento.

### **🚦 Triagem e Classificação de Risco**

- **Formulário de sintomas com pesos predefinidos** que resulta automaticamente em uma cor de prioridade: Vermelho, Amarelo ou Verde.
- **Remoção da subjetividade** no primeiro contato, garantindo detecção precoce de casos graves.

### **📋 Gestão da Fila de Espera**

- **Fila ordenada por gravidade** (Vermelho > Amarelo > Verde), com tempo de chegada usado apenas como critério de desempate.
- **Previsão de espera dinâmica**: posição na fila × tempo médio por cor de risco, atualizada a cada chamada.
- **Atualização ativa do painel**: ao acionar o painel, o sistema recalcula e persiste o tempo estimado de todos os pacientes em espera, verifica e persiste o status de SLA de cada um — aplicando alertas laranja ou vermelho quando os limites são atingidos — e só então retorna os dados consolidados com as contagens de violações e alertas epidemiológicos ativos.

### **⏱️ Monitoramento de SLA**

- **Alertas automáticos**: destaque laranja ao atingir 80% do prazo e alerta vermelho crítico ao atingir 100%.
- **Promoção automática ao topo da fila** quando o tempo de espera expira, garantindo que nenhum paciente crítico seja esquecido.

### **🏥 Atendimento Clínico**

- **Chamada e vínculo de consultório**: transição automática de status e vinculação do médico e consultório ao prontuário ao acionar o chamado.
- **Plano nutricional (NEM)**: cálculo de gramas diárias de ração com base no peso ideal e nível de atividade do animal.
- **Calculadora de dosagem segura**: bloqueio automático de prescrições que excedam o limite de segurança por kg cadastrado para o medicamento.
- **Data de fim do tratamento**: identificação automática do medicamento de uso mais longo para preencher a conclusão da receita.

---

> [!WARNING]
> **📦 Entregáveis do Projeto**
> 
> Abaixo estão os principais artefatos desenvolvidos e organizados na pasta [`/entregaveis`](https://github.com/Carlosesposito22/petCollar/tree/main/entregaveis):
>
> - 📽️ [Apresentação](https://github.com/Carlosesposito22/petCollar/tree/main/entregaveis/apresentacao)
> - 🧩 [CML](https://github.com/Carlosesposito22/petCollar/tree/main/entregaveis/cml)
> - 🧠 [Domínio](https://github.com/Carlosesposito22/petCollar/tree/main/entregaveis/dominio)
> - 🗺️ [Mapa de Histórias](https://github.com/Carlosesposito22/petCollar/tree/main/entregaveis/mapa)
> - 🎨 [Protótipo](https://disco-fancy-97893312.figma.site/)
>
> Clique nos nomes acima para acessar os arquivos correspondentes.  
> As imagens abaixo também são clicáveis e redirecionam para suas respectivas pastas.

---

## ▶️ **Como rodar o projeto**

O sistema é dividido em **dois módulos** que sobem separadamente:

| Módulo | Tecnologia | Porta | Pasta |
|---|---|---|---|
| **Backend (API)** | Java 17+ · Spring Boot · Spring Security · JWT | `8080` | `Projeto/apresentacao-backend` |
| **Frontend (SPA)** | React · Vite · TypeScript · Tailwind | `5173` | `Projeto/apresentacao-frontend/web` |

> O frontend faz proxy de `/api/**` → `http://localhost:8080`, então **suba primeiro o backend** e depois o frontend.

---

### 1) 📋 Pré-requisitos

Antes de começar, instale na sua máquina:

| Ferramenta | Versão | Verificar | Onde obter |
|---|---|---|---|
| **Git** | qualquer | `git --version` | <https://git-scm.com/downloads> |
| **JDK** | **17 ou superior** (21 LTS recomendado) | `java -version` | <https://adoptium.net/> |
| **Node.js** | **18 ou superior** (inclui `npm`) | `node -v` e `npm -v` | <https://nodejs.org/> (LTS) |

> ⚡ **Você NÃO precisa instalar o Maven.** O projeto inclui o **Maven Wrapper** (`mvnw` / `mvnw.cmd`) — basta usar `.\mvnw.cmd` em vez de `mvn` e ele baixa a versão correta do Maven automaticamente na primeira execução.

> 💡 **Windows (atalho):** com [winget](https://learn.microsoft.com/windows/package-manager/winget/) (já vem no Windows 10+), instale tudo numa linha:
> ```powershell
> winget install -e EclipseAdoptium.Temurin.21.JDK OpenJS.NodeJS.LTS Git.Git
> ```
> Feche e reabra o terminal para o `PATH` atualizar.

> ⚠️ **Verifique se `JAVA_HOME` está definido.** No PowerShell: `echo $env:JAVA_HOME`. Se vier vazio, defina (substitua o caminho pelo seu JDK):
> ```powershell
> [Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-21", "User")
> ```
> e reabra o terminal.

---

### 2) 📥 Clonar o repositório

Abra um terminal **na pasta onde você quer guardar o projeto** (ex.: `Documents`) e execute:

```bash
git clone https://github.com/Carlosesposito22/petCollar.git
cd petCollar
```

A partir deste ponto, **todos os caminhos abaixo são relativos à raiz `petCollar/`**.

---

### 3) 🟦 Subir o backend (porta 8080)

Abra um terminal **na raiz do repositório**. São dois passos: instalar todos os módulos no repositório local do Maven e depois subir só o backend.

**Windows (PowerShell):**
```powershell
cd Projeto

# 1) Compila e instala todos os módulos (só precisa rodar a primeira vez
#    ou depois de mexer em algum dominio-*/aplicacao/infraestrutura)
.\mvnw.cmd "-Dmaven.test.skip=true" install

# 2) Sobe o Spring Boot apenas no módulo de apresentação
.\mvnw.cmd -pl apresentacao-backend spring-boot:run
```

**Linux / macOS:**
```bash
cd Projeto
chmod +x mvnw     # apenas na primeira vez
./mvnw -Dmaven.test.skip=true install
./mvnw -pl apresentacao-backend spring-boot:run
```

> **Primeira execução:** o wrapper baixa o Maven 3.9.9 (~10 MB) em `~/.m2/wrapper`, depois o Maven baixa todas as dependências — pode levar alguns minutos. Da segunda vez em diante é instantâneo.
>
> ⚠️ **Por que dois comandos?** O Spring Boot Maven Plugin (`spring-boot:run`) é executado em todos os módulos do reactor quando usado com `-am`, e falha nos POMs intermediários. Rodar `install` primeiro popula o `~/.m2` local; depois `-pl apresentacao-backend` (sem `-am`) sobe só o módulo de apresentação.
>
> 💡 **Aspas em volta de `"-Dmaven.test.skip=true"` são necessárias no PowerShell** (sem elas o ponto é interpretado como operador).

Quando aparecer no log algo como `Started PetCollarApplication in X seconds`, o backend está pronto em **<http://localhost:8080>**.

**Deixe este terminal aberto** e abra outro para o passo seguinte.

---

### 4) 🟩 Subir o frontend (porta 5173)

Em um **novo terminal**, partindo da raiz do repositório:

```bash
cd Projeto/apresentacao-frontend/web
npm install
npm run dev
```

- `npm install` baixa as dependências do frontend (só precisa rodar uma vez, ou quando o `package.json` mudar).
- `npm run dev` sobe o Vite com hot-reload.

Acesse no navegador: **<http://localhost:5173>**

---

### 5) 🔐 Credenciais de demonstração

A senha de **todos** os usuários de exemplo é: `petcollar123`

| Perfil | Identificador | Observação |
|---|---|---|
| **Tutor** | `tutor@petcollar.com` | login por e-mail |
| **Tutor (suspenso)** | `suspenso@petcollar.com` | exibe banner vermelho de conta suspensa |
| **Recepcionista** | `100001` | matrícula de 6 dígitos |
| **Médico Veterinário** | `200001` | matrícula de 6 dígitos |

> Os usuários ficam em memória ([UsuarioRepositorioEmMemoria.java](Projeto/apresentacao-backend/src/main/java/br/com/cesar/petCollar/apresentacao/IdentidadeAcesso/UsuarioRepositorioEmMemoria.java)) enquanto o agregado de IdentidadeAcesso não estiver pronto.

---

### 6) 🛠️ Comandos úteis

Todos relativos à raiz do repositório.

**Backend** (use `.\mvnw.cmd` no PowerShell ou `./mvnw` no Linux/macOS)
```powershell
# compilar e instalar tudo sem rodar testes
cd Projeto; .\mvnw.cmd "-Dmaven.test.skip=true" install

# rodar apenas os testes
cd Projeto; .\mvnw.cmd test

# subir só o backend (pressuponha que o install já foi feito)
cd Projeto; .\mvnw.cmd -pl apresentacao-backend spring-boot:run

# limpar tudo (target/)
cd Projeto; .\mvnw.cmd clean
```

**Frontend**
```bash
cd Projeto/apresentacao-frontend/web

npm run dev      # servidor de desenvolvimento (hot-reload)
npm run build    # build de produção em dist/
npm run preview  # serve o build de produção localmente
```

---

### 7) 🐛 Resolução de problemas

| Sintoma | Causa provável | Solução |
|---|---|---|
| `mvn` não é reconhecido | Você está usando `mvn` em vez do wrapper | Use `.\mvnw.cmd` (PowerShell) ou `./mvnw` (bash) — não precisa instalar Maven |
| `JAVA_HOME not found in your environment` | Variável de ambiente faltando | Defina `JAVA_HOME` conforme nota em [§1](#1--pré-requisitos) e reabra o terminal |
| `Unable to find a suitable main class` (no `petCollar-pai`) | `spring-boot:run` rodou no POM-pai em vez do módulo | Use **dois comandos** como em [§3](#3--subir-o-backend-porta-8080): primeiro `install`, depois `-pl apresentacao-backend spring-boot:run` |
| `Failed to determine a suitable driver class` | Spring tentou ligar JPA/DataSource | Confirme que [application.yml](Projeto/apresentacao-backend/src/main/resources/application.yml) tem as quatro entradas em `spring.autoconfigure.exclude` |
| `Unknown lifecycle phase ".test.skip=true"` | PowerShell quebrou o argumento no ponto | Envolva em aspas: `"-Dmaven.test.skip=true"` |
| `Failed to delete ...\target\classes\...` | Arquivo travado por IDE/Java em execução | Feche a IDE/processo Java e rode novamente, ou apague os `target/` manualmente |
| `cannot find symbol: class PacienteId / MedicoId / AtendimentoId` | Os IDs do agregado de relatório ainda não existem na forma final | Mantidos como `record` placeholder em [Projeto/dominio-AtendimentoClinico/.../relatorio/](Projeto/dominio-AtendimentoClinico/src/main/java/petCollar/dominio/AtendimentoClinico/relatorio/) — substituir pelo tipo real do domínio quando ele for criado |
| `port 8080 already in use` | Outro processo na 8080 | `netstat -ano | findstr :8080` e finalize o PID, ou mude a porta em [application.yml](Projeto/apresentacao-backend/src/main/resources/application.yml) |
| `port 5173 already in use` | Vite já rodando | Feche a outra instância ou rode `npm run dev -- --port 5174` |
| `EACCES`/`EPERM` no `npm install` | Permissões do `node_modules` | Apague `node_modules` e `package-lock.json` e rode `npm install` de novo |
| Login retorna `401` | Senha errada — confira em [§5](#5--credenciais-de-demonstração) | Use `petcollar123` |
| Login retorna `423` | Conta marcada como suspensa | Use outro tutor (`tutor@petcollar.com`) — esse banner é proposital |
| Tela em branco no `:5173` | Backend offline; chamadas `/api` falham | Confirme que o backend está no ar em `:8080` |

---

## 🏛️ **Arquitetura DDD**

O sistema é estruturado em três **Bounded Contexts** com responsabilidades bem delimitadas, dois subdomínios **Core** e um **Supporting**, interligados por relações estratégicas de Upstream/Downstream e Shared Kernel.

```
ContextoRecepcao [U, OHS, PL] ──→ [D, ACL] ContextoGestaoEspera
ContextoRecepcao [U, OHS, PL] ──→ [D, ACL] ContextoClinico
ContextoGestaoEspera [U, S]   ──→ [D, C]   ContextoClinico

ContextoRecepcao     [SK] ↔ [SK] ContextoGestaoEspera
ContextoRecepcao     [SK] ↔ [SK] ContextoClinico
ContextoGestaoEspera [SK] ↔ [SK] ContextoClinico
```

### **Subdomínios**

| Subdomínio | Tipo | Responsabilidade |
|---|---|---|
| `TriagemRecepcaoSubdomain` | CORE | Identificação, cadastro e classificação de risco |
| `GestaoFluxoSubdomain` | SUPPORTING | Operacionalização da espera e SLAs |
| `AtendimentoPrescricaoSubdomain` | CORE | Execução clínica e segurança medicamentosa |

---

## 📦 **Bounded Contexts**

### 🟦 ContextoRecepcao — `TriagemRecepcaoSubdomain (CORE)`

Porta de entrada do sistema. Identifica o tutor, cadastra o paciente com tagueamento automático, registra queixa e comportamento, e executa a triagem por escore de sintomas que resulta na Cor de Risco.

**Agregados:** `PacienteKernel` · `Tutor` · `Paciente` · `Triagem`

**Repositories:** `PacienteReferenciaRepository` · `TutorRepository` · `PacienteRepository` · `TriagemRepository`

---

### 🟨 ContextoGestaoEspera — `GestaoFluxoSubdomain (SUPPORTING)`

Operacionaliza a sala de espera com inteligência. Calcula previsões dinâmicas, exibe o painel gerencial, ordena a fila por gravidade e monitora SLAs com alertas automáticos.

**Agregados:** `FilaDeEspera` · `MonitoramentoSLA`

**Repositories:** `FilaDeEsperaRepository` · `MonitoramentoSLARepository`

**Domain Event:** `PacienteChamadoEvent` — dispara o recálculo automático de todos os tempos estimados da fila a cada chamada.

---

### 🟥 ContextoClinico — `AtendimentoPrescricaoSubdomain (CORE)`

Executa o atendimento médico-veterinário com foco em segurança do paciente. Vincula médico e consultório ao prontuário, calcula o plano nutricional, trava dosagens inseguras e define a data de término do tratamento.

**Agregados:** `Atendimento` · `PlanoNutricional` · `Prescricao`

**Repositories:** `AtendimentoRepository` · `MedicoRepository` · `ConsultorioRepository` · `PlanoNutricionalRepository` · `PrescricaoRepository` · `MedicamentoRepository`

---

## 📋 **Distribuição de Tarefas**

### **Funcionalidades por Responsável**

| Funcionalidade | Responsável |
|---|---|
| **1 - Busca Inteligente com Varredura Epidemiológica** | 🎯 Carlos Eduardo |
| **2 - Cadastro com Tagueamento Automático** | 🎯 Mateus Ribeiro |
| **3 - Registro de Queixa e Comportamento** | 🎯 Felipe Marques |
| **4 - Triagem e Classificação de Risco** | 🎯 Bruno Assuncao |
| **5 - Previsão de Espera Dinâmica** | 🎯 Felipe Marques |
| **6 - Atualização Ativa do Painel de Espera** | 🎯 Carlos Eduardo |
| **7 - Fila Ordenada por Risco** | 🎯 Bruno Assuncao |
| **8 - Gestão de Alerta de SLA** | 🎯 Carlos Eduardo |
| **9 - Chamada e Vínculo de Consultório** | 🎯 Artur Sales |
| **10 - Plano Nutricional por Fórmula NEM** | 🎯 Mateus Ribeiro |
| **11 - Calculadora de Dosagem Segura** | 🎯 Mateus Ribeiro |
| **12 - Geração de Receita e Prazo de Tratamento** | 🎯 Artur Sales |
