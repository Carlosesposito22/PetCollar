package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import java.time.LocalDateTime;

/**
 * Projeção (read-model) de um atendimento clínico em andamento, exposta pela porta
 * {@link IConsultaAtendimento}. Traz o necessário para a ativação por timeout
 * (RN 1): paciente, tutor responsável e o instante da última interação do tutor.
 *
 * <p>É deliberadamente <b>somente leitura</b>: o protocolo nunca altera o
 * atendimento clínico, preservando sua continuidade (RN 8).
 */
public final class ResumoAtendimento {

    private final AtendimentoId atendimentoId;
    private final PacienteId pacienteId;
    private final TutorId tutorPrincipalId;
    private final LocalDateTime ultimaInteracaoTutorEm;
    private final boolean emAndamento;

    public ResumoAtendimento(AtendimentoId atendimentoId, PacienteId pacienteId,
                             TutorId tutorPrincipalId, LocalDateTime ultimaInteracaoTutorEm,
                             boolean emAndamento) {
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
    }

    public AtendimentoId getAtendimentoId()         { return atendimentoId; }
    public PacienteId getPacienteId()               { return pacienteId; }
    public TutorId getTutorPrincipalId()            { return tutorPrincipalId; }
    public LocalDateTime getUltimaInteracaoTutorEm(){ return ultimaInteracaoTutorEm; }
    public boolean isEmAndamento()                  { return emAndamento; }
}
