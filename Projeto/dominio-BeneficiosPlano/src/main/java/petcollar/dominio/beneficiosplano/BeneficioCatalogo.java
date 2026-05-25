package petcollar.dominio.beneficiosplano;

import petcollar.dominio.compartilhado.PlanoId;

import java.time.LocalDateTime;

public class BeneficioCatalogo {

    private final BeneficioCatalogoId id;
    private final PlanoId planoId;
    private String nome;
    private PeriodoRenovacao periodoRenovacao;
    private int limiteUsosPorPeriodo;
    private int carenciaDias;
    private boolean ativo;
    private final LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public BeneficioCatalogo(
            BeneficioCatalogoId id,
            PlanoId planoId,
            String nome,
            PeriodoRenovacao periodoRenovacao,
            int limiteUsosPorPeriodo,
            int carenciaDias
    ) {
        if (id == null) {
            throw new IllegalArgumentException("BeneficioCatalogoId não pode ser nulo.");
        }
        if (planoId == null) {
            throw new IllegalArgumentException("PlanoId não pode ser nulo.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do benefício não pode ser vazio.");
        }
        if (periodoRenovacao == null) {
            throw new IllegalArgumentException("PeriodoRenovacao não pode ser nulo.");
        }
        if (limiteUsosPorPeriodo < 0) {
            throw new IllegalArgumentException("Limite de usos por período não pode ser negativo.");
        }
        if (carenciaDias < 0) {
            throw new IllegalArgumentException("Carência em dias não pode ser negativa.");
        }

        this.id = id;
        this.planoId = planoId;
        this.nome = nome;
        this.periodoRenovacao = periodoRenovacao;
        this.limiteUsosPorPeriodo = limiteUsosPorPeriodo;
        this.carenciaDias = carenciaDias;
        this.ativo = true;
        this.criadoEm = LocalDateTime.now();
    }

    public BeneficioCatalogo(
            BeneficioCatalogoId id,
            PlanoId planoId,
            String nome,
            PeriodoRenovacao periodoRenovacao,
            int limiteUsosPorPeriodo,
            int carenciaDias,
            boolean ativo,
            LocalDateTime criadoEm,
            LocalDateTime atualizadoEm
    ) {
        if (id == null) {
            throw new IllegalArgumentException("BeneficioCatalogoId não pode ser nulo.");
        }
        if (planoId == null) {
            throw new IllegalArgumentException("PlanoId não pode ser nulo.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do benefício não pode ser vazio.");
        }
        if (periodoRenovacao == null) {
            throw new IllegalArgumentException("PeriodoRenovacao não pode ser nulo.");
        }
        if (limiteUsosPorPeriodo < 0) {
            throw new IllegalArgumentException("Limite de usos por período não pode ser negativo.");
        }
        if (carenciaDias < 0) {
            throw new IllegalArgumentException("Carência em dias não pode ser negativa.");
        }
        if (criadoEm == null) {
            throw new IllegalArgumentException("criadoEm não pode ser nulo.");
        }

        this.id = id;
        this.planoId = planoId;
        this.nome = nome;
        this.periodoRenovacao = periodoRenovacao;
        this.limiteUsosPorPeriodo = limiteUsosPorPeriodo;
        this.carenciaDias = carenciaDias;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public void alterarConfiguracao(String nome, PeriodoRenovacao periodoRenovacao, int limiteUsosPorPeriodo, int carenciaDias) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do benefício não pode ser vazio.");
        }
        if (periodoRenovacao == null) {
            throw new IllegalArgumentException("PeriodoRenovacao não pode ser nulo.");
        }
        if (limiteUsosPorPeriodo < 0) {
            throw new IllegalArgumentException("Limite de usos por período não pode ser negativo.");
        }
        if (carenciaDias < 0) {
            throw new IllegalArgumentException("Carência em dias não pode ser negativa.");
        }
        this.nome = nome;
        this.periodoRenovacao = periodoRenovacao;
        this.limiteUsosPorPeriodo = limiteUsosPorPeriodo;
        this.carenciaDias = carenciaDias;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void ativar() {
        this.ativo = true;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void inativar() {
        this.ativo = false;
        this.atualizadoEm = LocalDateTime.now();
    }

    public BeneficioCatalogoId getId() {
        return id;
    }

    public PlanoId getPlanoId() {
        return planoId;
    }

    public String getNome() {
        return nome;
    }

    public PeriodoRenovacao getPeriodoRenovacao() {
        return periodoRenovacao;
    }

    public int getLimiteUsosPorPeriodo() {
        return limiteUsosPorPeriodo;
    }

    public int getCarenciaDias() {
        return carenciaDias;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}

