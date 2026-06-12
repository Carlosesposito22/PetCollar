package br.com.cesar.petCollar.apresentacao.PortalTutor;

import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.CicloVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.CicloVacinalService;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.DoseVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.StatusDoseVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.TipoProtocolo;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.VacinaId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.aplicacao.BeneficiosPlano.ConsumirBeneficioUseCase;
import br.com.cesar.petCollar.aplicacao.BeneficiosPlano.ConsumirBeneficioUseCase.Categoria;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    private static final String VACINACAO_INDISPONIVEL =
            "A vacinação não está disponível de acordo com o seu plano.";

    private final PortalTutorRepositorio portalRepositorio;
    private final CicloVacinalService cicloVacinalService;
    private final ConsumirBeneficioUseCase consumirBeneficio;

    public VacinacaoController(PortalTutorRepositorio portalRepositorio,
                                CicloVacinalService cicloVacinalService,
                                ConsumirBeneficioUseCase consumirBeneficio) {
        this.portalRepositorio   = portalRepositorio;
        this.cicloVacinalService = cicloVacinalService;
        this.consumirBeneficio   = consumirBeneficio;
    }

    /** Retorna a carteira completa de vacinação do paciente (RN-072). */
    @GetMapping
    public CarteiraDTO carteira(@PathVariable String pacienteId, Principal principal) {
        Paciente paciente = obterPacienteDoTutor(pacienteId, principal);
        List<CicloVacinal> ciclos = cicloVacinalService.listarPorPaciente(PacienteId.de(pacienteId));
        List<CicloDTO> ciclosDTO = ciclos.stream().map(c -> {
            LocalDate sugerida = c.podeAgendarProximaDose()
                ? calcularSugerida(c) : null;
            return CicloDTO.de(c, sugerida);
        }).toList();
        return new CarteiraDTO(paciente.nome(), paciente.especie(), paciente.raca(), ciclosDTO);
    }

    /** Cria um novo ciclo vacinal com a primeira dose (RN-075). */
    @PostMapping
    public ResponseEntity<CicloDTO> agendarNova(@PathVariable String pacienteId,
                                                 @Valid @RequestBody RequisicaoNovaVacina req,
                                                 Principal principal) {
        obterPacienteDoTutor(pacienteId, principal);
        TutorId tutorId = TutorId.de(principal.getName());
        TipoProtocolo protocolo = resolverProtocolo(req.tipoProtocolo());
        // Gateia a dose pelo benefício "Vacinação" do plano (carência + limite).
        consumirBeneficio.consumir(tutorId, Categoria.VACINACAO, VACINACAO_INDISPONIVEL);
        try {
            CicloVacinal ciclo = cicloVacinalService.criarCicloComPrimeiraDose(
                PacienteId.de(pacienteId), req.ciclo(), protocolo,
                req.totalDoses() != null && req.totalDoses() > 0 ? req.totalDoses() : 1,
                req.intervaloDias(), req.data());
            LocalDate sugerida = ciclo.podeAgendarProximaDose() ? calcularSugerida(ciclo) : null;
            return ResponseEntity.status(HttpStatus.CREATED).body(CicloDTO.de(ciclo, sugerida));
        } catch (RuntimeException e) {
            consumirBeneficio.devolver(tutorId, Categoria.VACINACAO);
            throw e;
        }
    }

    /** Agenda a próxima dose de um ciclo existente; usa a estratégia do protocolo se data omitida (RN-075). */
    @PostMapping("/proxima-dose")
    public ResponseEntity<CicloDTO> agendarProximaDose(@PathVariable String pacienteId,
                                                        @Valid @RequestBody RequisicaoProximaDose req,
                                                        Principal principal) {
        obterPacienteDoTutor(pacienteId, principal);
        TutorId tutorId = TutorId.de(principal.getName());
        CicloVacinal ciclo = cicloVacinalService.buscarCicloPorNome(
            PacienteId.de(pacienteId), req.ciclo());
        // Cada dose agendada debita 1 uso do benefício "Vacinação" do plano.
        consumirBeneficio.consumir(tutorId, Categoria.VACINACAO, VACINACAO_INDISPONIVEL);
        try {
            CicloVacinal atualizado = cicloVacinalService.agendarProximaDose(ciclo.getId(), req.data());
            LocalDate sugerida = atualizado.podeAgendarProximaDose() ? calcularSugerida(atualizado) : null;
            return ResponseEntity.status(HttpStatus.CREATED).body(CicloDTO.de(atualizado, sugerida));
        } catch (RuntimeException e) {
            consumirBeneficio.devolver(tutorId, Categoria.VACINACAO);
            throw e;
        }
    }

    /** Remove um ciclo vacinal (doses pendentes ou registros importados incorretamente). */
    @DeleteMapping("/{cicloId}")
    public ResponseEntity<Void> removerCiclo(@PathVariable String pacienteId,
                                              @PathVariable String cicloId,
                                              Principal principal) {
        obterPacienteDoTutor(pacienteId, principal);
        cicloVacinalService.removerCiclo(VacinaId.de(cicloId));
        return ResponseEntity.noContent().build();
    }

    /** Configura ou remove o lembrete automático de próxima dose (tutor). */
    @PatchMapping("/{cicloId}/lembrete")
    public ResponseEntity<Void> configurarLembrete(@PathVariable String pacienteId,
                                                    @PathVariable String cicloId,
                                                    @RequestBody RequisicaoLembreteDTO req,
                                                    Principal principal) {
        obterPacienteDoTutor(pacienteId, principal);
        cicloVacinalService.configurarLembrete(VacinaId.de(cicloId), req.diasLembrete());
        return ResponseEntity.noContent().build();
    }

    /** Confirma a aplicação de uma dose — exclusivo para médico veterinário (RN-078). */
    @PatchMapping("/{cicloId}/doses/{doseId}/aplicar")
    public ResponseEntity<CicloDTO> aplicarDose(@PathVariable String pacienteId,
                                                 @PathVariable String cicloId,
                                                 @PathVariable String doseId,
                                                 @Valid @RequestBody RequisicaoAplicarDose req,
                                                 Principal principal) {
        obterPacienteDoTutor(pacienteId, principal);
        cicloVacinalService.aplicarDose(
            VacinaId.de(cicloId), VacinaId.de(doseId),
            req.dataAplicacao(), req.medico(), req.lote());
        CicloVacinal ciclo = cicloVacinalService.listarPorPaciente(PacienteId.de(pacienteId))
            .stream()
            .filter(c -> c.getId().getValor().equals(cicloId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Ciclo não encontrado."));
        return ResponseEntity.ok(CicloDTO.de(ciclo, null));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Paciente obterPacienteDoTutor(String pacienteId, Principal principal) {
        Paciente p = portalRepositorio.buscarPaciente(pacienteId)
                .orElseThrow(PacienteController.PacienteNaoEncontradoException::new);
        if (!p.tutorId().equalsIgnoreCase(principal.getName())) {
            throw new PacienteController.PacienteNaoEncontradoException();
        }
        return p;
    }

    private TipoProtocolo resolverProtocolo(String valor) {
        if (valor == null || valor.isBlank()) return TipoProtocolo.PERSONALIZADO;
        try {
            return TipoProtocolo.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return TipoProtocolo.PERSONALIZADO;
        }
    }

    private LocalDate calcularSugerida(CicloVacinal ciclo) {
        try {
            return cicloVacinalService.calcularProximaDataSugerida(ciclo);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    // ── DTOs ─────────────────────────────────────────────────────────────────

    public record RequisicaoNovaVacina(
            @NotBlank String ciclo,
            Integer totalDoses,
            @NotNull LocalDate data,
            String tipoProtocolo,
            Integer intervaloDias
    ) {}

    public record RequisicaoProximaDose(
            @NotBlank String ciclo,
            LocalDate data
    ) {}

    public record RequisicaoLembreteDTO(Integer diasLembrete) {}

    public record RequisicaoAplicarDose(
            @NotNull LocalDate dataAplicacao,
            @NotBlank String medico,
            @NotBlank String lote
    ) {}

    public record DoseDTO(
            String id,
            String ciclo,
            String rotulo,
            int doseNumero,
            int totalDoses,
            String status,
            LocalDate data,
            String medico,
            String lote
    ) {
        static DoseDTO de(DoseVacinal d, String nomeCiclo, int totalDoses) {
            String rotulo = totalDoses > 1
                ? nomeCiclo + " - Dose " + d.getDoseNumero() + "/" + totalDoses
                : nomeCiclo;
            return new DoseDTO(
                d.getId().getValor(), nomeCiclo, rotulo,
                d.getDoseNumero(), totalDoses,
                d.status().name(),
                d.getDataAgendada(),
                d.getMedico(), d.getLote());
        }
    }

    public record CicloDTO(
            String id,
            String ciclo,
            int totalDoses,
            int aplicadas,
            int registradas,
            boolean podeAgendarProxima,
            String tipoProtocolo,
            Integer intervaloDias,
            LocalDate dataProximaDoseSugerida,
            Integer diasLembrete,
            boolean lembreteAtivo,
            List<DoseDTO> doses
    ) {
        static CicloDTO de(CicloVacinal c, LocalDate sugerida) {
            List<DoseDTO> doses = c.getDoses().stream()
                .map(d -> DoseDTO.de(d, c.getNomeCiclo(), c.getTotalDoses()))
                .toList();
            return new CicloDTO(
                c.getId().getValor(),
                c.getNomeCiclo(),
                c.getTotalDoses(),
                c.quantidadeAplicadas(),
                c.getDoses().size(),
                c.podeAgendarProximaDose(),
                c.getTipoProtocolo().name(),
                c.getIntervaloDias(),
                sugerida,
                c.getDiasLembrete(),
                c.lembreteAtivo(),
                doses);
        }
    }

    public record CarteiraDTO(
            String pacienteNome,
            String especie,
            String raca,
            List<CicloDTO> ciclos
    ) {}

    // ── Handlers ─────────────────────────────────────────────────────────────

    @ExceptionHandler(PacienteController.PacienteNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(PacienteController.PacienteNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "PACIENTE_NAO_ENCONTRADO",
                "mensagem", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumentoInvalido(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", "AGENDAMENTO_INVALIDO",
                "mensagem", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflito(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", "CONFLITO",
                "mensagem", e.getMessage()));
    }
}
