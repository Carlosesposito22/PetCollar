package br.com.cesar.petCollar.apresentacao.RelacaoTutor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Tratamento de erros do slice RelacaoTutor (§7.3): argumento inválido vira
 * 400, conflito de regra de negócio vira 409. Escopo restrito a este pacote.
 */
@RestControllerAdvice(basePackages = "br.com.cesar.petCollar.apresentacao.RelacaoTutor")
public class IndicacaoExceptionHandler {

    public record ErroResponse(String mensagem) {}

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> argumentoInvalido(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErroResponse(e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErroResponse> conflitoDeRegra(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErroResponse(e.getMessage()));
    }
}
