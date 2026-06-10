package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano;

/**
 * Modelos pré-configurados de cronograma de transição alimentar (F-11 RN 5).
 * {@link #PERSONALIZADO} indica que o médico montou linhas manualmente.
 */
public enum TipoCronograma {
    PADRAO_7_DIAS,
    PADRAO_10_DIAS,
    PADRAO_14_DIAS,
    PERSONALIZADO
}
