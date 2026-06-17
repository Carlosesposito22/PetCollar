package br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.CalcularNEMPreviewUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.CompararEvolucaoNutricionalUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.CriarEFinalizarPlanoNutricionalUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.ConsultarPlanoNutricionalUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.FinalizarPlanoNutricionalUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.ListarCatalogoRacoesUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.RecomendarRacoesUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.SalvarRascunhoPlanoNutricionalUseCase;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.ContextoPacienteDTO;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.HistoricoEvolutivoDTO;
import br.com.cesar.petCollar.apresentacao.PortalTutor.Paciente;
import br.com.cesar.petCollar.apresentacao.PortalTutor.PacienteJpaRepository;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.PacienteRecepcaoJpa;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.PacienteRecepcaoJpaRepository;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.TutorRecepcaoJpaRepository;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.PlanoNutricionalDTO;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.PreviewNEMDTO;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.RacaoDTO;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.RacaoRecomendadaDTO;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.RequisicaoCriarEFinalizarDTO;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.RequisicaoFinalizarDTO;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.RequisicaoPreviewNEMDTO;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.RequisicaoRascunhoDTO;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.ObservacaoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricionalId;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RacaoId;
import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/medico/nutricao")
public class GestaoNutricionalController {

    private final CalcularNEMPreviewUseCase calcularPreview;
    private final SalvarRascunhoPlanoNutricionalUseCase salvarRascunho;
    private final FinalizarPlanoNutricionalUseCase finalizar;
    private final CriarEFinalizarPlanoNutricionalUseCase criarEFinalizar;
    private final ConsultarPlanoNutricionalUseCase consultar;
    private final RecomendarRacoesUseCase recomendarRacoes;
    private final ListarCatalogoRacoesUseCase listarCatalogoRacoes;
    private final CompararEvolucaoNutricionalUseCase compararEvolucao;
    private final PacienteJpaRepository pacientesPortalRepo;
    private final PacienteRecepcaoJpaRepository pacientesRecepcaoRepo;
    private final TutorRecepcaoJpaRepository tutoresRepo;

    public GestaoNutricionalController(CalcularNEMPreviewUseCase calcularPreview,
                                       SalvarRascunhoPlanoNutricionalUseCase salvarRascunho,
                                       FinalizarPlanoNutricionalUseCase finalizar,
                                       CriarEFinalizarPlanoNutricionalUseCase criarEFinalizar,
                                       ConsultarPlanoNutricionalUseCase consultar,
                                       RecomendarRacoesUseCase recomendarRacoes,
                                       ListarCatalogoRacoesUseCase listarCatalogoRacoes,
                                       CompararEvolucaoNutricionalUseCase compararEvolucao,
                                       PacienteJpaRepository pacientesPortalRepo,
                                       PacienteRecepcaoJpaRepository pacientesRecepcaoRepo,
                                       TutorRecepcaoJpaRepository tutoresRepo) {
        this.calcularPreview = calcularPreview;
        this.salvarRascunho = salvarRascunho;
        this.finalizar = finalizar;
        this.criarEFinalizar = criarEFinalizar;
        this.consultar = consultar;
        this.recomendarRacoes = recomendarRacoes;
        this.listarCatalogoRacoes = listarCatalogoRacoes;
        this.compararEvolucao = compararEvolucao;
        this.pacientesPortalRepo = pacientesPortalRepo;
        this.pacientesRecepcaoRepo = pacientesRecepcaoRepo;
        this.tutoresRepo = tutoresRepo;
    }

    @GetMapping("/pacientes/{pacienteId}/contexto")
    public ResponseEntity<ContextoPacienteDTO> contexto(@PathVariable String pacienteId) {
        Optional<ContextoPacienteDTO> doPortal = pacientesPortalRepo.findById(pacienteId)
                .map(jpa -> jpa.toDomain())
                .map(this::montarContextoPortal);
        if (doPortal.isPresent()) return ResponseEntity.ok(doPortal.get());

        return pacientesRecepcaoRepo.findById(pacienteId)
                .map(this::montarContextoRecepcao)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ContextoPacienteDTO montarContextoPortal(Paciente pac) {
        BigDecimal peso = pac.pesoKg() == null ? BigDecimal.ZERO : BigDecimal.valueOf(pac.pesoKg());
        return new ContextoPacienteDTO(
                pac.id(), pac.tutorId(),
                pac.nome(), nomeDoTutor(pac.tutorId()),
                peso, pac.idadeEmAnos());
    }

    private ContextoPacienteDTO montarContextoRecepcao(PacienteRecepcaoJpa pac) {
        LocalDate nasc = pac.getNascimento();
        int idadeAnos = nasc == null ? 0 : Math.max(0, Period.between(nasc, LocalDate.now()).getYears());
        BigDecimal peso = pac.getPesoKg() == null ? BigDecimal.ZERO : BigDecimal.valueOf(pac.getPesoKg());
        return new ContextoPacienteDTO(
                pac.getId(), pac.getTutorId(),
                pac.getNome(), nomeDoTutor(pac.getTutorId()),
                peso, idadeAnos);
    }

    private String nomeDoTutor(String tutorId) {
        return tutoresRepo.findById(tutorId).map(t -> t.getNome()).orElse("Tutor");
    }

    @PostMapping("/preview")
    public PreviewNEMDTO preview(@RequestBody RequisicaoPreviewNEMDTO req) {
        var resultado = calcularPreview.executar(NutricaoDTOs.paraDominio(req.parametros()));
        return PreviewNEMDTO.de(resultado);
    }

    @PostMapping("/rascunho")
    public PlanoNutricionalDTO salvarRascunho(@RequestBody RequisicaoRascunhoDTO req, Principal principal) {
        var entrada = new SalvarRascunhoPlanoNutricionalUseCase.Entrada(
                PacienteId.de(req.pacienteId()),
                TutorId.de(req.tutorId()),
                MedicoId.de(principal.getName()),
                NutricaoDTOs.paraDominio(req.parametros()),
                req.cronograma() == null ? null : req.cronograma().paraDominio(),
                req.observacoes() == null ? List.of()
                        : req.observacoes().stream().map(ObservacaoNutricional::new).toList(),
                req.racaoId() == null || req.racaoId().isBlank() ? null : RacaoId.de(req.racaoId()),
                req.justificativaDivergencia());
        return PlanoNutricionalDTO.de(salvarRascunho.executar(entrada));
    }

    @GetMapping("/racoes")
    public List<RacaoDTO> catalogoRacoes() {
        return listarCatalogoRacoes.executar().stream().map(RacaoDTO::de).toList();
    }

    @GetMapping("/racoes/recomendacoes")
    public List<RacaoRecomendadaDTO> recomendarRacoes(
            @RequestParam("pesoIdealKg") BigDecimal pesoIdealKg,
            @RequestParam("idadeAnos") int idadeAnos,
            @RequestParam("comorbidade") String comorbidade,
            @RequestParam(value = "topN", defaultValue = "3") int topN) {
        var entrada = new RecomendarRacoesUseCase.Entrada(
                pesoIdealKg, idadeAnos, Comorbidade.valueOf(comorbidade), topN);
        return recomendarRacoes.executar(entrada).stream().map(RacaoRecomendadaDTO::de).toList();
    }

    @GetMapping("/pacientes/{pacienteId}/evolucao")
    public HistoricoEvolutivoDTO evolucao(@PathVariable String pacienteId) {
        return HistoricoEvolutivoDTO.de(compararEvolucao.executar(PacienteId.de(pacienteId)));
    }

    @GetMapping("/pacientes/{pacienteId}/rascunho")
    public ResponseEntity<PlanoNutricionalDTO> rascunhoDoPaciente(@PathVariable String pacienteId) {
        return consultar.buscarRascunhoDoPaciente(PacienteId.de(pacienteId))
                .map(PlanoNutricionalDTO::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/pacientes/{pacienteId}/vigente")
    public ResponseEntity<PlanoNutricionalDTO> vigenteDoPaciente(@PathVariable String pacienteId) {
        return consultar.buscarVigenteDoPaciente(PacienteId.de(pacienteId))
                .map(PlanoNutricionalDTO::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/pacientes/{pacienteId}/finalizados")
    public List<PlanoNutricionalDTO> finalizadosDoPaciente(@PathVariable String pacienteId) {
        return consultar.listarFinalizadosDoPaciente(PacienteId.de(pacienteId)).stream()
                .map(PlanoNutricionalDTO::de).toList();
    }

    @PostMapping("/{planoId}/finalizar")
    public PlanoNutricionalDTO finalizar(@PathVariable String planoId,
                                         @RequestBody RequisicaoFinalizarDTO req,
                                         Principal principal) {
        PlanoNutricional finalizado = finalizar.executar(
                PlanoNutricionalId.de(planoId),
                MedicoId.de(principal.getName()),
                req.imagemAssinaturaBase64());
        return PlanoNutricionalDTO.de(finalizado);
    }

    @PostMapping("/finalizar-direto")
    public PlanoNutricionalDTO finalizarDireto(@RequestBody RequisicaoCriarEFinalizarDTO req,
                                               Principal principal) {
        var entrada = new CriarEFinalizarPlanoNutricionalUseCase.Entrada(
                PacienteId.de(req.pacienteId()),
                TutorId.de(req.tutorId()),
                MedicoId.de(principal.getName()),
                NutricaoDTOs.paraDominio(req.parametros()),
                req.cronograma() == null ? null : req.cronograma().paraDominio(),
                req.observacoes() == null ? List.of()
                        : req.observacoes().stream().map(ObservacaoNutricional::new).toList(),
                req.racaoId() == null || req.racaoId().isBlank() ? null : RacaoId.de(req.racaoId()),
                req.justificativaDivergencia(),
                req.imagemAssinaturaBase64());
        return PlanoNutricionalDTO.de(criarEFinalizar.executar(entrada));
    }

    @GetMapping("/{planoId}")
    public ResponseEntity<PlanoNutricionalDTO> detalhe(@PathVariable String planoId) {
        return consultar.buscarPorId(PlanoNutricionalId.de(planoId))
                .map(PlanoNutricionalDTO::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
