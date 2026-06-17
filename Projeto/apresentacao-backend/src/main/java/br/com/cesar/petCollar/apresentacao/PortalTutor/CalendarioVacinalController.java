package br.com.cesar.petCollar.apresentacao.PortalTutor;

import br.com.cesar.petCollar.dominio.SaudePreventiva.estrategia.FabricaDeProtocolo;
import br.com.cesar.petCollar.dominio.SaudePreventiva.estrategia.ICalculoProximaDoseStrategy;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.CicloVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.CicloVacinalService;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.DoseVacinal;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tutor/calendario-vacinal")
public class CalendarioVacinalController {

    private final PortalTutorRepositorio portalRepositorio;
    private final CicloVacinalService cicloVacinalService;

    public CalendarioVacinalController(PortalTutorRepositorio portalRepositorio,
                                        CicloVacinalService cicloVacinalService) {
        this.portalRepositorio   = portalRepositorio;
        this.cicloVacinalService = cicloVacinalService;
    }

    @GetMapping
    public CalendarioDTO calendario(@RequestParam String mes, Principal principal) {
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(mes);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Mês inválido — use o formato YYYY-MM (ex.: 2026-07).");
        }

        List<Paciente> pacientes = portalRepositorio.listarPacientesDoTutor(principal.getName());

        List<EventoVacinalDTO> eventos = pacientes.stream()
            .flatMap(p -> {
                List<CicloVacinal> ciclos = cicloVacinalService.listarPorPaciente(PacienteId.de(p.id()));
                return ciclos.stream().flatMap(c -> gerarEventos(c, p, yearMonth).stream());
            })
            .sorted(Comparator.comparing(EventoVacinalDTO::data))
            .toList();

        return new CalendarioDTO(mes, eventos);
    }

    private List<EventoVacinalDTO> gerarEventos(CicloVacinal ciclo, Paciente paciente, YearMonth mes) {
        List<EventoVacinalDTO> eventos = new ArrayList<>();

        for (DoseVacinal dose : ciclo.getDoses()) {
            LocalDate data = dose.dataEfetiva();
            if (YearMonth.from(data).equals(mes)) {
                eventos.add(new EventoVacinalDTO(
                    data,
                    paciente.id(),
                    paciente.nome(),
                    ciclo.getNomeCiclo(),
                    ciclo.getId().getValor(),
                    dose.getId().getValor(),
                    dose.getDoseNumero(),
                    ciclo.getTotalDoses(),
                    dose.status().name(),
                    ciclo.lembreteAtivo()));
            }
        }

        if (ciclo.podeAgendarProximaDose()) {
            try {
                ICalculoProximaDoseStrategy estrategia =
                    FabricaDeProtocolo.criar(ciclo.getTipoProtocolo(), ciclo.getIntervaloDias());
                LocalDate proxData = ciclo.calcularProximaDataComEstrategia(estrategia);
                if (YearMonth.from(proxData).equals(mes)) {
                    int proxNumero = ciclo.getDoses().size() + 1;
                    eventos.add(new EventoVacinalDTO(
                        proxData,
                        paciente.id(),
                        paciente.nome(),
                        ciclo.getNomeCiclo(),
                        ciclo.getId().getValor(),
                        null,
                        proxNumero,
                        ciclo.getTotalDoses(),
                        "PREVISTA",
                        ciclo.lembreteAtivo()));
                }
            } catch (IllegalStateException ignored) {

            }
        }

        return eventos;
    }

    public record EventoVacinalDTO(
            LocalDate data,
            String pacienteId,
            String pacienteNome,
            String nomeCiclo,
            String cicloId,
            String doseId,
            int doseNumero,
            int totalDoses,
            String status,
            boolean lembreteAtivo
    ) {}

    public record CalendarioDTO(String mes, List<EventoVacinalDTO> eventos) {}

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumentoInvalido(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflito(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensagem", e.getMessage()));
    }
}
