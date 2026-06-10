package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.StatusConsulta;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.CicloVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.CicloVacinalService;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.DoseVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.StatusDoseVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.VacinaId;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.Perfil;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioAutenticavel;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioRepositorio;
import br.com.cesar.petCollar.apresentacao.PortalTutor.Paciente;
import br.com.cesar.petCollar.apresentacao.PortalTutor.PacienteJpa;
import br.com.cesar.petCollar.apresentacao.PortalTutor.PacienteJpaRepository;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.FilaAtendimentoEmMemoria;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.PacienteRecepcaoJpa;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.PacienteRecepcaoJpaRepository;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.TriagemJpa;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.TriagemJpaRepository;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.TutorRecepcaoJpa;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.TutorRecepcaoJpaRepository;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/medico")
public class MedicoAgendaController {

    private final IConsultaRepositorio consultas;
    private final PacienteJpaRepository pacientes;
    private final UsuarioRepositorio usuarios;
    private final FilaAtendimentoEmMemoria fila;
    private final PacienteRecepcaoJpaRepository pacienteRecepcao;
    private final TriagemJpaRepository triagensRepo;
    private final TutorRecepcaoJpaRepository tutoresRecepcao;
    private final CicloVacinalService cicloVacinalService;

    public MedicoAgendaController(IConsultaRepositorio consultas,
                                  PacienteJpaRepository pacientes,
                                  UsuarioRepositorio usuarios,
                                  FilaAtendimentoEmMemoria fila,
                                  PacienteRecepcaoJpaRepository pacienteRecepcao,
                                  TriagemJpaRepository triagensRepo,
                                  TutorRecepcaoJpaRepository tutoresRecepcao,
                                  CicloVacinalService cicloVacinalService) {
        this.consultas           = consultas;
        this.pacientes           = pacientes;
        this.usuarios            = usuarios;
        this.fila                = fila;
        this.pacienteRecepcao    = pacienteRecepcao;
        this.triagensRepo        = triagensRepo;
        this.tutoresRecepcao     = tutoresRecepcao;
        this.cicloVacinalService = cicloVacinalService;
    }

    @GetMapping("/atendimentos")
    public List<AtendimentoMedicoDTO> atendimentos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            Principal principal) {
        MedicoId medicoId = MedicoId.de(principal.getName());

        LocalDateTime inicio = (data != null ? data : LocalDate.now()).atStartOfDay();
        LocalDateTime fim = (data != null) ? data.atTime(LocalTime.MAX) : inicio.plusYears(1);

        return consultas.listarPorMedicoEPeriodo(medicoId, inicio, fim).stream()
            .filter(c -> c.getStatus() != StatusConsulta.CANCELADA)
            .sorted(Comparator.comparing(c -> c.getHorario().getInicio()))
            .map(this::montar)
            .toList();
    }

    /**
     * Prontuário consolidado do paciente para o médico. O paciente pode ter sido
     * cadastrado pela recepção (tabela pacientes_recepcao) ou pelo próprio tutor
     * no portal (tabela pacientes). Buscamos nas duas fontes para que o médico
     * sempre veja os dados, independentemente de como chegou ao atendimento
     * (fila de triagem encaminhada ou consulta agendada).
     */
    @GetMapping("/pacientes/{pacienteId}")
    public ResponseEntity<ProntuarioMedicoDTO> prontuario(@PathVariable String pacienteId) {
        // Fonte canônica unificada: tabela `pacientes` (compartilhada entre recepção e portal).
        Paciente pTutor = pacientes.findById(pacienteId).map(PacienteJpa::toDomain).orElse(null);
        if (pTutor != null) {
            String nomeTutor = usuarios.buscar(Perfil.TUTOR, pTutor.tutorId())
                .map(UsuarioAutenticavel::nome)
                .orElse(pTutor.tutorId());
            return ResponseEntity.ok(montarProntuario(
                pTutor.id(), pTutor.nome(), nomeTutor, pTutor.especie(),
                pTutor.raca(), pTutor.nascimento(), pTutor.pesoKg(), pTutor.sexo(),
                pTutor.infectocontagiosoRecente()));
        }

        // Fallback legado: pacientes antigos só presentes em pacientes_recepcao.
        PacienteRecepcaoJpa pRec = pacienteRecepcao.findById(pacienteId).orElse(null);
        if (pRec != null) {
            String nomeTutor = tutoresRecepcao.findById(pRec.getTutorId())
                .map(TutorRecepcaoJpa::getNome)
                .orElse("—");
            return ResponseEntity.ok(montarProntuario(
                pRec.getId(), pRec.getNome(), nomeTutor, pRec.getEspecie(),
                pRec.getRaca(), pRec.getNascimento(), pRec.getPesoKg(), pRec.getSexo(),
                pRec.isInfectocontagiosoRecente()));
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Atualiza o último peso registrado do paciente ao finalizar o prontuário (F-10).
     * Funciona tanto para pacientes da recepção quanto do portal do tutor.
     */
    @PatchMapping("/pacientes/{pacienteId}/peso")
    public ResponseEntity<Void> atualizarPeso(@PathVariable String pacienteId,
                                              @RequestBody RequisicaoPesoDTO req) {
        // Fonte canônica: tabela `pacientes` unificada.
        PacienteJpa jpa = pacientes.findById(pacienteId).orElse(null);
        if (jpa != null) {
            Paciente d = jpa.toDomain();
            d.atualizar(d.nome(), d.especie(), d.raca(), d.nascimento(), req.pesoKg(), d.sexo());
            pacientes.save(PacienteJpa.fromDomain(d));
            return ResponseEntity.noContent().build();
        }
        // Fallback legado
        PacienteRecepcaoJpa pRec = pacienteRecepcao.findById(pacienteId).orElse(null);
        if (pRec != null) {
            pRec.setPesoKg(req.pesoKg());
            pacienteRecepcao.save(pRec);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Lista as doses vacinais ainda não aplicadas do paciente (carteira do F-06),
     * para o médico aplicá-las durante uma consulta preventiva. A carteira é a mesma
     * vista pelo tutor — ao aplicar, o status reflete no portal do tutor.
     */
    @GetMapping("/pacientes/{pacienteId}/vacinas-pendentes")
    public List<VacinaPendenteDTO> vacinasPendentes(@PathVariable String pacienteId) {
        return cicloVacinalService.listarPorPaciente(PacienteId.de(pacienteId)).stream()
            .flatMap(c -> c.getDoses().stream()
                .filter(d -> d.status() != StatusDoseVacinal.APLICADA)
                .map(d -> new VacinaPendenteDTO(
                    c.getId().getValor(),
                    d.getId().getValor(),
                    c.getNomeCiclo(),
                    c.getTotalDoses() > 1
                        ? c.getNomeCiclo() + " — Dose " + d.getDoseNumero() + "/" + c.getTotalDoses()
                        : c.getNomeCiclo(),
                    d.getDoseNumero(),
                    c.getTotalDoses(),
                    d.status().name(),
                    d.getDataAgendada().toString())))
            .toList();
    }

    /** Confirma a aplicação de uma dose vacinal pelo médico durante a consulta (RN-078). */
    @PostMapping("/pacientes/{pacienteId}/vacinas/aplicar")
    public ResponseEntity<Void> aplicarVacina(@PathVariable String pacienteId,
                                              @RequestBody RequisicaoAplicarVacinaDTO req,
                                              Principal principal) {
        String medico = usuarios.buscar(Perfil.MEDICO_VETERINARIO, principal.getName())
            .map(UsuarioAutenticavel::nome)
            .orElse(principal.getName());
        String lote = (req.lote() != null && !req.lote().isBlank()) ? req.lote() : "—";
        cicloVacinalService.aplicarDose(
            VacinaId.de(req.cicloId()), VacinaId.de(req.doseId()),
            LocalDate.now(), medico, lote);
        return ResponseEntity.noContent().build();
    }

    private ProntuarioMedicoDTO montarProntuario(String id, String nome, String nomeTutor,
                                                 String especie, String raca,
                                                 LocalDate nascimento, Double pesoKg, String sexo,
                                                 boolean infeccioso) {
        int idadeAnos = nascimento != null
            ? Period.between(nascimento, LocalDate.now()).getYears()
            : 0;

        List<TagDTO> tags = new ArrayList<>();
        if (idadeAnos < 1)      tags.add(new TagDTO("Filhote", false));
        else if (idadeAnos < 7) tags.add(new TagDTO("Adulto", false));
        else                    tags.add(new TagDTO("Sênior", false));
        if (infeccioso) tags.add(new TagDTO("Alerta Infeccioso", true));

        List<TriagemResumoDTO> triagens = triagensRepo.findByPacienteId(id).stream()
            .filter(t -> t.getFinalizadaEm() != null)
            .sorted(Comparator.comparing(TriagemJpa::getFinalizadaEm).reversed())
            .map(t -> {
                String motivo = t.getSintomasSelecionados() != null
                    && !t.getSintomasSelecionados().isBlank()
                    ? t.getSintomasSelecionados().split(",").length + " sintoma(s) registrado(s)"
                    : "Triagem realizada";
                return new TriagemResumoDTO(
                    t.getId(),
                    t.getFinalizadaEm().toLocalDate().toString(),
                    motivo,
                    t.getCorDeRisco() != null ? t.getCorDeRisco() : "VERDE",
                    t.getScoreTotal());
            })
            .toList();

        String sexoLabel = "MACHO".equalsIgnoreCase(sexo) ? "Macho"
            : "FEMEA".equalsIgnoreCase(sexo) ? "Fêmea"
            : "—";

        return new ProntuarioMedicoDTO(
            id,
            nome,
            nomeTutor,
            especie != null ? especie : "—",
            raca    != null ? raca    : "—",
            idadeAnos,
            pesoKg != null ? pesoKg : 0.0,
            sexoLabel,
            List.of(),
            tags,
            triagens);
    }

    @GetMapping("/fila-encaminhada")
    public List<FilaEncaminhadaDTO> filaEncaminhada(Principal principal) {
        return fila.listarPorMedico(principal.getName()).stream()
            .map(i -> new FilaEncaminhadaDTO(
                i.pacienteId(), i.triagemId(), i.corDeRisco(),
                i.finalizadaEm(), i.nomePaciente(), i.tutorId()))
            .toList();
    }

    private AtendimentoMedicoDTO montar(Consulta c) {
        String pacienteNome = pacientes.findById(c.getPacienteId().getValor())
            .map(p -> p.toDomain().nome())
            .orElse("Paciente");
        String tutorNome = usuarios.buscar(Perfil.TUTOR, c.getTutorId().getValor())
            .map(UsuarioAutenticavel::nome)
            .orElse("—");
        return new AtendimentoMedicoDTO(
            c.getId().getValor(),
            c.getPacienteId().getValor(),
            pacienteNome,
            tutorNome,
            c.getTipo().name(),
            c.getStatus().name(),
            c.getHorario().getInicio(),
            c.getHorario().getFim());
    }

    public record AtendimentoMedicoDTO(
        String consultaId,
        String pacienteId,
        String pacienteNome,
        String tutorNome,
        String tipo,
        String status,
        LocalDateTime inicio,
        LocalDateTime fim
    ) {}

    public record FilaEncaminhadaDTO(
        String pacienteId,
        String triagemId,
        String corDeRisco,
        LocalDateTime finalizadaEm,
        String nomePaciente,
        String tutorId
    ) {}

    record RequisicaoPesoDTO(Double pesoKg) {}

    record VacinaPendenteDTO(String cicloId, String doseId, String ciclo, String rotulo,
                             int doseNumero, int totalDoses, String status, String dataAgendada) {}

    record RequisicaoAplicarVacinaDTO(String cicloId, String doseId, String lote) {}

    record TagDTO(String rotulo, boolean alerta) {}

    record TriagemResumoDTO(String id, String data, String motivo, String corDeRisco, int pesoTotal) {}

    record ProntuarioMedicoDTO(
        String pacienteId,
        String nomePet,
        String nomeTutor,
        String especie,
        String raca,
        int idadeAnos,
        double pesoKg,
        String sexo,
        List<String> alergias,
        List<TagDTO> tags,
        List<TriagemResumoDTO> triagens
    ) {}
}