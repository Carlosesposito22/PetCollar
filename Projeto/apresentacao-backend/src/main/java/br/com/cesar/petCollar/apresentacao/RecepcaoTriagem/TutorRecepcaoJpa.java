package br.com.cesar.petCollar.apresentacao.RecepcaoTriagem;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tutores_recepcao")
public class TutorRecepcaoJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String cpf;

    private String telefone;
    private String email;
    private LocalDateTime criadoEm;

    protected TutorRecepcaoJpa() {}

    public TutorRecepcaoJpa(String id, String nome, String cpf,
                             String telefone, String email, LocalDateTime criadoEm) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
        this.criadoEm = criadoEm;
    }

    public String getId()              { return id; }
    public String getNome()            { return nome; }
    public String getCpf()             { return cpf; }
    public String getTelefone()        { return telefone; }
    public String getEmail()           { return email; }
    public LocalDateTime getCriadoEm() { return criadoEm; }

    public void setNome(String nome)         { this.nome = nome; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public void setEmail(String email)       { this.email = email; }
}
