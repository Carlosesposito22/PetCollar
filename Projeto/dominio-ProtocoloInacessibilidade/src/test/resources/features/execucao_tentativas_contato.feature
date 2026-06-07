# language: pt
Funcionalidade: Execução das tentativas de contato com o tutor
  Como clínica, quero executar a etapa de contato com o tutor nos canais habilitados,
  na ordem configurada e até o limite por canal (RN 2), registrando cada tentativa de
  forma auditável (RN 3), notificando o tutor a cada tentativa (RN 11) com criticidade
  proporcional à etapa (RN 9). A etapa é a subclasse EtapaContatoTutorService do
  Template Method.

  Cenário: Cada tentativa é registrada e o tutor é notificado a cada canal
    Dado uma configuração de protocolo vigente
    E um protocolo ativado para o atendimento
    E o tutor não responde em nenhum canal
    Quando o sistema executa a etapa de contato com o tutor
    Então o protocolo deve conter 6 tentativas de contato
    E todas as tentativas devem ter status "SEM_RESPOSTA"
    E o tutor deve ser notificado

  Cenário: As tentativas seguem a ordem dos canais habilitados e o limite por canal
    Dado uma configuração de protocolo vigente
    E um protocolo ativado para o atendimento
    E o tutor não responde em nenhum canal
    Quando o sistema executa a etapa de contato com o tutor
    Então os canais utilizados nas tentativas devem ser "TELEFONE,TELEFONE,SMS,SMS,EMAIL,EMAIL"

  Cenário: A notificação ao tutor tem criticidade baixa
    Dado uma configuração de protocolo vigente
    E um protocolo ativado para o atendimento
    E o tutor não responde em nenhum canal
    Quando o sistema executa a etapa de contato com o tutor
    Então a última notificação ao tutor deve ter criticidade "BAIXA"

  Cenário: Tutor responde e o protocolo é encerrado com sucesso sem tentar os demais canais
    Dado uma configuração de protocolo vigente
    E um protocolo ativado para o atendimento
    E o tutor responde no canal "TELEFONE"
    Quando o sistema executa a etapa de contato com o tutor
    Então o protocolo deve ficar com status "ENCERRADO_COM_SUCESSO"
    E o protocolo deve conter 1 tentativa de contato

  Cenário: Esgotados os canais sem resposta, a etapa avança para os responsáveis secundários
    Dado uma configuração de protocolo vigente
    E um protocolo ativado para o atendimento
    E o tutor não responde em nenhum canal
    Quando o sistema executa a etapa de contato com o tutor
    Então o protocolo deve ficar com status "EM_TENTATIVA_SECUNDARIOS"
