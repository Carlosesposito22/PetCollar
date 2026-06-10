# language: pt
Funcionalidade: Programa de Conquistas e Gamificacao

  Cenario: Evento unico concede badge imediatamente
    Dado existe uma badge de evento unico com chave "primeira_consulta"
    E o tutor ainda nao conquistou essa badge
    Quando o servico avaliar as badges para o evento "primeira_consulta"
    Entao a badge deve ser concedida ao tutor
    E a conquista deve ser registrada

  Cenario: Badge quantitativa exige atingir a meta
    Dado existe uma badge quantitativa com meta de 5 e chave "consulta_realizada"
    E existe um progresso do tutor com valor atual 4 para essa badge
    Quando o servico atualizar o progresso em 1 para essa badge
    Entao o percentual de conclusao deve ser 100
    E a badge deve ter meta atingida

  Cenario: Badge ja conquistada nao e concedida novamente
    Dado existe uma badge de evento unico com chave "vacina_aplicada"
    E o tutor ja conquistou essa badge anteriormente
    Quando o servico avaliar as badges para o evento "vacina_aplicada"
    Entao a badge nao deve ser concedida novamente

  Cenario: Listar proximas conquistas por percentual
    Dado o tutor possui 3 progressos com percentuais 80 60 e 40
    Quando o servico listar as 2 proximas conquistas do tutor
    Entao as 2 badges retornadas devem estar em ordem decrescente de percentual

  Cenario: Badge quantitativa nao e concedida antes de atingir a meta
    Dado existe uma badge quantitativa com meta de 10 e chave "check_up_realizado"
    E o tutor ainda nao tem progresso para essa badge
    Quando o servico avaliar as badges para o evento "check_up_realizado"
    Entao a badge nao deve ser concedida ainda
    E o progresso do tutor deve ser 1

  Cenario: Incremento invalido lanca excecao
    Dado existe um progresso do tutor com valor atual 2 e meta 5
    Quando o servico tentar atualizar o progresso com incremento 0
    Entao deve ser lancada uma excecao de argumento invalido
