package petcollar.dominio.farmacovigilancia;

import java.time.LocalDateTime;

public class Medicamento {

    private final MedicamentoId id;
    private final String nome;
    private final double doseMinimaSeguraMgPorKg;
    private final double doseMaximaSeguraMgPorKg;
    private final double concentracaoMgPorMl;
    private final RestricaoManejo restricaoManejo;
    private final LocalDateTime criadoEm;

    // Construtor de CRIAÇÃO
    public Medicamento(MedicamentoId id,
                       String nome,
                       double doseMinimaSeguraMgPorKg,
                       double doseMaximaSeguraMgPorKg,
                       double concentracaoMgPorMl,
                       RestricaoManejo restricaoManejo) {
        if (id == null)
            throw new IllegalArgumentException("id não pode ser nulo.");
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("nome não pode ser vazio.");
        if (doseMaximaSeguraMgPorKg <= 0)
            throw new IllegalArgumentException("doseMaximaSeguraMgPorKg deve ser positiva.");
        if (concentracaoMgPorMl <= 0)
            throw new IllegalArgumentException("concentracaoMgPorMl deve ser positiva.");
        if (restricaoManejo == null)
            throw new IllegalArgumentException("restricaoManejo não pode ser nulo.");
        this.id = id;
        this.nome = nome;
        this.doseMinimaSeguraMgPorKg = doseMinimaSeguraMgPorKg;
        this.doseMaximaSeguraMgPorKg = doseMaximaSeguraMgPorKg;
        this.concentracaoMgPorMl = concentracaoMgPorMl;
        this.restricaoManejo = restricaoManejo;
        this.criadoEm = LocalDateTime.now();
    }

    // Construtor de RECONSTRUÇÃO
    public Medicamento(MedicamentoId id,
                       String nome,
                       double doseMinimaSeguraMgPorKg,
                       double doseMaximaSeguraMgPorKg,
                       double concentracaoMgPorMl,
                       RestricaoManejo restricaoManejo,
                       LocalDateTime criadoEm) {
        this.id = id;
        this.nome = nome;
        this.doseMinimaSeguraMgPorKg = doseMinimaSeguraMgPorKg;
        this.doseMaximaSeguraMgPorKg = doseMaximaSeguraMgPorKg;
        this.concentracaoMgPorMl = concentracaoMgPorMl;
        this.restricaoManejo = restricaoManejo;
        this.criadoEm = criadoEm;
    }

    public MedicamentoId getId()                        { return id; }
    public String getNome()                             { return nome; }
    public double getDoseMinimaSeguraMgPorKg()          { return doseMinimaSeguraMgPorKg; }
    public double getDoseMaximaSeguraMgPorKg()          { return doseMaximaSeguraMgPorKg; }
    public double getConcentracaoMgPorMl()              { return concentracaoMgPorMl; }
    public RestricaoManejo getRestricaoManejo()         { return restricaoManejo; }
    public LocalDateTime getCriadoEm()                 { return criadoEm; }
}
