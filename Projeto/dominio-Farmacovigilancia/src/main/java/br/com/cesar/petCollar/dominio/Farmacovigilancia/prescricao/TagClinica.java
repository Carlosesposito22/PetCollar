package br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao;

/**
 * Tags clínicas que ativam o redutor automático de 25% no teto de dosagem
 * (RN 5 — F-12). São derivadas dos dados existentes do paciente:
 * <ul>
 *   <li>{@link #GERIATRICO}: idade &gt; 7 anos.</li>
 *   <li>{@link #INSUFICIENCIA_RENAL}: comorbidade DOENCA_RENAL no plano nutricional vigente.</li>
 *   <li>{@link #INSUFICIENCIA_HEPATICA}: tag marcada manualmente em consultas futuras.</li>
 *   <li>{@link #CARDIOPATA}: idem.</li>
 * </ul>
 */
public enum TagClinica {
    GERIATRICO("Geriátrico"),
    INSUFICIENCIA_RENAL("Insuficiência Renal"),
    INSUFICIENCIA_HEPATICA("Insuficiência Hepática"),
    CARDIOPATA("Cardiopata");

    private final String rotulo;

    TagClinica(String rotulo) { this.rotulo = rotulo; }

    public String rotulo() { return rotulo; }

    /** Tags que ativam o redutor de 25% sobre a dose máxima. */
    public boolean reduzDoseMaxima() {
        return this == GERIATRICO
                || this == INSUFICIENCIA_RENAL
                || this == INSUFICIENCIA_HEPATICA;
    }
}
