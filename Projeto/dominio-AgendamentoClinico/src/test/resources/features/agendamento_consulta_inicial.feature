# language: pt
Funcionalidade: Agendamento de consulta inicial
  Como tutor, quero agendar a primeira consulta de um paciente escolhendo médico e
  horário disponível, com as regras de segurança do agendamento garantidas.

  Cenário: Médicos são filtrados pela especialidade escolhida
    Dado uma especialidade "Cardiologia" com 2 médicos cadastrados
    Quando o tutor lista os médicos da especialidade
    Então a lista de médicos retornada deve ter 2 itens

  Cenário: Motivo em branco impede a criação da consulta inicial
    Quando o tutor tenta criar uma consulta inicial com motivo "   "
    Então o agendamento deve ser recusado com erro de argumento

  Cenário: Prontuário inativo bloqueia o agendamento inicial
    Dado um prontuário com status "INATIVO" para o paciente
    E um horário livre na agenda do médico
    Quando o tutor agenda a consulta inicial
    Então o agendamento deve ser recusado por regra de negócio

  Cenário: Horário indisponível bloqueia o agendamento inicial
    Dado um prontuário com status "ATIVO" para o paciente
    E um horário ocupado na agenda do médico
    Quando o tutor agenda a consulta inicial
    Então o agendamento deve ser recusado por regra de negócio

  Cenário: Conflito de horário no paciente bloqueia o agendamento inicial
    Dado um prontuário com status "ATIVO" para o paciente
    E um horário livre na agenda do médico
    E o paciente já possui consulta no mesmo horário
    Quando o tutor agenda a consulta inicial
    Então o agendamento deve ser recusado por regra de negócio

  Cenário: Agendamento inicial confirma a consulta e notifica os envolvidos
    Dado um prontuário com status "ATIVO" para o paciente
    E um horário livre na agenda do médico
    Quando o tutor agenda a consulta inicial
    Então a consulta deve ficar com status "CONFIRMADA"
    E a consulta deve ser do tipo "INICIAL"
    E o médico deve ser notificado
    E o tutor deve ser notificado
