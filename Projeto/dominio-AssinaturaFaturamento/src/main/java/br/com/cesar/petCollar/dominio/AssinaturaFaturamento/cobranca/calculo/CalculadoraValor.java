package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.calculo;

import java.math.BigDecimal;

/**
 * <h2>Componente do padrão Decorator (GoF)</h2>
 *
 * Abstração comum para compor cadeias de cálculo do valor de uma Cobrança. Cada
 * implementação (concreta ou decorador) responde com um {@link BigDecimal}
 * resultante da aplicação de sua regra sobre o valor produzido pelo nível abaixo.
 *
 * <p>Exemplo de cadeia (lida da raiz para o topo):
 * <pre>
 *   JurosSimplesDecorator
 *     └─ DescontoIndicacaoDecorator
 *           └─ ValorBase
 * </pre>
 *
 * O resultado de {@link #calcular()} no nó externo já reflete a aplicação de
 * todas as decorações empilhadas.
 */
public interface CalculadoraValor {
    BigDecimal calcular();
}
