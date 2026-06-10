package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.nem;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.NivelAtividade;

/**
 * <h2>ConcreteDecorator — Multiplicador metabólico por nível de atividade</h2>
 *
 * Aplica a regra RN 1 da F-11:
 * <pre>
 *   NEM = NEM_inferior × fator_atividade
 * </pre>
 * onde o fator vem do enum {@link NivelAtividade} (1.2 para sedentário até 2.0
 * para atleta).
 */
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
