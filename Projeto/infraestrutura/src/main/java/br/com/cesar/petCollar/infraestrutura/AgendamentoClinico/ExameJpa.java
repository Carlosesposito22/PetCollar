package br.com.cesar.petCollar.infraestrutura.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.ExameResumo;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.StatusExame;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidade JPA dos exames vinculados a uma consulta de origem (read-model da porta
 * {@link br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaExame}).
 * Persistido como tabela própria ({@code exames}); o status/enum é gravado como String.
 */
@Entity
@Table(name = "exames")
public class ExameJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String consultaOrigemId;   // referência cross-agregado (String do ConsultaId)

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private String status;             // StatusExame.name()

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

    /** Conclui o exame (idempotente é responsabilidade do adapter). */
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
