package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros;

import java.math.BigDecimal;

/**
 * Categorias de nível de atividade do paciente — F-11 RN 1. Cada nível carrega
 * o fator multiplicador metabólico que será aplicado pelo
 * {@code NivelAtividadeDecorator} na cadeia de cálculo da NEM.
 */
public enum NivelAtividade {

    SEDENTARIO          (new BigDecimal("1.2")),
    POUCO_ATIVO         (new BigDecimal("1.4")),
    MODERADAMENTE_ATIVO (new BigDecimal("1.6")),
    MUITO_ATIVO         (new BigDecimal("1.8")),
    ATLETA              (new BigDecimal("2.0"));

    private final BigDecimal fator;

    NivelAtividade(BigDecimal fator) { this.fator = fator; }

    public BigDecimal getFator() { return fator; }
}
