package br.com.cesar.petCollar.apresentacao.PortalTutor;

import java.time.LocalDate;
import java.time.Period;

public class Paciente {

    private final String id;
    private final String tutorId;
    private String nome;
    private String especie;
    private String raca;
    private LocalDate nascimento;

    public Paciente(String id, String tutorId, String nome, String especie,
                    String raca, LocalDate nascimento) {
        this.id = id;
        this.tutorId = tutorId;
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.nascimento = nascimento;
    }

    public int idadeEmAnos() {
        if (nascimento == null) return 0;
        return Period.between(nascimento, LocalDate.now()).getYears();
    }

    public String id()            { return id; }
    public String tutorId()       { return tutorId; }
    public String nome()          { return nome; }
    public String especie()       { return especie; }
    public String raca()          { return raca; }
    public LocalDate nascimento() { return nascimento; }

    public void atualizar(String nome, String especie, String raca, LocalDate nascimento) {
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.nascimento = nascimento;
    }
}
