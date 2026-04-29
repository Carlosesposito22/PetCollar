package petcollar.dominio.farmacovigilancia;

import java.time.LocalDateTime;

public class ItemPrescricao {

    private final ItemPrescricaoId id;
    private final MedicamentoId medicamentoId;
    private final double dosePrescritaMg;
    private final int duracaoDias;
    private final LocalDateTime dataInicioUso;
    private StatusDosagem statusDosagem;
    private final LocalDateTime criadoEm;

    // Construtor de CRIAÇÃO
    public ItemPrescricao(ItemPrescricaoId id,
                          MedicamentoId medicamentoId,
                          double dosePrescritaMg,
                          int duracaoDias,
                          LocalDateTime dataInicioUso) {
        if (id == null)
            throw new IllegalArgumentException("id não pode ser nulo.");
        if (medicamentoId == null)
            throw new IllegalArgumentException("medicamentoId não pode ser nulo.");
        if (dosePrescritaMg <= 0)
            throw new IllegalArgumentException("dosePrescritaMg deve ser positiva.");
        if (duracaoDias <= 0)
            throw new IllegalArgumentException("duracaoDias deve ser positivo.");
        if (dataInicioUso == null)
            throw new IllegalArgumentException("dataInicioUso não pode ser nula.");
        this.id = id;
        this.medicamentoId = medicamentoId;
        this.dosePrescritaMg = dosePrescritaMg;
        this.duracaoDias = duracaoDias;
        this.dataInicioUso = dataInicioUso;
        this.statusDosagem = StatusDosagem.DENTRO_DO_LIMITE;
        this.criadoEm = LocalDateTime.now();
    }

    // Construtor de RECONSTRUÇÃO
    public ItemPrescricao(ItemPrescricaoId id,
                          MedicamentoId medicamentoId,
                          double dosePrescritaMg,
                          int duracaoDias,
                          LocalDateTime dataInicioUso,
                          StatusDosagem statusDosagem,
                          LocalDateTime criadoEm) {
        this.id = id;
        this.medicamentoId = medicamentoId;
        this.dosePrescritaMg = dosePrescritaMg;
        this.duracaoDias = duracaoDias;
        this.dataInicioUso = dataInicioUso;
        this.statusDosagem = statusDosagem;
        this.criadoEm = criadoEm;
    }

    public void definirStatusDosagem(StatusDosagem novoStatus) {
        if (novoStatus == null)
            throw new IllegalArgumentException("novoStatus não pode ser nulo.");
        this.statusDosagem = novoStatus;
    }

    public void travarPorInteracao() {
        this.statusDosagem = StatusDosagem.TRAVADO_POR_INTERACAO;
    }

    public ItemPrescricaoId getId()             { return id; }
    public MedicamentoId getMedicamentoId()     { return medicamentoId; }
    public double getDosePrescritaMg()          { return dosePrescritaMg; }
    public int getDuracaoDias()                 { return duracaoDias; }
    public LocalDateTime getDataInicioUso()     { return dataInicioUso; }
    public StatusDosagem getStatusDosagem()     { return statusDosagem; }
    public LocalDateTime getCriadoEm()          { return criadoEm; }
}
