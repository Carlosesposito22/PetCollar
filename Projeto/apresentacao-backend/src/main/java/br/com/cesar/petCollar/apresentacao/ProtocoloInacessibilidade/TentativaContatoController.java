package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.TentativaContatoDTO;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Consulta das tentativas de contato registradas em um protocolo (RN 3).
 */
@RestController
@RequestMapping("/api/protocolos/{id}/tentativas")
public class TentativaContatoController {

    private final IProtocoloInacessibilidadeRepositorio protocoloRepositorio;

    public TentativaContatoController(IProtocoloInacessibilidadeRepositorio protocoloRepositorio) {
        this.protocoloRepositorio = protocoloRepositorio;
    }

    @GetMapping
    public List<TentativaContatoDTO> listar(@PathVariable String id) {
        return protocolo(id).getTentativas().stream()
            .map(TentativaContatoDTO::de)
            .toList();
    }

    @GetMapping("/{tentativaId}")
    public TentativaContatoDTO detalhar(@PathVariable String id, @PathVariable String tentativaId) {
        return protocolo(id).getTentativas().stream()
            .filter(t -> t.getId().getValor().equals(tentativaId))
            .map(TentativaContatoDTO::de)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Tentativa não encontrada."));
    }

    private ProtocoloInacessibilidade protocolo(String id) {
        return protocoloRepositorio.buscarPorId(ProtocoloId.de(id))
            .orElseThrow(() -> new IllegalArgumentException("Protocolo não encontrado."));
    }
}
