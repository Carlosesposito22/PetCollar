package br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Cobranca;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.ICobrancaRepositorio;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.StatusCobranca;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano.IPlanoRepositorio;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano.Plano;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.servico.ClassificacaoInadimplenciaService;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.servico.SituacaoConta;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public class ConsultarResumoFinanceiroUseCase {

    private final ICobrancaRepositorio cobrancaRepositorio;
    private final IPlanoRepositorio planoRepositorio;
    private final ClassificacaoInadimplenciaService classificacaoInadimplencia;

    public ConsultarResumoFinanceiroUseCase(ICobrancaRepositorio cobrancaRepositorio,
                                            IPlanoRepositorio planoRepositorio,
                                            ClassificacaoInadimplenciaService classificacaoInadimplencia) {
        if (cobrancaRepositorio == null)
            throw new IllegalArgumentException("ICobrancaRepositorio é obrigatório.");
        if (planoRepositorio == null)
            throw new IllegalArgumentException("IPlanoRepositorio é obrigatório.");
        if (classificacaoInadimplencia == null)
            throw new IllegalArgumentException("ClassificacaoInadimplenciaService é obrigatório.");
        this.cobrancaRepositorio = cobrancaRepositorio;
        this.planoRepositorio = planoRepositorio;
        this.classificacaoInadimplencia = classificacaoInadimplencia;
    }

    public Resultado executar(TutorId tutorId) {
        if (tutorId == null) throw new IllegalArgumentException("TutorId é obrigatório.");

        List<Cobranca> cobrancas = cobrancaRepositorio.listarPorTutor(tutorId);
        SituacaoConta situacao = classificacaoInadimplencia.classificarPorTutor(tutorId);
        LocalDate proximoVencimento = calcularProximoVencimento(cobrancas);
        Plano plano = obterPlanoExibido(cobrancas).orElse(null);

        return new Resultado(plano, situacao, proximoVencimento, cobrancas);
    }

    private LocalDate calcularProximoVencimento(List<Cobranca> cobrancas) {
        return cobrancas.stream()
                .filter(c -> c.status() == StatusCobranca.PENDENTE)
                .map(Cobranca::getVencimento)
                .min(Comparator.naturalOrder())
                .orElseGet(() -> cobrancas.stream()
                        .filter(c -> c.status() == StatusCobranca.PAGA)
                        .map(Cobranca::getVencimento)
                        .max(Comparator.naturalOrder())
                        .map(d -> d.plusMonths(1))
                        .orElse(null));
    }

    private Optional<Plano> obterPlanoExibido(List<Cobranca> cobrancas) {
        if (cobrancas.isEmpty()) return Optional.empty();
        return planoRepositorio.buscarPorId(cobrancas.get(0).getPlanoId());
    }

    public record Resultado(Plano plano,
                            SituacaoConta situacaoConta,
                            LocalDate proximoVencimento,
                            List<Cobranca> cobrancas) {}
}
