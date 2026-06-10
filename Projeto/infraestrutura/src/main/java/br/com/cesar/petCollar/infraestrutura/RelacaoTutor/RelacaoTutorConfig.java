package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

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
 * Spring resolve IXxxRepositorio para os adapters JPA deste mesmo pacote.
 */
@Configuration
@EntityScan(basePackages = "br.com.cesar.petCollar.infraestrutura.RelacaoTutor")
@EnableJpaRepositories(basePackages = "br.com.cesar.petCollar.infraestrutura.RelacaoTutor")
public class RelacaoTutorConfig {

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
}
