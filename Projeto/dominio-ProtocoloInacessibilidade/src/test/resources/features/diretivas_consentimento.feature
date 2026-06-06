# language: pt
Funcionalidade: Consulta às diretivas de consentimento (RN 10)
  Como clínica, quero consultar as diretivas de consentimento previamente assinadas
  pelo tutor para autorizar ou bloquear condutas clínicas enquanto ele está
  inacessível.

  Cenário: Conduta previamente autorizada é liberada
    Dado uma diretiva que autoriza a conduta "MEDICACAO_CONTROLADA"
    Quando o sistema consulta a autorização da conduta "MEDICACAO_CONTROLADA"
    Então a conduta deve ser autorizada

  Cenário: Conduta sem autorização é bloqueada
    Dado uma diretiva que não autoriza a conduta "EUTANASIA"
    Quando o sistema consulta a autorização da conduta "EUTANASIA"
    Então a conduta deve ser bloqueada
