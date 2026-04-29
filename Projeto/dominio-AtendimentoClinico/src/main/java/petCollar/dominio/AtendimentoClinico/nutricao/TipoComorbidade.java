package petCollar.dominio.AtendimentoClinico.nutricao;

public enum TipoComorbidade {
    NENHUMA(1.0),
    GESTACAO(1.5),
    NEFROPATIA(0.85),
    HEPATOPATIA(0.9),
    DIABETES(0.9),
    OBESIDADE(0.8),
    CARDIOPATIA(0.7);

    private final double modificador;

    TipoComorbidade(double modificador) {
        this.modificador = modificador;
    }

    public double getModificador() {
        return modificador;
    }
}
