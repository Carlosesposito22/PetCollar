# language: pt
Funcionalidade: Central de Farmacovigilância Inteligente

  Cenário: Dose dentro do limite recebe status DENTRO_DO_LIMITE
    Dado existe um medicamento com dose maxima de 5 mg por kg
    E o paciente pesa 10 kg com dose prescrita de 30 mg
    Quando o servico validar a dosagem
    Então o status da dosagem deve ser "DENTRO_DO_LIMITE"

  Cenário: Dose acima do limite bloqueia o item
    Dado existe um medicamento com dose maxima de 5 mg por kg
    E o paciente pesa 10 kg com dose prescrita de 60 mg
    Quando o servico validar a dosagem
    Então o status da dosagem deve ser "TRAVADO_POR_DOSE"

  Cenário: Dose proxima do limite 80 porcento gera alerta
    Dado existe um medicamento com dose maxima de 5 mg por kg
    E o paciente pesa 10 kg com dose prescrita de 42 mg
    Quando o servico validar a dosagem
    Então o status da dosagem deve ser "PROXIMO_DO_LIMITE"

  Cenário: Interacao bloqueante entre dois medicamentos trava a prescricao
    Dado existe uma prescricao com dois medicamentos com interacao critica bloqueante
    Quando o servico verificar as interacoes medicamentosas
    Então a prescricao deve ter status "TRAVADA_AGUARDANDO_REVISAO"
    E o motivo deve indicar interacao bloqueante

  Cenário: Tag INSUFICIENCIA_RENAL reduz dose maxima em 50 porcento
    Dado existe um paciente com tag "INSUFICIENCIA_RENAL"
    E um medicamento com dose maxima de 10 mg por kg
    Quando o servico calcular o fator de reducao contextual
    Então a dose maxima efetiva deve ser 5 mg por kg

  Cenário: Data de fim do tratamento e o item de maior duracao
    Dado existe uma prescricao com item A de duracao 7 dias e item B de duracao 14 dias
    E ambos os itens foram iniciados hoje
    Quando o servico calcular a data de fim do tratamento
    Então a data de fim deve ser daqui a 14 dias

  Cenário: Emitir prescricao sem itens travados muda status para EMITIDA
    Dado existe uma prescricao com todos os itens com status "DENTRO_DO_LIMITE"
    Quando o servico emitir a prescricao
    Então o status da prescricao deve ser "EMITIDA"

  Cenário: Tentar emitir prescricao com item travado lanca excecao
    Dado existe uma prescricao com um item com status "TRAVADO_POR_DOSE"
    Quando o servico tentar emitir a prescricao
    Então deve ser lancada uma excecao de estado invalido
