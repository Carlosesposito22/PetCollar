package br.com.cesar.petCollar.apresentacao.PortalTutor;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/tutor/pacientes/{pacienteId}/vacinas")
public class VacinacaoController {

    private final PortalTutorRepositorio repositorio;

    public VacinacaoController(PortalTutorRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @GetMapping
    public CarteiraDTO carteira(@PathVariable String pacienteId, Principal principal) {
        Paciente paciente = obterPacienteDoTutor(pacienteId, principal);
        List<CicloDTO> ciclos = agruparEmCiclos(repositorio.listarVacinasDoPaciente(pacienteId));
        return new CarteiraDTO(paciente.nome(), paciente.especie(), paciente.raca(), ciclos);
    }

    /** Agenda uma vacina nova (cria um novo ciclo a partir da dose 1). */
    @PostMapping
    public ResponseEntity<DoseDTO> agendarNova(@PathVariable String pacienteId,
                                               @Valid @RequestBody RequisicaoNovaVacina req,
                                               Principal principal) {
        obterPacienteDoTutor(pacienteId, principal);

        int total = (req.totalDoses() == null || req.totalDoses() < 1) ? 1 : req.totalDoses();
        Integer doseNumero = total > 1 ? 1 : null;
        Integer totalDoses = total > 1 ? total : null;

        Vacina nova = new Vacina(repositorio.novoId(), pacienteId,
                req.ciclo().trim(), doseNumero, totalDoses, false, req.data(), null, null);
        repositorio.salvarVacina(nova);
        return ResponseEntity.status(HttpStatus.CREATED).body(DoseDTO.de(nova));
    }

    /** Agenda a próxima dose de um ciclo existente, numerando automaticamente. */
    @PostMapping("/proxima-dose")
    public ResponseEntity<DoseDTO> agendarProximaDose(@PathVariable String pacienteId,
                                                      @Valid @RequestBody RequisicaoProximaDose req,
                                                      Principal principal) {
        obterPacienteDoTutor(pacienteId, principal);

        List<Vacina> doses = repositorio.listarVacinasDoPaciente(pacienteId).stream()
                .filter(v -> v.ciclo().equalsIgnoreCase(req.ciclo().trim()))
                .toList();

        if (doses.isEmpty()) {
            throw new AgendamentoInvalidoException("Ciclo inexistente. Use 'Agendar nova vacina'.");
        }

        int total = totalDoCiclo(doses);
        int registradas = doses.size();
        if (total <= 1 || registradas >= total) {
            throw new AgendamentoInvalidoException("Este ciclo já tem todas as doses planejadas.");
        }

        int proximo = registradas + 1;
        Vacina nova = new Vacina(repositorio.novoId(), pacienteId,
                doses.get(0).ciclo(), proximo, total, false, req.data(), null, null);
        repositorio.salvarVacina(nova);
        return ResponseEntity.status(HttpStatus.CREATED).body(DoseDTO.de(nova));
    }

    // ── Agrupamento ──────────────────────────────────────────────────────────

    private List<CicloDTO> agruparEmCiclos(List<Vacina> vacinas) {
        Map<String, List<Vacina>> porCiclo = new LinkedHashMap<>();
        for (Vacina v : vacinas) {
            porCiclo.computeIfAbsent(v.ciclo(), k -> new ArrayList<>()).add(v);
        }

        List<CicloDTO> ciclos = new ArrayList<>();
        for (Map.Entry<String, List<Vacina>> e : porCiclo.entrySet()) {
            List<Vacina> doses = new ArrayList<>(e.getValue());
            doses.sort(Comparator.comparingInt(VacinacaoController::numeroOrdinal));

            int total = totalDoCiclo(doses);
            int registradas = doses.size();
            int aplicadas = (int) doses.stream().filter(Vacina::aplicada).count();
            boolean podeAgendarProxima = total > 1 && registradas < total;

            List<DoseDTO> doseDTOs = doses.stream().map(DoseDTO::de).toList();
            ciclos.add(new CicloDTO(e.getKey(), total, aplicadas, registradas, podeAgendarProxima, doseDTOs));
        }
        return ciclos;
    }

    private static int totalDoCiclo(List<Vacina> doses) {
        int declarado = doses.stream()
                .map(v -> v.totalDoses() == null ? 1 : v.totalDoses())
                .max(Integer::compareTo).orElse(1);
        return Math.max(declarado, doses.size());
    }

    private static int numeroOrdinal(Vacina v) {
        return v.doseNumero() == null ? 1 : v.doseNumero();
    }

    private Paciente obterPacienteDoTutor(String pacienteId, Principal principal) {
        Paciente p = repositorio.buscarPaciente(pacienteId)
                .orElseThrow(PacienteController.PacienteNaoEncontradoException::new);
        if (!p.tutorId().equalsIgnoreCase(principal.getName())) {
            throw new PacienteController.PacienteNaoEncontradoException();
        }
        return p;
    }

    // ── DTOs ─────────────────────────────────────────────────────────────────

    public record RequisicaoNovaVacina(
            @NotBlank String ciclo,
            Integer totalDoses,
            @NotNull LocalDate data
    ) {}

    public record RequisicaoProximaDose(
            @NotBlank String ciclo,
            @NotNull LocalDate data
    ) {}

    public record DoseDTO(
            String id, String ciclo, String rotulo,
            Integer doseNumero, Integer totalDoses,
            StatusVacina status, LocalDate data, String medico, String lote
    ) {
        static DoseDTO de(Vacina v) {
            return new DoseDTO(v.id(), v.ciclo(), v.rotulo(),
                    v.doseNumero(), v.totalDoses(),
                    v.status(), v.data(), v.medico(), v.lote());
        }
    }

    public record CicloDTO(
            String ciclo,
            int totalDoses,
            int aplicadas,
            int registradas,
            boolean podeAgendarProxima,
            List<DoseDTO> doses
    ) {}

    public record CarteiraDTO(
            String pacienteNome, String especie, String raca,
            List<CicloDTO> ciclos
    ) {}

    public static class AgendamentoInvalidoException extends RuntimeException {
        public AgendamentoInvalidoException(String msg) { super(msg); }
    }

    @ExceptionHandler(PacienteController.PacienteNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(PacienteController.PacienteNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "PACIENTE_NAO_ENCONTRADO",
                "mensagem", e.getMessage()
        ));
    }

    @ExceptionHandler(AgendamentoInvalidoException.class)
    public ResponseEntity<Map<String, String>> agendamentoInvalido(AgendamentoInvalidoException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", "AGENDAMENTO_INVALIDO",
                "mensagem", e.getMessage()
        ));
    }
}
