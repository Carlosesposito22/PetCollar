package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.evolucao;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.ResultadoNEM;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.StatusPlanoNutricional;

/**
 * Service de domínio que calcula a {@link EvolucaoNutricional} entre dois
 * planos finalizados. Stateless — apenas combina valores dos snapshots.
 *
 * <p>Limiar de "estável": variação de peso ≤ 2% do peso anterior.
 */
public class ComparacaoEvolutivaNutricionalService {

    private static final BigDecimal CEM = new BigDecimal("100");
    private static final BigDecimal LIMIAR_ESTAVEL_PCT = new BigDecimal("2");

    public EvolucaoNutricional comparar(PlanoNutricional anterior, PlanoNutricional atual) {
        if (anterior == null) throw new IllegalArgumentException("Plano anterior é obrigatório.");
        if (atual == null)    throw new IllegalArgumentException("Plano atual é obrigatório.");
        if (anterior.getStatus() != StatusPlanoNutricional.FINALIZADO
                || atual.getStatus() != StatusPlanoNutricional.FINALIZADO)
            throw new IllegalStateException("Comparação só faz sentido entre planos finalizados.");

        BigDecimal pesoAnt = anterior.getParametros().pesoAtualKg();
        BigDecimal pesoNov = atual.getParametros().pesoAtualKg();
        BigDecimal deltaPeso = pesoNov.subtract(pesoAnt).setScale(2, RoundingMode.HALF_UP);
        BigDecimal deltaPesoPct = pesoAnt.signum() == 0 ? BigDecimal.ZERO
                : deltaPeso.multiply(CEM).divide(pesoAnt, 2, RoundingMode.HALF_UP);

        ResultadoNEM nemAnt = anterior.getResultadoFinalizado();
        ResultadoNEM nemNov = atual.getResultadoFinalizado();
        BigDecimal deltaNemPct = nemAnt.nemTotal().signum() == 0 ? BigDecimal.ZERO
                : nemNov.nemTotal().subtract(nemAnt.nemTotal())
                        .multiply(CEM).divide(nemAnt.nemTotal(), 2, RoundingMode.HALF_UP);

        EvolucaoNutricional.Tendencia tendencia;
        BigDecimal absPct = deltaPesoPct.abs();
        if (absPct.compareTo(LIMIAR_ESTAVEL_PCT) <= 0)      tendencia = EvolucaoNutricional.Tendencia.ESTAVEL;
        else if (deltaPeso.signum() > 0)                    tendencia = EvolucaoNutricional.Tendencia.GANHO;
        else                                                tendencia = EvolucaoNutricional.Tendencia.PERDA;

        return new EvolucaoNutricional(
                anterior.getAtualizadoEm(), atual.getAtualizadoEm(),
                pesoAnt, pesoNov, deltaPeso, deltaPesoPct,
                nemAnt.nemTotal(), nemNov.nemTotal(), deltaNemPct,
                tendencia);
    }
}
