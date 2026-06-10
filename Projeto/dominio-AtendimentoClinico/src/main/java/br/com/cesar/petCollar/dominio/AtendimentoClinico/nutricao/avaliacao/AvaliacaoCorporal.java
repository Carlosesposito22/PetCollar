package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.avaliacao;

import java.math.BigDecimal;

/**
 * Resultado da {@link AvaliacaoCorporalService}: classificação corporal +
 * divergência percentual em relação ao peso ideal. Permite à UI montar o
 * alerta vermelho com o número exato (F-11 RN 6).
 */
public record AvaliacaoCorporal(
        Classificacao classificacao,
        BigDecimal divergenciaPercentual
) {
    public enum Classificacao {
        ADEQUADO,
        OBESIDADE,
        CAQUEXIA
    }

    public boolean exigeAlerta() { return classificacao != Classificacao.ADEQUADO; }
}
