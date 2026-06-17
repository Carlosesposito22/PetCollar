package br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo;

import java.util.Objects;
import java.util.Set;

public record InteracaoMedicamentosa(
        MedicamentoId medicamentoA,
        MedicamentoId medicamentoB,
        Gravidade gravidade,
        String descricao
) {

    public enum Gravidade {

        GRAVE,

        MODERADA,

        LEVE
    }

    public InteracaoMedicamentosa {
        if (medicamentoA == null || medicamentoB == null)
            throw new IllegalArgumentException("Ambos os medicamentos são obrigatórios.");
        if (medicamentoA.equals(medicamentoB))
            throw new IllegalArgumentException("Não há interação de um medicamento consigo mesmo.");
        if (gravidade == null) throw new IllegalArgumentException("Gravidade é obrigatória.");
        if (descricao == null || descricao.isBlank())
            throw new IllegalArgumentException("Descrição é obrigatória.");
    }

    public boolean envolve(MedicamentoId a, MedicamentoId b) {
        Set<MedicamentoId> par = Set.of(medicamentoA, medicamentoB);
        return par.equals(Set.of(Objects.requireNonNull(a), Objects.requireNonNull(b)));
    }
}
