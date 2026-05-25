package petcollar.dominio.beneficiosplano;

import java.time.LocalDateTime;

public class GeracaoTicketService {

    private final IBeneficioTutorRepositorio beneficioTutorRepositorio;
    private final ITicketBeneficioRepositorio ticketBeneficioRepositorio;
    private final CalculoStatusBeneficioService calculoStatusBeneficioService;

    public GeracaoTicketService(
            IBeneficioTutorRepositorio beneficioTutorRepositorio,
            ITicketBeneficioRepositorio ticketBeneficioRepositorio,
            CalculoStatusBeneficioService calculoStatusBeneficioService
    ) {
        this.beneficioTutorRepositorio = beneficioTutorRepositorio;
        this.ticketBeneficioRepositorio = ticketBeneficioRepositorio;
        this.calculoStatusBeneficioService = calculoStatusBeneficioService;
    }

    public TicketBeneficio gerarTicket(BeneficioTutorId beneficioTutorId, LocalDateTime agora) {
        if (beneficioTutorId == null) {
            throw new IllegalArgumentException("BeneficioTutorId não pode ser nulo.");
        }
        if (agora == null) {
            throw new IllegalArgumentException("A data atual não pode ser nula.");
        }

        BeneficioTutor beneficioTutor = beneficioTutorRepositorio.findById(beneficioTutorId);
        if (beneficioTutor == null) {
            throw new IllegalArgumentException("BeneficioTutor não encontrado com o id informado.");
        }

        calculoStatusBeneficioService.calcularStatus(beneficioTutor, agora);
        if (beneficioTutor.getStatus() != StatusBeneficio.DISPONIVEL) {
            throw new IllegalStateException("Só é possível gerar ticket quando o benefício estiver DISPONIVEL.");
        }

        if (ticketBeneficioRepositorio.existsByBeneficioTutorIdAndStatus(beneficioTutorId, StatusTicket.GERADO)) {
            throw new IllegalStateException("Já existe um ticket ativo para este benefício.");
        }

        beneficioTutor.debitarUso(agora);

        CodigoGUID codigoGUID;
        do {
            codigoGUID = CodigoGUID.gerar();
        } while (ticketBeneficioRepositorio.existsByCodigoGUID(codigoGUID.getValor()));

        TicketBeneficio ticketBeneficio = new TicketBeneficio(
                TicketBeneficioId.gerar(),
                beneficioTutorId,
                codigoGUID,
                agora,
                agora.plusHours(1)
        );

        ticketBeneficioRepositorio.save(ticketBeneficio);
        beneficioTutorRepositorio.save(beneficioTutor);
        return ticketBeneficio;
    }
}

