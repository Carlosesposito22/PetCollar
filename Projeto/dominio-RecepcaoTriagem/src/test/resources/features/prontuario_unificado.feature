Feature: Prontuario Unificado do Tutor com Analise Epidemiologica

  Scenario: Busca por CPF encontra tutor cadastrado
    Given existe um tutor cadastrado com CPF "123.456.789-00"
    When o servico buscar o tutor pelo CPF "123.456.789-00"
    Then o resultado deve indicar que o tutor foi encontrado
    And a lista de animais vinculados deve ser retornada

  Scenario: Busca por CPF nao encontra tutor e cadastra sem perder contexto
    Given nao existe tutor cadastrado com CPF "999.888.777-00"
    When o servico cadastrar o tutor com CPF "999.888.777-00" e nome "Carlos Silva"
    Then o tutor deve ser salvo no repositorio
    And o resultado da busca deve ser atualizado com o novo tutor

  Scenario: Animal com historico infectante recente ativa alerta epidemiologico
    Given existe um tutor com CPF "111.222.333-00" com um animal vinculado
    And o animal possui diagnostico infectante nos ultimos 40 dias
    When o servico executar a varredura epidemiologica
    Then o alerta epidemiologico global deve ser verdadeiro

  Scenario: Animal sem historico infectante recente nao ativa alerta
    Given existe um tutor com CPF "444.555.666-00" com um animal vinculado
    And o animal nao possui diagnosticos infectantes recentes
    When o servico executar a varredura epidemiologica
    Then o alerta epidemiologico global deve ser falso

  Scenario: CPF com formato invalido lanca excecao
    When o servico tentar buscar com CPF "123"
    Then deve ser lancada uma excecao de argumento invalido
