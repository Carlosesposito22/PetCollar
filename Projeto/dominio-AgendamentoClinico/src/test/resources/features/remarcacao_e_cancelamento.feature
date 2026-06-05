# language: pt
Funcionalidade: Remarcação e cancelamento de consultas
  Como tutor, quero remarcar ou cancelar consultas respeitando a antecedência
  mínima, com histórico e eventos auditáveis preservados.

  Cenário: Remarcação com antecedência suficiente atualiza o horário e audita
    Dado uma consulta confirmada com antecedência de 48 horas
    Quando o tutor remarca a consulta para um novo horário
    Então a consulta deve registrar 1 remarcação no histórico
    E o tutor deve ser notificado

  Cenário: Remarcação sem antecedência mínima é recusada
    Dado uma consulta confirmada com antecedência de 10 horas
    Quando o tutor remarca a consulta para um novo horário
    Então o agendamento deve ser recusado por regra de negócio

  Cenário: Cancelamento sem antecedência mínima é recusado
    Dado uma consulta confirmada com antecedência de 10 horas
    Quando o tutor cancela a consulta
    Então o agendamento deve ser recusado por regra de negócio

  Cenário: Cada operação registra um evento auditável
    Dado uma consulta confirmada com antecedência de 48 horas
    Quando o tutor remarca a consulta para um novo horário
    Então a consulta deve possuir um evento "REMARCADA"
