package br.com.cesar.petCollar.apresentacao.RecepcaoTriagem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recepcao/fila-legado")
public class FilaAtendimentoController {

    private final FilaAtendimentoEmMemoria fila;

    public FilaAtendimentoController(FilaAtendimentoEmMemoria fila) {
        this.fila = fila;
    }

    @GetMapping
    public List<FilaAtendimentoEmMemoria.ItemFilaDTO> listar() {
        return fila.listar();
    }

    @DeleteMapping("/{triagemId}")
    public ResponseEntity<Void> remover(@PathVariable String triagemId) {
        fila.remover(triagemId);
        return ResponseEntity.noContent().build();
    }
}
