# language: pt
Feature: Programa de Conquistas e Gamificacao

  Scenario: Evento unico concede badge imediatamente
    Given existe uma badge de evento unico com chave "primeira_consulta"
    And o tutor ainda nao conquistou essa badge
    When o servico avaliar as badges para o evento "primeira_consulta"
    Then a badge deve ser concedida ao tutor
    And a conquista deve ser registrada

  Scenario: Badge quantitativa exige atingir a meta
    Given existe uma badge quantitativa com meta de 5 e chave "consulta_realizada"
    And existe um progresso do tutor com valor atual 4 para essa badge
    When o servico atualizar o progresso em 1 para essa badge
    Then o percentual de conclusao deve ser 100
    And a badge deve ter meta atingida

  Scenario: Badge ja conquistada nao e concedida novamente
    Given existe uma badge de evento unico com chave "vacina_aplicada"
    And o tutor ja conquistou essa badge anteriormente
    When o servico avaliar as badges para o evento "vacina_aplicada"
    Then a badge nao deve ser concedida novamente

  Scenario: Listar proximas conquistas por percentual
    Given o tutor possui 3 progressos com percentuais 80 60 e 40
    When o servico listar as 2 proximas conquistas do tutor
    Then as 2 badges retornadas devem estar em ordem decrescente de percentual

  Scenario: Badge quantitativa nao e concedida antes de atingir a meta
    Given existe uma badge quantitativa com meta de 10 e chave "check_up_realizado"
    And o tutor ainda nao tem progresso para essa badge
    When o servico avaliar as badges para o evento "check_up_realizado"
    Then a badge nao deve ser concedida ainda
    And o progresso do tutor deve ser 1

  Scenario: Incremento invalido lanca excecao
    Given existe um progresso do tutor com valor atual 2 e meta 5
    When o servico tentar atualizar o progresso com incremento 0
    Then deve ser lancada uma excecao de argumento invalido
