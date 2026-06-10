package br.com.cesar.petCollar.dominio.BeneficiosPlano.bdd;

import org.mockito.Mockito;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioTutor;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioTutorId;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.CalculoStatusBeneficioService;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.ExpiracaoTicketService;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.GeracaoTicketService;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioTutorRepositorio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.ITicketBeneficioRepositorio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.StatusBeneficio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.TicketBeneficio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.TicketBeneficioId;

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

