package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano;

/**
 * Estados do plano nutricional. {@link #FINALIZADO} é a transição irreversível
 * que dispara a assinatura digital e torna o agregado imutável (F-11 RN 8).
 */
public enum StatusPlanoNutricional {
    RASCUNHO,
    FINALIZADO
}
