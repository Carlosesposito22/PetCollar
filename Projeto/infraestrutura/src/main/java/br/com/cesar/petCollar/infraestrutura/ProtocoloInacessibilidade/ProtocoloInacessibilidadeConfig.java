package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.IConfiguracaoProtocoloRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IConsultaAtendimento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IDiretivaConsentimentoRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IResponsavelSecundarioRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoCanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa.EtapaContatoResponsaveisSecundariosService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa.EtapaContatoTutorService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa.EtapaEscalonamentoService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa.OrquestradorEtapasProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.AtivacaoProtocoloService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.ConsultaDiretivaConsentimentoService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.ConsultaStatusProtocoloService;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelEscalonamento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

/**
 * Wiring canônico (§6.5) dos serviços de domínio do ProtocoloInacessibilidade
 * como beans, a partir dos adapters JPA deste módulo.
 */
@Configuration
@EntityScan(basePackages = "br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade")
@EnableJpaRepositories(basePackages = "br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade")
public class ProtocoloInacessibilidadeConfig {

    private static final Logger log = LoggerFactory.getLogger(ProtocoloInacessibilidadeConfig.class);

    @Bean
    public AtivacaoProtocoloService ativacaoProtocoloService(
            IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
            IConfiguracaoProtocoloRepositorio configuracaoRepositorio,
            IConsultaAtendimento atendimentos, IServicoNotificacao notificacao) {
        return new AtivacaoProtocoloService(protocoloRepositorio, configuracaoRepositorio,
            atendimentos, notificacao);
    }

    // ── Etapas do protocolo via Template Method (uma subclasse por fase) ──────

    @Bean
    public EtapaContatoTutorService etapaContatoTutorService(
            IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
            IServicoNotificacao notificacao,
            IConfiguracaoProtocoloRepositorio configuracaoRepositorio,
            IServicoCanalContato servicoCanalContato) {
        return new EtapaContatoTutorService(protocoloRepositorio, notificacao,
            configuracaoRepositorio, servicoCanalContato);
    }

    @Bean
    public EtapaContatoResponsaveisSecundariosService etapaContatoResponsaveisSecundariosService(
            IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
            IServicoNotificacao notificacao,
            IResponsavelSecundarioRepositorio responsaveis,
            IServicoCanalContato servicoCanalContato) {
        return new EtapaContatoResponsaveisSecundariosService(protocoloRepositorio, notificacao,
            responsaveis, servicoCanalContato);
    }

    @Bean
    public EtapaEscalonamentoService etapaEscalonamentoService(
            IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
            IServicoNotificacao notificacao,
            IConfiguracaoProtocoloRepositorio configuracaoRepositorio) {
        return new EtapaEscalonamentoService(protocoloRepositorio, notificacao, configuracaoRepositorio);
    }

    @Bean
    public OrquestradorEtapasProtocolo orquestradorEtapasProtocolo(
            EtapaContatoTutorService etapaTutor,
            EtapaContatoResponsaveisSecundariosService etapaSecundarios,
            EtapaEscalonamentoService etapaEscalonamento) {
        return new OrquestradorEtapasProtocolo(etapaTutor, etapaSecundarios, etapaEscalonamento);
    }

    @Bean
    public ConsultaDiretivaConsentimentoService consultaDiretivaConsentimentoService(
            IDiretivaConsentimentoRepositorio diretivas) {
        return new ConsultaDiretivaConsentimentoService(diretivas);
    }

    @Bean
    public ConsultaStatusProtocoloService consultaStatusProtocoloService(
            IProtocoloInacessibilidadeRepositorio protocoloRepositorio) {
        return new ConsultaStatusProtocoloService(protocoloRepositorio);
    }

    /** Seed operacional: cria a configuração padrão (RN 1/2/6) se não existir. */
    @Bean
    public CommandLineRunner seedConfiguracaoProtocolo(
            IConfiguracaoProtocoloRepositorio configuracaoRepositorio) {
        return args -> {
            if (configuracaoRepositorio.buscarVigente().isEmpty()) {
                configuracaoRepositorio.salvar(new ConfiguracaoProtocolo(
                        ConfiguracaoProtocoloId.gerar(), 15,
                        List.of(CanalContato.TELEFONE, CanalContato.SMS, CanalContato.EMAIL),
                        5, 2,
                        List.of(NivelEscalonamento.NIVEL_1_ADMINISTRATIVO,
                                NivelEscalonamento.NIVEL_2_COORDENACAO,
                                NivelEscalonamento.NIVEL_3_CLINICO,
                                NivelEscalonamento.NIVEL_4_DIRECAO)));
                log.info("[SEED] Configuração padrão do ProtocoloInacessibilidade criada.");
            }
        };
    }
}
