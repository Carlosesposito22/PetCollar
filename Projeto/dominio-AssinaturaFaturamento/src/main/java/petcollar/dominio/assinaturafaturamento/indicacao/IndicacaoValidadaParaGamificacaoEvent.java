package petcollar.dominio.assinaturafaturamento.indicacao;

import java.time.LocalDateTime;

public class IndicacaoValidadaParaGamificacaoEvent {

    private final RecompensaIndicacaoId recompensaId;
    private final String tutorIndicadorId;
    private final String chaveEvento;
    private final LocalDateTime ocorridoEm;

    public IndicacaoValidadaParaGamificacaoEvent(RecompensaIndicacaoId recompensaId,
                                                 String tutorIndicadorId,
                                                 String chaveEvento) {
        if (recompensaId == null)
            throw new IllegalArgumentException("RecompensaIndicacaoId não pode ser nulo.");
        if (tutorIndicadorId == null || tutorIndicadorId.isBlank())
            throw new IllegalArgumentException("tutorIndicadorId não pode ser vazio.");
        if (chaveEvento == null || chaveEvento.isBlank())
            throw new IllegalArgumentException("chaveEvento não pode ser vazia.");
        this.recompensaId = recompensaId;
        this.tutorIndicadorId = tutorIndicadorId;
        this.chaveEvento = chaveEvento;
        this.ocorridoEm = LocalDateTime.now();
    }

    public RecompensaIndicacaoId getRecompensaId()  { return recompensaId; }
    public String getTutorIndicadorId()             { return tutorIndicadorId; }
    public String getChaveEvento()                  { return chaveEvento; }
    public LocalDateTime getOcorridoEm()            { return ocorridoEm; }
}
