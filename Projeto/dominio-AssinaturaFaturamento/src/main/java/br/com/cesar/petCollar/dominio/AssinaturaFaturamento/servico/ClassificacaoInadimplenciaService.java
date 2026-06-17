package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.servico;

import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.ICobrancaRepositorio;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public class ClassificacaoInadimplenciaService {

    public static final int LIMIAR_INADIMPLENCIA = 1;

    public static final int LIMIAR_SUSPENSAO = 3;

    private final ICobrancaRepositorio cobrancaRepositorio;

    public ClassificacaoInadimplenciaService(ICobrancaRepositorio cobrancaRepositorio) {
        if (cobrancaRepositorio == null)
            throw new IllegalArgumentException("ICobrancaRepositorio é obrigatório.");
        this.cobrancaRepositorio = cobrancaRepositorio;
    }

    public SituacaoConta classificarPorTutor(TutorId tutorId) {
        if (tutorId == null)
            throw new IllegalArgumentException("TutorId não pode ser nulo.");
        long emAtraso = cobrancaRepositorio.contarEmAtrasoPorTutor(tutorId);
        return classificar((int) emAtraso);
    }

    public SituacaoConta classificar(int cobrancasEmAtraso) {
        if (cobrancasEmAtraso < 0)
            throw new IllegalArgumentException("Quantidade em atraso não pode ser negativa.");
        if (cobrancasEmAtraso >= LIMIAR_SUSPENSAO)  return SituacaoConta.SUSPENSA;
        if (cobrancasEmAtraso >= LIMIAR_INADIMPLENCIA) return SituacaoConta.INADIMPLENTE;
        return SituacaoConta.ATIVA;
    }
}
