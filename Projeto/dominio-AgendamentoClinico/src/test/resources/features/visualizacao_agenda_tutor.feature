# language: pt
Funcionalidade: Visualização da agenda do tutor
  Como tutor, quero visualizar as consultas do meu paciente filtrando por status,
  tipo e período (RN 17).

  Cenário: Tutor filtra as consultas do paciente por status
    Dado o paciente possui consultas registradas na agenda
    Quando o tutor lista a agenda filtrando pelo status "CONFIRMADA"
    Então devem ser listadas apenas consultas com status "CONFIRMADA"
