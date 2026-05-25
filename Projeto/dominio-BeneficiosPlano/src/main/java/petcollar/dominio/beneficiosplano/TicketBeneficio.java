package petcollar.dominio.beneficiosplano;

import java.time.LocalDateTime;

public class TicketBeneficio {

    private final TicketBeneficioId id;
    private final BeneficioTutorId beneficioTutorId;
    private final CodigoGUID codigoGUID;
    private StatusTicket status;
    private final LocalDateTime geradoEm;
    private final LocalDateTime expiraEm;
    private LocalDateTime apresentadoEm;
    private LocalDateTime utilizadoEm;
    private LocalDateTime expiradoEm;
    private LocalDateTime canceladoEm;

    public TicketBeneficio(
            TicketBeneficioId id,
            BeneficioTutorId beneficioTutorId,
            CodigoGUID codigoGUID,
            LocalDateTime geradoEm,
            LocalDateTime expiraEm
    ) {
        if (id == null) {
            throw new IllegalArgumentException("TicketBeneficioId não pode ser nulo.");
        }
        if (beneficioTutorId == null) {
            throw new IllegalArgumentException("BeneficioTutorId não pode ser nulo.");
        }
        if (codigoGUID == null) {
            throw new IllegalArgumentException("CodigoGUID não pode ser nulo.");
        }
        if (geradoEm == null) {
            throw new IllegalArgumentException("geradoEm não pode ser nulo.");
        }
        if (expiraEm == null) {
            throw new IllegalArgumentException("expiraEm não pode ser nulo.");
        }
        if (!expiraEm.isAfter(geradoEm)) {
            throw new IllegalArgumentException("expiraEm deve ser posterior ao geradoEm.");
        }

        this.id = id;
        this.beneficioTutorId = beneficioTutorId;
        this.codigoGUID = codigoGUID;
        this.status = StatusTicket.GERADO;
        this.geradoEm = geradoEm;
        this.expiraEm = expiraEm;
    }

    public TicketBeneficio(
            TicketBeneficioId id,
            BeneficioTutorId beneficioTutorId,
            CodigoGUID codigoGUID,
            StatusTicket status,
            LocalDateTime geradoEm,
            LocalDateTime expiraEm,
            LocalDateTime apresentadoEm,
            LocalDateTime utilizadoEm,
            LocalDateTime expiradoEm,
            LocalDateTime canceladoEm
    ) {
        if (id == null) {
            throw new IllegalArgumentException("TicketBeneficioId não pode ser nulo.");
        }
        if (beneficioTutorId == null) {
            throw new IllegalArgumentException("BeneficioTutorId não pode ser nulo.");
        }
        if (codigoGUID == null) {
            throw new IllegalArgumentException("CodigoGUID não pode ser nulo.");
        }
        if (status == null) {
            throw new IllegalArgumentException("StatusTicket não pode ser nulo.");
        }
        if (geradoEm == null) {
            throw new IllegalArgumentException("geradoEm não pode ser nulo.");
        }
        if (expiraEm == null) {
            throw new IllegalArgumentException("expiraEm não pode ser nulo.");
        }

        this.id = id;
        this.beneficioTutorId = beneficioTutorId;
        this.codigoGUID = codigoGUID;
        this.status = status;
        this.geradoEm = geradoEm;
        this.expiraEm = expiraEm;
        this.apresentadoEm = apresentadoEm;
        this.utilizadoEm = utilizadoEm;
        this.expiradoEm = expiradoEm;
        this.canceladoEm = canceladoEm;
    }

    public boolean estaExpirado(LocalDateTime agora) {
        if (agora == null) {
            throw new IllegalArgumentException("A data atual não pode ser nula.");
        }
        return !agora.isBefore(expiraEm);
    }

    public void apresentar(LocalDateTime agora) {
        if (agora == null) {
            throw new IllegalArgumentException("A data atual não pode ser nula.");
        }
        if (status != StatusTicket.GERADO) {
            throw new IllegalStateException("Só é possível apresentar um ticket com status GERADO.");
        }
        if (estaExpirado(agora)) {
            throw new IllegalStateException("Não é possível apresentar um ticket expirado.");
        }
        this.status = StatusTicket.APRESENTADO;
        this.apresentadoEm = agora;
    }

    public void utilizar(LocalDateTime agora) {
        if (agora == null) {
            throw new IllegalArgumentException("A data atual não pode ser nula.");
        }
        if (status != StatusTicket.APRESENTADO) {
            throw new IllegalStateException("Só é possível utilizar um ticket com status APRESENTADO.");
        }
        if (estaExpirado(agora)) {
            throw new IllegalStateException("Não é possível utilizar um ticket expirado.");
        }
        this.status = StatusTicket.UTILIZADO;
        this.utilizadoEm = agora;
    }

    public void expirar(LocalDateTime agora) {
        if (agora == null) {
            throw new IllegalArgumentException("A data atual não pode ser nula.");
        }
        if (status == StatusTicket.UTILIZADO || status == StatusTicket.CANCELADO || status == StatusTicket.EXPIRADO) {
            throw new IllegalStateException("Só é possível expirar um ticket ativo.");
        }
        this.status = StatusTicket.EXPIRADO;
        this.expiradoEm = agora;
    }

    public void cancelar(LocalDateTime agora) {
        if (agora == null) {
            throw new IllegalArgumentException("A data atual não pode ser nula.");
        }
        if (status == StatusTicket.UTILIZADO || status == StatusTicket.CANCELADO || status == StatusTicket.EXPIRADO) {
            throw new IllegalStateException("Só é possível cancelar um ticket ativo.");
        }
        this.status = StatusTicket.CANCELADO;
        this.canceladoEm = agora;
    }

    public TicketBeneficioId getId() {
        return id;
    }

    public BeneficioTutorId getBeneficioTutorId() {
        return beneficioTutorId;
    }

    public CodigoGUID getCodigoGUID() {
        return codigoGUID;
    }

    public StatusTicket getStatus() {
        return status;
    }

    public LocalDateTime getGeradoEm() {
        return geradoEm;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public LocalDateTime getApresentadoEm() {
        return apresentadoEm;
    }

    public LocalDateTime getUtilizadoEm() {
        return utilizadoEm;
    }

    public LocalDateTime getExpiradoEm() {
        return expiradoEm;
    }

    public LocalDateTime getCanceladoEm() {
        return canceladoEm;
    }
}

