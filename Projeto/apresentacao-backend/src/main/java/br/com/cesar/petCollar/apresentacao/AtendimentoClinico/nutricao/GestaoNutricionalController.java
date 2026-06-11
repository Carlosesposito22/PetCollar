package br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.CalcularNEMPreviewUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.CompararEvolucaoNutricionalUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.ConsultarPlanoNutricionalUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.FinalizarPlanoNutricionalUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.ListarCatalogoRacoesUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.RecomendarRacoesUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.SalvarRascunhoPlanoNutricionalUseCase;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.HistoricoEvolutivoDTO;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.PlanoNutricionalDTO;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.PreviewNEMDTO;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.RacaoDTO;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.RacaoRecomendadaDTO;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.RequisicaoFinalizarDTO;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.RequisicaoPreviewNEMDTO;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.RequisicaoRascunhoDTO;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.ObservacaoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricionalId;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RacaoId;
import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * F-11 — endpoints do médico para gestão nutricional. Protegido pelo
 * {@code SecurityConfig} (perfil MEDICO_VETERINARIO). O id do médico autenticado
 * vem do {@code Principal} (subject do JWT).
 */
@RestController
@RequestMapping("/api/medico/nutricao")
public class GestaoNutricionalController {

    private final CalcularNEMPreviewUseCase calcularPreview;
    private final SalvarRascunhoPlanoNutricionalUseCase salvarRascunho;
    private final FinalizarPlanoNutricionalUseCase finalizar;
    private final ConsultarPlanoNutricionalUseCase consultar;
    private final RecomendarRacoesUseCase recomendarRacoes;
    private final ListarCatalogoRacoesUseCase listarCatalogoRacoes;
    private final CompararEvolucaoNutricionalUseCase compararEvolucao;

    public GestaoNutricionalController(CalcularNEMPreviewUseCase calcularPreview,
                                       SalvarRascunhoPlanoNutricionalUseCase salvarRascunho,
                                       FinalizarPlanoNutricionalUseCase finalizar,
                                       ConsultarPlanoNutricionalUseCase consultar,
                                       RecomendarRacoesUseCase recomendarRacoes,
                                       ListarCatalogoRacoesUseCase listarCatalogoRacoes,
                                       CompararEvolucaoNutricionalUseCase compararEvolucao) {
        this.calcularPreview = calcularPreview;
        this.salvarRascunho = salvarRascunho;
        this.finalizar = finalizar;
        this.consultar = consultar;
        this.recomendarRacoes = recomendarRacoes;
        this.listarCatalogoRacoes = listarCatalogoRacoes;
        this.compararEvolucao = compararEvolucao;
    }

    /** Cálculo NEM ao vivo — não persiste nada. */
    @PostMapping("/preview")
    public PreviewNEMDTO preview(@RequestBody RequisicaoPreviewNEMDTO req) {
        var resultado = calcularPreview.executar(NutricaoDTOs.paraDominio(req.parametros()));
        return PreviewNEMDTO.de(resultado);
    }

    /** Cria/atualiza o único rascunho aberto do paciente (idempotente). */
    @PostMapping("/rascunho")
    public PlanoNutricionalDTO salvarRascunho(@RequestBody RequisicaoRascunhoDTO req, Principal principal) {
        var entrada = new SalvarRascunhoPlanoNutricionalUseCase.Entrada(
                PacienteId.de(req.pacienteId()),
                TutorId.de(req.tutorId()),
                MedicoId.de(principal.getName()),
                NutricaoDTOs.paraDominio(req.parametros()),
                req.cronograma() == null ? null : req.cronograma().paraDominio(),
                req.observacoes() == null ? List.of()
                        : req.observacoes().stream().map(ObservacaoNutricional::new).toList(),
                req.racaoId() == null || req.racaoId().isBlank() ? null : RacaoId.de(req.racaoId()),
                req.justificativaDivergencia());
        return PlanoNutricionalDTO.de(salvarRascunho.executar(entrada));
    }

    /** Catálogo completo de rações. */
    @GetMapping("/racoes")
    public List<RacaoDTO> catalogoRacoes() {
        return listarCatalogoRacoes.executar().stream().map(RacaoDTO::de).toList();
    }

    /** Top-N rações recomendadas para um perfil — usa o Strategy do domínio. */
    @GetMapping("/racoes/recomendacoes")
    public List<RacaoRecomendadaDTO> recomendarRacoes(
            @RequestParam("pesoIdealKg") BigDecimal pesoIdealKg,
            @RequestParam("idadeAnos") int idadeAnos,
            @RequestParam("comorbidade") String comorbidade,
            @RequestParam(value = "topN", defaultValue = "3") int topN) {
        var entrada = new RecomendarRacoesUseCase.Entrada(
                pesoIdealKg, idadeAnos, Comorbidade.valueOf(comorbidade), topN);
        return recomendarRacoes.executar(entrada).stream().map(RacaoRecomendadaDTO::de).toList();
    }

    /** Histórico de planos finalizados + deltas entre planos sequenciais. */
    @GetMapping("/pacientes/{pacienteId}/evolucao")
    public HistoricoEvolutivoDTO evolucao(@PathVariable String pacienteId) {
        return HistoricoEvolutivoDTO.de(compararEvolucao.executar(PacienteId.de(pacienteId)));
    }

    /** Rascunho aberto do paciente — 204 se não houver. */
    @GetMapping("/pacientes/{pacienteId}/rascunho")
    public ResponseEntity<PlanoNutricionalDTO> rascunhoDoPaciente(@PathVariable String pacienteId) {
        return consultar.buscarRascunhoDoPaciente(PacienteId.de(pacienteId))
                .map(PlanoNutricionalDTO::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /** Histórico de planos finalizados de um paciente. */
    @GetMapping("/pacientes/{pacienteId}/finalizados")
    public List<PlanoNutricionalDTO> finalizadosDoPaciente(@PathVariable String pacienteId) {
        return consultar.listarFinalizadosDoPaciente(PacienteId.de(pacienteId)).stream()
                .map(PlanoNutricionalDTO::de).toList();
    }

    /** Finaliza o plano capturando a assinatura digital (PNG base64). */
    @PostMapping("/{planoId}/finalizar")
    public PlanoNutricionalDTO finalizar(@PathVariable String planoId,
                                         @RequestBody RequisicaoFinalizarDTO req,
                                         Principal principal) {
        PlanoNutricional finalizado = finalizar.executar(
                PlanoNutricionalId.de(planoId),
                MedicoId.de(principal.getName()),
                req.imagemAssinaturaBase64());
        return PlanoNutricionalDTO.de(finalizado);
    }

    /** Detalhe de um plano específico. */
    @GetMapping("/{planoId}")
    public ResponseEntity<PlanoNutricionalDTO> detalhe(@PathVariable String planoId) {
        return consultar.buscarPorId(PlanoNutricionalId.de(planoId))
                .map(PlanoNutricionalDTO::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
