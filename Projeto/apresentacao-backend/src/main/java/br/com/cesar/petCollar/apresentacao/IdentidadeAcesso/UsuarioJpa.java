package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class UsuarioJpa {

    @Id
    @Column(nullable = false)
    private String identificador;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String perfil;

    @Column(nullable = false)
    private String senhaHash;

    @Column(nullable = false)
    private String status;

    private String cpf;
    private String telefone;
    private String endereco;
    private String email;
    private String planoId;

    protected UsuarioJpa() {}

    public static UsuarioJpa fromDomain(UsuarioAutenticavel u) {
        UsuarioJpa jpa = new UsuarioJpa();
        jpa.identificador = u.identificador();
        jpa.nome          = u.nome();
        jpa.perfil        = u.perfil().name();
        jpa.senhaHash     = u.senhaHash();
        jpa.status        = u.status().name();
        jpa.cpf           = u.cpf();
        jpa.telefone      = u.telefone();
        jpa.endereco      = u.endereco();
        jpa.email         = u.email();
        jpa.planoId       = u.planoId();
        return jpa;
    }

    public UsuarioAutenticavel toDomain() {
        return new UsuarioAutenticavel(
                identificador, nome,
                Perfil.valueOf(perfil),
                senhaHash,
                StatusConta.valueOf(status),
                cpf, telefone, endereco, email, planoId);
    }

    public String getIdentificador() { return identificador; }
    public String getPerfil()        { return perfil; }
}
