package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.evolucao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resultado da comparação entre dois planos finalizados sequenciais. Carrega
 * deltas absolutos e percentuais que a UI usa para "Evolução desde o último
 * plano: -2,4 kg / NEM reduzido em 18%".
 *
 * <p>Sinais: positivo = aumento; negativo = redução.
 */
public record EvolucaoNutricional(
        LocalDateTime planoAnteriorEm,
        LocalDateTime planoAtualEm,
        BigDecimal pesoAtualAnteriorKg,
        BigDecimal pesoAtualNovoKg,
        BigDecimal deltaPesoKg,
        BigDecimal deltaPesoPercentual,
        BigDecimal nemAnteriorKcal,
        BigDecimal nemNovoKcal,
        BigDecimal deltaNemPercentual,
        Tendencia tendenciaPeso
) {

    public enum Tendencia { GANHO, PERDA, ESTAVEL }

    public EvolucaoNutricional {
        if (planoAnteriorEm == null || planoAtualEm == null)
            throw new IllegalArgumentException("Datas dos planos são obrigatórias.");
        if (planoAtualEm.isBefore(planoAnteriorEm))
            throw new IllegalArgumentException("Plano atual deve ser posterior ao anterior.");
    }
}
