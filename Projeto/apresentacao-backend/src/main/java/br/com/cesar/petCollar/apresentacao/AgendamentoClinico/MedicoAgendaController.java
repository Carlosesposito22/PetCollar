package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.StatusConsulta;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.Perfil;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioAutenticavel;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioRepositorio;
import br.com.cesar.petCollar.apresentacao.PortalTutor.PacienteJpaRepository;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

/**
 * Endpoint da agenda do médico (F-05): lista as consultas agendadas com ele,
 * filtradas pelo médico autenticado ({@code Principal} = matrícula = medicoId das
 * consultas). Sem data, retorna as consultas de hoje em diante; com {@code data},
 * apenas as do dia informado.
 */
@RestController
@RequestMapping("/api/medico")
public class MedicoAgendaController {

    private final IConsultaRepositorio consultas;
    private final PacienteJpaRepository pacientes;
    private final UsuarioRepositorio usuarios;

    public MedicoAgendaController(IConsultaRepositorio consultas,
                                  PacienteJpaRepository pacientes,
                                  UsuarioRepositorio usuarios) {
        this.consultas = consultas;
        this.pacientes = pacientes;
        this.usuarios = usuarios;
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

    public record AtendimentoMedicoDTO(String consultaId, String pacienteId, String pacienteNome,
                                       String tutorNome, String tipo, String status,
                                       LocalDateTime inicio, LocalDateTime fim) {}
}
