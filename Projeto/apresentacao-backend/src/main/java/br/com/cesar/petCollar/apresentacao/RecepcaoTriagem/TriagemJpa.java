package br.com.cesar.petCollar.apresentacao.RecepcaoTriagem;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "triagens_recepcao")
public class TriagemJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String pacienteId;

    @Column(nullable = false)
    private String tutorId;

    private int scoreTotal;
    private String corDeRisco;
    private String status;

    @Column(length = 2000)
    private String sintomasSelecionados;

    private LocalDateTime criadoEm;
    private LocalDateTime finalizadaEm;
    private String responsavelId;

    protected TriagemJpa() {}

    public TriagemJpa(String id, String pacienteId, String tutorId, String responsavelId) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.tutorId = tutorId;
        this.responsavelId = responsavelId;
        this.status = "EM_ELABORACAO";
        this.scoreTotal = 0;
        this.criadoEm = LocalDateTime.now();
        this.sintomasSelecionados = "";
    }

    public String getId()              { return id; }
    public String getPacienteId()      { return pacienteId; }
    public String getTutorId()         { return tutorId; }
    public int getScoreTotal()         { return scoreTotal; }
    public String getCorDeRisco()      { return corDeRisco; }
    public String getStatus()          { return status; }
    public String getSintomasSelecionados() { return sintomasSelecionados; }
    public LocalDateTime getCriadoEm()     { return criadoEm; }
    public LocalDateTime getFinalizadaEm() { return finalizadaEm; }
    public String getResponsavelId()       { return responsavelId; }

    public void setScoreTotal(int score)          { this.scoreTotal = score; }
    public void setCorDeRisco(String cor)         { this.corDeRisco = cor; }
    public void setStatus(String status)          { this.status = status; }
    public void setSintomasSelecionados(String s) { this.sintomasSelecionados = s; }
    public void setFinalizadaEm(LocalDateTime t)  { this.finalizadaEm = t; }
}