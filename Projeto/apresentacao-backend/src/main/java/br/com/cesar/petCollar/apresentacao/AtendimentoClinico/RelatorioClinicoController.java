package br.com.cesar.petCollar.apresentacao.AtendimentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import petCollar.dominio.AtendimentoClinico.relatorio.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/medico/relatorio")
public class RelatorioClinicoController {

    private final RelatorioClinicoService relatorioService;
    private final GeracaoEvolucaoComparativaService evolucaoService;

    public RelatorioClinicoController(RelatorioClinicoService relatorioService,
                                       GeracaoEvolucaoComparativaService evolucaoService) {
        this.relatorioService = relatorioService;
        this.evolucaoService = evolucaoService;
    }

    @PostMapping("/iniciar")
    public ResponseEntity<RelatorioDTO> iniciar(@RequestBody RequisicaoIniciarRelatorioDTO req) {
        TipoRelatorio tipo = req.tipoRelatorio() != null
            ? TipoRelatorio.valueOf(req.tipoRelatorio()) : TipoRelatorio.ROTINEIRO;
        RelatorioClinico relatorio = relatorioService.iniciarRelatorio(
            AtendimentoId.de(req.atendimentoId()),
            PacienteId.de(req.pacienteId()),
            MedicoId.de(req.medicoId()),
            tipo
        );
        return ResponseEntity.ok(RelatorioDTO.de(relatorio));
    }

    @GetMapping("/{relatorioId}")
    public ResponseEntity<RelatorioDTO> buscar(@PathVariable String relatorioId) {
        RelatorioClinico relatorio = relatorioService.buscarPorId(RelatorioClinicoId.de(relatorioId));
        return ResponseEntity.ok(RelatorioDTO.de(relatorio));
    }

    @PatchMapping("/{relatorioId}/sinais-vitais")
    public ResponseEntity<RelatorioDTO> registrarSinaisVitais(@PathVariable String relatorioId,
                                                               @RequestBody RequisicaoSinaisVitaisDTO req) {
        SinaisVitais sinais = new SinaisVitais(
            req.pesoKg(), req.temperaturaCelsius(), req.frequenciaCardiacaBpm(), LocalDateTime.now());
        RelatorioClinico relatorio = evolucaoService.consolidarSinaisVitais(
            RelatorioClinicoId.de(relatorioId), sinais);
        return ResponseEntity.ok(RelatorioDTO.de(relatorio));
    }

    @PatchMapping("/{relatorioId}")
    public ResponseEntity<RelatorioDTO> atualizarConteudo(@PathVariable String relatorioId,
                                                           @RequestBody RequisicaoConteudoDTO req) {
        RelatorioClinico relatorio = relatorioService.atualizarConteudo(
            RelatorioClinicoId.de(relatorioId),
            req.diagnosticoTecnico(),
            req.resumoParaTutor(),
            req.orientacoesManejo(),
            req.cuidadosPosOperatorios(),
            req.tempoRecuperacaoEstimado()
        );
        return ResponseEntity.ok(RelatorioDTO.de(relatorio));
    }

    @PostMapping("/{relatorioId}/medicamento")
    public ResponseEntity<RelatorioDTO> adicionarMedicamento(@PathVariable String relatorioId,
                                                              @RequestBody RequisicaoMedicamentoDTO req) {
        RelatorioClinico relatorio = relatorioService.adicionarMedicamento(
            RelatorioClinicoId.de(relatorioId), req.medicamento());
        return ResponseEntity.ok(RelatorioDTO.de(relatorio));
    }

    @PostMapping("/{relatorioId}/assinar")
    public ResponseEntity<RelatorioDTO> assinar(@PathVariable String relatorioId) {
        RelatorioClinico relatorio = relatorioService.assinarRelatorio(RelatorioClinicoId.de(relatorioId));
        return ResponseEntity.ok(RelatorioDTO.de(relatorio));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<RelatorioDTO>> listarPorPaciente(@PathVariable String pacienteId) {
        List<RelatorioDTO> dtos = relatorioService.listarPorPaciente(PacienteId.de(pacienteId))
            .stream().map(RelatorioDTO::de).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/paciente/{pacienteId}/historico")
    public ResponseEntity<List<RegistroHistoricoDTO>> historico(@PathVariable String pacienteId) {
        List<RegistroHistoricoDTO> dtos = relatorioService
            .buscarHistoricoComparativo(PacienteId.de(pacienteId))
            .stream()
            .filter(r -> r.getSinaisVitais() != null)
            .map(r -> new RegistroHistoricoDTO(
                r.getCriadoEm().toLocalDate().toString(),
                r.getSinaisVitais().getPesoKg(),
                r.getSinaisVitais().getTemperaturaCelsius()
            ))
            .toList();
        return ResponseEntity.ok(dtos);
    }

    // ── DTOs ─────────────────────────────────────────────────────────────────

    record RequisicaoIniciarRelatorioDTO(
        String atendimentoId, String pacienteId, String medicoId, String tipoRelatorio) {}

    record RequisicaoSinaisVitaisDTO(
        double pesoKg, double temperaturaCelsius, int frequenciaCardiacaBpm) {}

    record RequisicaoConteudoDTO(
        String diagnosticoTecnico, String resumoParaTutor, String orientacoesManejo,
        String cuidadosPosOperatorios, String tempoRecuperacaoEstimado) {}

    record RequisicaoMedicamentoDTO(String medicamento) {}

    record RegistroHistoricoDTO(String data, double pesoKg, double temperaturaCelsius) {}

    record RelatorioDTO(
        String id,
        String atendimentoId,
        String pacienteId,
        String medicoId,
        String tipoRelatorio,
        String diagnosticoTecnico,
        String orientacoesManejo,
        String resumoParaTutor,
        String cuidadosPosOperatorios,
        String tempoRecuperacaoEstimado,
        List<String> medicamentosPrescritos,
        double pesoKg,
        double temperaturaCelsius,
        boolean imutavel,
        String criadoEm,
        String assinadoEm
    ) {
        static RelatorioDTO de(RelatorioClinico r) {
            return new RelatorioDTO(
                r.getId().getValor(),
                r.getAtendimentoId().getValor(),
                r.getPacienteId().getValor(),
                r.getMedicoId().getValor(),
                r.getTipoRelatorio().name(),
                r.getDiagnosticoTecnico(),
                r.getOrientacoesManejo(),
                r.getResumoParaTutor(),
                r.getCuidadosPosOperatorios(),
                r.getTempoRecuperacaoEstimado(),
                r.getMedicamentosPrescritos(),
                r.getSinaisVitais() != null ? r.getSinaisVitais().getPesoKg() : 0,
                r.getSinaisVitais() != null ? r.getSinaisVitais().getTemperaturaCelsius() : 0,
                r.isImutavel(),
                r.getCriadoEm() != null ? r.getCriadoEm().toString() : null,
                r.getAssinadoEm() != null ? r.getAssinadoEm().toString() : null
            );
        }
    }
}
