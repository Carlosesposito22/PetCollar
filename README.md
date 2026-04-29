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
| **1 - Prontuário Unificado do Tutor com Análise Epidemiológica** | 🎯 Artur Sales |
| **2 -Triagem Clínica com Classificação Automática de Risco** | 🎯 Artur Sales   |
| **3 - Protocolo Automatizado de Tutor Inacessível com Escalonamento** | 🎯 Carlos Eduardo |
| **4 - Programa de Indicação com Recompensas** | 🎯 Carlos Eduardo |
| **5 - Agendamento de Retorno com Integração de Exames Diagnósticos** | 🎯 Carlos Eduardo |
| **6 - Monitoramento, Agendamento e Gestão Inteligente do Ciclo Vacinal** | 🎯 Mateus Ribeiro |
| **7 - Assinatura de Plano e Gestão Financeira do Tutor** | 🎯 Felipe Marques |
| **8 - Gerenciamento de Carência de Benefícios do Plano** | 🎯 Bruno Assuncao |
| **9 - Programa de Conquistas e Gamificação do Assinante** | 🎯 Bruno Assuncao |
| **10 - Emissão de Relatório Clínico Evolutivo e Sumário de Saúde** | 🎯 Mateus Ribeiro |
| **11 - Gestão Nutricional Avançada e Prescrição de NEM** | 🎯 Felipe Marques |
| **12 - Central de Farmacovigilância Inteligente e Gestão Terapêutica Integrada** | 🎯 Felipe Marques |
