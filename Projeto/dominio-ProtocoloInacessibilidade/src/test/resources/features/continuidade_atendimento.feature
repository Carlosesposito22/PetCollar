# language: pt
Funcionalidade: Continuidade do atendimento clínico (RN 8)
  Como clínica, quero garantir que o protocolo de inacessibilidade rode em paralelo
  ao atendimento, sem nunca interromper ou alterar o estado do atendimento clínico.

  Cenário: O protocolo não altera o estado do atendimento clínico
    Dado uma configuração de protocolo vigente
    E um atendimento clínico em andamento
    Quando o sistema ativa o protocolo e executa 2 tentativas de contato
    Então o atendimento deve permanecer em andamento
