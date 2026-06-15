package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import br.com.cesar.petCollar.aplicacao.RelacaoTutor.ConfirmarConversaoIndicacaoUseCase;
import br.com.cesar.petCollar.aplicacao.RelacaoTutor.ObterOuGerarLinkIndicacaoUseCase;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IDescontoFaturaPort;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IEventoAuditoriaRepositorio;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IIndicacaoRepositorio;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.ILinkIndicacaoRepositorio;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IMotorGamificacaoPort;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IRegistroCliqueRepositorio;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.ProgramaIndicacaoService;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Wiring canônico (§6.5) do domínio RelacaoTutor / F-04.
 *
 * <p><strong>Padrão Template Method</strong> — o {@link ProgramaIndicacaoService}
 * instancia sob demanda as implementações concretas de
 * {@link br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.ProcessamentoWebhookTemplate}
 * via {@code new}, passando as dependências injetadas neste bean:
 * <ul>
 *   <li>{@link br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.ProcessamentoWebhookAutomatico}
 *       — confirmação via gateway com validação de fraude por método de pagamento (RN-8).</li>
 *   <li>{@link br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.ProcessamentoWebhookManual}
 *       — confirmação administrativa que dispensa a verificação automática.</li>
 * </ul>
 * As subclasses não são registradas como beans singleton pois são stateless e
 * criadas por chamada — mantendo-as como objetos de colaboração transitórios
 * evita preocupações com escopo e ciclo de vida.
 */
@Configuration
@EntityScan(basePackages = "br.com.cesar.petCollar.infraestrutura.RelacaoTutor")
@EnableJpaRepositories(basePackages = "br.com.cesar.petCollar.infraestrutura.RelacaoTutor")
public class RelacaoTutorConfig {

    // ── Service de domínio ─────────────────────────────────────────────────

    @Bean
    public ProgramaIndicacaoService programaIndicacaoService(
            ILinkIndicacaoRepositorio linkRepositorio,
            IRegistroCliqueRepositorio registroCliqueRepositorio,
            IIndicacaoRepositorio indicacaoRepositorio,
            IEventoAuditoriaRepositorio auditoriaRepositorio,
            IMotorGamificacaoPort motorGamificacao,
            IDescontoFaturaPort descontoFatura) {
        return new ProgramaIndicacaoService(
            linkRepositorio, registroCliqueRepositorio,
            indicacaoRepositorio, auditoriaRepositorio,
            motorGamificacao, descontoFatura
        );
    }

    // ── Casos de uso ───────────────────────────────────────────────────────

    @Bean
    public ConfirmarConversaoIndicacaoUseCase confirmarConversaoIndicacaoUseCase(
            ProgramaIndicacaoService programaIndicacao) {
        return new ConfirmarConversaoIndicacaoUseCase(programaIndicacao);
    }

    @Bean
    public ObterOuGerarLinkIndicacaoUseCase obterOuGerarLinkIndicacaoUseCase(
            ProgramaIndicacaoService programaIndicacao) {
        return new ObterOuGerarLinkIndicacaoUseCase(programaIndicacao);
    }
}
