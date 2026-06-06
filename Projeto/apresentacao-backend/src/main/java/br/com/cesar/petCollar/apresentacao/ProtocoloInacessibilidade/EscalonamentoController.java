package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.EventoEscalonamentoDTO;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Consulta dos eventos de escalonamento auditáveis de um protocolo (RN 7).
 */
@RestController
@RequestMapping("/api/protocolos/{id}/escalonamentos")
public class EscalonamentoController {

    private final IProtocoloInacessibilidadeRepositorio protocoloRepositorio;

    public EscalonamentoController(IProtocoloInacessibilidadeRepositorio protocoloRepositorio) {
        this.protocoloRepositorio = protocoloRepositorio;
    }

    @GetMapping
    public List<EventoEscalonamentoDTO> listar(@PathVariable String id) {
        return protocoloRepositorio.buscarPorId(ProtocoloId.de(id))
            .orElseThrow(() -> new IllegalArgumentException("Protocolo não encontrado."))
            .getEventosEscalonamento().stream()
            .map(EventoEscalonamentoDTO::de)
            .toList();
    }
}
