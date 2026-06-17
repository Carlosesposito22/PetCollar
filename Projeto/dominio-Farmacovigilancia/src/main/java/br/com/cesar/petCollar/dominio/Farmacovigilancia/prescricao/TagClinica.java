package br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao;

public enum TagClinica {
    GERIATRICO("Geriátrico"),
    INSUFICIENCIA_RENAL("Insuficiência Renal"),
    INSUFICIENCIA_HEPATICA("Insuficiência Hepática"),
    CARDIOPATA("Cardiopata");

    private final String rotulo;

    TagClinica(String rotulo) { this.rotulo = rotulo; }

    public String rotulo() { return rotulo; }

    public boolean reduzDoseMaxima() {
        return this == GERIATRICO
                || this == INSUFICIENCIA_RENAL
                || this == INSUFICIENCIA_HEPATICA;
    }
}
