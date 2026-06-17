package br.com.cesar.petCollar.infraestrutura.Gamificacao;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import br.com.cesar.petCollar.aplicacao.Gamificacao.ConsultarConquistasTutorUseCase;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.ICobrancaRepositorio;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.Badge;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.BadgeId;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.CalculoProgressoService;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.CategoriaBadge;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.ConcessaoBadgeObservador;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.ConcessaoBadgeService;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.IBadgeRepositorio;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.IConquistaTutorRepositorio;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.IProgressoBadgeRepositorio;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.RaridadeBadge;
import br.com.cesar.petCollar.dominio.compartilhado.eventos.PublicadorDeEventosDoTutor;

@Configuration
@EntityScan(basePackages = "br.com.cesar.petCollar.infraestrutura.Gamificacao")
@EnableJpaRepositories(basePackages = "br.com.cesar.petCollar.infraestrutura.Gamificacao")
public class GamificacaoConfig {

    private static final Object[][] BADGES_PADRAO = {
            {"Primeira Consulta", "Concedida ao realizar a primeira consulta na clínica", CategoriaBadge.FIDELIDADE, RaridadeBadge.COMUM, "primeira_consulta", true, 1},
            {"Vacina em Dia", "Concedida ao registrar a aplicação de uma vacina", CategoriaBadge.SAUDE_DO_PET, RaridadeBadge.INCOMUM, "vacina_aplicada", true, 1},
            {"Cliente Fiel", "Concedida ao completar 5 consultas na clínica", CategoriaBadge.FIDELIDADE, RaridadeBadge.RARA, "consulta_realizada", false, 5},
            {"Pagador Pontual", "Concedida ao confirmar 3 pagamentos de mensalidade", CategoriaBadge.FIDELIDADE, RaridadeBadge.EPICA, "pagamento_confirmado", false, 3},
            {"Indicador Top", "Concedida ao ter 3 indicações de tutores aceitas", CategoriaBadge.ENGAJAMENTO, RaridadeBadge.LENDARIA, "indicacao_aceita", false, 3},
            {"Bem-vindo ao Clube", "Concedida ao utilizar o primeiro benefício do plano", CategoriaBadge.ENGAJAMENTO, RaridadeBadge.COMUM, "ticket_beneficio_utilizado", true, 1},
    };

    @Bean
    public CalculoProgressoService calculoProgressoService(
            IProgressoBadgeRepositorio progressoRepositorio,
            IConquistaTutorRepositorio conquistaRepositorio) {
        return new CalculoProgressoService(progressoRepositorio, conquistaRepositorio);
    }

    @Bean
    public ConcessaoBadgeService concessaoBadgeService(
            IBadgeRepositorio badgeRepositorio,
            IConquistaTutorRepositorio conquistaRepositorio,
            IProgressoBadgeRepositorio progressoRepositorio) {
        return new ConcessaoBadgeService(badgeRepositorio, conquistaRepositorio, progressoRepositorio);
    }

    @Bean
    public PublicadorDeEventosDoTutor publicadorDeEventosDoTutor(ConcessaoBadgeService concessaoBadgeService) {
        PublicadorDeEventosDoTutor publicador = new PublicadorDeEventosDoTutor();
        publicador.inscrever(new ConcessaoBadgeObservador(concessaoBadgeService));
        return publicador;
    }

    @Bean
    public ConsultarConquistasTutorUseCase consultarConquistasTutorUseCase(
            IBadgeRepositorio badgeRepositorio,
            IConquistaTutorRepositorio conquistaRepositorio,
            IProgressoBadgeRepositorio progressoRepositorio,
            ICobrancaRepositorio cobrancaRepositorio) {
        return new ConsultarConquistasTutorUseCase(
                badgeRepositorio, conquistaRepositorio, progressoRepositorio, cobrancaRepositorio);
    }

    @Bean
    public CommandLineRunner seedBadges(IBadgeRepositorio badgeRepositorio) {
        return args -> {
            for (Object[] dados : BADGES_PADRAO) {
                String chaveEvento = (String) dados[4];
                if (!badgeRepositorio.findByChaveEvento(chaveEvento).isEmpty()) {
                    continue;
                }
                badgeRepositorio.save(new Badge(
                        BadgeId.gerar(),
                        (String) dados[0],
                        (String) dados[1],
                        (CategoriaBadge) dados[2],
                        (RaridadeBadge) dados[3],
                        chaveEvento,
                        (boolean) dados[5],
                        (int) dados[6]
                ));
            }
        };
    }
}
