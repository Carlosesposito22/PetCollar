package br.com.cesar.petCollar.dominio.Farmacovigilancia.seguranca;

import java.math.BigDecimal;
import java.util.Set;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Medicamento;

/**
 * Camada de bloqueio total: se o paciente é alérgico a qualquer componente
 * do medicamento, a dose máxima segura colapsa para zero — o que dispara
 * uma violação BLOQUEIO pelo validador. É a defesa final contra prescrever
 * princípio ativo declarado como alergia no prontuário.
 */
public class RedutorPorAlergiaDecorator extends CalculadoraDoseDecorator {

    private final Medicamento medicamento;
    private final Set<String> alergiasDoPaciente;

    public RedutorPorAlergiaDecorator(CalculadoraDoseMaximaSegura base,
                                      Medicamento medicamento,
                                      Set<String> alergiasDoPaciente) {
        super(base);
        if (medicamento == null) throw new IllegalArgumentException("Medicamento é obrigatório.");
        this.medicamento = medicamento;
        this.alergiasDoPaciente = alergiasDoPaciente == null ? Set.of() : Set.copyOf(alergiasDoPaciente);
    }

    @Override
    public BigDecimal calcular() {
        if (foiAplicado()) return BigDecimal.ZERO;
        return base.calcular();
    }

    public boolean foiAplicado() {
        return medicamento.compartilhaComponentesCom(alergiasDoPaciente);
    }
}
