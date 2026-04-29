Feature: Emissao de Relatorio Clinico Evolutivo e Sumario de Saude

  Scenario: Gerar relatorio com sinais vitais do atendimento
    Given existe um atendimento em curso para o paciente
    And os sinais vitais foram aferidos com peso 5.2 kg e temperatura 38.5 graus
    When o servico consolidar os sinais vitais do atendimento
    Then os sinais vitais devem ser registrados no relatorio
    And o repositorio deve ter salvo o relatorio

  Scenario: Evolucao comparativa com atendimento anterior
    Given existe um relatorio com sinais vitais registrados de peso 5.2 kg
    And existe um historico de atendimento anterior com peso 4.8 kg
    When o servico gerar a evolucao comparativa
    Then a variacao de peso deve ser 0.4 kg
    And o resumo textual deve conter informacao de ganho de peso

  Scenario: Primeiro atendimento do paciente gera resumo especial
    Given existe um relatorio sem historico anterior com peso 3.5 kg
    When o servico gerar a evolucao comparativa sem historico
    Then o resumo textual deve indicar primeiro atendimento registrado

  Scenario: Assinar relatorio o torna imutavel
    Given existe um relatorio nao assinado com diagnostico e orientacoes preenchidos
    When o servico assinar digitalmente o relatorio
    Then o relatorio deve ter a flag imutavel verdadeira
    And o campo assinadoEm deve ser preenchido
    And o repositorio deve ter salvo o relatorio

  Scenario: Modificar relatorio assinado lanca excecao
    Given existe um relatorio ja assinado digitalmente
    When o servico tentar modificar o diagnostico do relatorio
    Then deve ser lancada uma excecao de estado invalido

  Scenario: Assinar relatorio sem diagnostico tecnico lanca excecao
    Given existe um relatorio nao assinado sem diagnostico tecnico
    When o servico tentar assinar o relatorio sem diagnostico
    Then deve ser lancada uma excecao de estado invalido

  Scenario: Adicionar multiplos anexos ao relatorio
    Given existe um relatorio nao assinado com diagnostico e orientacoes preenchidos
    When o servico adicionar um anexo do tipo "FOTO_LESAO" com nome "lesao_pata.jpg"
    And o servico adicionar um anexo do tipo "LAUDO_PDF" com nome "laudo_hemograma.pdf"
    Then o relatorio deve conter 2 anexos
