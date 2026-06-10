package br.com.cesar.petCollar.aplicacao.BeneficiosPlano;

import java.time.LocalDateTime;

import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogo;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioTutor;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioTutorId;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.GeracaoTicketService;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioCatalogoRepositorio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioTutorRepositorio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.TicketBeneficio;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.compartilhado.eventos.PublicadorDeEventosDoTutor;

/**
 * Encapsula a geração de um ticket de uso de benefício para o tutor logado,
 * garantindo que o {@code BeneficioTutor} informado realmente pertence a ele
 * antes de delegar ao {@link GeracaoTicketService} do domínio. Publica o evento
 * "ticket_beneficio_utilizado" (padrão Observer) — a Gamificação reage avaliando
 * badges de engajamento, sem que este caso de uso precise conhecer o serviço de
 * badges diretamente.
 */
public class GerarTicketBeneficioUseCase {

    public static final String EVENTO_TICKET_BENEFICIO_UTILIZADO = "ticket_beneficio_utilizado";

    private final IBeneficioTutorRepositorio beneficioTutorRepositorio;
    private final IBeneficioCatalogoRepositorio beneficioCatalogoRepositorio;
    private final GeracaoTicketService geracaoTicketService;
    private final PublicadorDeEventosDoTutor publicadorDeEventos;

    public GerarTicketBeneficioUseCase(IBeneficioTutorRepositorio beneficioTutorRepositorio,
                                       IBeneficioCatalogoRepositorio beneficioCatalogoRepositorio,
                                       GeracaoTicketService geracaoTicketService,
                                       PublicadorDeEventosDoTutor publicadorDeEventos) {
        if (beneficioTutorRepositorio == null)
            throw new IllegalArgumentException("IBeneficioTutorRepositorio é obrigatório.");
        if (beneficioCatalogoRepositorio == null)
            throw new IllegalArgumentException("IBeneficioCatalogoRepositorio é obrigatório.");
        if (geracaoTicketService == null)
            throw new IllegalArgumentException("GeracaoTicketService é obrigatório.");
        if (publicadorDeEventos == null)
            throw new IllegalArgumentException("PublicadorDeEventosDoTutor é obrigatório.");
        this.beneficioTutorRepositorio = beneficioTutorRepositorio;
        this.beneficioCatalogoRepositorio = beneficioCatalogoRepositorio;
        this.geracaoTicketService = geracaoTicketService;
        this.publicadorDeEventos = publicadorDeEventos;
    }

    public Resultado executar(TutorId tutorId, BeneficioTutorId beneficioTutorId) {
        if (tutorId == null) throw new IllegalArgumentException("TutorId é obrigatório.");
        if (beneficioTutorId == null) throw new IllegalArgumentException("BeneficioTutorId é obrigatório.");

        BeneficioTutor beneficioTutor = beneficioTutorRepositorio.findById(beneficioTutorId);
        if (beneficioTutor == null || !beneficioTutor.getTutorId().equals(tutorId)) {
            throw new IllegalArgumentException("Benefício não encontrado para o tutor informado.");
        }

        TicketBeneficio ticket = geracaoTicketService.gerarTicket(beneficioTutorId, LocalDateTime.now());
        BeneficioCatalogo catalogo = beneficioCatalogoRepositorio.findById(beneficioTutor.getBeneficioCatalogoId());
        String nomeBeneficio = catalogo == null ? null : catalogo.getNome();
        publicadorDeEventos.publicar(tutorId, EVENTO_TICKET_BENEFICIO_UTILIZADO);
        return new Resultado(ticket, nomeBeneficio);
    }

    /** Saída do caso de uso: o ticket gerado e o nome do benefício associado (para exibição). */
    public record Resultado(TicketBeneficio ticket, String nomeBeneficio) {}
}
