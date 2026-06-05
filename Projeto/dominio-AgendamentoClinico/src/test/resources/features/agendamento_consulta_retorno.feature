# language: pt
Funcionalidade: Agendamento de consulta de retorno
  Como tutor, quero agendar o retorno apresentando os exames da consulta anterior,
  liberando o calendário do médico apenas quando houver exame concluído.

  Cenário: Retorno exige vínculo com a consulta de origem
    Quando o tutor tenta criar um retorno sem consulta de origem
    Então o agendamento deve ser recusado com erro de argumento

  Cenário: Retorno é bloqueado quando não há exame concluído
    Dado um prontuário com status "ATIVO" para o paciente
    E uma consulta de origem elegível a retorno
    E a consulta de origem não possui exames concluídos
    Quando o tutor agenda o retorno
    Então o agendamento deve ser recusado por regra de negócio

  Cenário: Retorno é liberado com ao menos um exame concluído
    Dado um prontuário com status "ATIVO" para o paciente
    E uma consulta de origem elegível a retorno
    E a consulta de origem possui 1 exame concluído
    Quando o tutor agenda o retorno
    Então a consulta deve ficar com status "CONFIRMADA"
    E o médico deve ser notificado
    E o tutor deve ser notificado

  Cenário: Retorno é bloqueado quando a consulta de origem não está elegível
    Dado um prontuário com status "ATIVO" para o paciente
    E uma consulta de origem não elegível a retorno
    E a consulta de origem possui 1 exame concluído
    Quando o tutor agenda o retorno
    Então o agendamento deve ser recusado por regra de negócio

  Cenário: Exibição obrigatória dos exames solicitados da consulta de origem
    Dado uma consulta de origem com 2 exames solicitados
    Quando o tutor consulta os exames solicitados
    Então devem ser exibidos 2 exames

  Cenário: Confirmação de exame pelo tutor é registrada
    Dado um exame solicitado "EX-1"
    Quando o tutor confirma a realização do exame
    Então a porta de exames deve registrar a confirmação do exame
