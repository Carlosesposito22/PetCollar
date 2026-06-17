package petcollar.dominio.recepcaotriagem.prontuario;

import java.time.LocalDateTime;

public class ResultadoBusca {

    private final ResultadoBuscaId id;
    private final CPF cpf;
    private Tutor tutor;
    private boolean alertaEpidemiologicoGlobal;
    private final LocalDateTime criadoEm;

    public ResultadoBusca(ResultadoBuscaId id, CPF cpf) {
        if (id == null)
            throw new IllegalArgumentException("Id do resultado de busca não pode ser nulo.");
        if (cpf == null)
            throw new IllegalArgumentException("CPF do resultado de busca não pode ser nulo.");
        this.id = id;
        this.cpf = cpf;
        this.alertaEpidemiologicoGlobal = false;
        this.criadoEm = LocalDateTime.now();
    }

    public ResultadoBusca(ResultadoBuscaId id, CPF cpf, Tutor tutor,
                          boolean alertaEpidemiologicoGlobal, LocalDateTime criadoEm) {
        this.id = id;
        this.cpf = cpf;
        this.tutor = tutor;
        this.alertaEpidemiologicoGlobal = alertaEpidemiologicoGlobal;
        this.criadoEm = criadoEm;
    }

    public void associarTutor(Tutor tutor) {
        if (tutor == null)
            throw new IllegalArgumentException("Tutor não pode ser nulo ao associar ao resultado.");
        this.tutor = tutor;
    }

    public void ativarAlertaEpidemiologico() {
        this.alertaEpidemiologicoGlobal = true;
    }

    public boolean isTutorEncontrado() {
        return tutor != null;
    }

    public ResultadoBuscaId getId()              { return id; }
    public CPF getCpf()                          { return cpf; }
    public Tutor getTutor()                      { return tutor; }
    public boolean isAlertaEpidemiologicoGlobal() { return alertaEpidemiologicoGlobal; }
    public LocalDateTime getCriadoEm()           { return criadoEm; }
}
