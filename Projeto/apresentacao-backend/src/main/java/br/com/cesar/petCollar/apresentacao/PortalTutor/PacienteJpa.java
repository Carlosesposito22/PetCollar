package br.com.cesar.petCollar.apresentacao.PortalTutor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "pacientes")
public class PacienteJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String tutorId;

    @Column(nullable = false)
    private String nome;

    private String especie;
    private String raca;
    private LocalDate nascimento;

    protected PacienteJpa() {}

    public static PacienteJpa fromDomain(Paciente p) {
        PacienteJpa jpa = new PacienteJpa();
        jpa.id        = p.id();
        jpa.tutorId   = p.tutorId();
        jpa.nome      = p.nome();
        jpa.especie   = p.especie();
        jpa.raca      = p.raca();
        jpa.nascimento = p.nascimento();
        return jpa;
    }

    public Paciente toDomain() {
        return new Paciente(id, tutorId, nome, especie, raca, nascimento);
    }

    public String getId()     { return id; }
    public String getTutorId(){ return tutorId; }
}
