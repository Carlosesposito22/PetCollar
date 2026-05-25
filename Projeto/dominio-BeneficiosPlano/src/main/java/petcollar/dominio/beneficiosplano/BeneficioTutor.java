package petcollar.dominio.beneficiosplano;

import petcollar.dominio.compartilhado.PlanoId;
import petcollar.dominio.compartilhado.TutorId;

import java.time.LocalDateTime;

public class BeneficioTutor {

    private final BeneficioTutorId id;
    private final TutorId tutorId;
    private final PlanoId planoId;
    private final BeneficioCatalogoId beneficioCatalogoId;
    private final LocalDateTime dataLiberacao;
    private StatusBeneficio status;
    private final PeriodoRenovacao periodoRenovacao;
    private final int limiteUsosPorPeriodo;
    private int usosRestantesPeriodoAtual;
    private LocalDateTime inicioPeriodoAtual;
    private final LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public BeneficioTutor(
            BeneficioTutorId id,
            TutorId tutorId,
            PlanoId planoId,
            BeneficioCatalogoId beneficioCatalogoId,
            LocalDateTime dataLiberacao,
            PeriodoRenovacao periodoRenovacao,
            int limiteUsosPorPeriodo
    ) {
        if (id == null) {
            throw new IllegalArgumentException("BeneficioTutorId não pode ser nulo.");
        }
        if (tutorId == null) {
            throw new IllegalArgumentException("TutorId não pode ser nulo.");
        }
        if (planoId == null) {
            throw new IllegalArgumentException("PlanoId não pode ser nulo.");
        }
        if (beneficioCatalogoId == null) {
            throw new IllegalArgumentException("BeneficioCatalogoId não pode ser nulo.");
        }
        if (dataLiberacao == null) {
            throw new IllegalArgumentException("Data de liberação não pode ser nula.");
        }
        if (periodoRenovacao == null) {
            throw new IllegalArgumentException("PeriodoRenovacao não pode ser nulo.");
        }
        if (limiteUsosPorPeriodo < 0) {
            throw new IllegalArgumentException("Limite de usos por período não pode ser negativo.");
        }

        LocalDateTime agora = LocalDateTime.now();
        this.id = id;
        this.tutorId = tutorId;
        this.planoId = planoId;
        this.beneficioCatalogoId = beneficioCatalogoId;
        this.dataLiberacao = dataLiberacao;
        this.periodoRenovacao = periodoRenovacao;
        this.limiteUsosPorPeriodo = limiteUsosPorPeriodo;
        this.usosRestantesPeriodoAtual = limiteUsosPorPeriodo;
        this.inicioPeriodoAtual = dataLiberacao.isAfter(agora) ? dataLiberacao : agora;
        this.status = calcularStatusBaseadoEm(agora);
        this.criadoEm = agora;
    }

    public BeneficioTutor(
            BeneficioTutorId id,
            TutorId tutorId,
            PlanoId planoId,
            BeneficioCatalogoId beneficioCatalogoId,
            LocalDateTime dataLiberacao,
            StatusBeneficio status,
            PeriodoRenovacao periodoRenovacao,
            int limiteUsosPorPeriodo,
            int usosRestantesPeriodoAtual,
            LocalDateTime inicioPeriodoAtual,
            LocalDateTime criadoEm,
            LocalDateTime atualizadoEm
    ) {
        if (id == null) {
            throw new IllegalArgumentException("BeneficioTutorId não pode ser nulo.");
        }
        if (tutorId == null) {
            throw new IllegalArgumentException("TutorId não pode ser nulo.");
        }
        if (planoId == null) {
            throw new IllegalArgumentException("PlanoId não pode ser nulo.");
        }
        if (beneficioCatalogoId == null) {
            throw new IllegalArgumentException("BeneficioCatalogoId não pode ser nulo.");
        }
        if (dataLiberacao == null) {
            throw new IllegalArgumentException("Data de liberação não pode ser nula.");
        }
        if (status == null) {
            throw new IllegalArgumentException("StatusBeneficio não pode ser nulo.");
        }
        if (periodoRenovacao == null) {
            throw new IllegalArgumentException("PeriodoRenovacao não pode ser nulo.");
        }
        if (limiteUsosPorPeriodo < 0) {
            throw new IllegalArgumentException("Limite de usos por período não pode ser negativo.");
        }
        if (usosRestantesPeriodoAtual < 0) {
            throw new IllegalArgumentException("Usos restantes do período atual não podem ser negativos.");
        }
        if (usosRestantesPeriodoAtual > limiteUsosPorPeriodo) {
            throw new IllegalArgumentException("Usos restantes do período atual não podem exceder o limite do período.");
        }
        if (inicioPeriodoAtual == null) {
            throw new IllegalArgumentException("inicioPeriodoAtual não pode ser nulo.");
        }
        if (criadoEm == null) {
            throw new IllegalArgumentException("criadoEm não pode ser nulo.");
        }

        this.id = id;
        this.tutorId = tutorId;
        this.planoId = planoId;
        this.beneficioCatalogoId = beneficioCatalogoId;
        this.dataLiberacao = dataLiberacao;
        this.status = status;
        this.periodoRenovacao = periodoRenovacao;
        this.limiteUsosPorPeriodo = limiteUsosPorPeriodo;
        this.usosRestantesPeriodoAtual = usosRestantesPeriodoAtual;
        this.inicioPeriodoAtual = inicioPeriodoAtual;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public void reiniciarPeriodoSeNecessario(LocalDateTime agora) {
        if (agora == null) {
            throw new IllegalArgumentException("A data atual não pode ser nula.");
        }
        if (agora.isBefore(dataLiberacao)) {
            return;
        }

        LocalDateTime fimPeriodoAtual = periodoRenovacao.adicionarA(inicioPeriodoAtual);
        if (!agora.isBefore(fimPeriodoAtual)) {
            this.usosRestantesPeriodoAtual = limiteUsosPorPeriodo;
            this.inicioPeriodoAtual = agora;
            this.atualizadoEm = agora;
        }
    }

    public StatusBeneficio recalcularStatus(LocalDateTime agora) {
        if (agora == null) {
            throw new IllegalArgumentException("A data atual não pode ser nula.");
        }

        if (agora.isBefore(dataLiberacao)) {
            this.status = StatusBeneficio.EM_CARENCIA;
        } else if (usosRestantesPeriodoAtual == 0) {
            this.status = StatusBeneficio.ESGOTADO;
        } else {
            this.status = StatusBeneficio.DISPONIVEL;
        }
        this.atualizadoEm = agora;
        return this.status;
    }

    public void debitarUso(LocalDateTime agora) {
        if (agora == null) {
            throw new IllegalArgumentException("A data atual não pode ser nula.");
        }

        reiniciarPeriodoSeNecessario(agora);
        recalcularStatus(agora);
        if (this.status != StatusBeneficio.DISPONIVEL) {
            throw new IllegalStateException("Só é possível debitar uso quando o benefício estiver DISPONIVEL.");
        }
        if (this.usosRestantesPeriodoAtual <= 0) {
            throw new IllegalStateException("Não há usos restantes para este benefício.");
        }

        this.usosRestantesPeriodoAtual--;
        this.status = this.usosRestantesPeriodoAtual == 0 ? StatusBeneficio.ESGOTADO : StatusBeneficio.DISPONIVEL;
        this.atualizadoEm = agora;
    }

    public void devolverUso(LocalDateTime agora) {
        if (agora == null) {
            throw new IllegalArgumentException("A data atual não pode ser nula.");
        }

        reiniciarPeriodoSeNecessario(agora);
        if (this.usosRestantesPeriodoAtual < this.limiteUsosPorPeriodo) {
            this.usosRestantesPeriodoAtual++;
        }
        recalcularStatus(agora);
    }

    private StatusBeneficio calcularStatusBaseadoEm(LocalDateTime agora) {
        if (agora.isBefore(dataLiberacao)) {
            return StatusBeneficio.EM_CARENCIA;
        }
        if (usosRestantesPeriodoAtual == 0) {
            return StatusBeneficio.ESGOTADO;
        }
        return StatusBeneficio.DISPONIVEL;
    }

    public BeneficioTutorId getId() {
        return id;
    }

    public TutorId getTutorId() {
        return tutorId;
    }

    public PlanoId getPlanoId() {
        return planoId;
    }

    public BeneficioCatalogoId getBeneficioCatalogoId() {
        return beneficioCatalogoId;
    }

    public LocalDateTime getDataLiberacao() {
        return dataLiberacao;
    }

    public StatusBeneficio getStatus() {
        return status;
    }

    public PeriodoRenovacao getPeriodoRenovacao() {
        return periodoRenovacao;
    }

    public int getLimiteUsosPorPeriodo() {
        return limiteUsosPorPeriodo;
    }

    public int getUsosRestantesPeriodoAtual() {
        return usosRestantesPeriodoAtual;
    }

    public LocalDateTime getInicioPeriodoAtual() {
        return inicioPeriodoAtual;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}

