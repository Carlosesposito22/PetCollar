package br.com.cesar.petCollar.apresentacao.PortalTutor;

import java.math.BigDecimal;

public record Plano(String nome, BigDecimal valor) {
    public static final Plano BASICO_MENSAL = new Plano("Plano Básico Mensal", new BigDecimal("150.00"));
}
