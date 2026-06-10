package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.nem.ComorbidadeDecorator;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.nem.NEMBase;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.nem.NivelAtividadeDecorator;

/**
 * Resultado consolidado do cálculo da NEM com breakdown de cada passo da
 * cadeia de Decorators. Imutável — útil para exibir na UI ou persistir junto
 * com o plano finalizado.
 *
 * <p>A fábrica {@link #calcular(ParametrosPaciente)} encapsula a montagem da
 * cadeia <strong>NEMBase → NivelAtividadeDecorator → ComorbidadeDecorator</strong>
 * e produz todos os componentes exibidos pelo Figma.
 */
public record ResultadoNEM(
        BigDecimal pesoMetabolico,
        BigDecimal nemBase,
        BigDecimal fatorAtividade,
        BigDecimal modificadorComorbidade,
        BigDecimal nemTotal,
        BigDecimal quantidadeRecomendadaGramasPorDia
) {
    public static ResultadoNEM calcular(ParametrosPaciente p) {
        NEMBase base = new NEMBase(p.pesoIdealKg());
        NivelAtividadeDecorator comAtividade = new NivelAtividadeDecorator(base, p.nivelAtividade());
        ComorbidadeDecorator comComorbidade = new ComorbidadeDecorator(comAtividade, p.comorbidade());

        BigDecimal nemTotal = comComorbidade.calcular();
        BigDecimal gramasPorDia = nemTotal
                .multiply(new BigDecimal("1000"))
                .divide(p.densidadeCaloricaKcalPorKg(), 0, RoundingMode.HALF_UP);

        return new ResultadoNEM(
                base.pesoMetabolico(),
                base.calcular(),
                comAtividade.getFator(),
                comComorbidade.foiAplicado() ? comComorbidade.getModificador() : null,
                nemTotal,
                gramasPorDia
        );
    }
}
