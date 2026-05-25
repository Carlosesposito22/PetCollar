package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private final AutenticacaoService servico;

    public AutenticacaoController(AutenticacaoService servico) {
        this.servico = servico;
    }

    @PostMapping("/login")
    public RespostaAutenticacao login(@Valid @RequestBody RequisicaoLogin requisicao) {
        return servico.autenticar(requisicao);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<Map<String, String>> tratarCredenciaisInvalidas(CredenciaisInvalidasException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "status", "CREDENCIAIS_INVALIDAS",
                "mensagem", e.getMessage()
        ));
    }

    @ExceptionHandler(ContaSuspensaException.class)
    public ResponseEntity<Map<String, String>> tratarContaSuspensa(ContaSuspensaException e) {
        return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of(
                "status", "CONTA_SUSPENSA",
                "mensagem", e.getMessage()
        ));
    }

    @ExceptionHandler(PagamentoPendenteException.class)
    public ResponseEntity<Map<String, String>> tratarPagamentoPendente(PagamentoPendenteException e) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(Map.of(
                "status", "PAGAMENTO_PENDENTE",
                "mensagem", e.getMessage(),
                "contaId", e.getContaId()
        ));
    }
}
