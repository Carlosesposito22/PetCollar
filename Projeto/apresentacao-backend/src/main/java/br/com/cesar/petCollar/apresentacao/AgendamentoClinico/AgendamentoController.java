package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento.AgendamentoConsultaInicialService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento.AgendamentoRetornoService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento.RequisicaoAgendamento;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.FiltroConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.GestaoAgendamentoService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.MotivoConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.StatusConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.TipoConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.EspecialidadeId;
import br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto.ConsultaDTO;
import br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto.RequisicaoAgendarConsultaInicialDTO;
import br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto.RequisicaoAgendarRetornoDTO;
import br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto.RequisicaoFinalizarConsultaDTO;
import br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto.RequisicaoRemarcarDTO;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaExame;
import br.com.cesar.petCollar.aplicacao.BeneficiosPlano.ConsumirBeneficioUseCase;
import br.com.cesar.petCollar.aplicacao.BeneficiosPlano.ConsumirBeneficioUseCase.Categoria;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Endpoints de agendamento (consulta inicial e retorno), remarcação, cancelamento e
 * visualização da agenda do tutor (RN 17). Apenas traduz DTO ↔ domínio e delega aos
 * serviços; as exceções sobem para o {@link AgendamentoExceptionHandler}.
 */
@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private static final String CONSULTA_INDISPONIVEL =
            "O agendamento não está disponível de acordo com o seu plano.";

    private final AgendamentoConsultaInicialService inicialService;
    private final AgendamentoRetornoService retornoService;
    private final GestaoAgendamentoService gestaoService;
    private final IConsultaRepositorio consultaRepositorio;
    private final ConsumirBeneficioUseCase consumirBeneficio;
    private final IConsultaExame exameRepositorio;

    public AgendamentoController(AgendamentoConsultaInicialService inicialService,
                                 AgendamentoRetornoService retornoService,
                                 GestaoAgendamentoService gestaoService,
                                 IConsultaRepositorio consultaRepositorio,
                                 ConsumirBeneficioUseCase consumirBeneficio,
                                 IConsultaExame exameRepositorio) {
        this.inicialService = inicialService;
        this.retornoService = retornoService;
        this.gestaoService = gestaoService;
        this.consultaRepositorio = consultaRepositorio;
        this.consumirBeneficio = consumirBeneficio;
        this.exameRepositorio = exameRepositorio;
    }

    @PostMapping("/consulta-inicial")
    public ResponseEntity<ConsultaDTO> agendarInicial(
            @RequestBody RequisicaoAgendarConsultaInicialDTO req) {
        TutorId tutorId = TutorId.de(req.tutorId());
        RequisicaoAgendamento requisicao = new RequisicaoAgendamento(
            PacienteId.de(req.pacienteId()),
            tutorId,
            MedicoId.de(req.medicoId()),
            EspecialidadeId.de(req.especialidadeId()),
            MotivoConsulta.de(req.motivo()),
            new HorarioConsulta(req.inicio(), req.fim()));
        // Gateia a consulta pelo benefício do plano: debita 1 uso (respeitando
        // carência e limite) antes de agendar; devolve o uso se o agendamento falhar.
        consumirBeneficio.consumir(tutorId, Categoria.CONSULTA, CONSULTA_INDISPONIVEL);
        try {
            Consulta consulta = inicialService.agendar(requisicao);
            return ResponseEntity.status(HttpStatus.CREATED).body(ConsultaDTO.de(consulta));
        } catch (RuntimeException e) {
            consumirBeneficio.devolver(tutorId, Categoria.CONSULTA);
            throw e;
        }
    }

    @PostMapping("/retorno")
    public ResponseEntity<ConsultaDTO> agendarRetorno(
            @RequestBody RequisicaoAgendarRetornoDTO req) {
        RequisicaoAgendamento requisicao = new RequisicaoAgendamento(
            PacienteId.de(req.pacienteId()),
            TutorId.de(req.tutorId()),
            MedicoId.de(req.medicoId()),
            EspecialidadeId.de(req.especialidadeId()),
            MotivoConsulta.de(req.motivo()),
            new HorarioConsulta(req.inicio(), req.fim()),
            ConsultaId.de(req.consultaOrigemId()));
        Consulta retorno = retornoService.agendar(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(ConsultaDTO.de(retorno));
    }

    /**
     * Médico finaliza a consulta após o atendimento (RN 7/8). Confirma a consulta se
     * ainda estiver AGENDADA, marca como REALIZADA e, conforme a escolha do médico,
     * transiciona para AGUARDANDO_RETORNO ou EXAMES_SOLICITADOS, tornando o retorno
     * visível para agendamento pelo tutor.
     */
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<ConsultaDTO> finalizar(@PathVariable String id,
                                                 @RequestBody RequisicaoFinalizarConsultaDTO req) {
        ConsultaId consultaId = ConsultaId.de(id);
        Consulta consulta = consultaRepositorio.buscarPorId(consultaId)
            .orElseThrow(() -> new IllegalArgumentException("Consulta não encontrada: " + id));

        // Aceita tanto AGENDADA quanto CONFIRMADA: se o médico atendeu, confirma implicitamente.
        if (consulta.getStatus() == StatusConsulta.AGENDADA) {
            consulta.confirmar();
        }
        consulta.marcarComoRealizada();

        if (req.temRetorno()) {
            if (req.comExames()) {
                consulta.solicitarExames();
                if (req.examesSolicitados() != null) {
                    req.examesSolicitados().stream()
                        .filter(e -> e != null && !e.isBlank())
                        .forEach(e -> exameRepositorio.adicionar(consulta.getId(), e.trim()));
                }
            } else {
                consulta.aguardarRetorno();
            }
        }

        consultaRepositorio.salvar(consulta);
        return ResponseEntity.ok(ConsultaDTO.de(consulta));
    }

    @PutMapping("/{id}/remarcar")
    public ConsultaDTO remarcar(@PathVariable String id, @RequestBody RequisicaoRemarcarDTO req) {
        Consulta consulta = gestaoService.remarcar(
            ConsultaId.de(id), new HorarioConsulta(req.inicio(), req.fim()));
        return ConsultaDTO.de(consulta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable String id) {
        ConsultaId consultaId = ConsultaId.de(id);
        // Captura o tutor antes do cancelamento para devolver o uso do benefício.
        TutorId tutorId = consultaRepositorio.buscarPorId(consultaId)
            .map(Consulta::getTutorId)
            .orElse(null);
        gestaoService.cancelar(consultaId);
        if (tutorId != null) {
            consumirBeneficio.devolver(tutorId, Categoria.CONSULTA);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<ConsultaDTO> listarAgenda(
            @RequestParam String pacienteId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        FiltroConsulta filtro = new FiltroConsulta(
            status == null ? null : StatusConsulta.valueOf(status),
            tipo == null ? null : TipoConsulta.valueOf(tipo),
            inicio, fim);
        return consultaRepositorio.listarPorPaciente(PacienteId.de(pacienteId), filtro).stream()
            .map(ConsultaDTO::de)
            .toList();
    }
}
