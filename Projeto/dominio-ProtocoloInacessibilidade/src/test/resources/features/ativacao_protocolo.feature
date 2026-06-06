# language: pt
Funcionalidade: Ativação automática do protocolo de inacessibilidade
  Como clínica, quero ativar automaticamente o protocolo de contingência quando o
  tutor não responde dentro do tempo configurado (RN 1), notificando-o de imediato
  (RN 12), sem criar protocolos duplicados para o mesmo atendimento.

  Cenário: Ativação por timeout do tutor notifica o tutor
    Dado uma configuração de protocolo vigente
    E um atendimento em andamento com última interação do tutor há 30 minutos
    Quando o sistema verifica a ativação do protocolo para o atendimento
    Então o protocolo deve ficar com status "ATIVADO"
    E o tutor deve ser notificado

  Cenário: Dentro do tempo limite o protocolo não é ativado
    Dado uma configuração de protocolo vigente
    E um atendimento em andamento com última interação do tutor há 5 minutos
    Quando o sistema verifica a ativação do protocolo para o atendimento
    Então nenhum protocolo deve ser ativado

  Cenário: Verificação é idempotente e não duplica protocolos
    Dado uma configuração de protocolo vigente
    E um atendimento em andamento com última interação do tutor há 30 minutos
    E um protocolo já ativo para o atendimento
    Quando o sistema verifica novamente a ativação do protocolo
    Então deve haver apenas um protocolo ativo para o atendimento

  Cenário: Sem configuração vigente a ativação é recusada
    Dado um atendimento em andamento com última interação do tutor há 30 minutos
    Quando o sistema verifica a ativação do protocolo para o atendimento
    Então a operação deve ser recusada por regra de negócio
