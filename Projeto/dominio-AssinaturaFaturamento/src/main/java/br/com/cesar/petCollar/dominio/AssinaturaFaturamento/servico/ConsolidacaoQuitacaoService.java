package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.servico;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Cobranca;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.ICobrancaRepositorio;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.StatusCobranca;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public class ConsolidacaoQuitacaoService {

    private final ICobrancaRepositorio cobrancaRepositorio;

    public ConsolidacaoQuitacaoService(ICobrancaRepositorio cobrancaRepositorio) {
        if (cobrancaRepositorio == null)
            throw new IllegalArgumentException("ICobrancaRepositorio é obrigatório.");
        this.cobrancaRepositorio = cobrancaRepositorio;
    }

    public BigDecimal somarDebitosAtualizados(TutorId tutorId) {
        if (tutorId == null)
            throw new IllegalArgumentException("TutorId não pode ser nulo.");
        List<Cobranca> emAtraso = cobrancaRepositorio.listarPorTutor(tutorId).stream()
                .filter(c -> c.status() == StatusCobranca.EM_ATRASO)
                .toList();
        BigDecimal total = BigDecimal.ZERO;
        for (Cobranca c : emAtraso) total = total.add(c.valorAtualizado());
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
