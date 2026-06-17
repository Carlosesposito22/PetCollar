package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

public class UsuarioAutenticavel {

    private final String identificador;
    private final String nome;
    private final Perfil perfil;
    private final String senhaHash;
    private StatusConta status;

    private final String cpf;
    private final String telefone;
    private final String endereco;
    private final String email;
    private final String planoId;

    public UsuarioAutenticavel(String identificador, String nome, Perfil perfil,
                               String senhaHash, StatusConta status) {
        this(identificador, nome, perfil, senhaHash, status, null, null, null, null, null);
    }

    public UsuarioAutenticavel(String identificador, String nome, Perfil perfil,
                               String senhaHash, StatusConta status,
                               String cpf, String telefone, String endereco, String email) {
        this(identificador, nome, perfil, senhaHash, status, cpf, telefone, endereco, email, null);
    }

    public UsuarioAutenticavel(String identificador, String nome, Perfil perfil,
                               String senhaHash, StatusConta status,
                               String cpf, String telefone, String endereco, String email,
                               String planoId) {
        this.identificador = identificador;
        this.nome = nome;
        this.perfil = perfil;
        this.senhaHash = senhaHash;
        this.status = status;
        this.cpf = cpf;
        this.telefone = telefone;
        this.endereco = endereco;
        this.email = email;
        this.planoId = planoId;
    }

    public String identificador()  { return identificador; }
    public String nome()           { return nome; }
    public Perfil perfil()         { return perfil; }
    public String senhaHash()      { return senhaHash; }
    public StatusConta status()    { return status; }
    public String cpf()            { return cpf; }
    public String telefone()       { return telefone; }
    public String endereco()       { return endereco; }
    public String email()          { return email; }
    public String planoId()        { return planoId; }

    public void mudarStatus(StatusConta novo) {
        this.status = novo;
    }
}
