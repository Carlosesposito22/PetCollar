package petcollar.dominio.recepcaotriagem.paciente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Paciente {

    private final PacienteId id;
    private final String nome;
    private final Especie especie;
    private final String raca;
    private final LocalDate dataNascimento;
    private final List<TagVisual> tags;
    private QueixaComportamento queixa;
    private final LocalDateTime criadoEm;

    // Construtor de CRIAÇÃO
    public Paciente(PacienteId id, String nome, Especie especie, String raca, LocalDate dataNascimento) {
        if (id == null)
            throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do paciente não pode ser vazio.");
        if (especie == null)
            throw new IllegalArgumentException("Espécie do paciente não pode ser nula.");
        if (raca == null || raca.isBlank())
            throw new IllegalArgumentException("Raça do paciente não pode ser vazia.");
        if (dataNascimento == null)
            throw new IllegalArgumentException("Data de nascimento do paciente não pode ser nula.");
        this.id = id;
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.dataNascimento = dataNascimento;
        this.tags = new ArrayList<>();
        this.criadoEm = LocalDateTime.now();
    }

    // Construtor de RECONSTRUÇÃO
    public Paciente(PacienteId id, String nome, Especie especie, String raca,
                    LocalDate dataNascimento, List<TagVisual> tags,
                    QueixaComportamento queixa, LocalDateTime criadoEm) {
        this.id = id;
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.dataNascimento = dataNascimento;
        this.tags = new ArrayList<>(tags);
        this.queixa = queixa;
        this.criadoEm = criadoEm;
    }

    public void adicionarTag(TagVisual tag) {
        if (tag == null)
            throw new IllegalArgumentException("Tag não pode ser nula.");
        boolean jaExiste = tags.stream().anyMatch(t -> t.getCodigo().equals(tag.getCodigo()));
        if (!jaExiste) {
            tags.add(tag);
        }
    }

    public void registrarQueixa(QueixaComportamento queixa) {
        if (queixa == null)
            throw new IllegalArgumentException("Queixa de comportamento não pode ser nula.");
        this.queixa = queixa;
    }

    public PacienteId getId()                   { return id; }
    public String getNome()                     { return nome; }
    public Especie getEspecie()                 { return especie; }
    public String getRaca()                     { return raca; }
    public LocalDate getDataNascimento()        { return dataNascimento; }
    public List<TagVisual> getTags()            { return Collections.unmodifiableList(tags); }
    public QueixaComportamento getQueixa()      { return queixa; }
    public LocalDateTime getCriadoEm()          { return criadoEm; }
}
