# language: pt
Funcionalidade: Auditabilidade das notificações (RN 16)
  Como clínica, quero que cada notificação disparada durante o protocolo seja
  encaminhada ao contexto de Notificacao para registro auditável (destinatário,
  conteúdo, criticidade e instante).

  Cenário: Cada tentativa de contato encaminha uma notificação para registro
    Dado uma configuração de protocolo vigente
    E um protocolo ativado para o atendimento
    E o tutor não responde em nenhum canal
    Quando o sistema executa 2 tentativas de contato seguidas
    Então o serviço de notificação deve ter sido acionado 2 vezes
