package petcollar.dominio.recepcaotriagem.triagem;

import petcollar.dominio.recepcaotriagem.paciente.PacienteId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Triagem {

    private final TriagemId id;
    private final PacienteId pacienteId;
    private final TriagemId triagemAnteriorId;
    private final List<RespostaSintoma> respostas;
    private CorDeRisco corDeRisco;
    private StatusTriagem status;
    private final LocalDateTime criadoEm;
    private LocalDateTime finalizadaEm;

    // Construtor de CRIAÇÃO — nova triagem
    public Triagem(TriagemId id, PacienteId pacienteId) {
        this(id, pacienteId, null);
    }

    // Construtor de CRIAÇÃO — triagem de correção (referencia a anterior)
    public Triagem(TriagemId id, PacienteId pacienteId, TriagemId triagemAnteriorId) {
        if (id == null)
            throw new IllegalArgumentException("Id da triagem não pode ser nulo.");
        if (pacienteId == null)
            throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
        this.id = id;
        this.pacienteId = pacienteId;
        this.triagemAnteriorId = triagemAnteriorId;
        this.respostas = new ArrayList<>();
        this.status = StatusTriagem.EM_ELABORACAO;
        this.criadoEm = LocalDateTime.now();
    }

    // Construtor de RECONSTRUÇÃO — todos os campos
    public Triagem(TriagemId id, PacienteId pacienteId, TriagemId triagemAnteriorId,
                   List<RespostaSintoma> respostas, CorDeRisco corDeRisco,
                   StatusTriagem status, LocalDateTime criadoEm, LocalDateTime finalizadaEm) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.triagemAnteriorId = triagemAnteriorId;
        this.respostas = new ArrayList<>(respostas);
        this.corDeRisco = corDeRisco;
        this.status = status;
        this.criadoEm = criadoEm;
        this.finalizadaEm = finalizadaEm;
    }

    public void adicionarResposta(RespostaSintoma resposta) {
        if (resposta == null)
            throw new IllegalArgumentException("Resposta de sintoma não pode ser nula.");
        if (this.status != StatusTriagem.EM_ELABORACAO)
            throw new IllegalStateException(
                "Só é possível adicionar respostas em triagens com status EM_ELABORACAO.");
        this.respostas.add(resposta);
    }

    public void definirCorDeRisco(CorDeRisco corDeRisco) {
        if (corDeRisco == null)
            throw new IllegalArgumentException("Cor de risco não pode ser nula.");
        if (this.status != StatusTriagem.EM_ELABORACAO)
            throw new IllegalStateException(
                "Só é possível definir a cor de risco em triagens com status EM_ELABORACAO.");
        this.corDeRisco = corDeRisco;
    }

    public void finalizar() {
        if (this.status != StatusTriagem.EM_ELABORACAO)
            throw new IllegalStateException(
                "Só é possível finalizar triagens com status EM_ELABORACAO.");
        this.status = StatusTriagem.FINALIZADA;
        this.finalizadaEm = LocalDateTime.now();
    }

    public void bloquear() {
        if (this.status != StatusTriagem.FINALIZADA)
            throw new IllegalStateException(
                "Só é possível bloquear triagens com status FINALIZADA.");
        this.status = StatusTriagem.BLOQUEADA;
    }

    public boolean isBloqueadaParaAlteracao() {
        return this.status == StatusTriagem.FINALIZADA || this.status == StatusTriagem.BLOQUEADA;
    }

    public TriagemId getId()                        { return id; }
    public PacienteId getPacienteId()               { return pacienteId; }
    public TriagemId getTriagemAnteriorId()         { return triagemAnteriorId; }
    public List<RespostaSintoma> getRespostas()     { return Collections.unmodifiableList(respostas); }
    public CorDeRisco getCorDeRisco()               { return corDeRisco; }
    public StatusTriagem getStatus()                { return status; }
    public LocalDateTime getCriadoEm()              { return criadoEm; }
    public LocalDateTime getFinalizadaEm()          { return finalizadaEm; }
}
