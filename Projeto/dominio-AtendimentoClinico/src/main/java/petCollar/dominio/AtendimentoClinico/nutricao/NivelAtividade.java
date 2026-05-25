package petCollar.dominio.AtendimentoClinico.nutricao;

public enum NivelAtividade {
    SEDENTARIO(1.2),
    POUCO_ATIVO(1.4),
    MODERADAMENTE_ATIVO(1.6),
    MUITO_ATIVO(1.8),
    ATLETA(2.0);

    private final double fator;

    NivelAtividade(double fator) {
        this.fator = fator;
    }

    public double getFator() {
        return fator;
    }
}
