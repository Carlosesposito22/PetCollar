package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaExame;
import br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto.ConsultaElegivelRetornoDTO;
import br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto.ExameDTO;
import br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto.RequisicaoLaudoDTO;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.Perfil;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioRepositorio;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RetornoController {

    private final IConsultaRepositorio consultaRepositorio;
    private final IConsultaExame exames;
    private final UsuarioRepositorio usuarioRepositorio;

    public RetornoController(IConsultaRepositorio consultaRepositorio,
                             IConsultaExame exames,
                             UsuarioRepositorio usuarioRepositorio) {
        this.consultaRepositorio = consultaRepositorio;
        this.exames = exames;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @GetMapping("/pacientes/{id}/consultas-elegiveis-retorno")
    public List<ConsultaElegivelRetornoDTO> elegiveisRetorno(@PathVariable String id) {
        return consultaRepositorio.listarElegiveisRetorno(PacienteId.de(id)).stream()
            .map(c -> ConsultaElegivelRetornoDTO.de(c, resolverNomeMedico(c.getMedicoId())))
            .toList();
    }

    private String resolverNomeMedico(MedicoId medicoId) {
        return usuarioRepositorio.buscar(Perfil.MEDICO_VETERINARIO, medicoId.getValor())
            .map(u -> u.nome())
            .orElse(medicoId.getValor());
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
