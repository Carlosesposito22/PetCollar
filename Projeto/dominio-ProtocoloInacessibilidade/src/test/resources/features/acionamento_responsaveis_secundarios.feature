# language: pt
Funcionalidade: Acionamento dos responsáveis secundários
  Como clínica, quero acionar os responsáveis secundários cadastrados na ordem de
  prioridade (RN 4) antes de qualquer escalonamento (RN 5), reutilizando o mesmo
  conteúdo de notificação enviado ao tutor (RN 14).

  Cenário: Responsáveis secundários são acionados antes do escalonamento
    Dado uma configuração de protocolo vigente
    E um protocolo ativado para o atendimento
    E 2 responsáveis secundários cadastrados que não respondem
    Quando o sistema aciona todos os responsáveis secundários
    Então o protocolo deve ficar com status "EM_TENTATIVA_SECUNDARIOS"
    E todos os responsáveis secundários devem ter sido acionados

  Cenário: Não é possível escalonar antes de acionar os secundários
    Dado uma configuração de protocolo vigente
    E um protocolo ativado para o atendimento
    Quando o sistema tenta iniciar o escalonamento
    Então a operação deve ser recusada por regra de negócio

  Cenário: Responsável secundário responde e encerra o protocolo
    Dado uma configuração de protocolo vigente
    E um protocolo ativado para o atendimento
    E um responsável secundário que responde no canal "TELEFONE"
    Quando o sistema aciona todos os responsáveis secundários
    Então o protocolo deve ficar com status "ENCERRADO_COM_SUCESSO"

  Cenário: Responsáveis secundários recebem notificação de criticidade média
    Dado uma configuração de protocolo vigente
    E um protocolo ativado para o atendimento
    E 2 responsáveis secundários cadastrados que não respondem
    Quando o sistema aciona todos os responsáveis secundários
    Então o responsável secundário deve ser notificado com criticidade "MEDIA"
