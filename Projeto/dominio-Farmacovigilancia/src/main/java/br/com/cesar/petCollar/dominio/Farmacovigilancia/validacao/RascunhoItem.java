package br.com.cesar.petCollar.dominio.Farmacovigilancia.validacao;

import java.math.BigDecimal;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Frequencia;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.MedicamentoId;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.ViaAdministracao;

/**
 * Dados crus do item proposto pelo médico (antes de virar {@code ItemPrescricao}
 * finalizado). É o input do {@link ValidadorPrescricao}.
 */
public record RascunhoItem(
        MedicamentoId medicamentoId,
        BigDecimal doseMgPorKg,
        int duracaoDias,
        Frequencia frequencia,
        ViaAdministracao via
) {

    public RascunhoItem {
        if (medicamentoId == null) throw new IllegalArgumentException("MedicamentoId é obrigatório.");
        if (doseMgPorKg == null || doseMgPorKg.signum() <= 0)
            throw new IllegalArgumentException("Dose mg/kg deve ser positiva.");
        if (duracaoDias <= 0) throw new IllegalArgumentException("Duração deve ser positiva.");
        if (frequencia == null) throw new IllegalArgumentException("Frequência é obrigatória.");
        if (via == null) throw new IllegalArgumentException("Via é obrigatória.");
    }
}
