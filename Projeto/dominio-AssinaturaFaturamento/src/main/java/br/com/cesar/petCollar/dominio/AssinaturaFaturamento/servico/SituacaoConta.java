package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.servico;

/**
 * Classificação dinâmica da conta do tutor segundo F-07 RN 6/7.
 * Não há classe Assinatura — a situação é derivada do histórico de cobranças.
 */
public enum SituacaoConta {
    /** Conta recém-contratada aguardando confirmação do 1º pagamento. */
    PENDENTE,
    /** 0 cobranças em atraso. */
    ATIVA,
    /** 1 ou 2 cobranças consecutivas em atraso — acesso mantido com alerta. */
    INADIMPLENTE,
    /** 3 ou mais cobranças consecutivas em atraso — login bloqueado (RN 7). */
    SUSPENSA
}
