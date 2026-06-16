package br.com.cesar.petCollar.apresentacao.AtendimentoClinico.nutricao.admin;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.admin.AlterarStatusRacaoUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.admin.AtualizarRacaoUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.admin.CriarRacaoUseCase;
import br.com.cesar.petCollar.aplicacao.AtendimentoClinico.nutricao.admin.ListarRacoesAdminUseCase;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.FaixaEtaria;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.Porte;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.Racao;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RacaoId;

/**
 * F-11 / Admin — CRUD do catálogo de rações. Protegido pelo SecurityConfig
 * (perfil ADMIN_CLINICA). Devolve {@link RacaoAdminDTO} contendo todas as
 * rações (ativas + desativadas) para o painel administrativo.
 */
@RestController
@RequestMapping("/api/admin/nutricao/racoes")
public class RacoesAdminController {

    private final ListarRacoesAdminUseCase listar;
    private final CriarRacaoUseCase criar;
    private final AtualizarRacaoUseCase atualizar;
    private final AlterarStatusRacaoUseCase alterarStatus;

    public RacoesAdminController(ListarRacoesAdminUseCase listar,
                                  CriarRacaoUseCase criar,
                                  AtualizarRacaoUseCase atualizar,
                                  AlterarStatusRacaoUseCase alterarStatus) {
        this.listar = listar;
        this.criar = criar;
        this.atualizar = atualizar;
        this.alterarStatus = alterarStatus;
    }

    @GetMapping
    public List<RacaoAdminDTO> listar() {
        return listar.executar().stream().map(RacaoAdminDTO::de).toList();
    }

    @PostMapping
    public RacaoAdminDTO criar(@RequestBody RequisicaoRacaoDTO req) {
        Racao criada = criar.executar(new CriarRacaoUseCase.Entrada(
                req.fabricante(), req.linha(),
                req.densidadeCaloricaKcalPorKg(),
                parseFaixas(req.faixasIndicadas()),
                parsePortes(req.portesIndicados()),
                parseComorbidades(req.comorbidadesIndicadas())));
        return RacaoAdminDTO.de(criada);
    }

    @PutMapping("/{racaoId}")
    public RacaoAdminDTO atualizar(@PathVariable String racaoId,
                                    @RequestBody RequisicaoRacaoDTO req) {
        Racao atualizada = atualizar.executar(
                RacaoId.de(racaoId),
                new AtualizarRacaoUseCase.Entrada(
                        req.fabricante(), req.linha(),
                        req.densidadeCaloricaKcalPorKg(),
                        parseFaixas(req.faixasIndicadas()),
                        parsePortes(req.portesIndicados()),
                        parseComorbidades(req.comorbidadesIndicadas())));
        return RacaoAdminDTO.de(atualizada);
    }

    /**
     * Soft-delete. Retorna no body o número de planos que prescreveram esta
     * ração, para o frontend mostrar o histórico de impacto.
     */
    @DeleteMapping("/{racaoId}")
    public ResponseEntity<DesativacaoDTO> desativar(@PathVariable String racaoId) {
        RacaoId id = RacaoId.de(racaoId);
        long planosAfetados = alterarStatus.contarPlanosUsando(id);
        Racao desativada = alterarStatus.desativar(id);
        return ResponseEntity.ok(new DesativacaoDTO(
                RacaoAdminDTO.de(desativada), planosAfetados));
    }

    @PostMapping("/{racaoId}/reativar")
    public RacaoAdminDTO reativar(@PathVariable String racaoId) {
        return RacaoAdminDTO.de(alterarStatus.reativar(RacaoId.de(racaoId)));
    }

    /** Endpoint auxiliar para o frontend pré-consultar o impacto antes de desativar. */
    @GetMapping("/{racaoId}/impacto")
    public ImpactoDTO impacto(@PathVariable String racaoId) {
        return new ImpactoDTO(alterarStatus.contarPlanosUsando(RacaoId.de(racaoId)));
    }

    // ── DTOs ─────────────────────────────────────────────────────────────────

    public record RequisicaoRacaoDTO(
            String fabricante,
            String linha,
            BigDecimal densidadeCaloricaKcalPorKg,
            List<String> faixasIndicadas,
            List<String> portesIndicados,
            List<String> comorbidadesIndicadas) {}

    public record RacaoAdminDTO(
            String id, String fabricante, String linha, String descricaoCurta,
            BigDecimal densidadeCaloricaKcalPorKg,
            List<String> faixasIndicadas,
            List<String> portesIndicados,
            List<String> comorbidadesIndicadas,
            boolean desativada) {
        public static RacaoAdminDTO de(Racao r) {
            return new RacaoAdminDTO(
                    r.getId().getValor(), r.getFabricante(), r.getLinha(),
                    r.descricaoCurta(), r.getDensidadeCaloricaKcalPorKg(),
                    r.getFaixasIndicadas().stream().map(Enum::name).toList(),
                    r.getPortesIndicados().stream().map(Enum::name).toList(),
                    r.getComorbidadesIndicadas().stream().map(Enum::name).toList(),
                    r.isDesativada());
        }
    }

    public record DesativacaoDTO(RacaoAdminDTO racao, long planosAfetados) {}

    public record ImpactoDTO(long planosAfetados) {}

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static Set<FaixaEtaria> parseFaixas(List<String> nomes) {
        if (nomes == null) return EnumSet.noneOf(FaixaEtaria.class);
        return nomes.stream().map(FaixaEtaria::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(FaixaEtaria.class)));
    }

    private static Set<Porte> parsePortes(List<String> nomes) {
        if (nomes == null) return EnumSet.noneOf(Porte.class);
        return nomes.stream().map(Porte::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Porte.class)));
    }

    private static Set<Comorbidade> parseComorbidades(List<String> nomes) {
        if (nomes == null || nomes.isEmpty()) return Set.of(Comorbidade.NENHUMA);
        return nomes.stream().map(Comorbidade::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Comorbidade.class)));
    }
}
