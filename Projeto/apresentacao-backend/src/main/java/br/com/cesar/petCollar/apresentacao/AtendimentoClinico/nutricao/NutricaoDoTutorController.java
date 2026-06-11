package br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.ConsultarPlanoNutricionalUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.ListarCatalogoRacoesUseCase;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.PlanoNutricionalDTO;
import br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.NutricaoDTOs.RacaoDTO;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricionalId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

/**
 * F-11 — endpoints read-only do tutor para visualizar planos nutricionais
 * finalizados (assinados) pelos médicos dos seus pets. Protegido pelo
 * {@code SecurityConfig} (perfil TUTOR). O id do tutor vem do JWT.
 */
@RestController
@RequestMapping("/api/tutor/nutricao")
public class NutricaoDoTutorController {

    private final ConsultarPlanoNutricionalUseCase consultar;
    private final ListarCatalogoRacoesUseCase listarCatalogoRacoes;

    public NutricaoDoTutorController(ConsultarPlanoNutricionalUseCase consultar,
                                     ListarCatalogoRacoesUseCase listarCatalogoRacoes) {
        this.consultar = consultar;
        this.listarCatalogoRacoes = listarCatalogoRacoes;
    }

    /** Catálogo de rações — read-only para o tutor identificar a ração prescrita. */
    @GetMapping("/racoes")
    public List<RacaoDTO> catalogoRacoes() {
        return listarCatalogoRacoes.executar().stream().map(RacaoDTO::de).toList();
    }

    /**
     * Lista o plano nutricional <strong>vigente</strong> de cada paciente deste
     * tutor — no máximo 1 por paciente. Prescrições anteriores foram
     * substituídas e ficam visíveis apenas para o médico (auditoria).
     */
    @GetMapping
    public List<PlanoNutricionalDTO> meusPlanos(Principal principal) {
        return consultar.listarAtivosDoTutor(TutorId.de(principal.getName())).stream()
                .map(PlanoNutricionalDTO::de).toList();
    }

    /** Detalhe — o use case já trafega só planos do tutor (controlado pelo filter abaixo). */
    @GetMapping("/{planoId}")
    public ResponseEntity<PlanoNutricionalDTO> detalhe(@PathVariable String planoId, Principal principal) {
        TutorId tutorAutenticado = TutorId.de(principal.getName());
        return consultar.buscarPorId(PlanoNutricionalId.de(planoId))
                .filter((PlanoNutricional p) -> p.getTutorId().equals(tutorAutenticado))
                .map(PlanoNutricionalDTO::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
