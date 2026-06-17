package br.com.cesar.petCollar.aplicacao.Farmacovigilancia;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.TagClinica;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.validacao.RascunhoItem;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.validacao.ResultadoValidacao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.validacao.ValidadorPrescricao;

public class ValidarPrescricaoUseCase {

    private final ValidadorPrescricao validador;

    public ValidarPrescricaoUseCase(ValidadorPrescricao validador) {
        if (validador == null) throw new IllegalArgumentException("ValidadorPrescricao é obrigatório.");
        this.validador = validador;
    }

    public ResultadoValidacao executar(Entrada entrada) {
        if (entrada == null) throw new IllegalArgumentException("Entrada é obrigatória.");
        return validador.validar(entrada.pesoPacienteKg, entrada.tags, entrada.alergias, entrada.itens);
    }

    public record Entrada(
            BigDecimal pesoPacienteKg,
            Set<TagClinica> tags,
            Set<String> alergias,
            List<RascunhoItem> itens
    ) {}
}
