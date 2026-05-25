package petcollar.dominio.beneficiosplano;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExpiracaoTicketService {

    private final ITicketBeneficioRepositorio ticketBeneficioRepositorio;
    private final IBeneficioTutorRepositorio beneficioTutorRepositorio;

    public ExpiracaoTicketService(
            ITicketBeneficioRepositorio ticketBeneficioRepositorio,
            IBeneficioTutorRepositorio beneficioTutorRepositorio
    ) {
        this.ticketBeneficioRepositorio = ticketBeneficioRepositorio;
        this.beneficioTutorRepositorio = beneficioTutorRepositorio;
    }

    public List<TicketBeneficio> cancelarTicketsExpirados(LocalDateTime agora) {
        if (agora == null) {
            throw new IllegalArgumentException("A data atual não pode ser nula.");
        }

        List<TicketBeneficio> ticketsAtivos = new ArrayList<>();
        List<TicketBeneficio> gerados = ticketBeneficioRepositorio.findByStatus(StatusTicket.GERADO);
        List<TicketBeneficio> apresentados = ticketBeneficioRepositorio.findByStatus(StatusTicket.APRESENTADO);

        if (gerados != null) {
            ticketsAtivos.addAll(gerados);
        }
        if (apresentados != null) {
            ticketsAtivos.addAll(apresentados);
        }
        if (ticketsAtivos.isEmpty()) {
            return Collections.emptyList();
        }

        List<TicketBeneficio> ticketsExpirados = new ArrayList<>();
        for (TicketBeneficio ticketBeneficio : ticketsAtivos) {
            if (!ticketBeneficio.estaExpirado(agora)) {
                continue;
            }

            BeneficioTutor beneficioTutor = beneficioTutorRepositorio.findById(ticketBeneficio.getBeneficioTutorId());
            if (beneficioTutor == null) {
                throw new IllegalArgumentException("BeneficioTutor não encontrado com o id informado.");
            }

            ticketBeneficio.expirar(agora);
            beneficioTutor.devolverUso(agora);

            ticketBeneficioRepositorio.save(ticketBeneficio);
            beneficioTutorRepositorio.save(beneficioTutor);
            ticketsExpirados.add(ticketBeneficio);
        }

        return ticketsExpirados;
    }
}

