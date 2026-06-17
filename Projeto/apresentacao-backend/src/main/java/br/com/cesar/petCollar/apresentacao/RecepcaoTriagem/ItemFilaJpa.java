package br.com.cesar.petCollar.apresentacao.RecepcaoTriagem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidade JPA da fila de atendimento operacional. A chave primária é o
 * triagemId, que identifica unicamente a entrada na fila. Campos de
 * encaminhamento (medicoId/nomeMedico) são nulos até a recepcionista chamar
 * o paciente.
 */
@Entity
@Table(name = "fila_atendimento")
public class ItemFilaJpa {

    @Id
    private String triagemId;

    @Column(nullable = false)
    private String pacienteId;

    @Column(nullable = false)
    private String corDeRisco;

    @Column(nullable = false)
    private LocalDateTime finalizadaEm;

    private String nomePaciente;
    private String tutorId;
    private String medicoId;
    private String nomeMedico;

    @Column(nullable = false)
    private boolean aplicacaoVacina;

    protected ItemFilaJpa() {}

    ItemFilaJpa(String triagemId, String pacienteId, String corDeRisco,
                LocalDateTime finalizadaEm, String nomePaciente, String tutorId,
                String medicoId, String nomeMedico, boolean aplicacaoVacina) {
        this.triagemId       = triagemId;
        this.pacienteId      = pacienteId;
        this.corDeRisco      = corDeRisco;
        this.finalizadaEm    = finalizadaEm;
        this.nomePaciente    = nomePaciente;
        this.tutorId         = tutorId;
        this.medicoId        = medicoId;
        this.nomeMedico      = nomeMedico;
        this.aplicacaoVacina = aplicacaoVacina;
    }

    String getTriagemId()          { return triagemId; }
    String getPacienteId()         { return pacienteId; }
    String getCorDeRisco()         { return corDeRisco; }
    LocalDateTime getFinalizadaEm(){ return finalizadaEm; }
    String getNomePaciente()       { return nomePaciente; }
    String getTutorId()            { return tutorId; }
    String getMedicoId()           { return medicoId; }
    String getNomeMedico()         { return nomeMedico; }
    boolean isAplicacaoVacina()    { return aplicacaoVacina; }

    void setMedicoId(String medicoId)     { this.medicoId = medicoId; }
    void setNomeMedico(String nomeMedico) { this.nomeMedico = nomeMedico; }
}
