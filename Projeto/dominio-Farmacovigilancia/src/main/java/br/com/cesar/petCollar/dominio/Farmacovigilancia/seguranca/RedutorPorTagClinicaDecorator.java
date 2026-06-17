package br.com.cesar.petCollar.dominio.Farmacovigilancia.seguranca;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.TagClinica;

public class RedutorPorTagClinicaDecorator extends CalculadoraDoseDecorator {

    private static final BigDecimal FATOR_REDUTOR = new BigDecimal("0.75");

    private final Set<TagClinica> tags;

    public RedutorPorTagClinicaDecorator(CalculadoraDoseMaximaSegura base, Set<TagClinica> tags) {
        super(base);
        if (tags == null) throw new IllegalArgumentException("Conjunto de tags não pode ser nulo.");
        this.tags = Set.copyOf(tags);
    }

    @Override
    public BigDecimal calcular() {
        BigDecimal resultado = base.calcular();
        if (foiAplicado())
            resultado = resultado.multiply(FATOR_REDUTOR).setScale(3, RoundingMode.HALF_UP);
        return resultado;
    }

    public boolean foiAplicado() {
        return tags.stream().anyMatch(TagClinica::reduzDoseMaxima);
    }
}
