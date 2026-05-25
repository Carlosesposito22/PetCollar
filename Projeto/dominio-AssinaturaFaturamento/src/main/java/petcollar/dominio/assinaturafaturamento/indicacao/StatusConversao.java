package petcollar.dominio.assinaturafaturamento.indicacao;

public enum StatusConversao {
    AGUARDANDO_CADASTRO,
    AGUARDANDO_PAGAMENTO,
    CONFIRMADA,
    INVALIDA_FRAUDE,
    INVALIDA_CPF_DUPLICADO
}
