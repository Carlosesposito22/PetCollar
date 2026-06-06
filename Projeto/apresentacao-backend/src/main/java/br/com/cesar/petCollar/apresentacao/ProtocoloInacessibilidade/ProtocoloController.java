package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.MotivoEncerramento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.AtivacaoProtocoloService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.ConsultaStatusProtocoloService;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.ProtocoloDTO;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.RequisicaoAtivarManualmenteDTO;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.RequisicaoEncerrarProtocoloDTO;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.StatusProtocoloDTO;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.VisaoProtocoloDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints do protocolo de inacessibilidade: visão para o tutor (RN 15), ativação
 * manual, encerramento por intervenção manual e listagem de protocolos ativos
 * (administração). Apenas traduz DTO ↔ domínio e delega; exceções sobem para o
 * {@link ProtocoloExceptionHandler}.
 */
@RestController
@RequestMapping("/api/protocolos")
public class ProtocoloController {

    private final ConsultaStatusProtocoloService statusService;
    private final AtivacaoProtocoloService ativacaoService;
    private final IProtocoloInacessibilidadeRepositorio protocoloRepositorio;

    public ProtocoloController(ConsultaStatusProtocoloService statusService,
                               AtivacaoProtocoloService ativacaoService,
                               IProtocoloInacessibilidadeRepositorio protocoloRepositorio) {
        this.statusService = statusService;
        this.ativacaoService = ativacaoService;
        this.protocoloRepositorio = protocoloRepositorio;
    }

    /** RN 15 — visão consolidada do protocolo ativo de um atendimento, para o tutor. */
    @GetMapping("/{atendimentoId}")
    public VisaoProtocoloDTO visualizar(@PathVariable String atendimentoId) {
        return VisaoProtocoloDTO.de(statusService.montarVisao(AtendimentoId.de(atendimentoId)));
    }

    @GetMapping("/ativos")
    public List<StatusProtocoloDTO> listarAtivos() {
        return protocoloRepositorio.listarAtivos().stream()
            .map(StatusProtocoloDTO::de)
            .toList();
    }

    @PostMapping("/ativar-manualmente")
    public ResponseEntity<ProtocoloDTO> ativarManualmente(
            @RequestBody RequisicaoAtivarManualmenteDTO req) {
        ProtocoloInacessibilidade protocolo =
            ativacaoService.ativarManualmente(AtendimentoId.de(req.atendimentoId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ProtocoloDTO.de(protocolo));
    }

    @PostMapping("/{id}/encerrar")
    public ProtocoloDTO encerrar(@PathVariable String id, @RequestBody RequisicaoEncerrarProtocoloDTO req) {
        ProtocoloInacessibilidade protocolo = protocoloRepositorio.buscarPorId(ProtocoloId.de(id))
            .orElseThrow(() -> new IllegalArgumentException("Protocolo não encontrado."));
        protocolo.encerrarComSucesso(MotivoEncerramento.intervencaoManual(req.detalhes()));
        protocoloRepositorio.salvar(protocolo);
        return ProtocoloDTO.de(protocolo);
    }
}
