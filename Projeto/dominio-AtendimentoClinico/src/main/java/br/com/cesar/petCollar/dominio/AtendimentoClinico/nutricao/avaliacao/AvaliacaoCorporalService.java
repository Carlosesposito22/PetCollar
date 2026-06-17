package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.avaliacao;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.ParametrosPaciente;

public class AvaliacaoCorporalService {

    public static final BigDecimal LIMIAR_PERCENTUAL = new BigDecimal("15");

    public AvaliacaoCorporal avaliar(ParametrosPaciente parametros) {
        if (parametros == null)
            throw new IllegalArgumentException("Parâmetros são obrigatórios para a avaliação corporal.");

        BigDecimal pesoAtual = parametros.pesoAtualKg();
        BigDecimal pesoIdeal = parametros.pesoIdealKg();

        BigDecimal divergencia = pesoAtual.subtract(pesoIdeal)
                .multiply(new BigDecimal("100"))
                .divide(pesoIdeal, 2, RoundingMode.HALF_UP);

        AvaliacaoCorporal.Classificacao classificacao;
        if (divergencia.abs().compareTo(LIMIAR_PERCENTUAL) <= 0) {
            classificacao = AvaliacaoCorporal.Classificacao.ADEQUADO;
        } else if (divergencia.signum() > 0) {
            classificacao = AvaliacaoCorporal.Classificacao.OBESIDADE;
        } else {
            classificacao = AvaliacaoCorporal.Classificacao.CAQUEXIA;
        }
        return new AvaliacaoCorporal(classificacao, divergencia);
    }
}
