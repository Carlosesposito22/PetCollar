# language: pt
Funcionalidade: Gerenciamento de Carencia de Beneficios do Plano

  Cenario: Beneficio ainda em carencia nao pode ser utilizado
    Dado existe um beneficio do tutor com data de liberacao no futuro
    Quando o servico calcular o status do beneficio
    Entao o status deve ser "EM_CARENCIA"

  Cenario: Beneficio disponivel apos cumprir carencia
    Dado existe um beneficio do tutor com data de liberacao no passado
    E o beneficio tem 2 usos restantes no periodo
    Quando o servico calcular o status do beneficio
    Entao o status deve ser "DISPONIVEL"

  Cenario: Beneficio esgotado quando sem usos restantes
    Dado existe um beneficio disponivel com 0 usos restantes
    Quando o servico calcular o status do beneficio
    Entao o status deve ser "ESGOTADO"

  Cenario: Novo periodo reinicia os usos restantes do beneficio
    Dado existe um beneficio com status "ESGOTADO" e 0 usos restantes
    E o periodo atual do beneficio comecou ha 2 meses
    Quando o servico calcular o status do beneficio
    Entao o status deve ser "DISPONIVEL"
    E os usos restantes do periodo atual devem ser 2

  Cenario: Gerar ticket decrementa usos restantes
    Dado existe um beneficio com status "DISPONIVEL" e 3 usos restantes
    Quando o servico gerar um ticket para o beneficio
    Entao o ticket deve ter status "GERADO"
    E os usos restantes do beneficio devem ser 2
    E o ticket deve ter um codigo GUID unico

  Cenario: Gerar ticket falha quando o beneficio nao estiver disponivel
    Dado existe um beneficio do tutor com data de liberacao no futuro
    Quando o servico gerar um ticket para o beneficio
    Entao deve ser lancada uma excecao de estado invalido

  Cenario: Gerar ticket falha quando ja existe ticket ativo para este beneficio
    Dado existe um beneficio com status "DISPONIVEL" e 2 usos restantes
    E ja existe um ticket ativo para este beneficio
    Quando o servico gerar um ticket para o beneficio
    Entao deve ser lancada uma excecao de estado invalido

  Cenario: Ticket expirado devolve uso ao saldo
    Dado existe um ticket com status "GERADO" gerado ha mais de 1 hora
    Quando o servico cancelar os tickets expirados
    Entao o ticket deve ter status "EXPIRADO"
    E o uso deve ser devolvido ao saldo do beneficio

  Cenario: Ticket recente nao e expirado
    Dado existe um ticket com status "GERADO" gerado recentemente
    Quando o servico cancelar os tickets expirados
    Entao nenhum ticket deve ter sido expirado

  Cenario: Cancelamento falha quando o beneficio associado nao existe no repositorio
    Dado existe um ticket com status "GERADO" gerado ha mais de 1 hora
    E o beneficio associado nao existe no repositorio
    Quando o servico cancelar os tickets expirados
    Entao deve ser lancada uma excecao de argumento invalido

