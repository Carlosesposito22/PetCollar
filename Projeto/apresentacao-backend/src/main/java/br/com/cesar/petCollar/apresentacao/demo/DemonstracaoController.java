package br.com.cesar.petCollar.apresentacao.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos do Modo Demonstração. Ativado pelo atalho Shift+D no
 * frontend — não requer autenticação para que possa ser acionado antes de
 * existir qualquer usuário cadastrado.
 */
@RestController
@RequestMapping("/api/demo")
public class DemonstracaoController {

    private final DadosDemonstracaoService servico;

    public DemonstracaoController(DadosDemonstracaoService servico) {
        this.servico = servico;
    }

    @GetMapping("/status")
    public ResponseEntity<StatusDemoDTO> status() {
        return ResponseEntity.ok(new StatusDemoDTO(servico.estaAtivo()));
    }

    @PostMapping("/ativar")
    public ResponseEntity<StatusDemoDTO> ativar() {
        servico.ativar();
        return ResponseEntity.ok(new StatusDemoDTO(true));
    }

    @PostMapping("/desativar")
    public ResponseEntity<StatusDemoDTO> desativar() {
        servico.desativar();
        return ResponseEntity.ok(new StatusDemoDTO(false));
    }

    record StatusDemoDTO(boolean ativo) {}
}
