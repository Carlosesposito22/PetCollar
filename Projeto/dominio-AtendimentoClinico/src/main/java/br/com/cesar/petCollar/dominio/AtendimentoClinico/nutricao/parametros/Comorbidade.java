package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros;

import java.math.BigDecimal;

/**
 * Comorbidades clínicas que aplicam um modificador metabólico sobre a NEM
 * (F-11 RN 3). {@link #NENHUMA} mantém o valor sem alteração — modelagem útil
 * para o frontend sem precisar de {@code null}.
 */
public enum Comorbidade {

    NENHUMA       ("Nenhuma",        new BigDecimal("1.0")),
    OBESIDADE     ("Obesidade",      new BigDecimal("0.8")),
    DIABETES      ("Diabetes",       new BigDecimal("0.85")),
    DOENCA_RENAL  ("Doença Renal",   new BigDecimal("0.85"));

    private final String rotulo;
    private final BigDecimal modificador;

    Comorbidade(String rotulo, BigDecimal modificador) {
        this.rotulo = rotulo;
        this.modificador = modificador;
    }

    public String getRotulo()           { return rotulo; }
    public BigDecimal getModificador()  { return modificador; }
    public boolean aplicaModificador()  { return this != NENHUMA; }
}
