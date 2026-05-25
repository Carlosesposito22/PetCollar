package petcollar.dominio.beneficiosplano;

import java.util.List;

public interface ITicketBeneficioRepositorio {
    void save(TicketBeneficio ticketBeneficio);
    TicketBeneficio findById(TicketBeneficioId id);
    List<TicketBeneficio> findByBeneficioTutorId(BeneficioTutorId beneficioTutorId);
    List<TicketBeneficio> findByStatus(StatusTicket status);
    boolean existsByCodigoGUID(String codigoGUID);
    boolean existsByBeneficioTutorIdAndStatus(BeneficioTutorId beneficioTutorId, StatusTicket status);
}

