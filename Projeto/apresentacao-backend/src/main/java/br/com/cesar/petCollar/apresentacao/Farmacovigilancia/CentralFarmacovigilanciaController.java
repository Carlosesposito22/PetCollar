package br.com.cesar.petCollar.apresentacao.Farmacovigilancia;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.Period;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.ConsultarPlanoNutricionalUseCase;
import br.com.cesar.petCollar.aplicacao.Farmacovigilancia.ConsultarPrescricaoUseCase;
import br.com.cesar.petCollar.aplicacao.Farmacovigilancia.CriarEFinalizarPrescricaoUseCase;
import br.com.cesar.petCollar.aplicacao.Farmacovigilancia.ListarCatalogoMedicamentosUseCase;
import br.com.cesar.petCollar.aplicacao.Farmacovigilancia.ListarTemplatesPrescricaoUseCase;
import br.com.cesar.petCollar.aplicacao.Farmacovigilancia.ValidarPrescricaoUseCase;
import br.com.cesar.petCollar.apresentacao.Farmacovigilancia.FarmacovigilanciaDTOs.ContextoPacienteDTO;
import br.com.cesar.petCollar.apresentacao.Farmacovigilancia.FarmacovigilanciaDTOs.HistoricoDTO;
import br.com.cesar.petCollar.apresentacao.Farmacovigilancia.FarmacovigilanciaDTOs.InteracaoDTO;
import br.com.cesar.petCollar.apresentacao.Farmacovigilancia.FarmacovigilanciaDTOs.MedicamentoDTO;
import br.com.cesar.petCollar.apresentacao.Farmacovigilancia.FarmacovigilanciaDTOs.PrescricaoDTO;
import br.com.cesar.petCollar.apresentacao.Farmacovigilancia.FarmacovigilanciaDTOs.RequisicaoFinalizarDTO;
import br.com.cesar.petCollar.apresentacao.Farmacovigilancia.FarmacovigilanciaDTOs.RequisicaoValidarDTO;
import br.com.cesar.petCollar.apresentacao.Farmacovigilancia.FarmacovigilanciaDTOs.ResultadoValidacaoDTO;
import br.com.cesar.petCollar.apresentacao.Farmacovigilancia.FarmacovigilanciaDTOs.TemplateDTO;
import br.com.cesar.petCollar.apresentacao.PortalTutor.Paciente;
import br.com.cesar.petCollar.apresentacao.PortalTutor.PacienteJpaRepository;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.PacienteRecepcaoJpa;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.PacienteRecepcaoJpaRepository;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.TutorRecepcaoJpaRepository;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricional;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Frequencia;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.IMedicamentoRepositorio;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Medicamento;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.MedicamentoId;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.ViaAdministracao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.Prescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.PrescricaoId;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.TagClinica;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.validacao.RascunhoItem;
import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

/**
 * F-12 — endpoints do médico para a Central de Farmacovigilância. Protegido
 * pelo SecurityConfig (perfil MEDICO_VETERINARIO).
 */
@RestController
@RequestMapping("/api/medico/farmacovigilancia")
public class CentralFarmacovigilanciaController {

    private final ValidarPrescricaoUseCase validar;
    private final CriarEFinalizarPrescricaoUseCase criarEFinalizar;
    private final ListarCatalogoMedicamentosUseCase listarCatalogo;
    private final ListarTemplatesPrescricaoUseCase listarTemplates;
    private final ConsultarPrescricaoUseCase consultar;
    private final IMedicamentoRepositorio medicamentosRepo;
    private final PacienteJpaRepository pacientesPortalRepo;
    private final PacienteRecepcaoJpaRepository pacientesRecepcaoRepo;
    private final TutorRecepcaoJpaRepository tutoresRepo;
    private final ConsultarPlanoNutricionalUseCase consultarPlano;

    public CentralFarmacovigilanciaController(
            ValidarPrescricaoUseCase validar,
            CriarEFinalizarPrescricaoUseCase criarEFinalizar,
            ListarCatalogoMedicamentosUseCase listarCatalogo,
            ListarTemplatesPrescricaoUseCase listarTemplates,
            ConsultarPrescricaoUseCase consultar,
            IMedicamentoRepositorio medicamentosRepo,
            PacienteJpaRepository pacientesPortalRepo,
            PacienteRecepcaoJpaRepository pacientesRecepcaoRepo,
            TutorRecepcaoJpaRepository tutoresRepo,
            ConsultarPlanoNutricionalUseCase consultarPlano) {
        this.validar = validar;
        this.criarEFinalizar = criarEFinalizar;
        this.listarCatalogo = listarCatalogo;
        this.listarTemplates = listarTemplates;
        this.consultar = consultar;
        this.medicamentosRepo = medicamentosRepo;
        this.pacientesPortalRepo = pacientesPortalRepo;
        this.pacientesRecepcaoRepo = pacientesRecepcaoRepo;
        this.tutoresRepo = tutoresRepo;
        this.consultarPlano = consultarPlano;
    }

    /** Catálogo completo de medicamentos. */
    @GetMapping("/medicamentos")
    public List<MedicamentoDTO> catalogo() {
        return listarCatalogo.executar().stream().map(MedicamentoDTO::de).toList();
    }

    /** Matriz de interação completa (somente os pares relevantes do catálogo). */
    @GetMapping("/medicamentos/interacoes")
    public List<InteracaoDTO> interacoes() {
        List<MedicamentoId> ids = listarCatalogo.executar().stream().map(Medicamento::getId).toList();
        return medicamentosRepo.buscarInteracoesEntre(ids).stream().map(InteracaoDTO::de).toList();
    }

    /** Templates pré-configurados (Gastroproteção, Antiemético, Antibiótico). */
    @GetMapping("/templates")
    public List<TemplateDTO> templates() {
        return listarTemplates.executar().stream().map(TemplateDTO::de).toList();
    }

    /** Validação ao vivo — não persiste, devolve violações e dose máx. segura por item. */
    @PostMapping("/validar")
    public ResultadoValidacaoDTO validar(@RequestBody RequisicaoValidarDTO req) {
        var entrada = new ValidarPrescricaoUseCase.Entrada(
                req.pesoPacienteKg(),
                parseTags(req.tagsClinicas()),
                req.alergias() == null ? Set.of() : Set.copyOf(req.alergias()),
                req.itens().stream().map(this::toRascunho).toList());
        return ResultadoValidacaoDTO.de(validar.executar(entrada));
    }

    /** Cria + finaliza atomicamente. Mesmo padrão da F-11 (rejeita se houver BLOQUEIO). */
    @PostMapping("/finalizar-direto")
    public PrescricaoDTO finalizarDireto(@RequestBody RequisicaoFinalizarDTO req, Principal principal) {
        List<CriarEFinalizarPrescricaoUseCase.DadosItem> itens = req.itens().stream()
                .map(it -> new CriarEFinalizarPrescricaoUseCase.DadosItem(
                        MedicamentoId.de(it.medicamentoId()),
                        it.doseMgPorKg(),
                        it.duracaoDias(),
                        Frequencia.valueOf(it.frequencia()),
                        ViaAdministracao.valueOf(it.via()),
                        it.horarios() == null ? List.of() : it.horarios(),
                        it.notaCuidado()))
                .toList();
        List<RascunhoItem> rascunhos = req.itens().stream()
                .map(it -> new RascunhoItem(
                        MedicamentoId.de(it.medicamentoId()),
                        it.doseMgPorKg(),
                        it.duracaoDias(),
                        Frequencia.valueOf(it.frequencia()),
                        ViaAdministracao.valueOf(it.via())))
                .toList();
        var entrada = new CriarEFinalizarPrescricaoUseCase.Entrada(
                PacienteId.de(req.pacienteId()),
                TutorId.de(req.tutorId()),
                MedicoId.de(principal.getName()),
                req.pesoPacienteKg(),
                parseTags(req.tagsClinicas()),
                req.alergias() == null ? Set.of() : Set.copyOf(req.alergias()),
                rascunhos, itens,
                req.instrucoesGerais() == null ? List.of() : req.instrucoesGerais(),
                req.imagemAssinaturaBase64());
        return PrescricaoDTO.de(criarEFinalizar.executar(entrada));
    }

    /** Prescrição vigente do paciente — 204 se não houver. */
    @GetMapping("/pacientes/{pacienteId}/vigente")
    public ResponseEntity<PrescricaoDTO> vigenteDoPaciente(@PathVariable String pacienteId) {
        return consultar.buscarVigenteDoPaciente(PacienteId.de(pacienteId))
                .map(PrescricaoDTO::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /** Histórico de prescrições do paciente (FINALIZADA + SUBSTITUIDA). */
    @GetMapping("/pacientes/{pacienteId}/historico")
    public HistoricoDTO historicoDoPaciente(@PathVariable String pacienteId) {
        List<Prescricao> historico = consultar.listarHistoricoDoPaciente(PacienteId.de(pacienteId));
        Map<String, String> nomes = new HashMap<>();
        for (Medicamento m : listarCatalogo.executar()) nomes.put(m.getId().getValor(), m.getNome());
        return new HistoricoDTO(historico.stream().map(PrescricaoDTO::de).toList(), nomes);
    }

    /** Detalhe de uma prescrição específica. */
    @GetMapping("/{prescricaoId}")
    public ResponseEntity<PrescricaoDTO> detalhe(@PathVariable String prescricaoId) {
        return consultar.buscarPorId(PrescricaoId.de(prescricaoId))
                .map(PrescricaoDTO::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Contexto do paciente — resolve tutorId, peso atual, idade, alergias e
     * tags clínicas DERIVADAS (geriátrico por idade > 7, insuficiência renal
     * via comorbidade do plano nutricional vigente da F-11).
     */
    @GetMapping("/pacientes/{pacienteId}/contexto")
    public ResponseEntity<ContextoPacienteDTO> contexto(@PathVariable String pacienteId) {
        Optional<ContextoPacienteDTO> doPortal = pacientesPortalRepo.findById(pacienteId)
                .map(jpa -> jpa.toDomain())
                .map(p -> montar(p.id(), p.tutorId(), p.nome(),
                        p.pesoKg() == null ? BigDecimal.ZERO : BigDecimal.valueOf(p.pesoKg()),
                        p.idadeEmAnos()));
        if (doPortal.isPresent()) return ResponseEntity.ok(doPortal.get());

        return pacientesRecepcaoRepo.findById(pacienteId)
                .map(this::montarDaRecepcao)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ContextoPacienteDTO montarDaRecepcao(PacienteRecepcaoJpa pac) {
        LocalDate nasc = pac.getNascimento();
        int idade = nasc == null ? 0 : Math.max(0, Period.between(nasc, LocalDate.now()).getYears());
        BigDecimal peso = pac.getPesoKg() == null ? BigDecimal.ZERO : BigDecimal.valueOf(pac.getPesoKg());
        return montar(pac.getId(), pac.getTutorId(), pac.getNome(), peso, idade);
    }

    private ContextoPacienteDTO montar(String pacienteId, String tutorId,
                                        String nomePet, BigDecimal pesoKg, int idadeAnos) {
        String nomeTutor = tutoresRepo.findById(tutorId).map(t -> t.getNome()).orElse("Tutor");

        // Tags clínicas DERIVADAS: geriátrico (idade) + renal (comorbidade da F-11)
        Set<TagClinica> tags = EnumSet.noneOf(TagClinica.class);
        if (idadeAnos > 7) tags.add(TagClinica.GERIATRICO);
        consultarPlano.buscarVigenteDoPaciente(PacienteId.de(pacienteId))
                .ifPresent((PlanoNutricional plano) -> {
                    Comorbidade c = plano.getParametros().comorbidade();
                    if (c == Comorbidade.DOENCA_RENAL) tags.add(TagClinica.INSUFICIENCIA_RENAL);
                });

        // Alergias: por enquanto não há tabela própria — devolve vazio, a UI permite o médico inserir manualmente.
        return new ContextoPacienteDTO(
                pacienteId, tutorId, nomePet, nomeTutor,
                pesoKg, idadeAnos,
                List.of(),
                tags.stream().map(TagClinica::name).toList());
    }

    private Set<TagClinica> parseTags(List<String> nomes) {
        if (nomes == null || nomes.isEmpty()) return EnumSet.noneOf(TagClinica.class);
        return nomes.stream().map(TagClinica::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(TagClinica.class)));
    }

    private RascunhoItem toRascunho(FarmacovigilanciaDTOs.RequisicaoRascunhoItemDTO it) {
        return new RascunhoItem(
                MedicamentoId.de(it.medicamentoId()),
                it.doseMgPorKg(),
                it.duracaoDias(),
                Frequencia.valueOf(it.frequencia()),
                ViaAdministracao.valueOf(it.via()));
    }
}
