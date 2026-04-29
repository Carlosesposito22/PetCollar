Feature: Gestao Nutricional Avancada e Prescricao de NEM

  Scenario: Calcular NEM para paciente moderadamente ativo com peso ideal 10 kg
    Given existe um plano nutricional com peso ideal 10.0 kg
    And o nivel de atividade e "MODERADAMENTE_ATIVO"
    And nao ha comorbidades
    When o servico calcular o NEM
    Then o resultado deve ser aproximadamente 630 kcal por dia

  Scenario: Comorbidade OBESIDADE reduz o NEM calculado
    Given existe um plano com peso ideal 10.0 kg e nivel "MODERADAMENTE_ATIVO"
    And o paciente tem a comorbidade "OBESIDADE" com modificador 0.8
    When o servico calcular o NEM
    Then o resultado deve ser aproximadamente 504 kcal por dia

  Scenario: Divergencia de peso acima de 20 porcento gera alerta de obesidade
    Given existe um plano com peso atual 15.0 kg e peso ideal 12.0 kg
    When o servico avaliar o alerta de manejo critico
    Then o tipo de alerta deve ser "OBESIDADE"

  Scenario: Gerar cronograma de transicao de 7 dias
    When o servico gerar o cronograma de transicao de 7 dias
    Then o dia 1 deve ter 25 porcento da dieta nova
    And o dia 7 deve ter 100 porcento da dieta nova

  Scenario: Calcular gramas diarias com base nas kcal e densidade energetica
    Given o NEM calculado e de 630 kcal
    And a racao tem 3.5 kcal por grama
    When o servico calcular as gramas diarias
    Then o resultado deve ser aproximadamente 180 gramas por dia
