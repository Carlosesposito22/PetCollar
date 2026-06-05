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
import br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto.RequisicaoRemarcarDTO;

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

    private final AgendamentoConsultaInicialService inicialService;
    private final AgendamentoRetornoService retornoService;
    private final GestaoAgendamentoService gestaoService;
    private final IConsultaRepositorio consultaRepositorio;

    public AgendamentoController(AgendamentoConsultaInicialService inicialService,
                                 AgendamentoRetornoService retornoService,
                                 GestaoAgendamentoService gestaoService,
                                 IConsultaRepositorio consultaRepositorio) {
        this.inicialService = inicialService;
        this.retornoService = retornoService;
        this.gestaoService = gestaoService;
        this.consultaRepositorio = consultaRepositorio;
    }

    @PostMapping("/consulta-inicial")
    public ResponseEntity<ConsultaDTO> agendarInicial(
            @RequestBody RequisicaoAgendarConsultaInicialDTO req) {
        RequisicaoAgendamento requisicao = new RequisicaoAgendamento(
            PacienteId.de(req.pacienteId()),
            TutorId.de(req.tutorId()),
            MedicoId.de(req.medicoId()),
            EspecialidadeId.de(req.especialidadeId()),
            MotivoConsulta.de(req.motivo()),
            new HorarioConsulta(req.inicio(), req.fim()));
        Consulta consulta = inicialService.agendar(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(ConsultaDTO.de(consulta));
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

    @PutMapping("/{id}/remarcar")
    public ConsultaDTO remarcar(@PathVariable String id, @RequestBody RequisicaoRemarcarDTO req) {
        Consulta consulta = gestaoService.remarcar(
            ConsultaId.de(id), new HorarioConsulta(req.inicio(), req.fim()));
        return ConsultaDTO.de(consulta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable String id) {
        gestaoService.cancelar(ConsultaId.de(id));
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
