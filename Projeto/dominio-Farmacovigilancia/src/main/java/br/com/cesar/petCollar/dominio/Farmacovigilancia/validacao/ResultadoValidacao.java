package br.com.cesar.petCollar.dominio.Farmacovigilancia.validacao;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.MedicamentoId;

/**
 * Resultado consolidado da validação cruzada da prescrição: as violações
 * detectadas e o detalhe por item (dose máxima segura calculada e dose
 * total proposta). A UI usa o detalhe pra mostrar pro médico o que cada
 * camada do Decorator fez.
 */
public record ResultadoValidacao(
        List<Violacao> violacoes,
        Map<MedicamentoId, DetalheItem> detalhePorItem
) {

    public ResultadoValidacao {
        violacoes = violacoes == null ? List.of() : Collections.unmodifiableList(List.copyOf(violacoes));
        detalhePorItem = detalhePorItem == null ? Map.of() : Map.copyOf(detalhePorItem);
    }

    public boolean podeFinalizar() {
        return violacoes.stream().noneMatch(v -> v.nivel() == Violacao.Nivel.BLOQUEIO);
    }

    public record DetalheItem(
            BigDecimal doseMaximaSeguraCalculada,
            BigDecimal doseTotalPropostaMg,
            BigDecimal volumeFinalMl,
            boolean tagAplicada,
            boolean alergiaAplicada
    ) {}
}
