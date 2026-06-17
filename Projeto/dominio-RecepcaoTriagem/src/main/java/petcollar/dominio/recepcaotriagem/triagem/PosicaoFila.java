package petcollar.dominio.recepcaotriagem.triagem;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import java.time.LocalDateTime;

public class PosicaoFila {

    private final PacienteId   pacienteId;
    private final TriagemId    triagemId;
    private final CorDeRisco   corDeRisco;
    private final LocalDateTime finalizadaEm;

    public PosicaoFila(PacienteId pacienteId, TriagemId triagemId,
                       CorDeRisco corDeRisco, LocalDateTime finalizadaEm) {
        if (pacienteId == null)
            throw new IllegalArgumentException("PacienteId não pode ser nulo.");
        if (triagemId == null)
            throw new IllegalArgumentException("TriagemId não pode ser nulo.");
        if (corDeRisco == null)
            throw new IllegalArgumentException("CorDeRisco não pode ser nulo.");
        if (finalizadaEm == null)
            throw new IllegalArgumentException("FinalizadaEm não pode ser nulo.");

        this.pacienteId  = pacienteId;
        this.triagemId   = triagemId;
        this.corDeRisco  = corDeRisco;
        this.finalizadaEm = finalizadaEm;
    }

    public PacienteId    getPacienteId()  { return pacienteId; }
    public TriagemId     getTriagemId()   { return triagemId; }
    public CorDeRisco    getCorDeRisco()  { return corDeRisco; }
    public LocalDateTime getFinalizadaEm(){ return finalizadaEm; }
}