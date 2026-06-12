package br.com.cesar.petCollar.dominio.Farmacovigilancia.seguranca;

import java.math.BigDecimal;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Medicamento;

/**
 * Componente concreto base do Decorator: devolve a dose máxima de segurança
 * (mg/kg) cadastrada para o medicamento no catálogo, sem nenhum redutor.
 */
public class DoseMaximaBase implements CalculadoraDoseMaximaSegura {

    private final Medicamento medicamento;

    public DoseMaximaBase(Medicamento medicamento) {
        if (medicamento == null) throw new IllegalArgumentException("Medicamento é obrigatório.");
        this.medicamento = medicamento;
    }

    @Override
    public BigDecimal calcular() {
        return medicamento.getDoseMaximaMgPorKg();
    }
}
