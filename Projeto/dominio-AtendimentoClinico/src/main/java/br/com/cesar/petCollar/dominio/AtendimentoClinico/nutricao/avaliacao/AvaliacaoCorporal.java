package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.avaliacao;

import java.math.BigDecimal;

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
