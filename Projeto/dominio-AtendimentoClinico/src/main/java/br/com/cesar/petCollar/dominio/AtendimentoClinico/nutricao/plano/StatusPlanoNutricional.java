package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano;

/**
 * Estados do plano nutricional. {@link #FINALIZADO} é a transição irreversível
 * que dispara a assinatura digital e torna o agregado imutável (F-11 RN 8).
 *
 * <p>{@link #SUBSTITUIDO} marca planos finalizados que foram superados por uma
 * nova prescrição posterior — eles continuam no banco para auditoria e para o
 * histórico evolutivo do médico, mas o tutor só vê o plano ATIVO (o último
 * FINALIZADO de cada paciente).
 */
public enum StatusPlanoNutricional {
    RASCUNHO,
    FINALIZADO,
    SUBSTITUIDO
}
