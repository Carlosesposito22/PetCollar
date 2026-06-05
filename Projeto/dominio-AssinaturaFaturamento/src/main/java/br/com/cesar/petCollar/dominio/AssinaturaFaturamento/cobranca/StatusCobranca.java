package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca;

public enum StatusCobranca {
    /** Mensalidade aberta, dentro do prazo de vencimento. */
    PENDENTE,
    /** Mensalidade vencida sem pagamento — acumula juros (RN 4). */
    EM_ATRASO,
    /** Mensalidade quitada — juros são fixados permanentemente no registro. */
    PAGA
}
