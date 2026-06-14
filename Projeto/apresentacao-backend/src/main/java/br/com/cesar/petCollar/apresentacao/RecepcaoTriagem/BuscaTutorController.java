package br.com.cesar.petCollar.apresentacao.RecepcaoTriagem;

import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.Perfil;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.StatusConta;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioRepositorio;
import br.com.cesar.petCollar.apresentacao.PortalTutor.Paciente;
import br.com.cesar.petCollar.apresentacao.PortalTutor.PortalTutorRepositorio;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.CicloVacinalService;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.StatusDoseVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.TipoProtocolo;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private final UsuarioRepositorio usuarioRepositorio;
    private final PortalTutorRepositorio portal;
    private final CicloVacinalService cicloVacinalService;

    public BuscaTutorController(TutorRecepcaoJpaRepository tutorRepo,
                                PacienteRecepcaoJpaRepository pacienteRepo,
                                TriagemJpaRepository triagemRepo,
                                FilaAtendimentoEmMemoria fila,
                                UsuarioRepositorio usuarioRepositorio,
                                PortalTutorRepositorio portal,
                                CicloVacinalService cicloVacinalService) {
        this.tutorRepo          = tutorRepo;
        this.pacienteRepo       = pacienteRepo;
        this.triagemRepo        = triagemRepo;
        this.fila               = fila;
        this.usuarioRepositorio = usuarioRepositorio;
        this.portal             = portal;
        this.cicloVacinalService = cicloVacinalService;
    }

    /**
     * Chave única do tutor compartilhada entre a recepção e o portal do tutor.
     * Usamos o e-mail (mesmo identificador de login do tutor no portal), de modo
     * que o paciente cadastrado em qualquer um dos fluxos apareça nos dois.
     * Sem e-mail, cai no id da recepção (visível só internamente).
     */
    private String chaveTutor(TutorRecepcaoJpa t) {
        return (t.getEmail() != null && !t.getEmail().isBlank())
            ? t.getEmail().trim()
            : t.getId();
    }

    /** Aceita tanto o e-mail quanto o ID de recepção como tutorId válido do paciente. */
    private boolean pertenceAoTutor(String pacienteTutorId, TutorRecepcaoJpa t) {
        if (pacienteTutorId == null) return false;
        return pacienteTutorId.equalsIgnoreCase(chaveTutor(t))
            || pacienteTutorId.equalsIgnoreCase(t.getId());
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
        TutorRecepcaoJpa t = tutorRepo.findById(id).orElseThrow(TutorNaoEncontradoException::new);
        portal.listarPacientesDoTutor(chaveTutor(t)).forEach(p -> portal.removerPaciente(p.id()));
        tutorRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Pacientes do tutor ────────────────────────────────────────────────────

    @GetMapping("/tutores/{tutorId}/pacientes")
    public List<PacienteDTO> listarPacientes(@PathVariable String tutorId) {
        TutorRecepcaoJpa t = tutorRepo.findById(tutorId).orElseThrow(TutorNaoEncontradoException::new);
        return portal.listarPacientesDoTutor(chaveTutor(t)).stream()
            .map(PacienteDTO::de).toList();
    }

    @PostMapping("/tutores/{tutorId}/pacientes")
    public ResponseEntity<PacienteDTO> cadastrarPaciente(
            @PathVariable String tutorId,
            @Valid @RequestBody RequisicaoPaciente req) {
        TutorRecepcaoJpa t = tutorRepo.findById(tutorId).orElseThrow(TutorNaoEncontradoException::new);
        Paciente novo = new Paciente(
            portal.novoId(), chaveTutor(t),
            req.nome(), req.especie(), req.raca(), req.nascimento(),
            req.pesoKg(), req.sexo());
        portal.salvarPaciente(novo);
        return ResponseEntity.status(HttpStatus.CREATED).body(PacienteDTO.de(novo));
    }

    @PutMapping("/tutores/{tutorId}/pacientes/{pacienteId}")
    public ResponseEntity<PacienteDTO> editarPaciente(
            @PathVariable String tutorId,
            @PathVariable String pacienteId,
            @Valid @RequestBody RequisicaoPaciente req) {
        TutorRecepcaoJpa t = tutorRepo.findById(tutorId).orElseThrow(TutorNaoEncontradoException::new);
        Paciente p = portal.buscarPaciente(pacienteId)
            .filter(x -> pertenceAoTutor(x.tutorId(), t))
            .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));
        p.atualizar(req.nome(), req.especie(), req.raca(), req.nascimento(),
            req.pesoKg(), req.sexo());
        portal.salvarPaciente(p);
        return ResponseEntity.ok(PacienteDTO.de(p));
    }

    @DeleteMapping("/tutores/{tutorId}/pacientes/{pacienteId}")
    public ResponseEntity<Void> excluirPaciente(
            @PathVariable String tutorId,
            @PathVariable String pacienteId) {
        TutorRecepcaoJpa t = tutorRepo.findById(tutorId).orElseThrow(TutorNaoEncontradoException::new);
        Paciente p = portal.buscarPaciente(pacienteId)
            .filter(x -> pertenceAoTutor(x.tutorId(), t))
            .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));
        portal.removerPaciente(p.id());
        return ResponseEntity.noContent().build();
    }

    // ── F02: Sintomas ─────────────────────────────────────────────────────────

    @GetMapping("/sintomas")
    public List<SintomaDTO> listarSintomas() {
        return CATALOGO;
    }

    // ── F02: Triagem ──────────────────────────────────────────────────────────

    @PostMapping("/tutores/{tutorId}/pacientes/{pacienteId}/triagens")
    public ResponseEntity<?> criarTriagem(
            @PathVariable String tutorId,
            @PathVariable String pacienteId,
            @Valid @RequestBody RequisicaoTriagem req,
            Principal principal) {

        TutorRecepcaoJpa t = tutorRepo.findById(tutorId).orElseThrow(TutorNaoEncontradoException::new);
        Paciente paciente = portal.buscarPaciente(pacienteId)
            .filter(x -> pertenceAoTutor(x.tutorId(), t))
            .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        // RN: não faz sentido triar/atender de novo um paciente que já está na fila.
        if (fila.contemPaciente(pacienteId))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "mensagem", paciente.nome() + " já está na fila de espera. "
                          + "Finalize ou remova o atendimento atual antes de triar novamente."));

        var emElaboracao = triagemRepo.findByPacienteIdAndStatus(pacienteId, "EM_ELABORACAO");
        if (!emElaboracao.isEmpty())
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "mensagem", "Já existe uma triagem em elaboração para este paciente."));

        boolean ehVacina = req.aplicacaoVacina();
        List<String> sintomas = req.codigosSintomas() != null ? req.codigosSintomas() : List.of();

        // Aplicação de vacina não passa por triagem clínica: score 0, classificação VERDE
        // e entra na fila com a MENOR prioridade (flag aplicacaoVacina ordena por último).
        int score = ehVacina ? 0 : calcularScore(sintomas);
        String cor = ehVacina ? "VERDE" : classificarCor(score);

        TriagemJpa triagem = new TriagemJpa(
            UUID.randomUUID().toString(), pacienteId, tutorId,
            principal != null ? principal.getName() : "recepcao");
        triagem.setScoreTotal(score);
        triagem.setCorDeRisco(cor);
        triagem.setSintomasSelecionados(String.join(",", sintomas));
        triagem.setStatus("FINALIZADA");
        triagem.setFinalizadaEm(LocalDateTime.now());
        triagem.setAplicacaoVacina(ehVacina);
        triagemRepo.save(triagem);

        fila.inserir(new FilaAtendimentoEmMemoria.ItemFila(
            pacienteId, triagem.getId(), cor, triagem.getFinalizadaEm(),
            paciente.nome(), tutorId, ehVacina));

        boolean grave = !ehVacina && sintomas.stream()
            .anyMatch(c -> List.of("S05", "S06", "S07", "S14").contains(c));
        if (score >= 10 || grave) {
            paciente.marcarInfectocontagioso(true, LocalDateTime.now());
            portal.salvarPaciente(paciente);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(TriagemDTO.de(triagem));
    }

    // ── Vacinas (recepção) ────────────────────────────────────────────────────

    /**
     * Doses vacinais ainda não aplicadas do paciente. A recepção usa para decidir se
     * pode enviar à fila como "aplicação de vacina" (só se houver dose pendente).
     */
    @GetMapping("/pacientes/{pacienteId}/vacinas-pendentes")
    public List<VacinaPendenteRecepDTO> vacinasPendentes(@PathVariable String pacienteId) {
        return cicloVacinalService.listarPorPaciente(PacienteId.de(pacienteId)).stream()
            .flatMap(c -> c.getDoses().stream()
                .filter(d -> d.status() != StatusDoseVacinal.APLICADA)
                .map(d -> new VacinaPendenteRecepDTO(
                    c.getNomeCiclo(), d.getDoseNumero(), c.getTotalDoses(), d.status().name())))
            .toList();
    }

    /**
     * Cadastra um novo ciclo vacinal para o paciente pela recepção (RN-075). Reflete
     * na carteira do tutor e habilita o médico a aplicar a dose. Mesma lógica do portal.
     */
    @PostMapping("/pacientes/{pacienteId}/vacinas")
    public ResponseEntity<Void> cadastrarVacina(@PathVariable String pacienteId,
                                                @Valid @RequestBody RequisicaoNovaVacinaRecep req) {
        portal.buscarPaciente(pacienteId)
            .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));
        TipoProtocolo protocolo = resolverProtocolo(req.tipoProtocolo());
        cicloVacinalService.criarCicloComPrimeiraDose(
            PacienteId.de(pacienteId), req.ciclo(), protocolo,
            req.totalDoses() != null && req.totalDoses() > 0 ? req.totalDoses() : 1,
            req.intervaloDias(), req.data());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private TipoProtocolo resolverProtocolo(String valor) {
        if (valor == null || valor.isBlank()) return TipoProtocolo.PERSONALIZADO;
        try {
            return TipoProtocolo.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return TipoProtocolo.PERSONALIZADO;
        }
    }

    // ── Fila de atendimento ───────────────────────────────────────────────────

    @GetMapping("/fila")
    public List<FilaAtendimentoEmMemoria.ItemFilaDTO> listarFila() {
        return fila.listar();
    }

    @DeleteMapping("/fila/{triagemId}")
    public ResponseEntity<Void> removerDaFila(@PathVariable String triagemId) {
        fila.remover(triagemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Encaminha um paciente da fila para um médico específico.
     * A recepcionista chama este endpoint após escolher o médico na tela "Chamar".
     */
    @PostMapping("/fila/{triagemId}/encaminhar")
    public ResponseEntity<?> encaminharParaMedico(
            @PathVariable String triagemId,
            @Valid @RequestBody RequisicaoEncaminhar req) {

        boolean ok = fila.encaminhar(triagemId, req.medicoId(), req.nomeMedico());
        if (!ok)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensagem", "Item não encontrado na fila."));

        return ResponseEntity.ok(Map.of(
            "mensagem", "Paciente encaminhado com sucesso.",
            "medicoId", req.medicoId()));
    }

    /**
     * Lista os médicos ativos — acessível à recepcionista apenas neste contexto
     * de encaminhamento. Não expõe senhas nem dados sensíveis.
     */
    @GetMapping("/medicos")
    public List<MedicoDTO> listarMedicos() {
        return usuarioRepositorio.listarPorPerfil(Perfil.MEDICO_VETERINARIO)
            .stream()
            .filter(u -> u.status() == StatusConta.ATIVA)
            .map(u -> new MedicoDTO(u.identificador(), u.nome()))
            .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private RespostaTutorDTO montarRespostaTutor(TutorRecepcaoJpa t) {
        List<Paciente> pacientes = portal.listarPacientesDoTutor(chaveTutor(t));
        boolean alerta = pacientes.stream().anyMatch(p ->
            p.infectocontagiosoRecente()
            && p.dataUltimoDiagnostico() != null
            && p.dataUltimoDiagnostico().isAfter(LocalDateTime.now().minusDays(40)));
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

    // ── DTOs ──────────────────────────────────────────────────────────────────

    public record RespostaTutorDTO(
        String id, String nome, String cpf, String telefone, String email,
        boolean alertaEpidemiologico, List<PacienteDTO> pacientes) {}

    public record PacienteDTO(
        String id, String tutorId, String nome, String especie,
        String raca, LocalDate nascimento, Double pesoKg, String sexo,
        boolean infectocontagiosoRecente) {
        public static PacienteDTO de(Paciente p) {
            return new PacienteDTO(p.id(), p.tutorId(), p.nome(),
                p.especie(), p.raca(), p.nascimento(),
                p.pesoKg(), p.sexo(),
                p.infectocontagiosoRecente());
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

    public record MedicoDTO(String id, String nome) {}

    public record RequisicaoCadastroTutor(
        @NotBlank @Size(min = 11, max = 14) String cpf,
        @NotBlank @Size(min = 3, max = 120) String nome,
        String telefone, String email) {}

    public record RequisicaoEdicaoTutor(
        @NotBlank @Size(min = 3, max = 120) String nome,
        String telefone, String email) {}

    public record RequisicaoPaciente(
        @NotBlank String nome, String especie, String raca, LocalDate nascimento,
        Double pesoKg, String sexo) {}

    public record RequisicaoTriagem(List<String> codigosSintomas, boolean aplicacaoVacina) {}

    public record VacinaPendenteRecepDTO(String ciclo, int doseNumero, int totalDoses, String status) {}

    public record RequisicaoNovaVacinaRecep(
        @NotBlank String ciclo,
        Integer totalDoses,
        @NotNull LocalDate data,
        String tipoProtocolo,
        Integer intervaloDias) {}

    public record RequisicaoEncaminhar(
        @NotBlank String medicoId,
        @NotBlank String nomeMedico) {}

    // ── Exceptions ────────────────────────────────────────────────────────────

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