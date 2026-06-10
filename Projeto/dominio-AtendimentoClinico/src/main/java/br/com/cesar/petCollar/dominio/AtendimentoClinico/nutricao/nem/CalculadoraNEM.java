package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.nem;

import java.math.BigDecimal;

/**
 * <h2>Componente do padrão Decorator (GoF) — F-11 NEM</h2>
 *
 * Abstração comum para compor cadeias de cálculo da Necessidade Energética de
 * Manutenção. Cada nível devolve um {@link BigDecimal} (kcal/dia) resultante
 * da aplicação de sua regra sobre o nível inferior.
 *
 * <p>Exemplo de cadeia (lida da raiz para o topo):
 * <pre>
 *   ComorbidadeDecorator
 *     └─ NivelAtividadeDecorator
 *           └─ NEMBase
 * </pre>
 *
 * O resultado de {@link #calcular()} no nó externo já reflete todas as
 * decorações empilhadas: <strong>70 × peso<sup>0,75</sup> × fator × modificador</strong>.
 */
public interface CalculadoraNEM {
    BigDecimal calcular();
}
