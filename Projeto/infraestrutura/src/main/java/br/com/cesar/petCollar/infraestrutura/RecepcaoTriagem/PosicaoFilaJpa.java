package br.com.cesar.petCollar.infraestrutura.RecepcaoTriagem;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import petcollar.dominio.recepcaotriagem.triagem.CorDeRisco;
import petcollar.dominio.recepcaotriagem.triagem.PosicaoFila;
import petcollar.dominio.recepcaotriagem.triagem.TriagemId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidade JPA de {@link PosicaoFila}. A chave primária é o valor de
 * {@link TriagemId} (String). Enums e VOs de Id persistidos como String (§6.1).
 */
@Entity
@Table(name = "posicoes_fila")
public class PosicaoFilaJpa {

    @Id
    private String triagemId;

    @Column(nullable = false)
    private String pacienteId;

    @Column(nullable = false)
    private String corDeRisco;   // CorDeRisco.name()

    @Column(nullable = false)
    private LocalDateTime finalizadaEm;

    protected PosicaoFilaJpa() {}

    public static PosicaoFilaJpa fromDomain(PosicaoFila p) {
        PosicaoFilaJpa jpa = new PosicaoFilaJpa();
        jpa.triagemId    = p.getTriagemId().getValor();
        jpa.pacienteId   = p.getPacienteId().getValor();
        jpa.corDeRisco   = p.getCorDeRisco().name();
        jpa.finalizadaEm = p.getFinalizadaEm();
        return jpa;
    }

    public PosicaoFila toDomain() {
        return new PosicaoFila(
                PacienteId.de(pacienteId),
                TriagemId.de(triagemId),
                CorDeRisco.valueOf(corDeRisco),
                finalizadaEm);
    }
}
