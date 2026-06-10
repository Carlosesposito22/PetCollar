package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.nem;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;

/**
 * <h2>ConcreteDecorator — Modificador metabólico por comorbidade</h2>
 *
 * Aplica a regra RN 3 da F-11:
 * <pre>
 *   NEM = NEM_inferior × modificador_comorbidade
 * </pre>
 * Exemplos canônicos: obesidade ×0.8 (restrição calórica), doença renal ×0.85,
 * diabetes ×0.85. Se a comorbidade for {@link Comorbidade#NENHUMA}, o decorator
 * é transparente — devolve o valor inalterado.
 */
public final class ComorbidadeDecorator extends CalculadoraNEMDecorator {

    private final Comorbidade comorbidade;

    public ComorbidadeDecorator(CalculadoraNEM base, Comorbidade comorbidade) {
        super(base);
        if (comorbidade == null)
            throw new IllegalArgumentException("Comorbidade é obrigatória (use NENHUMA quando não houver).");
        this.comorbidade = comorbidade;
    }

    @Override
    public BigDecimal calcular() {
        return base.calcular()
                .multiply(comorbidade.getModificador())
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Comorbidade getComorbidade()  { return comorbidade; }
    public BigDecimal getModificador()   { return comorbidade.getModificador(); }
    public boolean foiAplicado()         { return comorbidade.aplicaModificador(); }
}
