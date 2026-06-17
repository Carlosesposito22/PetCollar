package br.com.cesar.petCollar.apresentacao.PortalTutor;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cesar.petCollar.aplicacao.BeneficiosPlano.ConsultarBeneficiosTutorUseCase;
import br.com.cesar.petCollar.aplicacao.BeneficiosPlano.GerarTicketBeneficioUseCase;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogo;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioTutor;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioTutorId;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.PeriodoRenovacao;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.StatusBeneficio;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

@RestController
@RequestMapping("/api/tutor/beneficios")
public class BeneficiosController {

    private final ConsultarBeneficiosTutorUseCase consultarBeneficios;
    private final GerarTicketBeneficioUseCase gerarTicket;

    public BeneficiosController(ConsultarBeneficiosTutorUseCase consultarBeneficios,
                                GerarTicketBeneficioUseCase gerarTicket) {
        this.consultarBeneficios = consultarBeneficios;
        this.gerarTicket = gerarTicket;
    }

    @GetMapping
    public List<BeneficioItemDTO> listar(Principal principal) {
        TutorId tutorId = TutorId.de(principal.getName());
        return consultarBeneficios.executar(tutorId).stream().map(BeneficioItemDTO::de).toList();
    }

    @PostMapping("/{id}/usar")
    public TicketGeradoDTO usar(@PathVariable String id, Principal principal) {
        TutorId tutorId = TutorId.de(principal.getName());
        try {
            return TicketGeradoDTO.de(gerarTicket.executar(tutorId, BeneficioTutorId.de(id)));
        } catch (IllegalArgumentException e) {
            throw new BeneficioNaoEncontradoException();
        }
    }

    public record BeneficioItemDTO(
            String id,
            String nome,
            StatusBeneficio status,
            int usosRestantes,
            int limiteUsosPorPeriodo,
            PeriodoRenovacao periodoRenovacao,
            LocalDate dataReferencia,
            int carenciaDias
    ) {
        static BeneficioItemDTO de(ConsultarBeneficiosTutorUseCase.Item item) {
            BeneficioTutor bt = item.beneficioTutor();
            BeneficioCatalogo catalogo = item.catalogo();
            return new BeneficioItemDTO(
                    bt.getId().getValor(),
                    catalogo == null ? null : catalogo.getNome(),
                    bt.getStatus(),
                    bt.getUsosRestantesPeriodoAtual(),
                    bt.getLimiteUsosPorPeriodo(),
                    bt.getPeriodoRenovacao(),
                    dataReferencia(bt),
                    catalogo == null ? 0 : catalogo.getCarenciaDias()
            );
        }

        private static LocalDate dataReferencia(BeneficioTutor bt) {
            return switch (bt.getStatus()) {
                case EM_CARENCIA -> bt.getDataLiberacao().toLocalDate();
                case ESGOTADO -> bt.getPeriodoRenovacao().adicionarA(bt.getInicioPeriodoAtual()).toLocalDate();
                case DISPONIVEL -> null;
            };
        }
    }

    public record TicketGeradoDTO(String codigoGUID, LocalDateTime expiraEm, String nomeBeneficio) {
        static TicketGeradoDTO de(GerarTicketBeneficioUseCase.Resultado resultado) {
            return new TicketGeradoDTO(
                    resultado.ticket().getCodigoGUID().getValor(),
                    resultado.ticket().getExpiraEm(),
                    resultado.nomeBeneficio()
            );
        }
    }

    public static class BeneficioNaoEncontradoException extends RuntimeException {
        public BeneficioNaoEncontradoException() { super("Benefício não encontrado para o tutor logado."); }
    }

    @ExceptionHandler(BeneficioNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(BeneficioNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "BENEFICIO_NAO_ENCONTRADO",
                "mensagem", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflito(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", "CONFLITO",
                "mensagem", e.getMessage()));
    }
}
