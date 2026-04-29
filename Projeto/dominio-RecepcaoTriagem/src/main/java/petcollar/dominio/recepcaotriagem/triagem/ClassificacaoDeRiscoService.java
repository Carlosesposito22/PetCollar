package petcollar.dominio.recepcaotriagem.triagem;

import java.util.List;

public class ClassificacaoDeRiscoService {

    public PesoTotal calcular(List<RespostaSintoma> respostas, int limiteVermelho, int limiteAmarelo) {
        if (respostas == null)
            throw new IllegalArgumentException("Lista de respostas não pode ser nula.");
        int total = respostas.stream()
                .filter(RespostaSintoma::isPresente)
                .mapToInt(r -> r.getSintoma().getPeso())
                .sum();
        return new PesoTotal(total, limiteVermelho, limiteAmarelo);
    }
}
