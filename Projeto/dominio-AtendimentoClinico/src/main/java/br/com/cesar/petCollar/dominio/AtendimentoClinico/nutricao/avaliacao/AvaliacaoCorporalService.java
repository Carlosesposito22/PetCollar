package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.avaliacao;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.ParametrosPaciente;

/**
 * Service de domínio que aplica a RN 6 de F-11 — emite alerta visual
 * impeditivo quando a divergência do peso atual em relação ao peso ideal
 * supera o limiar de {@link #LIMIAR_PERCENTUAL}.
 *
 * <ul>
 *   <li>Peso atual &lt; peso ideal por mais que o limiar → <strong>CAQUEXIA</strong></li>
 *   <li>Peso atual &gt; peso ideal por mais que o limiar → <strong>OBESIDADE</strong></li>
 *   <li>Caso contrário → <strong>ADEQUADO</strong></li>
 * </ul>
 */
public class AvaliacaoCorporalService {

    /** Limiar canônico da F-11 RN 6 — 15% de divergência aciona alerta. */
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
