package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IConsultaAtendimento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.MotivoEncerramento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.aplicacao.ProtocoloInacessibilidade.AtivarProtocoloUseCase;
import br.com.cesar.petCollar.aplicacao.ProtocoloInacessibilidade.ConsultarStatusProtocoloUseCase;
import br.com.cesar.petCollar.aplicacao.ProtocoloInacessibilidade.EncerrarProtocoloUseCase;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.ProtocoloDTO;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.RequisicaoAtivarManualmenteDTO;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.RequisicaoEncerrarProtocoloDTO;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.ResumoAtendimentoDTO;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.StatusProtocoloDTO;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.VisaoProtocoloDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    private final ConsultarStatusProtocoloUseCase consultarStatusUseCase;
    private final AtivarProtocoloUseCase ativarProtocoloUseCase;
    private final EncerrarProtocoloUseCase encerrarProtocoloUseCase;
    private final IProtocoloInacessibilidadeRepositorio protocoloRepositorio;
    private final IConsultaAtendimento consultaAtendimento;

    public ProtocoloController(ConsultarStatusProtocoloUseCase consultarStatusUseCase,
                               AtivarProtocoloUseCase ativarProtocoloUseCase,
                               EncerrarProtocoloUseCase encerrarProtocoloUseCase,
                               IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
                               IConsultaAtendimento consultaAtendimento) {
        this.consultarStatusUseCase = consultarStatusUseCase;
        this.ativarProtocoloUseCase = ativarProtocoloUseCase;
        this.encerrarProtocoloUseCase = encerrarProtocoloUseCase;
        this.protocoloRepositorio = protocoloRepositorio;
        this.consultaAtendimento = consultaAtendimento;
    }

    /**
     * RN 15 — visão do protocolo ativo do tutor autenticado (sem precisar informar atendimentoId).
     * O identificador do JWT é o TutorId, permitindo a busca direta.
     */
    @GetMapping("/meu-protocolo-ativo")
    public ResponseEntity<VisaoProtocoloDTO> meuProtocoloAtivo(Authentication auth) {
        TutorId tutorId = TutorId.de(auth.getName());
        return protocoloRepositorio.buscarAtivoPorTutor(tutorId)
            .map(p -> ResponseEntity.ok(VisaoProtocoloDTO.de(
                consultarStatusUseCase.executar(p.getAtendimentoId()))))
            .orElse(ResponseEntity.notFound().build());
    }

    /** RN 15 — visão consolidada do protocolo ativo de um atendimento, para o tutor. */
    @GetMapping("/{atendimentoId}")
    public VisaoProtocoloDTO visualizar(@PathVariable String atendimentoId) {
        return VisaoProtocoloDTO.de(consultarStatusUseCase.executar(AtendimentoId.de(atendimentoId)));
    }

    /** Lista os atendimentos clínicos em andamento para seleção no modal de ativação manual. */
    @GetMapping("/atendimentos-em-andamento")
    public List<ResumoAtendimentoDTO> listarAtendimentosEmAndamento() {
        return consultaAtendimento.listarEmAndamento().stream()
            .map(ResumoAtendimentoDTO::de)
            .toList();
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
            ativarProtocoloUseCase.executar(AtendimentoId.de(req.atendimentoId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ProtocoloDTO.de(protocolo));
    }

    @PostMapping("/{id}/encerrar")
    public ProtocoloDTO encerrar(@PathVariable String id, @RequestBody RequisicaoEncerrarProtocoloDTO req) {
        ProtocoloInacessibilidade protocolo = encerrarProtocoloUseCase.executar(
            ProtocoloId.de(id), MotivoEncerramento.intervencaoManual(req.detalhes()));
        return ProtocoloDTO.de(protocolo);
    }
}
