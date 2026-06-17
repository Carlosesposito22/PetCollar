package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.nem.ComorbidadeDecorator;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.nem.NEMBase;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.nem.NivelAtividadeDecorator;

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
