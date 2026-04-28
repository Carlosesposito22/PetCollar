### **Domínio de Negócio da Inteligência Clínica e Gestão Veterinária**

O ecossistema da clínica veterinária organiza-se em torno da segurança do paciente, da agilidade no atendimento e da precisão clínica, garantindo que a gravidade médica dite o ritmo de toda a operação, desde a recepção até a alta.

**1\. Busca Inteligente com Varredura Epidemiológica** 

A jornada do paciente inicia-se pela localização ágil do tutor na base de dados através do CPF. Ao identificar o tutor e consolidar os pacientes vinculados, o sistema executa uma varredura automática no histórico clínico de cada paciente. A inteligência cruza a data atual com diagnósticos anteriores para verificar se o animal apresentou alguma doença infectocontagiosa nos últimos 40 dias. Caso o critério seja atendido, o sistema aplica imediatamente a tag de "Alerta Epidemiológico" no perfil do paciente durante a busca. Essa classificação interfere de forma ativa no fluxo operacional: ao dar entrada, a tag força o sistema a ignorar a ordem cronológica e o nível de prioridade padrão, alocando o paciente automaticamente no topo da fila de espera para atendimento imediato, minimizando o tempo de exposição e o risco de contaminação cruzada na recepção. 

**2\. Cadastro de Paciente com Tagueamento Automático**  
O registro do perfil base do animal gera inteligência imediata para o atendimento. Ao salvar dados como data de nascimento e raça, o sistema calcula a idade em tempo real e aplica automaticamente tags visuais fixas, como "Braquiocefálico" ou "Geriátrico", que acompanharão o paciente em todas as etapas para alertar sobre cuidados específicos da espécie ou idade.

**3\. Registro de Queixa e Observações Comportamentais**  
Antes da triagem clínica, o atendente registra o motivo principal da consulta e seleciona o tipo de atendimento. Neste momento, é fundamental a sinalização de características comportamentais (ex: animal agressivo ou estressado), o que gera um alerta visual imediato para que a equipe clínica se prepare para o manejo seguro do animal.

**4\. Triagem e Classificação de Risco**  
A avaliação clínica inicial é padronizada por um formulário de sintomas onde cada item possui um peso predefinido. A soma desses pesos resulta em uma classificação automática por cores de prioridade (ex: Emergência Vermelha ou Urgência Amarela), removendo a subjetividade do primeiro contato e garantindo que casos graves sejam detectados precocemente.

**5\. Previsão de Espera Dinâmica**  
Com base na classificação de risco, o sistema comunica ao tutor uma estimativa de tempo de espera. Esse cálculo é dinâmico, multiplicando a posição do paciente na fila pelo tempo médio de consulta definido para sua cor de risco, sendo atualizado automaticamente a cada chamada para manter a transparência e reduzir a ansiedade na recepção.

**6\. Painel Gerencial com Atualização Ativa de SLA** 

O painel atua como o centro de monitoramento ativo do fluxo clínico. Ao ser acionado pela equipe, o sistema engatilha uma rotina de processamento que recalcula e persiste o tempo estimado atualizado de todos os pacientes na fila. Simultaneamente, a plataforma verifica o status de SLA de cada atendimento, aplicando automaticamente alertas de segurança (laranja ou vermelho) sempre que os limites de tempo configurados são atingidos ou excedidos. Apenas após essa validação e persistência sistêmica, a interface retorna os dados consolidados, entregando à recepção um panorama gerencial completo.

**7\. Fila de Espera Ordenada por Risco**  
A organização dos atendimentos rompe com a ordem cronológica tradicional. O sistema prioriza os pacientes estritamente pela gravidade da classificação de risco (Vermelho \> Amarelo \> Verde). O tempo de chegada à clínica é utilizado apenas como critério de desempate entre pacientes que possuam a mesma cor de prioridade.

**8\. Gestão de Alerta de Tempo de Espera Excedido (SLA)**  
Para garantir que nenhum paciente aguarde além do limite de segurança, o sistema monitora os prazos de cada cor de risco (ex: 45min para Amarelo). Ao atingir 80% do limite, o card do paciente é destacado; se o tempo expirar (100%), o sistema emite um alerta visual crítico e move o paciente automaticamente para o topo de sua categoria para chamada imediata.

**9\. Gestão de Chamada e Vínculo de Consultório**  
A transição para a área clínica é automatizada: ao acionar a chamada, o status do paciente muda de "Aguardando" para "Em atendimento". Simultaneamente, o sistema vincula o prontuário digital ao médico veterinário e ao consultório específico, garantindo o rastreio de onde e por quem o animal está sendo assistido.

**10\. Plano Nutricional por Fórmula Padrão**  
Durante a consulta, o suporte dietético é calculado com base em parâmetros fisiológicos. O veterinário insere o peso ideal e o nível de atividade do animal, e o sistema aplica fórmulas matemáticas de Necessidade Energética de Manutenção (NEM) para prescrever a quantidade exata de ração diária em gramas, garantindo a precisão nutricional do tratamento.

**11\. Calculadora de Dosagem Segura**  
Como camada crítica de segurança, o sistema atua como um limitador para a prescrição medicamentosa. Ao cruzar o peso do animal com os limites máximos de segurança de uma base pré-cadastrada, a plataforma bloqueia qualquer tentativa de prescrever dosagens que ultrapassem o limite de segurança por kg, emitindo um alerta impeditivo ao veterinário.

**12\. Geração de Receita e Prazo de Tratamento**  
O encerramento do atendimento consolida todas as prescrições em um documento final. O sistema analisa individualmente a duração de cada medicamento prescrito, identifica qual deles possui o ciclo de uso mais longo e utiliza esse prazo para calcular e preencher automaticamente a "Data de Fim do Tratamento" no prontuário do paciente.

### **Glossário da Linguagem Onipresente**

| Termo do Negócio | Significado / Contexto dentro do Sistema |
| :---- | :---- |
| **Tutor** | Dono ou responsável legal pelo animal. É a entidade que chega à recepção, tem seus dados buscados para iniciar o fluxo de atendimento e possui todos os pacientes vinculados ao seu perfil.  |
| **Paciente** | O animal de estimação que recebe o atendimento. É a entidade central do sistema, em torno da qual a triagem, a fila, o consultório e a prescrição são organizados.  |
| **Tag de Perfil**  | Etiqueta de classificação visual do paciente, gerada com base em sua raça e faixa etária. É o identificador que comunica à equipe os cuidados específicos que aquele perfil exige antes mesmo do atendimento começar.  |
| **Alerta Epidemiológico**  | Tag dinâmica de segurança aplicada automaticamente ao paciente que possui histórico de doença contagiosa nos últimos 40 dias. É a regra de negócio que sobrepõe a Classificação de Risco padrão, forçando a ida imediata do paciente para o topo da fila a fim de garantir o isolamento rápido e a biossegurança da clínica.  |
| **Alerta Comportamental**  | Sinalização associada ao perfil do paciente que comunica à equipe clínica características de manejo observadas na chegada, como agressividade ou estresse.  |
| **Triagem** | Avaliação clínica inicial do paciente que, a partir de um formulário de sintomas com pesos, define a gravidade do caso e determina a prioridade de atendimento.  |
| **Peso do Sintoma**  | Valor numérico atribuído a cada sintoma do formulário de triagem. É o critério quantitativo que transforma uma avaliação clínica subjetiva em uma classificação objetiva de risco.  |
| **PesoTotal**  | Escore resultante da soma dos pesos dos sintomas marcados na triagem. É o valor que o sistema compara com os limiares configurados para determinar a Cor de Risco do paciente.  |
| **Classificação de Risco** | Status de prioridade do atendimento gerado na triagem — Vermelho (Emergência), Amarelo (Urgência) ou Verde (Eletivo). É a regra que governa a ordem de chamada e os prazos de espera de todo o fluxo.  |
| **Cor de Risco**  | Representação canônica da Classificação de Risco compartilhada entre todos os contextos do sistema. É o dado que conecta a triagem, a fila e o consultório em torno da mesma informação de gravidade.    |
| **Fila de Espera Dinâmica**  | Lista de pacientes aguardando atendimento, cuja ordem é determinada pela gravidade clínica e não pela chegada. É o mecanismo central que garante que casos críticos nunca esperem atrás de casos simples.  |
| **Posição na Fila**  | Número ordinal que indica onde o paciente está na Fila de Espera Dinâmica. É a variável base para o cálculo da Previsão de Espera comunicada ao tutor.  |
| **Previsão de Espera**  | Estimativa de tempo de espera comunicada ao tutor, calculada com base na posição do paciente e no tempo médio de atendimento de sua Cor de Risco.    |
| **Painel Gerencial**  | Interface de controle da sala de espera que centraliza a visão de todos os pacientes aguardando, com suas prioridades e status, para que a equipe gerencie o fluxo em tempo real.  |
| **SLA (Service Level Agreement)**  | Prazo máximo de segurança definido por Cor de Risco dentro do qual o paciente deve ser chamado. É o contrato interno que o sistema monitora para garantir que nenhum caso crítico seja negligenciado.  |
| **Alerta Laranja**  | Sinal de atenção emitido quando o paciente atingiu 80% do seu prazo de SLA. É o aviso preventivo que permite à equipe agir antes que o prazo expire.  |
| **Alerta Vermelho / Topo da Fila**  | Estado crítico atingido ao expirar 100% do prazo de SLA. É a condição que eleva automaticamente o paciente ao topo de sua categoria, tornando-o o próximo a ser chamado.  |
| **Chamada**  | Ação que formaliza a transição do paciente da sala de espera para o consultório, alterando seu status e registrando o início do atendimento no sistema.  |
| **Vínculo de Consultório**  | Associação entre o prontuário do paciente, o médico responsável e o consultório onde o atendimento ocorre. É o registro que garante a rastreabilidade clínica de cada atendimento.  |
| **Necessidade Energética de Manutenção (NEM)** | Fórmula padrão de prescrição dietética que calcula a quantidade diária de alimento em gramas com base no peso ideal e no nível de atividade do paciente.  |
| **Nível de Atividade**  | Parâmetro fisiológico do paciente que determina o fator multiplicador aplicado no cálculo NEM. Representa o perfil de gasto energético do animal em 5 categorias: Sedentário, Pouco Ativo, Moderadamente Ativo, Muito Ativo e Atleta.  |
| **Dose Máxima de Segurança**  | Limite máximo de um medicamento por quilograma do animal que o sistema usa como barreira de proteção contra superdosagem na prescrição.    |
| **Travamento de Prescrição**  | Impedimento automático que o sistema aplica ao veterinário quando a dose prescrita ultrapassa a Dose Máxima de Segurança. É a camada que torna a prescrição segura por padrão.  |
| **Data de Fim do Tratamento**  | Data que representa o encerramento do ciclo terapêutico do paciente, calculada com base no medicamento de uso mais longo da receita e registrada automaticamente no prontuário.  |

### 