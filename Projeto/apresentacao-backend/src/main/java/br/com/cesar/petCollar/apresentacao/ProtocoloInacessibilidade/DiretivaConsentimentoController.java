package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.TipoConduta;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.ConsultaDiretivaConsentimentoService;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.DiretivaConsentimentoDTO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Endpoint que expõe as diretivas de consentimento do tutor para o paciente do
 * protocolo (RN 10). Lista todas as condutas possíveis indicando quais foram
 * autorizadas previamente pelo tutor.
 */
@RestController
@RequestMapping("/api/protocolos")
public class DiretivaConsentimentoController {

    private final IProtocoloInacessibilidadeRepositorio protocoloRepositorio;
    private final ConsultaDiretivaConsentimentoService consultaDiretivas;

    public DiretivaConsentimentoController(IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
                                           ConsultaDiretivaConsentimentoService consultaDiretivas) {
        this.protocoloRepositorio = protocoloRepositorio;
        this.consultaDiretivas = consultaDiretivas;
    }

    /**
     * RN 10 — retorna todas as condutas clínicas com o status de autorização do
     * tutor para o paciente do protocolo informado.
     */
    @GetMapping("/{id}/diretivas")
    public List<DiretivaConsentimentoDTO> listar(@PathVariable String id) {
        ProtocoloInacessibilidade protocolo = protocoloRepositorio.buscarPorId(ProtocoloId.de(id))
            .orElseThrow(() -> new IllegalArgumentException("Protocolo não encontrado."));

        PacienteId pacienteId = protocolo.getPacienteId();
        Set<TipoConduta> autorizadas = Set.copyOf(consultaDiretivas.listarCondutasAutorizadas(pacienteId));

        return Arrays.stream(TipoConduta.values())
            .map(c -> autorizadas.contains(c)
                ? DiretivaConsentimentoDTO.autorizada(c)
                : DiretivaConsentimentoDTO.bloqueada(c))
            .toList();
    }
}
