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
import br.com.cesar.petCollar.aplicacao.ProtocoloInacessibilidade.ExecutarEtapaProtocoloUseCase;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.ProtocoloDTO;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.RequisicaoAtivarManualmenteDTO;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.RequisicaoEncerrarProtocoloDTO;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.ResumoAtendimentoDTO;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.StatusProtocoloDTO;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.VisaoProtocoloDTO;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusProtocolo;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.FilaAtendimentoEmMemoria;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.TutorRecepcaoJpa;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.TutorRecepcaoJpaRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/protocolos")
public class ProtocoloController {

    private final ConsultarStatusProtocoloUseCase consultarStatusUseCase;
    private final AtivarProtocoloUseCase ativarProtocoloUseCase;
    private final EncerrarProtocoloUseCase encerrarProtocoloUseCase;
    private final ExecutarEtapaProtocoloUseCase executarEtapaUseCase;
    private final IProtocoloInacessibilidadeRepositorio protocoloRepositorio;
    private final IConsultaAtendimento consultaAtendimento;
    private final FilaAtendimentoEmMemoria fila;
    private final TutorRecepcaoJpaRepository tutorRecepcaoRepo;

    public ProtocoloController(ConsultarStatusProtocoloUseCase consultarStatusUseCase,
                               AtivarProtocoloUseCase ativarProtocoloUseCase,
                               EncerrarProtocoloUseCase encerrarProtocoloUseCase,
                               ExecutarEtapaProtocoloUseCase executarEtapaUseCase,
                               IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
                               IConsultaAtendimento consultaAtendimento,
                               FilaAtendimentoEmMemoria fila,
                               TutorRecepcaoJpaRepository tutorRecepcaoRepo) {
        this.consultarStatusUseCase = consultarStatusUseCase;
        this.ativarProtocoloUseCase = ativarProtocoloUseCase;
        this.encerrarProtocoloUseCase = encerrarProtocoloUseCase;
        this.executarEtapaUseCase = executarEtapaUseCase;
        this.protocoloRepositorio = protocoloRepositorio;
        this.consultaAtendimento = consultaAtendimento;
        this.fila = fila;
        this.tutorRecepcaoRepo = tutorRecepcaoRepo;
    }

    @GetMapping("/meu-protocolo-ativo")
    public ResponseEntity<VisaoProtocoloDTO> meuProtocoloAtivo(Authentication auth) {
        List<TutorRecepcaoJpa> tutores = tutorRecepcaoRepo.findByEmailIgnoreCase(auth.getName());
        if (tutores.isEmpty()) return ResponseEntity.notFound().build();
        TutorId tutorId = TutorId.de(tutores.get(0).getId());
        return protocoloRepositorio.buscarAtivoPorTutor(tutorId)
            .map(p -> ResponseEntity.ok(VisaoProtocoloDTO.de(
                consultarStatusUseCase.executar(p.getAtendimentoId()))))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{atendimentoId}")
    public VisaoProtocoloDTO visualizar(@PathVariable String atendimentoId) {
        return VisaoProtocoloDTO.de(consultarStatusUseCase.executar(AtendimentoId.de(atendimentoId)));
    }

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

    @PostMapping("/{id}/avancar-etapa")
    public ProtocoloDTO avancarEtapa(@PathVariable String id) {
        ProtocoloId protocoloId = ProtocoloId.de(id);
        executarEtapaUseCase.executar(protocoloId);
        ProtocoloInacessibilidade protocolo = protocoloRepositorio.buscarPorId(protocoloId)
            .orElseThrow(() -> new IllegalArgumentException("Protocolo não encontrado."));
        if (protocolo.getStatus() == StatusProtocolo.ENCERRADO_POR_ESGOTAMENTO)
            fila.removerPorPaciente(protocolo.getPacienteId().getValor());
        return ProtocoloDTO.de(protocolo);
    }

    @PostMapping("/{id}/confirmar-presenca")
    public ProtocoloDTO confirmarPresenca(@PathVariable String id, Authentication auth) {
        ProtocoloId protocoloId = ProtocoloId.de(id);
        ProtocoloInacessibilidade protocolo = protocoloRepositorio.buscarPorId(protocoloId)
            .orElseThrow(() -> new IllegalArgumentException("Protocolo não encontrado."));
        List<TutorRecepcaoJpa> tutores = tutorRecepcaoRepo.findByEmailIgnoreCase(auth.getName());
        if (tutores.isEmpty() || !protocolo.getTutorPrincipalId().getValor().equals(tutores.get(0).getId()))
            throw new IllegalStateException("Você não tem permissão para confirmar presença neste protocolo.");
        ProtocoloInacessibilidade encerrado = encerrarProtocoloUseCase.executar(
            protocoloId, MotivoEncerramento.sucessoComTutor("Tutor confirmou presença pelo portal."));
        return ProtocoloDTO.de(encerrado);
    }
}
