## 🐾 **Sobre o Projeto**

O petCollar é uma plataforma de gestão veterinária de alta complexidade, desenvolvida para modernizar e proteger o fluxo assistencial em clínicas veterinárias. O sistema tem como motor central a segurança do paciente e a gravidade médica, garantindo que o tempo de resposta clínico dite o ritmo de toda a operação, desde a recepção inteligente até a inteligência farmacológica avançada.

---

## ⭐ **Funcionalidades Principais**

### **🔍 Recepção e Biossegurança Clínica**

- **Prontuário Unificado com Análise Epidemiológica:** Identificação inteligente do Tutor via CPF que executa uma varredura preditiva de riscos infectocontagiosos nos últimos 40 dias. Caso detectado, o sistema aplica o Alerta Epidemiológico, forçando a priorização imediata para isolamento sanitário.
- **Triagem e Gestão Dinâmica de Fila:** Motor de cálculo que converte sintomas em um PesoTotal determinístico, definindo a Cor de Risco e a prioridade na Fila de Espera Dinâmica. O sistema monitora o SLA e automatiza a promoção ao topo da fila em casos de atraso crítico, provendo Previsão de Espera em tempo real.
- **Protocolo de Tutor Inacessível com Escalonamento:** Fluxo de contingência automatizado para garantir a continuidade assistencial quando o responsável não responde. Inclui tentativas progressivas de contato e escalonamento para níveis de decisão clínica via responsáveis secundários.

### **🚀 Crescimento e Fidelização Gamificada**

- **Programa de Indicação com Recompensas de Elite:** Motor de crescimento orgânico que gerencia links permanentes e automatiza recompensas de duas vias: 30% de desconto para o indicado e 15% de desconto mais uma Conquista Lendária para o indicador após a confirmação do pagamento.
- **Programa de Conquistas e Gamificação do Assinante:** Sistema orientado a eventos que concede badges de raridade (Comum a Lendária) baseadas em marcos de saúde e fidelidade. Inclui barras de progresso em tempo real para incentivar o engajamento contínuo.

### **📋 Portal do Tutor e Medicina Preventiva**

- **Agendamento de Retorno Integrado a Exames:** Validador clínico que vincula obrigatoriamente a consulta de retorno à conclusão de laudos e exames solicitados anteriormente, otimizando a precisão do diagnóstico final.
- **Gestão Inteligente do Ciclo Vacinal:** Painel sentinela que realiza cálculos preditivos de doses futuras baseados em intervalos biológicos e protocolos clínicos, disparando alertas de proximidade e recalculando previsões em caso de atrasos.

### **🩺 Inteligência Médica e Terapêutica**

- **Relatório Clínico Evolutivo e Sumário de Saúde:** Compilação automática de dados vitais para geração de gráficos de evolução comparativa entre os atendimentos, finalizado com assinatura digital imutável para transparência do Tutor.
- **Gestão Nutricional Avançada e Prescrição de NEM:** Suporte à decisão clínica que automatiza o cálculo da Necessidade Energética de Manutenção via fórmula metabólica (P^{0,75}), integrando comorbidades e cronogramas de transição alimentar.
- **Central de Farmacovigilância e Gestão Terapêutica:**Motor de segurança farmacológica que executa a Blindagem de Dosagem, valida interações medicamentosas graves e projeta a Data de Fim do Tratamento baseada no fármaco de maior duração.

### **🏥 Atendimento Clínico**

- **Chamada e vínculo de consultório**: transição automática de status e vinculação do médico e consultório ao prontuário ao acionar o chamado.
- **Plano nutricional (NEM)**: cálculo de gramas diárias de ração com base no peso ideal e nível de atividade do animal.
- **Calculadora de dosagem segura**: bloqueio automático de prescrições que excedam o limite de segurança por kg cadastrado para o medicamento.
- **Data de fim do tratamento**: identificação automática do medicamento de uso mais longo para preencher a conclusão da receita.

### **💳 Gestão Financeira e de Benefícios**

- **Assinatura de Plano e Gestão Financeira**: Controle dinâmico de acesso com processamento de Juros Simples de 0,033% ao dia e transições automáticas de status da conta para Inadimplente ou Suspensa.
- **Gerenciamento de Carência de Benefícios do Plano**: Motor de cálculo que monitora limites e períodos de carência em tempo real, emitindo Tickets de Benefício com código GUID único e validade de 48 horas.

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
> - 🎨 [Protótipo](https://wink-equity-09401367.figma.site/)
>
> Clique nos nomes acima para acessar os arquivos correspondentes.  
> As imagens abaixo também são clicáveis e redirecionam para suas respectivas pastas.

---
<!--
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
  -->

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
