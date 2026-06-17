package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.DisponibilidadeAgendaService;
import br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto.HorarioDisponivelDTO;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/medicos")
public class AgendaController {

    private final DisponibilidadeAgendaService disponibilidade;

    public AgendaController(DisponibilidadeAgendaService disponibilidade) {
        this.disponibilidade = disponibilidade;
    }

    @GetMapping("/{id}/horarios-disponiveis")
    public List<HorarioDisponivelDTO> horariosDisponiveis(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return disponibilidade.listarHorariosLivres(MedicoId.de(id), inicio, fim).stream()
            .map(HorarioDisponivelDTO::de)
            .toList();
    }
}
