package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IConsultaNotificacaoProtocolo;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.NotificacaoProtocoloDTO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint de auditoria de notificações enviadas durante a execução do protocolo
 * (RN 16). Delega à porta {@link IConsultaNotificacaoProtocolo} cuja implementação
 * concreta persiste os registros no banco de dados (infraestrutura).
 */
@RestController
@RequestMapping("/api/protocolos")
public class NotificacaoController {

    private final IConsultaNotificacaoProtocolo consulta;

    public NotificacaoController(IConsultaNotificacaoProtocolo consulta) {
        this.consulta = consulta;
    }

    /** RN 16 — lista, do mais recente ao mais antigo, as notificações do protocolo. */
    @GetMapping("/{id}/notificacoes")
    public List<NotificacaoProtocoloDTO> listar(@PathVariable String id) {
        return consulta.listarPorProtocolo(id).stream()
            .map(NotificacaoProtocoloDTO::de)
            .toList();
    }
}
