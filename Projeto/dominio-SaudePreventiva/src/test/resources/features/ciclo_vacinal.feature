# language: pt
Funcionalidade: Ciclo Vacinal — Carteira Digital de Vacinação (F-06)
  Como Tutor de um paciente
  Quero visualizar o histórico de vacinas e receber previsões de datas para próximas doses
  Para manter a proteção do meu pet atualizada e evitar atrasos nos reforços

  Cenário: Criar ciclo vacinal de filhote com primeira dose agendada
    Dado que existe um paciente com id "pac-f06-001"
    Quando o médico cria o ciclo "V10" com 3 doses e protocolo "FILHOTE" para a data "2026-01-01"
    Então o ciclo deve ter 1 dose registrada
    E o número da primeira dose deve ser 1
    E o status da primeira dose deve ser "PENDENTE"

  Cenário: Calcular data prevista da próxima dose com protocolo de filhote (21 dias)
    Dado um ciclo "V10" com protocolo "FILHOTE" e primeira dose aplicada em "2026-01-01"
    Quando o serviço calcula a data sugerida para a próxima dose
    Então a data sugerida deve ser "2026-01-22"

  Cenário: Calcular data prevista com protocolo de reforço anual (12 meses)
    Dado um ciclo "Antirrábica" com protocolo "REFORCO_ANUAL" e primeira dose aplicada em "2026-01-01"
    Quando o serviço calcula a data sugerida para a próxima dose
    Então a data sugerida deve ser "2027-01-01"

  Cenário: Calcular data prevista com protocolo de viagem (30 dias)
    Dado um ciclo "Vacina Raiva" com protocolo "VIAGEM" e primeira dose aplicada em "2026-02-01"
    Quando o serviço calcula a data sugerida para a próxima dose
    Então a data sugerida deve ser "2026-03-03"

  Cenário: Calcular data prevista com protocolo personalizado
    Dado um ciclo "Leishmania" com protocolo "PERSONALIZADO" de 45 dias e primeira dose aplicada em "2026-01-01"
    Quando o serviço calcula a data sugerida para a próxima dose
    Então a data sugerida deve ser "2026-02-15"

  Cenário: Detectar dose vacinal em atraso
    Dado uma dose do ciclo "Giardíase" agendada para "2025-01-01"
    E a dose não foi aplicada
    Quando o sistema verifica o status da dose
    Então o status da dose deve ser "EM_ATRASO"

  Cenário: Aplicar uma dose vacinal pelo veterinário
    Dado uma dose pendente do ciclo "Antirrábica" agendada para "2026-06-07"
    Quando o médico aplica a dose em "2026-06-07" com médico "Dr. Carlos Silva" e lote "L12345"
    Então o status da dose deve ser "APLICADA"
    E a data de aplicação deve ser "2026-06-07"
    E o médico registrado deve ser "Dr. Carlos Silva"

  Cenário: Não permitir aplicar dose já aplicada
    Dado uma dose já aplicada do ciclo "V10" em "2026-01-01"
    Quando se tenta aplicar a dose novamente em "2026-01-02"
    Então deve ser lançada exceção com mensagem contendo "já foi aplicada"

  Cenário: Não permitir agendar além do limite de doses do ciclo
    Dado um ciclo "V10" com 2 doses e 2 doses já agendadas
    Quando se tenta agendar uma terceira dose
    Então deve ser lançada exceção com mensagem contendo "todas as doses planejadas"

  Cenário: Detectar paciente com vacina em atraso
    Dado que existe um paciente com id "pac-f06-002"
    E o paciente possui um ciclo com dose em atraso
    Quando o serviço verifica se o paciente possui vacina em atraso
    Então o resultado deve ser verdadeiro
