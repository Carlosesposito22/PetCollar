package petcollar.dominio.assinaturafaturamento.indicacao;

import java.time.LocalDateTime;

public class IndicacaoConfirmadaEvent {

    private final ConversaoId conversaoId;
    private final String tutorIndicadorId;
    private final String cpfIndicado;
    private final LocalDateTime ocorridoEm;

    public IndicacaoConfirmadaEvent(ConversaoId conversaoId, String tutorIndicadorId,
                                    String cpfIndicado) {
        if (conversaoId == null)
            throw new IllegalArgumentException("ConversaoId não pode ser nulo.");
        if (tutorIndicadorId == null || tutorIndicadorId.isBlank())
            throw new IllegalArgumentException("tutorIndicadorId não pode ser vazio.");
        if (cpfIndicado == null || cpfIndicado.isBlank())
            throw new IllegalArgumentException("cpfIndicado não pode ser vazio.");
        this.conversaoId = conversaoId;
        this.tutorIndicadorId = tutorIndicadorId;
        this.cpfIndicado = cpfIndicado;
        this.ocorridoEm = LocalDateTime.now();
    }

    public ConversaoId getConversaoId()     { return conversaoId; }
    public String getTutorIndicadorId()     { return tutorIndicadorId; }
    public String getCpfIndicado()          { return cpfIndicado; }
    public LocalDateTime getOcorridoEm()   { return ocorridoEm; }
}
