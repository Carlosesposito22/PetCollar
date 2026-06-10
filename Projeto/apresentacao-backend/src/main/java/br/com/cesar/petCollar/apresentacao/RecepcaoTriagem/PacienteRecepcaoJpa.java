package br.com.cesar.petCollar.apresentacao.RecepcaoTriagem;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pacientes_recepcao")
public class PacienteRecepcaoJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String tutorId;

    @Column(nullable = false)
    private String nome;

    private String especie;
    private String raca;
    private LocalDate nascimento;
    private Double pesoKg;
    private String sexo;                 // MACHO | FEMEA (pode ser nulo)
    private boolean infectocontagiosoRecente;
    private LocalDateTime dataUltimoDiagnostico;

    protected PacienteRecepcaoJpa() {}

    public PacienteRecepcaoJpa(String id, String tutorId, String nome,
                                String especie, String raca, LocalDate nascimento,
                                Double pesoKg, String sexo) {
        this.id = id;
        this.tutorId = tutorId;
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.nascimento = nascimento;
        this.pesoKg = pesoKg;
        this.sexo = sexo;
        this.infectocontagiosoRecente = false;
    }

    public String getId()          { return id; }
    public String getTutorId()     { return tutorId; }
    public String getNome()        { return nome; }
    public String getEspecie()     { return especie; }
    public String getRaca()        { return raca; }
    public LocalDate getNascimento()             { return nascimento; }
    public Double getPesoKg()      { return pesoKg; }
    public String getSexo()        { return sexo; }
    public boolean isInfectocontagiosoRecente()  { return infectocontagiosoRecente; }
    public LocalDateTime getDataUltimoDiagnostico() { return dataUltimoDiagnostico; }

    public void setNome(String nome)                { this.nome = nome; }
    public void setEspecie(String especie)          { this.especie = especie; }
    public void setRaca(String raca)                { this.raca = raca; }
    public void setNascimento(LocalDate nascimento) { this.nascimento = nascimento; }
    public void setPesoKg(Double pesoKg)            { this.pesoKg = pesoKg; }
    public void setSexo(String sexo)                { this.sexo = sexo; }
    public void setInfectocontagiosoRecente(boolean v, LocalDateTime data) {
        this.infectocontagiosoRecente = v;
        this.dataUltimoDiagnostico = data;
    }
}