package br.com.cesar.petCollar.apresentacao.RecepcaoTriagem;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/recepcao")
public class BuscaTutorController {

    public static final List<SintomaDTO> CATALOGO = List.of(
        new SintomaDTO("S01", "Febre (>39°C)",             4),
        new SintomaDTO("S02", "Vômito frequente",           3),
        new SintomaDTO("S03", "Diarreia com sangue",        4),
        new SintomaDTO("S04", "Prostração intensa",         3),
        new SintomaDTO("S05", "Dificuldade respiratória",   5),
        new SintomaDTO("S06", "Convulsão",                  6),
        new SintomaDTO("S07", "Sangramento externo",        5),
        new SintomaDTO("S08", "Perda de apetite",           2),
        new SintomaDTO("S09", "Coceira / prurido",          1),
        new SintomaDTO("S10", "Espirros / coriza",          2),
        new SintomaDTO("S11", "Lesão de pele",              2),
        new SintomaDTO("S12", "Dor ao toque",               3),
        new SintomaDTO("S13", "Inchaço / edema",            3),
        new SintomaDTO("S14", "Desmaio / síncope",          5),
        new SintomaDTO("S15", "Urina com sangue",           4)
    );

    private final TutorRecepcaoJpaRepository tutorRepo;
    private final PacienteRecepcaoJpaRepository pacienteRepo;
    private final TriagemJpaRepository triagemRepo;
    private final FilaAtendimentoEmMemoria fila;

    public BuscaTutorController(TutorRecepcaoJpaRepository tutorRepo,
                                PacienteRecepcaoJpaRepository pacienteRepo,
                                TriagemJpaRepository triagemRepo,
                                FilaAtendimentoEmMemoria fila) {
        this.tutorRepo    = tutorRepo;
        this.pacienteRepo = pacienteRepo;
        this.triagemRepo  = triagemRepo;
        this.fila         = fila;
    }

    // ── F01: Busca tutor por CPF ──────────────────────────────────────────────

    @GetMapping("/tutores")
    public ResponseEntity<?> buscarPorCpf(@RequestParam String cpf) {
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        if (cpfLimpo.length() != 11)
            return ResponseEntity.badRequest()
                .body(Map.of("mensagem", "CPF inválido. Informe 11 dígitos."));

        Optional<TutorRecepcaoJpa> opt = tutorRepo.findByCpf(cpfLimpo);
        if (opt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensagem", "Tutor não encontrado no sistema"));

        return ResponseEntity.ok(montarRespostaTutor(opt.get()));
    }

    @PostMapping("/tutores")
    public ResponseEntity<?> cadastrarTutor(@Valid @RequestBody RequisicaoCadastroTutor req) {
        String cpfLimpo = req.cpf().replaceAll("[^0-9]", "");
        if (cpfLimpo.length() != 11)
            return ResponseEntity.badRequest()
                .body(Map.of("mensagem", "CPF inválido. Informe 11 dígitos."));
        if (tutorRepo.existsByCpf(cpfLimpo))
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensagem", "CPF já cadastrado no sistema."));

        TutorRecepcaoJpa novo = new TutorRecepcaoJpa(
            UUID.randomUUID().toString(), req.nome(), cpfLimpo,
            req.telefone(), req.email(), LocalDateTime.now());
        tutorRepo.save(novo);
        return ResponseEntity.status(HttpStatus.CREATED).body(montarRespostaTutor(novo));
    }

    @PutMapping("/tutores/{id}")
    public ResponseEntity<?> editarTutor(@PathVariable String id,
                                          @Valid @RequestBody RequisicaoEdicaoTutor req) {
        TutorRecepcaoJpa t = tutorRepo.findById(id)
            .orElseThrow(TutorNaoEncontradoException::new);
        t.setNome(req.nome());
        t.setTelefone(req.telefone());
        t.setEmail(req.email());
        tutorRepo.save(t);
        return ResponseEntity.ok(montarRespostaTutor(t));
    }

    @DeleteMapping("/tutores/{id}")
    public ResponseEntity<Void> excluirTutor(@PathVariable String id) {
        if (!tutorRepo.existsById(id)) throw new TutorNaoEncontradoException();
        pacienteRepo.findByTutorId(id).forEach(pacienteRepo::delete);
        tutorRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Pacientes do tutor ────────────────────────────────────────────────────

    @GetMapping("/tutores/{tutorId}/pacientes")
    public List<PacienteDTO> listarPacientes(@PathVariable String tutorId) {
        return pacienteRepo.findByTutorId(tutorId).stream()
            .map(PacienteDTO::de).toList();
    }

    @PostMapping("/tutores/{tutorId}/pacientes")
    public ResponseEntity<PacienteDTO> cadastrarPaciente(
            @PathVariable String tutorId,
            @Valid @RequestBody RequisicaoPaciente req) {
        if (!tutorRepo.existsById(tutorId))
            throw new TutorNaoEncontradoException();
        PacienteRecepcaoJpa p = new PacienteRecepcaoJpa(
            UUID.randomUUID().toString(), tutorId,
            req.nome(), req.especie(), req.raca(), req.nascimento());
        pacienteRepo.save(p);
        return ResponseEntity.status(HttpStatus.CREATED).body(PacienteDTO.de(p));
    }

    @PutMapping("/tutores/{tutorId}/pacientes/{pacienteId}")
    public ResponseEntity<PacienteDTO> editarPaciente(
            @PathVariable String tutorId,
            @PathVariable String pacienteId,
            @Valid @RequestBody RequisicaoPaciente req) {
        PacienteRecepcaoJpa p = pacienteRepo.findById(pacienteId)
            .filter(x -> x.getTutorId().equals(tutorId))
            .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));
        p.setNome(req.nome());
        p.setEspecie(req.especie());
        p.setRaca(req.raca());
        p.setNascimento(req.nascimento());
        pacienteRepo.save(p);
        return ResponseEntity.ok(PacienteDTO.de(p));
    }

    @DeleteMapping("/tutores/{tutorId}/pacientes/{pacienteId}")
    public ResponseEntity<Void> excluirPaciente(
            @PathVariable String tutorId,
            @PathVariable String pacienteId) {
        PacienteRecepcaoJpa p = pacienteRepo.findById(pacienteId)
            .filter(x -> x.getTutorId().equals(tutorId))
            .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));
        pacienteRepo.delete(p);
        return ResponseEntity.noContent().build();
    }

    // ── F02: Triagem ──────────────────────────────────────────────────────────

    @GetMapping("/sintomas")
    public List<SintomaDTO> listarSintomas() {
        return CATALOGO;
    }

    @PostMapping("/tutores/{tutorId}/pacientes/{pacienteId}/triagens")
    public ResponseEntity<TriagemDTO> criarTriagem(
            @PathVariable String tutorId,
            @PathVariable String pacienteId,
            @Valid @RequestBody RequisicaoTriagem req,
            Principal principal) {

        PacienteRecepcaoJpa paciente = pacienteRepo.findById(pacienteId)
            .filter(x -> x.getTutorId().equals(tutorId))
            .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        var emElaboracao = triagemRepo.findByPacienteIdAndStatus(pacienteId, "EM_ELABORACAO");
        if (!emElaboracao.isEmpty())
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);

        int score = calcularScore(req.codigosSintomas());
        String cor = classificarCor(score);

        TriagemJpa triagem = new TriagemJpa(
            UUID.randomUUID().toString(), pacienteId, tutorId,
            principal != null ? principal.getName() : "recepcao");
        triagem.setScoreTotal(score);
        triagem.setCorDeRisco(cor);
        triagem.setSintomasSelecionados(String.join(",", req.codigosSintomas()));
        triagem.setStatus("FINALIZADA");
        triagem.setFinalizadaEm(LocalDateTime.now());
        triagemRepo.save(triagem);

        fila.inserir(new FilaAtendimentoEmMemoria.ItemFila(
            pacienteId, triagem.getId(), cor, triagem.getFinalizadaEm(),
            paciente.getNome(), tutorId));

        boolean grave = req.codigosSintomas().stream()
            .anyMatch(c -> List.of("S05","S06","S07","S14").contains(c));
        if (score >= 10 || grave) {
            paciente.setInfectocontagiosoRecente(true, LocalDateTime.now());
            pacienteRepo.save(paciente);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(TriagemDTO.de(triagem));
    }

    @GetMapping("/fila")
    public List<FilaAtendimentoEmMemoria.ItemFilaDTO> listarFila() {
        return fila.listar();
    }

    @DeleteMapping("/fila/{triagemId}")
    public ResponseEntity<Void> removerDaFila(@PathVariable String triagemId) {
        fila.remover(triagemId);
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private RespostaTutorDTO montarRespostaTutor(TutorRecepcaoJpa t) {
        List<PacienteRecepcaoJpa> pacientes = pacienteRepo.findByTutorId(t.getId());
        boolean alerta = pacientes.stream().anyMatch(p ->
            p.isInfectocontagiosoRecente()
            && p.getDataUltimoDiagnostico() != null
            && p.getDataUltimoDiagnostico().isAfter(LocalDateTime.now().minusDays(40)));
        return new RespostaTutorDTO(
            t.getId(), t.getNome(), t.getCpf(), t.getTelefone(), t.getEmail(),
            alerta,
            pacientes.stream().map(PacienteDTO::de).toList());
    }

    private int calcularScore(List<String> codigos) {
        return CATALOGO.stream()
            .filter(s -> codigos.contains(s.codigo()))
            .mapToInt(SintomaDTO::peso)
            .sum();
    }

    private String classificarCor(int score) {
        if (score >= 10) return "VERMELHO";
        if (score >= 5)  return "AMARELO";
        return "VERDE";
    }

    // ── DTOs e Records ────────────────────────────────────────────────────────

    public record RespostaTutorDTO(
        String id, String nome, String cpf, String telefone, String email,
        boolean alertaEpidemiologico, List<PacienteDTO> pacientes) {}

    public record PacienteDTO(
        String id, String tutorId, String nome, String especie,
        String raca, LocalDate nascimento, boolean infectocontagiosoRecente) {
        public static PacienteDTO de(PacienteRecepcaoJpa p) {
            return new PacienteDTO(p.getId(), p.getTutorId(), p.getNome(),
                p.getEspecie(), p.getRaca(), p.getNascimento(),
                p.isInfectocontagiosoRecente());
        }
    }

    public record TriagemDTO(
        String id, String pacienteId, int scoreTotal,
        String corDeRisco, String status, LocalDateTime finalizadaEm) {
        public static TriagemDTO de(TriagemJpa t) {
            return new TriagemDTO(t.getId(), t.getPacienteId(), t.getScoreTotal(),
                t.getCorDeRisco(), t.getStatus(), t.getFinalizadaEm());
        }
    }

    public record SintomaDTO(String codigo, String descricao, int peso) {}

    public record RequisicaoCadastroTutor(
        @NotBlank @Size(min = 11, max = 14) String cpf,
        @NotBlank @Size(min = 3, max = 120) String nome,
        String telefone, String email) {}

    public record RequisicaoEdicaoTutor(
        @NotBlank @Size(min = 3, max = 120) String nome,
        String telefone, String email) {}

    public record RequisicaoPaciente(
        @NotBlank String nome, String especie, String raca, LocalDate nascimento) {}

    public record RequisicaoTriagem(List<String> codigosSintomas) {}

    // ── Exception handlers ────────────────────────────────────────────────────

    public static class TutorNaoEncontradoException extends RuntimeException {
        public TutorNaoEncontradoException() { super("Tutor não encontrado."); }
    }

    @ExceptionHandler(TutorNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> tratar(TutorNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> tratarGenerico(RuntimeException e) {
        return ResponseEntity.badRequest()
            .body(Map.of("mensagem", e.getMessage()));
    }
}