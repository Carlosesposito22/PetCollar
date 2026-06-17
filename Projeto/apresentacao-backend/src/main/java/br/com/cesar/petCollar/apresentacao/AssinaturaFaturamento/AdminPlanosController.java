package br.com.cesar.petCollar.apresentacao.AssinaturaFaturamento;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento.GerenciarPlanoUseCase;
import br.com.cesar.petCollar.aplicacao.BeneficiosPlano.ConfigurarBeneficiosDoPlanoUseCase;
import br.com.cesar.petCollar.aplicacao.BeneficiosPlano.ConfigurarBeneficiosDoPlanoUseCase.ConfigBeneficio;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano.Plano;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogo;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.PeriodoRenovacao;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@RestController
public class AdminPlanosController {

    private final GerenciarPlanoUseCase gerenciarPlano;
    private final ConfigurarBeneficiosDoPlanoUseCase configurarBeneficios;

    public AdminPlanosController(GerenciarPlanoUseCase gerenciarPlano,
                                 ConfigurarBeneficiosDoPlanoUseCase configurarBeneficios) {
        this.gerenciarPlano = gerenciarPlano;
        this.configurarBeneficios = configurarBeneficios;
    }

    @GetMapping("/api/planos")
    public List<PlanoDTO> listarPublico() {
        return listarComBeneficios();
    }

    @GetMapping("/api/admin/planos")
    public List<PlanoDTO> listar() {
        return listarComBeneficios();
    }

    @PostMapping("/api/admin/planos")
    public ResponseEntity<PlanoDTO> criar(@Valid @RequestBody RequisicaoPlanoDTO req) {
        Plano plano = gerenciarPlano.criar(req.nome(), req.valorMensalidade().toPlainString());
        configurarBeneficios.configurar(plano.getId(), toConfigs(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(montarDTO(plano));
    }

    @PutMapping("/api/admin/planos/{id}")
    public PlanoDTO alterar(@PathVariable String id, @Valid @RequestBody RequisicaoPlanoDTO req) {
        Plano plano = gerenciarPlano.alterar(
                PlanoId.de(id),
                req.nome(),
                req.valorMensalidade().toPlainString());
        configurarBeneficios.configurar(plano.getId(), toConfigs(req));
        return montarDTO(plano);
    }

    @DeleteMapping("/api/admin/planos/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        gerenciarPlano.excluir(PlanoId.de(id));
        return ResponseEntity.noContent().build();
    }

    private List<PlanoDTO> listarComBeneficios() {
        return gerenciarPlano.listar().stream().map(this::montarDTO).toList();
    }

    private PlanoDTO montarDTO(Plano plano) {
        List<BeneficioDTO> beneficios = configurarBeneficios.listar(plano.getId()).stream()
                .map(BeneficioDTO::de)
                .toList();
        return new PlanoDTO(
                plano.getId().getValor(),
                plano.getNome(),
                plano.getMensalidade().getValor(),
                beneficios);
    }

    private static List<ConfigBeneficio> toConfigs(RequisicaoPlanoDTO req) {
        List<RequisicaoBeneficioDTO> entrada = req.beneficios() == null ? List.of() : req.beneficios();
        return entrada.stream()
                .map(b -> new ConfigBeneficio(
                        b.nome(),
                        PeriodoRenovacao.valueOf(b.periodoRenovacao()),
                        b.limiteUsosPorPeriodo(),
                        b.carenciaDias()))
                .toList();
    }

    public record PlanoDTO(String id, String nome, BigDecimal valorMensalidade, List<BeneficioDTO> beneficios) {}

    public record BeneficioDTO(
            String nome,
            String periodoRenovacao,
            int limiteUsosPorPeriodo,
            int carenciaDias) {
        static BeneficioDTO de(BeneficioCatalogo c) {
            return new BeneficioDTO(
                    c.getNome(),
                    c.getPeriodoRenovacao().name(),
                    c.getLimiteUsosPorPeriodo(),
                    c.getCarenciaDias());
        }
    }

    public record RequisicaoPlanoDTO(
            @NotBlank @Size(min = 3, max = 120) String nome,
            @NotNull @Positive BigDecimal valorMensalidade,
            @Valid List<RequisicaoBeneficioDTO> beneficios
    ) {}

    public record RequisicaoBeneficioDTO(
            @NotBlank @Size(min = 2, max = 120) String nome,
            @NotBlank String periodoRenovacao,
            @Min(0) int limiteUsosPorPeriodo,
            @Min(0) int carenciaDias
    ) {}

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumentoInvalido(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
    }
}
