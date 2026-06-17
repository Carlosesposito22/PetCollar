package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import java.time.LocalDateTime;

public final class ResumoAtendimento {

    private final AtendimentoId atendimentoId;
    private final PacienteId pacienteId;
    private final TutorId tutorPrincipalId;
    private final LocalDateTime ultimaInteracaoTutorEm;
    private final boolean emAndamento;
    private final String nomePaciente;

    public ResumoAtendimento(AtendimentoId atendimentoId, PacienteId pacienteId,
                             TutorId tutorPrincipalId, LocalDateTime ultimaInteracaoTutorEm,
                             boolean emAndamento) {
        this(atendimentoId, pacienteId, tutorPrincipalId, ultimaInteracaoTutorEm, emAndamento, null);
    }

    public ResumoAtendimento(AtendimentoId atendimentoId, PacienteId pacienteId,
                             TutorId tutorPrincipalId, LocalDateTime ultimaInteracaoTutorEm,
                             boolean emAndamento, String nomePaciente) {
        if (atendimentoId == null)
            throw new IllegalArgumentException("Id do atendimento não pode ser nulo.");
        if (pacienteId == null)
            throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
        if (tutorPrincipalId == null)
            throw new IllegalArgumentException("Id do tutor principal não pode ser nulo.");
        if (ultimaInteracaoTutorEm == null)
            throw new IllegalArgumentException("Instante da última interação do tutor não pode ser nulo.");
        this.atendimentoId = atendimentoId;
        this.pacienteId = pacienteId;
        this.tutorPrincipalId = tutorPrincipalId;
        this.ultimaInteracaoTutorEm = ultimaInteracaoTutorEm;
        this.emAndamento = emAndamento;
        this.nomePaciente = nomePaciente;
    }

    public AtendimentoId getAtendimentoId()         { return atendimentoId; }
    public PacienteId getPacienteId()               { return pacienteId; }
    public TutorId getTutorPrincipalId()            { return tutorPrincipalId; }
    public LocalDateTime getUltimaInteracaoTutorEm(){ return ultimaInteracaoTutorEm; }
    public boolean isEmAndamento()                  { return emAndamento; }
    public String getNomePaciente()                 { return nomePaciente; }
}
