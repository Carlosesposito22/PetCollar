package br.com.cesar.petCollar.aplicacao.BeneficiosPlano;

import java.time.LocalDateTime;
import java.util.Optional;

import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogo;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioTutor;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioCatalogoRepositorio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioTutorRepositorio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.StatusBeneficio;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

/**
 * Orquestra o consumo (e a devolução) de um benefício do tutor a partir de
 * outro subdomínio — ex.: agendar uma consulta debita o benefício "Consulta",
 * registrar uma dose debita "Vacinação" (CLAUDE.md §5: orquestração entre
 * subdomínios). Garante que o serviço só seja liberado conforme o plano:
 * respeita a carência ({@code EM_CARENCIA}) e o limite de usos do período
 * ({@code ESGOTADO}), lançando {@link IllegalStateException} (→ 409) quando o
 * benefício está indisponível.
 */
public class ConsumirBeneficioUseCase {

    private final IBeneficioTutorRepositorio beneficioTutorRepositorio;
    private final IBeneficioCatalogoRepositorio beneficioCatalogoRepositorio;

    public ConsumirBeneficioUseCase(IBeneficioTutorRepositorio beneficioTutorRepositorio,
                                    IBeneficioCatalogoRepositorio beneficioCatalogoRepositorio) {
        if (beneficioTutorRepositorio == null)
            throw new IllegalArgumentException("IBeneficioTutorRepositorio é obrigatório.");
        if (beneficioCatalogoRepositorio == null)
            throw new IllegalArgumentException("IBeneficioCatalogoRepositorio é obrigatório.");
        this.beneficioTutorRepositorio = beneficioTutorRepositorio;
        this.beneficioCatalogoRepositorio = beneficioCatalogoRepositorio;
    }

    /**
     * Debita um uso do benefício do tutor na categoria informada. Lança
     * {@link IllegalStateException} com {@code mensagemIndisponivel} quando o
     * tutor não possui o benefício no plano, ainda está em carência ou esgotou
     * os usos do período.
     */
    public void consumir(TutorId tutorId, Categoria categoria, String mensagemIndisponivel) {
        if (tutorId == null) throw new IllegalArgumentException("TutorId é obrigatório.");
        if (categoria == null) throw new IllegalArgumentException("Categoria é obrigatória.");

        BeneficioTutor beneficio = localizar(tutorId, categoria)
                .orElseThrow(() -> new IllegalStateException(mensagemIndisponivel));

        LocalDateTime agora = LocalDateTime.now();
        beneficio.reiniciarPeriodoSeNecessario(agora);
        beneficio.recalcularStatus(agora);
        if (beneficio.getStatus() != StatusBeneficio.DISPONIVEL) {
            throw new IllegalStateException(mensagemIndisponivel);
        }
        beneficio.debitarUso(agora);
        beneficioTutorRepositorio.save(beneficio);
    }

    /**
     * Devolve um uso ao benefício do tutor na categoria informada (ex.: ao
     * cancelar uma consulta). Nunca ultrapassa o teto do período e é silencioso
     * quando o benefício não existe — a devolução jamais deve falhar a operação
     * que a originou.
     */
    public void devolver(TutorId tutorId, Categoria categoria) {
        if (tutorId == null) throw new IllegalArgumentException("TutorId é obrigatório.");
        if (categoria == null) throw new IllegalArgumentException("Categoria é obrigatória.");

        localizar(tutorId, categoria).ifPresent(beneficio -> {
            beneficio.devolverUso(LocalDateTime.now());
            beneficioTutorRepositorio.save(beneficio);
        });
    }

    private Optional<BeneficioTutor> localizar(TutorId tutorId, Categoria categoria) {
        return beneficioTutorRepositorio.findByTutorId(tutorId).stream()
                .filter(bt -> {
                    BeneficioCatalogo catalogo =
                            beneficioCatalogoRepositorio.findById(bt.getBeneficioCatalogoId());
                    return catalogo != null && categoria.corresponde(catalogo.getNome());
                })
                .findFirst();
    }

    /** Categorias de benefício consumidas por outros subdomínios (F-05, F-06). */
    public enum Categoria {
        CONSULTA("consulta"),
        VACINACAO("vacina");

        private final String prefixo;

        Categoria(String prefixo) { this.prefixo = prefixo; }

        /** Casa pelo prefixo do nome do benefício no catálogo (ex.: "Consulta", "Vacinação"). */
        public boolean corresponde(String nomeBeneficio) {
            return nomeBeneficio != null
                    && nomeBeneficio.trim().toLowerCase().startsWith(prefixo);
        }
    }
}
