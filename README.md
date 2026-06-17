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

## ▶️ **Como rodar o projeto**

O jeito mais simples de subir o petCollar é via **Docker Compose**, que sobe o banco de dados (PostgreSQL), o backend (Spring Boot) e o frontend (React/Nginx) com um único comando.

| Serviço | URL de acesso |
|---|---|
| **Frontend** | <http://localhost:3000> |
| **Backend (API)** | <http://localhost:8080> |
| **Banco de dados** | `localhost:5433` (usuário/senha/banco: `petcollar`) |

---

### 1) 📋 Pré-requisito

Instale o **Docker Desktop** (inclui o Docker Compose):

| SO | Link |
|---|---|
| Windows / macOS | <https://www.docker.com/products/docker-desktop/> |
| Linux | <https://docs.docker.com/engine/install/> |

Verifique a instalação:

```bash
docker --version
docker compose version
```

---

### 2) 📥 Clonar o repositório

```bash
git clone https://github.com/Carlosesposito22/petCollar.git
cd petCollar
```

---

### 3) 🚀 Subir a stack completa

Na **raiz do repositório** (onde está o `docker-compose.yml`), execute:

```bash
docker compose up --build
```

> Na primeira execução o Docker faz o build das imagens — pode levar alguns minutos. Das próximas vezes, sem o `--build`, a inicialização é quase imediata.

Quando o log do backend exibir `Started PetCollarApplication`, acesse o frontend em **<http://localhost:3000>**.

Para derrubar a stack:

```bash
docker compose down          # para os containers, preserva o banco
docker compose down -v       # para os containers e apaga o volume do banco
```

---

### 4) 🔐 Credenciais de demonstração

A senha de **todos** os usuários de exemplo é: `petcollar123`

| Perfil | Onde logar | Identificador | Observação |
|---|---|---|---|
| **Administrador** | link "Sou administrador da clínica →" no rodapé | `admin@petcollar.com` | cadastra funcionários e gerencia contas |
| **Tutor** | card "Tutor" | `tutor@petcollar.com` | login por e-mail |
| **Tutor (suspenso)** | card "Tutor" | `suspenso@petcollar.com` | exibe banner vermelho de conta suspensa |
| **Recepcionista** | card "Recepcionista" | `100001` | matrícula de 6 dígitos |
| **Médico Veterinário** | card "Médico Veterinário" | `200001` | matrícula de 6 dígitos |

**Fluxos para experimentar:**
1. **Contratar plano** (link na tela do Tutor) → preencher dados → ver QR Code → **"Já paguei (simular)"** → voltar e logar como o novo tutor.
2. **Login admin** → aba **Funcionários** → **+ Cadastrar funcionário** → criar recepcionista/médico (matrícula gerada automaticamente) → logar com a matrícula nova.
3. Como admin, na aba **Tutores**, **suspender** um tutor → tentar logar como ele (banner vermelho).

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

## 🧩 **Padrões de Projeto Implementados**

O projeto aplica cinco padrões GoF, um por membro do grupo, cada um escolhido pelo problema de domínio que resolve melhor. A tabela abaixo mostra o padrão, quem implementou, em qual funcionalidade e quais arquivos `.java` compõem a implementação.

---

### Iterator — Artur Sales · F-01 e F-02

**Problema:** a fila de atendimento precisa ser percorrida de duas formas distintas — por prioridade clínica (Cor de Risco) e por risco epidemiológico — sem expor a estrutura interna da coleção nem duplicar a lógica de ordenação.

**Solução:** dois iteradores concretos que encapsulam cada estratégia de percurso, permitindo ao serviço iterar sobre a fila de forma uniforme independentemente da ordem escolhida.

| Arquivo | Papel no padrão |
|---|---|
| [FilaPorPrioridadeIterator.java](Projeto/dominio-RecepcaoTriagem/src/main/java/petcollar/dominio/recepcaotriagem/triagem/FilaPorPrioridadeIterator.java) | Iterador concreto — percorre a fila ordenada por Cor de Risco (VERMELHO → AMARELO → VERDE) |
| [FilaEpidemiologicaIterator.java](Projeto/dominio-RecepcaoTriagem/src/main/java/petcollar/dominio/recepcaotriagem/prontuario/FilaEpidemiologicaIterator.java) | Iterador concreto — percorre priorizando pacientes com alerta epidemiológico ativo |

---

### Strategy — Mateus Ribeiro · F-06 e F-10

**Problema:** o cálculo da próxima dose vacinal varia conforme o protocolo clínico (filhote, reforço anual, viagem, personalizado); de forma análoga, a validação de um relatório clínico segue regras diferentes dependendo do tipo de relatório (rotineiro, preventivo, cirúrgico).

**Solução:** uma interface de estratégia para cada contexto e implementações intercambiáveis em tempo de execução, com fábricas que selecionam a estratégia correta.

**F-06 · Ciclo Vacinal**

| Arquivo | Papel no padrão |
|---|---|
| [ICalculoProximaDoseStrategy.java](Projeto/dominio-SaudePreventiva/src/main/java/br/com/cesar/petCollar/dominio/SaudePreventiva/estrategia/ICalculoProximaDoseStrategy.java) | Interface da estratégia |
| [ProtocoloFilhoteStrategy.java](Projeto/dominio-SaudePreventiva/src/main/java/br/com/cesar/petCollar/dominio/SaudePreventiva/estrategia/ProtocoloFilhoteStrategy.java) | Estratégia concreta — protocolo de filhote |
| [ProtocoloReforcoAnualStrategy.java](Projeto/dominio-SaudePreventiva/src/main/java/br/com/cesar/petCollar/dominio/SaudePreventiva/estrategia/ProtocoloReforcoAnualStrategy.java) | Estratégia concreta — reforço anual |
| [ProtocoloViagemStrategy.java](Projeto/dominio-SaudePreventiva/src/main/java/br/com/cesar/petCollar/dominio/SaudePreventiva/estrategia/ProtocoloViagemStrategy.java) | Estratégia concreta — protocolo de viagem |
| [ProtocoloPersonalizadoStrategy.java](Projeto/dominio-SaudePreventiva/src/main/java/br/com/cesar/petCollar/dominio/SaudePreventiva/estrategia/ProtocoloPersonalizadoStrategy.java) | Estratégia concreta — protocolo personalizado |
| [FabricaDeProtocolo.java](Projeto/dominio-SaudePreventiva/src/main/java/br/com/cesar/petCollar/dominio/SaudePreventiva/estrategia/FabricaDeProtocolo.java) | Fábrica que seleciona a estratégia pelo tipo de protocolo |

**F-10 · Relatório Clínico**

| Arquivo | Papel no padrão |
|---|---|
| [IValidadorRelatorioStrategy.java](Projeto/dominio-AtendimentoClinico/src/main/java/petCollar/dominio/AtendimentoClinico/estrategia/IValidadorRelatorioStrategy.java) | Interface da estratégia |
| [ValidadorRelatorioRotineiroStrategy.java](Projeto/dominio-AtendimentoClinico/src/main/java/petCollar/dominio/AtendimentoClinico/estrategia/ValidadorRelatorioRotineiroStrategy.java) | Estratégia concreta — relatório de rotina |
| [ValidadorRelatorioPreventivStrategy.java](Projeto/dominio-AtendimentoClinico/src/main/java/petCollar/dominio/AtendimentoClinico/estrategia/ValidadorRelatorioPreventivStrategy.java) | Estratégia concreta — relatório preventivo |
| [ValidadorRelatorioCirurgicoStrategy.java](Projeto/dominio-AtendimentoClinico/src/main/java/petCollar/dominio/AtendimentoClinico/estrategia/ValidadorRelatorioCirurgicoStrategy.java) | Estratégia concreta — relatório cirúrgico |
| [FabricaDeValidadorRelatorio.java](Projeto/dominio-AtendimentoClinico/src/main/java/petCollar/dominio/AtendimentoClinico/estrategia/FabricaDeValidadorRelatorio.java) | Fábrica que seleciona o validador pelo tipo do relatório |

---

### Decorator — Felipe Marques · F-07, F-11 e F-12

**Problema:** o cálculo do valor de uma cobrança, da NEM (Necessidade Energética de Manutenção) e da dose máxima segura de um medicamento precisam de camadas opcionais e combináveis de modificação (juros, desconto, comorbidade, nível de atividade, tag clínica) sem proliferação de subclasses.

**Solução:** uma interface/componente base e decoradores concretos que se encadeiam em tempo de execução, cada um adicionando ou reduzindo o valor calculado.

**F-07 · Assinatura e Financeiro**

| Arquivo | Papel no padrão |
|---|---|
| [CalculadoraValor.java](Projeto/dominio-AssinaturaFaturamento/src/main/java/br/com/cesar/petCollar/dominio/AssinaturaFaturamento/cobranca/calculo/CalculadoraValor.java) | Interface componente |
| [ValorBase.java](Projeto/dominio-AssinaturaFaturamento/src/main/java/br/com/cesar/petCollar/dominio/AssinaturaFaturamento/cobranca/calculo/ValorBase.java) | Componente concreto — valor original da mensalidade |
| [CalculadoraValorDecorator.java](Projeto/dominio-AssinaturaFaturamento/src/main/java/br/com/cesar/petCollar/dominio/AssinaturaFaturamento/cobranca/calculo/CalculadoraValorDecorator.java) | Decorator abstrato |
| [JurosSimplesDecorator.java](Projeto/dominio-AssinaturaFaturamento/src/main/java/br/com/cesar/petCollar/dominio/AssinaturaFaturamento/cobranca/calculo/JurosSimplesDecorator.java) | Decorator concreto — aplica juros simples de 0,033% ao dia |
| [DescontoIndicacaoDecorator.java](Projeto/dominio-AssinaturaFaturamento/src/main/java/br/com/cesar/petCollar/dominio/AssinaturaFaturamento/cobranca/calculo/DescontoIndicacaoDecorator.java) | Decorator concreto — aplica desconto de 15% por indicação |

**F-11 · Gestão Nutricional (NEM)**

| Arquivo | Papel no padrão |
|---|---|
| [CalculadoraNEM.java](Projeto/dominio-AtendimentoClinico/src/main/java/br/com/cesar/petCollar/dominio/AtendimentoClinico/nutricao/nem/CalculadoraNEM.java) | Interface componente |
| [NEMBase.java](Projeto/dominio-AtendimentoClinico/src/main/java/br/com/cesar/petCollar/dominio/AtendimentoClinico/nutricao/nem/NEMBase.java) | Componente concreto — NEM pelo peso metabólico (P^0,75) |
| [CalculadoraNEMDecorator.java](Projeto/dominio-AtendimentoClinico/src/main/java/br/com/cesar/petCollar/dominio/AtendimentoClinico/nutricao/nem/CalculadoraNEMDecorator.java) | Decorator abstrato |
| [NivelAtividadeDecorator.java](Projeto/dominio-AtendimentoClinico/src/main/java/br/com/cesar/petCollar/dominio/AtendimentoClinico/nutricao/nem/NivelAtividadeDecorator.java) | Decorator concreto — multiplica pelo coeficiente de atividade |
| [ComorbidadeDecorator.java](Projeto/dominio-AtendimentoClinico/src/main/java/br/com/cesar/petCollar/dominio/AtendimentoClinico/nutricao/nem/ComorbidadeDecorator.java) | Decorator concreto — aplica modificador metabólico por comorbidade |

**F-12 · Farmacovigilância**

| Arquivo | Papel no padrão |
|---|---|
| [CalculadoraDoseMaximaSegura.java](Projeto/dominio-Farmacovigilancia/src/main/java/br/com/cesar/petCollar/dominio/Farmacovigilancia/seguranca/CalculadoraDoseMaximaSegura.java) | Interface componente |
| [CalculadoraDoseDecorator.java](Projeto/dominio-Farmacovigilancia/src/main/java/br/com/cesar/petCollar/dominio/Farmacovigilancia/seguranca/CalculadoraDoseDecorator.java) | Decorator abstrato |
| [RedutorPorTagClinicaDecorator.java](Projeto/dominio-Farmacovigilancia/src/main/java/br/com/cesar/petCollar/dominio/Farmacovigilancia/seguranca/RedutorPorTagClinicaDecorator.java) | Decorator concreto — reduz 25% do teto para tags como Insuficiência Renal, Hepática ou Geriátrico |
| [RedutorPorAlergiaDecorator.java](Projeto/dominio-Farmacovigilancia/src/main/java/br/com/cesar/petCollar/dominio/Farmacovigilancia/seguranca/RedutorPorAlergiaDecorator.java) | Decorator concreto — reduz o teto para pacientes com alergias conhecidas |

---

### Template Method — Carlos Eduardo · F-03, F-04 e F-05

**Problema:** a execução de cada etapa do Protocolo de Tutor Inacessível (contato com tutor, acionamento de responsáveis secundários, escalonamento) segue sempre o mesmo esqueleto de passos, mas cada etapa tem destinatários, canais e critérios de conclusão diferentes. De forma análoga, o agendamento de uma consulta (inicial ou retorno) compartilha os mesmos passos fixos — validar prontuário, checar disponibilidade, salvar e notificar — mas cada tipo exige pré-condições e criação de consulta distintas; e o processamento do webhook de confirmação de pagamento de uma indicação possui fluxos automático e manual com passos distintos.

**Solução:** uma classe abstrata define o algoritmo como método `final` e delega os passos variáveis a métodos abstratos implementados pelas subclasses concretas.

**F-03 · Protocolo de Tutor Inacessível**

| Arquivo | Papel no padrão |
|---|---|
| [EtapaProtocoloService.java](Projeto/dominio-ProtocoloInacessibilidade/src/main/java/br/com/cesar/petCollar/dominio/ProtocoloInacessibilidade/etapa/EtapaProtocoloService.java) | Classe abstrata — define o template `executar()` como `final` e declara os métodos abstratos de cada passo |
| [EtapaContatoTutorService.java](Projeto/dominio-ProtocoloInacessibilidade/src/main/java/br/com/cesar/petCollar/dominio/ProtocoloInacessibilidade/etapa/EtapaContatoTutorService.java) | Subclasse concreta — etapa de contato com o tutor principal |
| [EtapaContatoResponsaveisSecundariosService.java](Projeto/dominio-ProtocoloInacessibilidade/src/main/java/br/com/cesar/petCollar/dominio/ProtocoloInacessibilidade/etapa/EtapaContatoResponsaveisSecundariosService.java) | Subclasse concreta — etapa de acionamento dos responsáveis secundários |
| [EtapaEscalonamentoService.java](Projeto/dominio-ProtocoloInacessibilidade/src/main/java/br/com/cesar/petCollar/dominio/ProtocoloInacessibilidade/etapa/EtapaEscalonamentoService.java) | Subclasse concreta — etapa de escalonamento para decisão clínica/administrativa |
| [OrquestradorEtapasProtocolo.java](Projeto/dominio-ProtocoloInacessibilidade/src/main/java/br/com/cesar/petCollar/dominio/ProtocoloInacessibilidade/etapa/OrquestradorEtapasProtocolo.java) | Orquestrador que encadeia as etapas em sequência |

**F-04 · Programa de Indicação**

| Arquivo | Papel no padrão |
|---|---|
| [ProcessamentoWebhookTemplate.java](Projeto/dominio-RelacaoTutor/src/main/java/br/com/cesar/petCollar/dominio/RelacaoTutor/indicacao/ProcessamentoWebhookTemplate.java) | Classe abstrata — define o template de processamento do webhook de confirmação de pagamento |
| [ProcessamentoWebhookAutomatico.java](Projeto/dominio-RelacaoTutor/src/main/java/br/com/cesar/petCollar/dominio/RelacaoTutor/indicacao/ProcessamentoWebhookAutomatico.java) | Subclasse concreta — processamento automático via gateway |
| [ProcessamentoWebhookManual.java](Projeto/dominio-RelacaoTutor/src/main/java/br/com/cesar/petCollar/dominio/RelacaoTutor/indicacao/ProcessamentoWebhookManual.java) | Subclasse concreta — processamento manual (simulação/testes) |

**F-05 · Agendamento de Retorno**

| Arquivo | Papel no padrão |
|---|---|
| [AgendamentoService.java](Projeto/dominio-AgendamentoClinico/src/main/java/br/com/cesar/petCollar/dominio/AgendamentoClinico/agendamento/AgendamentoService.java) | Classe abstrata — define o template `agendar()` como `final`, com os passos fixos (validar prontuário, verificar agenda, checar conflito, salvar, notificar) e três métodos abstratos para os passos variáveis |
| [AgendamentoConsultaInicialService.java](Projeto/dominio-AgendamentoClinico/src/main/java/br/com/cesar/petCollar/dominio/AgendamentoClinico/agendamento/AgendamentoConsultaInicialService.java) | Subclasse concreta — implementa os passos específicos de uma consulta inicial |
| [AgendamentoRetornoService.java](Projeto/dominio-AgendamentoClinico/src/main/java/br/com/cesar/petCollar/dominio/AgendamentoClinico/agendamento/AgendamentoRetornoService.java) | Subclasse concreta — implementa os passos específicos do retorno, validando exames concluídos e vinculando à consulta de origem |

---

### Observer — Bruno Assunção · F-08 e F-09

**Problema:** quando o estado de um benefício do plano é alterado (carência expirada, uso registrado, ticket gerado) ou quando uma badge de gamificação é conquistada, múltiplos componentes precisam ser notificados sem que o agregado publicador conheça seus observadores.

**Solução:** os agregados publicadores mantêm uma lista de observadores registrados via interface, notificando-os automaticamente a cada mudança relevante de estado.

| Arquivo | Papel no padrão |
|---|---|
| [IObservadorDeEventoTutor.java](Projeto/dominio-compartilhado/src/main/java/br/com/cesar/petCollar/dominio/compartilhado/eventos/IObservadorDeEventoTutor.java) | Interface do observador — contrato base compartilhado entre contextos |
| [PublicadorDeEventosDoTutor.java](Projeto/dominio-compartilhado/src/main/java/br/com/cesar/petCollar/dominio/compartilhado/eventos/PublicadorDeEventosDoTutor.java) | Publicador genérico do Shared Kernel |
| [IObservadorDeAlteracaoBeneficio.java](Projeto/dominio-BeneficiosPlano/src/main/java/br/com/cesar/petCollar/dominio/BeneficiosPlano/beneficio/IObservadorDeAlteracaoBeneficio.java) | Interface do observador — eventos de benefício (F-08) |
| [PublicadorDeAlteracoesBeneficio.java](Projeto/dominio-BeneficiosPlano/src/main/java/br/com/cesar/petCollar/dominio/BeneficiosPlano/beneficio/PublicadorDeAlteracoesBeneficio.java) | Sujeito/publicador concreto — gerencia e notifica os observadores de benefício |
| [SincronizacaoBeneficioTutorObservador.java](Projeto/dominio-BeneficiosPlano/src/main/java/br/com/cesar/petCollar/dominio/BeneficiosPlano/beneficio/SincronizacaoBeneficioTutorObservador.java) | Observador concreto — sincroniza o estado do benefício do tutor após cada alteração |
| [ConcessaoBadgeObservador.java](Projeto/dominio-Gamificacao/src/main/java/br/com/cesar/petCollar/dominio/Gamificacao/conquista/ConcessaoBadgeObservador.java) | Observador concreto — reage a eventos de conquista e concede a badge ao tutor (F-09) |
| [IObservadorDeAlteracaoPlano.java](Projeto/dominio-AssinaturaFaturamento/src/main/java/br/com/cesar/petCollar/dominio/AssinaturaFaturamento/plano/IObservadorDeAlteracaoPlano.java) | Interface do observador — eventos de alteração de plano |
| [PublicadorDeAlteracoesPlano.java](Projeto/dominio-AssinaturaFaturamento/src/main/java/br/com/cesar/petCollar/dominio/AssinaturaFaturamento/plano/PublicadorDeAlteracoesPlano.java) | Sujeito/publicador concreto — notifica observadores quando um plano é alterado |
| [NotificacaoAlteracaoPlanoObservador.java](Projeto/dominio-AssinaturaFaturamento/src/main/java/br/com/cesar/petCollar/dominio/AssinaturaFaturamento/plano/NotificacaoAlteracaoPlanoObservador.java) | Observador concreto — envia notificação ao tutor quando seu plano é modificado |

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
