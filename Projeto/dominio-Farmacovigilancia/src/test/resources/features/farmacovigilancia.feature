# language: pt
Feature: Central de Farmacovigilancia Inteligente

  Scenario: Dose dentro do limite recebe status DENTRO_DO_LIMITE
    Given existe um medicamento com dose maxima de 5.0 mg por kg
    And o paciente pesa 10.0 kg com dose prescrita de 30.0 mg
    When o servico validar a dosagem
    Then o status da dosagem deve ser "DENTRO_DO_LIMITE"

  Scenario: Dose acima do limite bloqueia o item
    Given existe um medicamento com dose maxima de 5.0 mg por kg
    And o paciente pesa 10.0 kg com dose prescrita de 60.0 mg
    When o servico validar a dosagem
    Then o status da dosagem deve ser "TRAVADO_POR_DOSE"

  Scenario: Dose proxima do limite 80 porcento gera alerta
    Given existe um medicamento com dose maxima de 5.0 mg por kg
    And o paciente pesa 10.0 kg com dose prescrita de 42.0 mg
    When o servico validar a dosagem
    Then o status da dosagem deve ser "PROXIMO_DO_LIMITE"

  Scenario: Interacao bloqueante entre dois medicamentos trava a prescricao
    Given existe uma prescricao com dois medicamentos com interacao critica bloqueante
    When o servico verificar as interacoes medicamentosas
    Then a prescricao deve ter status "TRAVADA_AGUARDANDO_REVISAO"
    And o motivo deve indicar interacao bloqueante

  Scenario: Tag INSUFICIENCIA_RENAL reduz dose maxima em 50 porcento
    Given existe um paciente com tag "INSUFICIENCIA_RENAL"
    And um medicamento com dose maxima de 10.0 mg por kg
    When o servico calcular o fator de reducao contextual
    Then a dose maxima efetiva deve ser 5.0 mg por kg

  Scenario: Data de fim do tratamento e o item de maior duracao
    Given existe uma prescricao com item A de duracao 7 dias e item B de duracao 14 dias
    And ambos os itens foram iniciados hoje
    When o servico calcular a data de fim do tratamento
    Then a data de fim deve ser daqui a 14 dias

  Scenario: Emitir prescricao sem itens travados muda status para EMITIDA
    Given existe uma prescricao com todos os itens com status "DENTRO_DO_LIMITE"
    When o servico emitir a prescricao
    Then o status da prescricao deve ser "EMITIDA"

  Scenario: Tentar emitir prescricao com item travado lanca excecao
    Given existe uma prescricao com um item com status "TRAVADO_POR_DOSE"
    When o servico tentar emitir a prescricao
    Then deve ser lancada uma excecao de estado invalido
