package br.com.cesar.petCollar.apresentacao.PortalTutor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public class Paciente {

    private final String id;
    private final String tutorId;
    private String nome;
    private String especie;
    private String raca;
    private LocalDate nascimento;
    private Double pesoKg;
    private String sexo;
    private boolean infectocontagiosoRecente;
    private LocalDateTime dataUltimoDiagnostico;

    public Paciente(String id, String tutorId, String nome, String especie,
                    String raca, LocalDate nascimento, Double pesoKg, String sexo) {
        this.id = id;
        this.tutorId = tutorId;
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.nascimento = nascimento;
        this.pesoKg = pesoKg;
        this.sexo = sexo;
    }

    public Paciente(String id, String tutorId, String nome, String especie,
                    String raca, LocalDate nascimento, Double pesoKg, String sexo,
                    boolean infectocontagiosoRecente, LocalDateTime dataUltimoDiagnostico) {
        this(id, tutorId, nome, especie, raca, nascimento, pesoKg, sexo);
        this.infectocontagiosoRecente = infectocontagiosoRecente;
        this.dataUltimoDiagnostico = dataUltimoDiagnostico;
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
    public Double pesoKg()        { return pesoKg; }
    public String sexo()          { return sexo; }
    public boolean infectocontagiosoRecente()  { return infectocontagiosoRecente; }
    public LocalDateTime dataUltimoDiagnostico() { return dataUltimoDiagnostico; }

    public void atualizar(String nome, String especie, String raca, LocalDate nascimento,
                          Double pesoKg, String sexo) {
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.nascimento = nascimento;
        this.pesoKg = pesoKg;
        this.sexo = sexo;
    }

    public void marcarInfectocontagioso(boolean valor, LocalDateTime data) {
        this.infectocontagiosoRecente = valor;
        this.dataUltimoDiagnostico = data;
    }
}
