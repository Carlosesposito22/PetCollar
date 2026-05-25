package petcollar.dominio.beneficiosplano.bdd;

import org.mockito.Mockito;
import petcollar.dominio.beneficiosplano.BeneficioTutor;
import petcollar.dominio.beneficiosplano.BeneficioTutorId;
import petcollar.dominio.beneficiosplano.CalculoStatusBeneficioService;
import petcollar.dominio.beneficiosplano.ExpiracaoTicketService;
import petcollar.dominio.beneficiosplano.GeracaoTicketService;
import petcollar.dominio.beneficiosplano.IBeneficioTutorRepositorio;
import petcollar.dominio.beneficiosplano.ITicketBeneficioRepositorio;
import petcollar.dominio.beneficiosplano.StatusBeneficio;
import petcollar.dominio.beneficiosplano.TicketBeneficio;
import petcollar.dominio.beneficiosplano.TicketBeneficioId;

import java.time.LocalDateTime;
import java.util.List;

public class ContextoCenario {

    public final IBeneficioTutorRepositorio beneficioTutorRepositorio = Mockito.mock(IBeneficioTutorRepositorio.class);
    public final ITicketBeneficioRepositorio ticketBeneficioRepositorio = Mockito.mock(ITicketBeneficioRepositorio.class);

    public final CalculoStatusBeneficioService calculoStatusBeneficioService = new CalculoStatusBeneficioService();
    public final GeracaoTicketService geracaoTicketService = new GeracaoTicketService(
            beneficioTutorRepositorio,
            ticketBeneficioRepositorio,
            calculoStatusBeneficioService
    );
    public final ExpiracaoTicketService expiracaoTicketService = new ExpiracaoTicketService(
            ticketBeneficioRepositorio,
            beneficioTutorRepositorio
    );

    public final LocalDateTime agora = LocalDateTime.of(2026, 4, 28, 12, 0);

    public BeneficioTutor beneficioTutor;
    public BeneficioTutorId beneficioTutorId;
    public TicketBeneficio ticketBeneficio;
    public TicketBeneficioId ticketBeneficioId;
    public StatusBeneficio statusCalculado;
    public List<TicketBeneficio> ticketsExpirados;
    public Exception excecaoCapturada;
}

