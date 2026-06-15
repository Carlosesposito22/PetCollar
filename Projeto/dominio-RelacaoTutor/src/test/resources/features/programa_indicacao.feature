# language: pt
Funcionalidade: Programa de indicacao com recompensas

  # RN-1
  Cenario: Tutor com conta ativa obtem link de indicacao
    Dado um Tutor com conta ativa
    Quando o Tutor solicitar o link de indicacao
    Entao o link de indicacao deve ser gerado com sucesso
    E o link deve conter um codigo alfanumerico de 8 caracteres

  # RN-1
  Cenario: Tutor com conta inativa nao consegue acessar o programa
    Dado um Tutor com conta inativa
    Quando o Tutor solicitar o link de indicacao
    Entao deve ocorrer o erro "Apenas Tutores com conta ativa podem acessar o painel de indicação."

  # RN-2
  Cenario: Link de indicacao e permanente e retornado sem recrear
    Dado um Tutor com conta ativa
    E o Tutor ja possui um link de indicacao cadastrado
    Quando o Tutor solicitar o link de indicacao
    Entao o link retornado deve ser o mesmo link ja existente

  # RN-7
  Cenario: Autoindicacao e bloqueada
    Dado um Tutor com CPF "12345678901"
    E um indicado com o mesmo CPF "12345678901"
    Quando o indicado tentar se inscrever como indicacao do proprio Tutor
    Entao deve ocorrer o erro "Autoindicação não é permitida no programa de indicação (RN-7)."

  # RN-10
  Cenario: CPF ja convertido nao pode ser contabilizado novamente
    Dado um Tutor com conta ativa
    E um indicado com CPF "98765432100" que ja possui conversao registrada
    Quando o indicado tentar se inscrever como indicacao
    Entao deve ocorrer o erro "Este CPF já foi contabilizado como indicação válida anteriormente (RN-10)."

  # RN-11
  Cenario: Ultimo Clique atribui recompensa ao Tutor do link mais recente
    Dado um Tutor A com codigo de link "AAAAAAAA"
    E um Tutor B com codigo de link "BBBBBBBB"
    E o indicado com CPF "11122233344" clicou primeiro no link do Tutor A
    E o indicado com CPF "11122233344" clicou depois no link do Tutor B
    Quando o indicado se inscrever na plataforma
    Entao a indicacao deve ser atribuida ao Tutor B

  # RN-4 / RN-5 / RN-6
  Cenario: Conversao confirmada aplica desconto ao indicador e concede Conquista Lendaria
    Dado um Tutor indicador com conta ativa
    E uma indicacao pendente para o indicado com CPF "55566677788"
    Quando o gateway confirmar o pagamento da primeira mensalidade do indicado
    Entao a indicacao deve ter status "CONVERTIDA"
    E o desconto de 15 porcento deve ser aplicado na proxima fatura do Tutor indicador
    E a Conquista Lendaria deve ser concedida ao Tutor indicador

  # Template Method — ProcessamentoWebhookManual
  Cenario: Confirmacao manual por administrador ignora verificacao de metodo de pagamento e converte indicacao
    Dado um Tutor indicador com conta ativa
    E uma indicacao pendente para o indicado com CPF "33344455566"
    E o indicado usou o mesmo metodo de pagamento do Tutor indicador
    Quando um administrador confirmar manualmente a conversao
    Entao a indicacao deve ter status "CONVERTIDA"
    E a Conquista Lendaria deve ser concedida ao Tutor indicador

  # RN-8
  Cenario: Recompensa invalidada quando metodo de pagamento coincide com o do indicador
    Dado um Tutor indicador com conta ativa
    E uma indicacao pendente para o indicado com CPF "44455566677"
    E o indicado usou o mesmo metodo de pagamento do Tutor indicador
    Quando o gateway confirmar o pagamento da primeira mensalidade do indicado
    Entao deve ocorrer o erro "Recompensa invalidada: método de pagamento idêntico ao do Tutor indicador (RN-8)."
    E a indicacao deve estar invalidada

  # RN-3
  Cenario: Desconto de boas-vindas de 30 porcento fica disponivel para aplicacao na primeira mensalidade do indicado
    Dado um Tutor indicador com conta ativa
    E uma indicacao pendente para o indicado com CPF "77788899900"
    Quando o sistema consultar a indicacao pendente para o CPF "77788899900"
    Entao a indicacao pendente deve ser encontrada com status "PENDENTE"
    E o percentual de desconto de boas-vindas deve ser de 30 porcento
