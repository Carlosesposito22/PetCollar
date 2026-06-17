package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.evolucao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
