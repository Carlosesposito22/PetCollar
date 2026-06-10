package br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.avaliacao.AvaliacaoCorporal;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.avaliacao.AvaliacaoCorporalService;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.ParametrosPaciente;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.ResultadoNEM;

/**
 * Caso de uso "ao vivo" — o médico digita os parâmetros e a UI pede o cálculo
 * sem persistir nada (NEM + avaliação corporal). Encapsula a montagem da
 * cadeia de Decorators e a regra do alerta (RN 6).
 */
public class CalcularNEMPreviewUseCase {

    private final AvaliacaoCorporalService avaliacaoCorporalService;

    public CalcularNEMPreviewUseCase(AvaliacaoCorporalService avaliacaoCorporalService) {
        if (avaliacaoCorporalService == null)
            throw new IllegalArgumentException("AvaliacaoCorporalService é obrigatório.");
        this.avaliacaoCorporalService = avaliacaoCorporalService;
    }

    public Resultado executar(ParametrosPaciente parametros) {
        if (parametros == null)
            throw new IllegalArgumentException("Parâmetros são obrigatórios.");
        ResultadoNEM nem = ResultadoNEM.calcular(parametros);
        AvaliacaoCorporal avaliacao = avaliacaoCorporalService.avaliar(parametros);
        return new Resultado(nem, avaliacao);
    }

    public record Resultado(ResultadoNEM nem, AvaliacaoCorporal avaliacaoCorporal) {}
}
