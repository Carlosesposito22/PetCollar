package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros;

import java.math.BigDecimal;

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
