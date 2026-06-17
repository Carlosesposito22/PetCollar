package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.nem;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.NivelAtividade;

public final class NivelAtividadeDecorator extends CalculadoraNEMDecorator {

    private final NivelAtividade nivel;

    public NivelAtividadeDecorator(CalculadoraNEM base, NivelAtividade nivel) {
        super(base);
        if (nivel == null)
            throw new IllegalArgumentException("Nível de atividade é obrigatório.");
        this.nivel = nivel;
    }

    @Override
    public BigDecimal calcular() {
        return base.calcular()
                .multiply(nivel.getFator())
                .setScale(2, RoundingMode.HALF_UP);
    }

    public NivelAtividade getNivel() { return nivel; }
    public BigDecimal getFator()     { return nivel.getFator(); }
}
