# Domínio de Negócio: Ecossistema petCollar

O ecossistema **petCollar** organiza-se em torno da segurança do paciente, da agilidade operacional e da precisão clínica. O fluxo integra desde a recepção inteligente e a medicina preventiva até a inteligência farmacológica avançada, garantindo que a gravidade médica dite o ritmo da operação e que o tutor tenha transparência total sobre a saúde e os custos do animal.

---

## Persona - Recepcionista
**F-01 Prontuário Unificado do Tutor com Análise Epidemiológica:** A jornada do atendimento inicia-se pela identificação inteligente do Tutor por CPF, permitindo sua localização rápida ou cadastro imediato sem interrupção do fluxo operacional. Ao localizar o responsável, o sistema consolida automaticamente todos os animais vinculados e executa uma triagem epidemiológica preditiva, analisando diagnósticos infectocontagiosos registrados nos últimos 40 dias. Caso riscos sanitários sejam identificados, o sistema aplica automaticamente a sinalização de Alerta Epidemiológico, impactando a priorização do atendimento para reduzir o risco de contaminação cruzada na recepção.

**F-02 Triagem Clínica com Classificação Automática de Risco:** Esta funcionalidade converte a avaliação inicial do Paciente em uma classificação objetiva de risco assistencial, partindo de um formulário padronizado de sintomas com pesos clínicos predefinidos. O sistema calcula automaticamente o PesoTotal e determina a Cor de Risco segundo regras determinísticas: Verde (score < 5), Amarelo (5-9) ou Vermelho (score >= 10), definindo a posição na fila de forma automática. Após finalizada, a Triagem torna-se um evento imutável, onde qualquer necessidade de atualização clínica exige a criação de um novo registro para garantir a rastreabilidade. 

**F-03 Protocolo Automatizado de Tutor Inacessível com Escalonamento:** Responsável por garantir a continuidade assistencial quando o responsável legal não pode ser contatado durante o fluxo clínico. O sistema ativa automaticamente um protocolo de contingência operacional que inclui tentativas de contato progressivas em múltiplos canais e a verificação de responsáveis secundários cadastrados. Em casos críticos, o sistema inicia um escalonamento automático para níveis superiores de decisão, consultando diretivas de consentimento pré-assinadas para autorizar condutas assistenciais em cenários de emergência.

---

## Persona - Tutor
**F-04 — Programa de Indicação com Recompensas:**  A expansão estratégica da base de assinantes é coordenada por um motor de crescimento orgânico que transforma o Tutor em um promotor ativo do ecossistema petCollar. O processo inicia-se com a geração de um link pessoal, único e permanente, integrado a uma camada de inteligência de rastreamento baseada na regra de Last Click. A funcionalidade provê um incentivo imediato de 30% de desconto na primeira mensalidade para novos usuários que ingressam via indicação, garantindo uma barreira de entrada reduzida e alta atratividade para o serviço.
A complexidade técnica do processo reside na orquestração síncrona entre o motor financeiro e o sistema de gamificação: no momento exato da confirmação do pagamento do novo assinate, o sistema dispara um gatilho automático que aplica um desconto de 15% na fatura subsequente do indicador e concede a prestigiosa Conquista Lendária, elevando o status do usuário no Programa de Conquistas (F-09). Para assegurar a viabilidade econômica e a integridade dos dados, a funcionalidade incorpora uma robusta camada de Prevenção de Fraude, que realiza validações cruzadas de CPFs e métodos de pagamento no banco relacional, impedindo autoindicações e garantindo que as recompensas sejam persistidas como eventos imutáveis e auditáveis apenas após a efetiva entrada de receita na plataforma.

**F-05 Agendamento de Retorno com Integração de Exames Diagnósticos:** Inicia-se no portal do Tutor, onde o sistema apresenta de forma proativa as consultas com indicação de retorno e exames pendentes. Ao selecionar uma consulta anterior, o sistema recupera a lista de exames solicitados pelo médico veterinário, exigindo que o Tutor confirme a conclusão ou anexe os resultados para liberar o calendário de agendamento. Este fluxo garante que o médico receba o Paciente com todos os subsídios necessários para a conclusão do diagnóstico, eliminando falhas de comunicação. 

**F-06 Monitoramento, Agendamento e Gestão Inteligente do Ciclo Vacinal:** A jornada de medicina preventiva é centralizada em um painel de imunização dinâmico que diferencia visualmente as vacinas como "Concluídas", "Agendadas" e "Atrasadas". Com base no protocolo clínico definido pelo veterinário, o sistema executa um cálculo preditivo das próximas doses, considerando intervalos biológicos necessários entre cada aplicação. A funcionalidade atua como um sentinela, disparando alertas automáticos de proximidade e recalculando previsões em caso de atrasos para garantir a saúde coletiva dos animais. 

**F-07 Assinatura de Plano e Gestão Financeira do Tutor:** O acesso à plataforma é condicionado à contratação de um plano de assinatura, cuja conta transiciona para Ativa apenas após a confirmação do pagamento inicial. A Área Financeira centraliza cobranças mensais onde juros simples de 0,033% ao dia são recalculados dinamicamente a cada acesso em casos de atraso. O sistema monitora o Status da Conta, assumindo estado Inadimplente para débitos recentes ou Suspensa ao atingir três mensalidades consecutivas não pagas, o que bloqueia o login até a regularização integral. 


**F-08 Gerenciamento de Carência de Benefícios do Plano:** A funcionalidade expõe um painel que centraliza todos os serviços incluídos no plano e o estado atual de cada benefício (Em Carência, Disponível ou Esgotado). O motor de cálculo processa continuamente os períodos de carência em dias corridos a partir da ativação da conta e reinicia automaticamente os limites de uso em cada novo ciclo. Ao utilizar um benefício, o sistema gera um Ticket de Benefício digital com código GUID único e validade de 48 horas, garantindo a validação de uso autônoma e transparente. 

**F-09 Programa de Conquistas e Gamificação do Assinante:** Transforma a jornada do assinante em uma experiência progressiva baseada em eventos reais de uso da plataforma, organizando badges em categorias como Fidelidade, Saúde do Pet e Engajamento. O motor de concessão opera inteiramente orientado a eventos, disparando verificação de elegibilidade e persistindo conquistas com timestamp imutável. O Tutor visualiza seu progresso através de barras de progresso em tempo real para critérios quantificáveis, recebendo notificações visuais com intensidade proporcional à raridade da badge conquistada. 

---

## Persona - Médico
**F-10 Emissão de Relatório Clínico Evolutivo e Sumário de Saúde:**  Após a consulta, o médico consolida os achados em um relatório estruturado que compila automaticamente dados vitais (peso, temperatura, frequência cardíaca) para gerar gráficos de evolução clínica imediata. O sistema exige o preenchimento de um "Resumo para o Tutor" em linguagem acessível e permite a anexação de mídias para ilustrar o diagnóstico. Uma vez finalizado e assinado digitalmente, o relatório torna-se somente leitura e é publicado instantaneamente no portal do Tutor para consulta histórica e auditável. 

**F-11 Gestão Nutricional Avançada e Prescrição de NEM (Necessidade Energética):** Atua como suporte à decisão clínica automatizando o cálculo da NEM via fórmulas metabólicas (P^{0,75}), integrando parâmetros fisiológicos e modificadores de comorbidades (como Diabetes ou Nefropatias). O sistema cruza os dados com a densidade calórica de rações comerciais e monitora a evolução ponderal, sugerindo ajustes percentuais caso as metas de peso não sejam atingidas. Gera automaticamente um cronograma de transição alimentar de 7 dias, garantindo clareza na execução para o responsável. 

**F-12 Central de Farmacovigilância Inteligente e Gestão Terapêutica Integrada:** Este motor de inteligência farmacológica executa a Blindagem de Dosagem baseada no peso e limites máximos por kg, bloqueando combinações de fármacos incompatíveis via matriz de compatibilidade. O sistema aplica ajustes de segurança por contexto, como redutores de 25% no teto de dosagem para pacientes geriátricos ou renais. Ao finalizar, projeta automaticamente a Data de Fim do Tratamento baseada no medicamento de ciclo mais longo e gera um escalonamento horário inteligente para aumentar a adesão terapêutica. 



## Glossário da Linguagem Onipresente

| Termo do Negócio | Significado / Contexto dentro do Sistema |
|---|---|
| **Tutor** | Dono ou responsável legal pelo animal. É a entidade que chega à recepção, tem seus dados buscados para iniciar o fluxo de atendimento e possui todos os pacientes vinculados ao seu perfil. |
| **Paciente** | O animal de estimação que recebe o atendimento. É a entidade central do sistema, em torno da qual a triagem, a fila, o consultório e a prescrição são organizados. |
| **Tag de Perfil** | Etiqueta de classificação visual do paciente, gerada com base em sua raça e faixa etária. É o identificador que comunica à equipe os cuidados específicos que aquele perfil exige antes mesmo do atendimento começar. |
| **Alerta Epidemiológico** | Tag dinâmica de segurança aplicada automaticamente ao paciente que possui histórico de doença contagiosa nos últimos 40 dias. É a regra de negócio que sobrepõe a Classificação de Risco padrão, forçando a ida imediata do paciente para o topo da fila a fim de garantir o isolamento rápido e a biossegurança da clínica. |
| **Alerta Comportamental** | Sinalização associada ao perfil do paciente que comunica à equipe clínica características de manejo observadas na chegada, como agressividade ou estresse. |
| **Triagem** | Avaliação clínica inicial do paciente que, a partir de um formulário de sintomas com pesos, define a gravidade do caso e determina a prioridade de atendimento. |
| **Peso do Sintoma** | Valor numérico atribuído a cada sintoma do formulário de triagem. É o critério quantitativo que transforma uma avaliação clínica subjetiva em uma classificação objetiva de risco. |
| **PesoTotal** | Escore resultante da soma dos pesos dos sintomas marcados na triagem. É o valor que o sistema compara com os limiares configurados para determinar a Cor de Risco do paciente. |
| **Classificação de Risco** | Status de prioridade do atendimento gerado na triagem — Vermelho (Emergência), Amarelo (Urgência) ou Verde (Eletivo). É a regra que governa a ordem de chamada e os prazos de espera de todo o fluxo. |
| **Cor de Risco** | Representação canônica da Classificação de Risco compartilhada entre todos os contextos do sistema. É o dado que conecta a triagem, a fila e o consultório em torno da mesma informação de gravidade. |
| **Fila de Espera Dinâmica** | Lista de pacientes aguardando atendimento, cuja ordem é determinada pela gravidade clínica e não pela chegada. É o mecanismo central que garante que casos críticos nunca esperem atrás de casos simples. |
| **Posição na Fila** | Número ordinal que indica onde o paciente está na Fila de Espera Dinâmica. É a variável base para o cálculo da Previsão de Espera comunicada ao tutor. |
| **Previsão de Espera** | Estimativa de tempo de espera comunicada ao tutor, calculada com base na posição do paciente e no tempo médio de atendimento de sua Cor de Risco. |
| **Painel Gerencial** | Interface de controle da sala de espera que centraliza a visão de todos os pacientes aguardando, com suas prioridades e status, para que a equipe gerencie o fluxo em tempo real. |
| **SLA (Service Level Agreement)** | Prazo máximo de segurança definido por Cor de Risco dentro do qual o paciente deve ser chamado. É o contrato interno que o sistema monitora para garantir que nenhum caso crítico seja negligenciado. |
| **Alerta Laranja** | Sinal de atenção emitido quando o paciente atingiu 80% do seu prazo de SLA. É o aviso preventivo que permite à equipe agir antes que o prazo expire. |
| **Alerta Vermelho / Topo da Fila** | Estado crítico atingido ao expirar 100% do prazo de SLA. É a condição que eleva automaticamente o paciente ao topo de sua categoria, tornando-o o próximo a ser chamado. |
| **Protocolo de Inacessibilidade** | Fluxo de contingência acionado quando o tutor não responde, envolvendo tentativas de contato progressivas e acionamento de responsáveis secundários. |
| **Responsável Secundário** | Contato alternativo previamente cadastrado pelo tutor para tomada de decisão clínica em caso de falha no contato principal. |
| **Chamada** | Ação que formaliza a transição do paciente da sala de espera para o consultório, alterando seu status e registrando o início do atendimento no sistema. |
| **Vínculo de Consultório** | Associação entre o prontuário do paciente, o médico responsável e o consultório onde o atendimento ocorre. É o registro que garante a rastreabilidade clínica de cada atendimento. |
| **Carteira Digital de Vacinação** | Interface de monitoramento onde o tutor visualiza o histórico de imunização e projeções de doses futuras baseadas em intervalos biológicos. |
| **Status da Conta** | Estado financeiro do tutor que dita o acesso à plataforma, podendo ser: Pendente, Ativa, Inadimplente ou Suspensa. |
| **Juros Simples** | Taxa de 0,033% ao dia aplicada automaticamente sobre mensalidades não pagas após o vencimento. |
| **Ticket de Benefício** | Cartão digital gerado pelo tutor para utilizar serviços do plano, contendo um código GUID único e validade de 48 horas. |
| **Badge (Conquista)** | Medalha de reconhecimento gamificado (Fidelidade, Saúde ou Engajamento) concedida automaticamente por eventos reais na plataforma. |
| **Relatório Clínico Evolutivo** | Documento médico estruturado que compila dados vitais e gera gráficos de evolução comparativa em relação aos últimos atendimentos. |
| **Necessidade Energética de Manutenção (NEM)** | Fórmula padrão de prescrição dietética que calcula a quantidade diária de alimento em gramas com base no peso ideal e no nível de atividade do paciente. |
| **Nível de Atividade** | Parâmetro fisiológico do paciente que determina o fator multiplicador aplicado no cálculo NEM. Representa o perfil de gasto energético do animal em 5 categorias: Sedentário, Pouco Ativo, Moderadamente Ativo, Muito Ativo e Atleta. |
| **Cronograma de Transição Alimentar** | Guia de 7 dias que escalona a proporção entre a dieta antiga e a nova para garantir a adaptação fisiológica do paciente. |
| **Central de Farmacovigilância** | Motor de inteligência que valida dosagens, identifica interações medicamentosas graves e ajusta limites por contexto clínico. |
| **Blindagem de Dosagem** | Barreira sistêmica que bloqueia a prescrição caso a dose ultrapasse a Dose Máxima de Segurança por quilograma. |
| **Dose Máxima de Segurança** | Limite máximo de um medicamento por quilograma do animal que o sistema usa como barreira de proteção contra superdosagem na prescrição. |
| **Travamento de Prescrição** | Impedimento automático que o sistema aplica ao veterinário quando a dose prescrita ultrapassa a Dose Máxima de Segurança. É a camada que torna a prescrição segura por padrão. |
| **Data de Fim do Tratamento** | Data que representa o encerramento do ciclo terapêutico do paciente, calculada com base no medicamento de uso mais longo da receita e registrada automaticamente no prontuário. |
