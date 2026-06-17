package br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Frequencia;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.MedicamentoId;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.ViaAdministracao;

public record ItemPrescricao(
        MedicamentoId medicamentoId,
        String nomeMedicamento,
        BigDecimal doseMgPorKg,
        BigDecimal doseTotalMg,
        BigDecimal volumeFinalMl,
        int duracaoDias,
        Frequencia frequencia,
        ViaAdministracao via,
        List<HorarioAdministracao> horarios,
        String notaCuidado
) {

    public ItemPrescricao {
        if (medicamentoId == null) throw new IllegalArgumentException("MedicamentoId é obrigatório.");
        if (nomeMedicamento == null || nomeMedicamento.isBlank())
            throw new IllegalArgumentException("Nome do medicamento é obrigatório.");
        if (doseMgPorKg == null || doseMgPorKg.signum() <= 0)
            throw new IllegalArgumentException("Dose mg/kg deve ser positiva.");
        if (doseTotalMg == null || doseTotalMg.signum() <= 0)
            throw new IllegalArgumentException("Dose total mg deve ser positiva.");
        if (volumeFinalMl == null || volumeFinalMl.signum() < 0)
            throw new IllegalArgumentException("Volume final em ml não pode ser negativo.");
        if (duracaoDias <= 0)
            throw new IllegalArgumentException("Duração em dias deve ser positiva.");
        if (frequencia == null) throw new IllegalArgumentException("Frequência é obrigatória.");
        if (via == null) throw new IllegalArgumentException("Via de administração é obrigatória.");
        horarios = horarios == null ? List.of() : Collections.unmodifiableList(List.copyOf(horarios));
    }

    public static ItemPrescricao calcular(MedicamentoId id, String nome,
                                           BigDecimal doseMgPorKg, BigDecimal pesoKg,
                                           BigDecimal concentracaoMgPorMl,
                                           int duracaoDias, Frequencia frequencia,
                                           ViaAdministracao via,
                                           List<HorarioAdministracao> horarios,
                                           String notaCuidado) {
        BigDecimal doseTotal = doseMgPorKg.multiply(pesoKg).setScale(3, RoundingMode.HALF_UP);
        BigDecimal volume = concentracaoMgPorMl.signum() == 0
                ? BigDecimal.ZERO
                : doseTotal.divide(concentracaoMgPorMl, 3, RoundingMode.HALF_UP);
        return new ItemPrescricao(id, nome, doseMgPorKg, doseTotal, volume,
                duracaoDias, frequencia, via, horarios, notaCuidado);
    }
}
