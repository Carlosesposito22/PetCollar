package br.com.cesar.petCollar.apresentacao.RecepcaoTriagem;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import petcollar.dominio.recepcaotriagem.triagem.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/recepcao/fila")
public class FilaAtendimentoController {

    private final GestaoFilaAtendimentoService gestaoFila;

    public FilaAtendimentoController(IFilaAtendimentoRepositorio repositorio) {
        this.gestaoFila = new GestaoFilaAtendimentoService(repositorio);
    }

    @GetMapping
    public List<FilaItemDTO> listar() {
        return gestaoFila.listarFila().stream()
                .map(FilaItemDTO::de)
                .toList();
    }

    @PostMapping
    public ResponseEntity<List<FilaItemDTO>> inserir(@RequestBody RequisicaoFilaDTO req) {
        TriagemId triagemId = TriagemId.de(req.triagemId());
        CorDeRisco cor = CorDeRisco.valueOf(req.corDeRisco());
        PacienteId pacienteId = PacienteId.de(req.pacienteId());

        Triagem triagem = new Triagem(triagemId, pacienteId);
        triagem.definirCorDeRisco(cor);
        triagem.finalizar();

        List<FilaItemDTO> filaAtualizada = gestaoFila.inserirNaFila(triagem).stream()
                .map(FilaItemDTO::de)
                .toList();
        return ResponseEntity.ok(filaAtualizada);
    }

    @DeleteMapping("/{triagemId}")
    public ResponseEntity<Void> remover(@PathVariable String triagemId) {
        gestaoFila.removerDaFila(TriagemId.de(triagemId));
        return ResponseEntity.noContent().build();
    }

    record RequisicaoFilaDTO(String pacienteId, String triagemId, String corDeRisco) {}

    record FilaItemDTO(String pacienteId, String triagemId, String corDeRisco,
                       LocalDateTime finalizadaEm) {
        static FilaItemDTO de(PosicaoFila p) {
            return new FilaItemDTO(
                p.getPacienteId().getValor(),
                p.getTriagemId().getValor(),
                p.getCorDeRisco().name(),
                p.getFinalizadaEm()
            );
        }
    }
}