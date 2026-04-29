package petcollar.dominio.recepcaotriagem.triagem;

import java.util.Objects;

public final class PesoTotal {

    private final int valor;
    private final int limiteVermelho;
    private final int limiteAmarelo;

    public PesoTotal(int valor, int limiteVermelho, int limiteAmarelo) {
        if (limiteAmarelo >= limiteVermelho)
            throw new IllegalArgumentException(
                "Limite amarelo deve ser menor que o limite vermelho.");
        if (valor < 0)
            throw new IllegalArgumentException("Valor do peso total não pode ser negativo.");
        this.valor = valor;
        this.limiteVermelho = limiteVermelho;
        this.limiteAmarelo = limiteAmarelo;
    }

    public CorDeRisco getCorDeRisco() {
        if (valor >= limiteVermelho) return CorDeRisco.VERMELHO;
        if (valor >= limiteAmarelo) return CorDeRisco.AMARELO;
        return CorDeRisco.VERDE;
    }

    public int getValor()          { return valor; }
    public int getLimiteVermelho() { return limiteVermelho; }
    public int getLimiteAmarelo()  { return limiteAmarelo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PesoTotal)) return false;
        PesoTotal other = (PesoTotal) o;
        return valor == other.valor
            && limiteVermelho == other.limiteVermelho
            && limiteAmarelo == other.limiteAmarelo;
    }

    @Override
    public int hashCode() { return Objects.hash(valor, limiteVermelho, limiteAmarelo); }
}
