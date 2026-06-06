# language: pt
Funcionalidade: Execução das tentativas de contato com o tutor
  Como clínica, quero executar tentativas progressivas de contato com o tutor nos
  canais habilitados, na ordem configurada (RN 2), registrando cada tentativa de
  forma auditável (RN 3), notificando o tutor a cada tentativa (RN 11) com
  criticidade proporcional à etapa (RN 9).

  Cenário: Cada tentativa é registrada e o tutor é notificado
    Dado uma configuração de protocolo vigente
    E um protocolo ativado para o atendimento
    E o tutor não responde em nenhum canal
    Quando o sistema executa a próxima tentativa de contato
    Então uma tentativa deve ser registrada com status "SEM_RESPOSTA"
    E o tutor deve ser notificado

  Cenário: As tentativas seguem a ordem dos canais habilitados
    Dado uma configuração de protocolo vigente
    E um protocolo ativado para o atendimento
    E o tutor não responde em nenhum canal
    Quando o sistema executa 3 tentativas de contato seguidas
    Então os canais utilizados nas tentativas devem ser "TELEFONE,TELEFONE,SMS"

  Cenário: A notificação ao tutor tem criticidade baixa
    Dado uma configuração de protocolo vigente
    E um protocolo ativado para o atendimento
    E o tutor não responde em nenhum canal
    Quando o sistema executa a próxima tentativa de contato
    Então a última notificação ao tutor deve ter criticidade "BAIXA"

  Cenário: Tutor responde e o protocolo é encerrado com sucesso
    Dado uma configuração de protocolo vigente
    E um protocolo ativado para o atendimento
    E o tutor responde no canal "TELEFONE"
    Quando o sistema executa a próxima tentativa de contato
    Então o protocolo deve ficar com status "ENCERRADO_COM_SUCESSO"

  Cenário: Esgotados os canais, novas tentativas com o tutor são recusadas
    Dado uma configuração de protocolo vigente
    E um protocolo ativado para o atendimento
    E o tutor não responde em nenhum canal
    E todos os canais de contato com o tutor foram esgotados
    Quando o sistema tenta executar mais uma tentativa de contato
    Então a operação deve ser recusada por regra de negócio
