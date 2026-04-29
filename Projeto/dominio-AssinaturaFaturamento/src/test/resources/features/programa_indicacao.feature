# language: pt
Feature: Programa de Indicacao com Recompensas

  Scenario: Tutor ativo gera link de indicacao unico
    Given o Tutor com status de conta "ATIVA" esta autenticado
    When o servico GeracaoLinkIndicacaoService for chamado para o Tutor com status "ATIVA"
    Then um CodigoIndicacao alfanumerico unico deve ser gerado
    And a UrlCompartilhamento permanente para WhatsApp deve ser criada
    And o StatusLink deve ser "ATIVO"

  Scenario: Idempotencia na geracao de link
    Given o Tutor ja possui um LinkIndicacao com status "ATIVO"
    When o servico GeracaoLinkIndicacaoService for chamado novamente
    Then o mesmo LinkIndicacao existente deve ser retornado sem criar um novo

  Scenario: Tutor inativo nao pode gerar link
    Given o Tutor com status de conta "INATIVA" esta autenticado
    When o servico GeracaoLinkIndicacaoService for chamado para o Tutor com status "INATIVA"
    Then o sistema deve rejeitar a operacao
    And nenhum link deve ser gerado

  Scenario: Prevencao de auto-indicacao por CPF
    Given existe um LinkIndicacao cujo proprietario tem CPF "111.111.111-11"
    And existe uma ConversaoIndicacao com CPF indicado "111.111.111-11"
    When o ValidacaoFraudeService validar auto-indicacao para o CPF do proprietario "111.111.111-11"
    Then o StatusConversao deve ser "INVALIDA_FRAUDE"
    And um EventoRastreio "FRAUDE_DETECTADA_AUTO_INDICACAO" deve constar na trilha de auditoria

  Scenario: CPF ja convertido anteriormente e rejeitado
    Given o CPF "222.222.222-22" ja foi contabilizado como indicacao confirmada
    And existe uma ConversaoIndicacao com CPF indicado "222.222.222-22"
    When o ValidacaoFraudeService validar CPF ja convertido
    Then o StatusConversao deve ser "INVALIDA_CPF_DUPLICADO"

  Scenario: Metodo de pagamento duplicado invalida recompensa
    Given existe uma ConversaoIndicacao em status "AGUARDANDO_PAGAMENTO" com hashMetodo igual ao do indicador
    When o ConfirmacaoConversaoService processar o pagamento
    Then o sistema deve rejeitar a operacao
    And um EventoRastreio "FRAUDE_DETECTADA_METODO_DUPLICADO" deve constar na trilha de auditoria
    And o StatusConversao deve ser "INVALIDA_FRAUDE"

  Scenario: Conversao valida dispara recompensa de duas vias
    Given existe uma ConversaoIndicacao com status "AGUARDANDO_PAGAMENTO" sem fraude detectada
    When o webhook do gateway confirmar o pagamento da primeira mensalidade do indicado
    Then o desconto de 30 por cento deve estar aplicado na Cobranca do indicado
    And o desconto de 15 por cento deve estar aplicado na fatura do indicador
    And o StatusConversao deve ser "CONFIRMADA"
    And um IndicacaoConfirmadaEvent deve ser emitido

  Scenario: Conquista Lendaria concedida ao indicador
    Given um IndicacaoConfirmadaEvent foi emitido para o TutorIndicador
    When o DisparoGamificacaoService processar o evento
    Then um IndicacaoValidadaParaGamificacaoEvent deve ser publicado com chaveEvento "INDICACAO_CONFIRMADA"
    And o campo gamificacaoDisparada da RecompensaIndicacao deve ser verdadeiro

  Scenario: Last Click atribui recompensa ao ultimo link clicado
    Given existe uma ConversaoIndicacao associada ao link do Tutor "tutor-a"
    When o AtribuicaoLastClickService atribuir o ultimo clique ao link do Tutor "tutor-b"
    Then o TutorIndicador atribuido deve ser "tutor-b"
    And um EventoRastreio "LAST_CLICK_ATRIBUIDO" deve constar na trilha de auditoria

  Scenario: Desconto de indicacao nao e cumulativo com cupom
    Given existe uma RecompensaIndicacao com desconto de indicacao de 30 por cento
    And o indicado possui um cupom promocional de 20 por cento
    When o AplicacaoDescontoIndicadoService resolver o desconto mais vantajoso
    Then o percentual de desconto aplicado deve ser 0.30
    And o desconto de indicacao deve estar marcado como aplicado
