package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaExame;
import br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto.ConsultaElegivelRetornoDTO;
import br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto.ExameDTO;
import br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto.RequisicaoLaudoDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints do fluxo de retorno: consultas elegíveis (RN 7), exibição dos exames
 * solicitados (RN 8) e confirmação / laudo de exames pelo tutor (RN 9).
 */
@RestController
@RequestMapping("/api")
public class RetornoController {

    private final IConsultaRepositorio consultaRepositorio;
    private final IConsultaExame exames;

    public RetornoController(IConsultaRepositorio consultaRepositorio, IConsultaExame exames) {
        this.consultaRepositorio = consultaRepositorio;
        this.exames = exames;
    }

    @GetMapping("/pacientes/{id}/consultas-elegiveis-retorno")
    public List<ConsultaElegivelRetornoDTO> elegiveisRetorno(@PathVariable String id) {
        return consultaRepositorio.listarElegiveisRetorno(PacienteId.de(id)).stream()
            .map(ConsultaElegivelRetornoDTO::de)
            .toList();
    }

    @GetMapping("/consultas/{id}/exames-solicitados")
    public List<ExameDTO> examesSolicitados(@PathVariable String id) {
        return exames.listarPorConsultaOrigem(ConsultaId.de(id)).stream()
            .map(ExameDTO::de)
            .toList();
    }

    @PostMapping("/consultas/{id}/exames/{exameId}/confirmar")
    public ResponseEntity<Void> confirmarExame(@PathVariable String id, @PathVariable String exameId) {
        exames.confirmar(exameId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/consultas/{id}/exames/{exameId}/laudo")
    public ResponseEntity<Void> registrarLaudo(@PathVariable String id, @PathVariable String exameId,
                                               @RequestBody RequisicaoLaudoDTO req) {
        exames.registrarLaudo(exameId, req.laudo());
        return ResponseEntity.noContent().build();
    }
}
