package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

public class UsuarioAutenticavel {

    private final String identificador;
    private final String nome;
    private final Perfil perfil;
    private final String senhaHash;
    private StatusConta status;

    // Dados extra opcionais (preenchidos no fluxo de contratação)
    private final String cpf;
    private final String telefone;
    private final String endereco;
    private final String email;

    public UsuarioAutenticavel(String identificador, String nome, Perfil perfil,
                               String senhaHash, StatusConta status) {
        this(identificador, nome, perfil, senhaHash, status, null, null, null, null);
    }

    public UsuarioAutenticavel(String identificador, String nome, Perfil perfil,
                               String senhaHash, StatusConta status,
                               String cpf, String telefone, String endereco, String email) {
        this.identificador = identificador;
        this.nome = nome;
        this.perfil = perfil;
        this.senhaHash = senhaHash;
        this.status = status;
        this.cpf = cpf;
        this.telefone = telefone;
        this.endereco = endereco;
        this.email = email;
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

    public void mudarStatus(StatusConta novo) {
        this.status = novo;
    }
}
