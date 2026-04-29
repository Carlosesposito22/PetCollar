# language: pt
Feature: Triagem Clinica com Classificacao Automatica de Risco

  Scenario: Sintomas graves resultam em cor Vermelho
    Given existe uma triagem em elaboracao para o paciente
    And o sintoma "DISPNEIA" com peso 10 esta presente
    And o sintoma "COLAPSO" com peso 8 esta presente
    And a configuracao define limiteVermelho 15 e limiteAmarelo 8
    When o servico calcular a cor de risco
    Then a cor de risco resultante deve ser "VERMELHO"

  Scenario: Sintomas moderados resultam em cor Amarelo
    Given existe uma triagem em elaboracao para o paciente
    And o sintoma "VOMITO" com peso 5 esta presente
    And o sintoma "APATIA" com peso 4 esta presente
    And a configuracao define limiteVermelho 15 e limiteAmarelo 8
    When o servico calcular a cor de risco
    Then a cor de risco resultante deve ser "AMARELO"

  Scenario: Sintomas leves resultam em cor Verde
    Given existe uma triagem em elaboracao para o paciente
    And o sintoma "COCEIRA" com peso 2 esta presente
    And a configuracao define limiteVermelho 15 e limiteAmarelo 8
    When o servico calcular a cor de risco
    Then a cor de risco resultante deve ser "VERDE"

  Scenario: Finalizar triagem a bloqueia para alteracao
    Given existe uma triagem em elaboracao com cor de risco calculada
    When o servico finalizar a triagem
    Then o status da triagem deve ser "FINALIZADA"
    And a triagem deve estar bloqueada para alteracao

  Scenario: Paciente cao com 8 anos recebe tag Geriatrico
    Given existe um paciente da especie "CANINO" com data de nascimento ha 8 anos
    When o servico aplicar as tags automaticas
    Then o paciente deve ter a tag "GERIATRICO"
