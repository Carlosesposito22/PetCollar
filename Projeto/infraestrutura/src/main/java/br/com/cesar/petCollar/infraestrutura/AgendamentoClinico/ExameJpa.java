package br.com.cesar.petCollar.infraestrutura.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.ExameResumo;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.StatusExame;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "exames")
public class ExameJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String consultaOrigemId;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String laudo;

    protected ExameJpa() {}

    public ExameJpa(String id, String consultaOrigemId, String descricao, StatusExame status) {
        this.id = id;
        this.consultaOrigemId = consultaOrigemId;
        this.descricao = descricao;
        this.status = status.name();
    }

    public ExameResumo toResumo() {
        return new ExameResumo(id, descricao, StatusExame.valueOf(status));
    }

    public void concluir(String laudo) {
        this.status = StatusExame.CONCLUIDO.name();
        if (laudo != null) {
            this.laudo = laudo;
        }
    }

    public String getId()              { return id; }
    public String getConsultaOrigemId(){ return consultaOrigemId; }
    public String getStatus()          { return status; }
    public String getLaudo()           { return laudo; }
}
