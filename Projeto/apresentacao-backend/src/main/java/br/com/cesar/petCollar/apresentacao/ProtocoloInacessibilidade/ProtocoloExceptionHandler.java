package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade")
public class ProtocoloExceptionHandler {

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
