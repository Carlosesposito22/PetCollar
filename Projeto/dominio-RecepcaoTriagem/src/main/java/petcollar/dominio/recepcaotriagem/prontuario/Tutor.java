package petcollar.dominio.recepcaotriagem.prontuario;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tutor {

    private final TutorId id;
    private final String nome;
    private final CPF cpf;
    private final String telefone;
    private final String email;
    private Endereco endereco;
    private final List<PacienteId> pacientesVinculados;
    private final LocalDateTime criadoEm;

    // Construtor de CRIAÇÃO — campos essenciais, lista vazia gerada automaticamente
    public Tutor(TutorId id, String nome, CPF cpf, String telefone, String email) {
        if (id == null)
            throw new IllegalArgumentException("Id do tutor não pode ser nulo.");
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do tutor não pode ser vazio.");
        if (cpf == null)
            throw new IllegalArgumentException("CPF do tutor não pode ser nulo.");
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
        this.pacientesVinculados = new ArrayList<>();
        this.criadoEm = LocalDateTime.now();
    }

    // Construtor de RECONSTRUÇÃO — todos os campos (para carregar da persistência)
    public Tutor(TutorId id, String nome, CPF cpf, String telefone, String email,
                 Endereco endereco, List<PacienteId> pacientesVinculados,
                 LocalDateTime criadoEm) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
        this.endereco = endereco;
        this.pacientesVinculados = pacientesVinculados != null
                ? new ArrayList<>(pacientesVinculados)
                : new ArrayList<>();
        this.criadoEm = criadoEm;
    }

    public void adicionarPaciente(PacienteId pacienteId) {
        if (pacienteId == null)
            throw new IllegalArgumentException("PacienteId não pode ser nulo.");
        pacientesVinculados.add(pacienteId);
    }

    public void atualizarEndereco(Endereco novoEndereco) {
        if (novoEndereco == null)
            throw new IllegalArgumentException("Endereço não pode ser nulo.");
        this.endereco = novoEndereco;
    }

    public TutorId getId()                              { return id; }
    public String getNome()                             { return nome; }
    public CPF getCpf()                                 { return cpf; }
    public String getTelefone()                         { return telefone; }
    public String getEmail()                            { return email; }
    public Endereco getEndereco()                       { return endereco; }
    public List<PacienteId> getPacientesVinculados()    { return Collections.unmodifiableList(pacientesVinculados); }
    public LocalDateTime getCriadoEm()                  { return criadoEm; }
}
