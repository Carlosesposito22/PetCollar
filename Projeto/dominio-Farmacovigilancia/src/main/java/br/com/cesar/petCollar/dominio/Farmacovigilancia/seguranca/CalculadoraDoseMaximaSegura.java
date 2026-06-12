package br.com.cesar.petCollar.dominio.Farmacovigilancia.seguranca;

import java.math.BigDecimal;

/**
 * Componente do padrão Decorator que calcula o teto de dosagem (mg/kg)
 * permitido para um medicamento em um paciente específico. O resultado
 * acumula as restrições de cada camada empilhada (tag clínica, alergia).
 *
 * <p>Mesma arquitetura usada na F-11 ({@code CalculadoraNEM}) — a
 * F-12 reaproveita o padrão Decorator do projeto.
 */
public interface CalculadoraDoseMaximaSegura {

    BigDecimal calcular();
}
