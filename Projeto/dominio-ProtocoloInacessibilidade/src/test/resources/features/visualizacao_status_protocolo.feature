# language: pt
Funcionalidade: Visualização do status do protocolo pelo tutor (RN 15)
  Como tutor, quero visualizar a situação atual do protocolo de contato — status,
  nível de escalonamento e histórico de tentativas e escalonamentos.

  Cenário: Tutor visualiza o status e o histórico de tentativas
    Dado uma configuração de protocolo vigente
    E um protocolo ativado para o atendimento
    E o tutor não responde em nenhum canal
    Quando o sistema executa a etapa de contato com o tutor
    E o tutor consulta a situação do protocolo
    Então a visão deve apresentar o status "EM_TENTATIVA_SECUNDARIOS"
    E a visão deve conter 6 tentativas no histórico
