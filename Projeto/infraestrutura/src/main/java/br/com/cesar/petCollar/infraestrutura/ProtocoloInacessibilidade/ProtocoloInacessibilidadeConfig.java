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
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResponsavelSecundario;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResponsavelSecundarioId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.TipoConduta;
import br.com.cesar.petCollar.aplicacao.ProtocoloInacessibilidade.AtivarProtocoloUseCase;
import br.com.cesar.petCollar.aplicacao.ProtocoloInacessibilidade.ConsultarDiretivasConsentimentoUseCase;
import br.com.cesar.petCollar.aplicacao.ProtocoloInacessibilidade.ConsultarStatusProtocoloUseCase;
import br.com.cesar.petCollar.aplicacao.ProtocoloInacessibilidade.EncerrarProtocoloUseCase;
import br.com.cesar.petCollar.aplicacao.ProtocoloInacessibilidade.ExecutarEtapaProtocoloUseCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

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

    @Bean
    public AtivarProtocoloUseCase ativarProtocoloUseCase(AtivacaoProtocoloService ativacaoService) {
        return new AtivarProtocoloUseCase(ativacaoService);
    }

    @Bean
    public EncerrarProtocoloUseCase encerrarProtocoloUseCase(
            IProtocoloInacessibilidadeRepositorio protocoloRepositorio) {
        return new EncerrarProtocoloUseCase(protocoloRepositorio);
    }

    @Bean
    public ConsultarStatusProtocoloUseCase consultarStatusProtocoloUseCase(
            ConsultaStatusProtocoloService statusService) {
        return new ConsultarStatusProtocoloUseCase(statusService);
    }

    @Bean
    public ExecutarEtapaProtocoloUseCase executarEtapaProtocoloUseCase(
            IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
            OrquestradorEtapasProtocolo orquestrador) {
        return new ExecutarEtapaProtocoloUseCase(protocoloRepositorio, orquestrador);
    }

    @Bean
    public ConsultarDiretivasConsentimentoUseCase consultarDiretivasConsentimentoUseCase(
            ConsultaDiretivaConsentimentoService consultaService) {
        return new ConsultarDiretivasConsentimentoUseCase(consultaService);
    }

    @Bean
    public CommandLineRunner seedConfiguracaoProtocolo(
            IConfiguracaoProtocoloRepositorio configuracaoRepositorio) {
        return args -> {
            var vigente = configuracaoRepositorio.buscarVigente();
            if (vigente.isEmpty()) {
                configuracaoRepositorio.salvar(new ConfiguracaoProtocolo(
                        ConfiguracaoProtocoloId.gerar(), 15,
                        List.of(CanalContato.TELEFONE, CanalContato.SMS, CanalContato.EMAIL),
                        5, 1,
                        List.of(NivelEscalonamento.NIVEL_1_ADMINISTRATIVO,
                                NivelEscalonamento.NIVEL_2_COORDENACAO,
                                NivelEscalonamento.NIVEL_3_CLINICO,
                                NivelEscalonamento.NIVEL_4_DIRECAO)));
                log.info("[SEED] Configuração padrão do ProtocoloInacessibilidade criada.");
            } else {
                ConfiguracaoProtocolo config = vigente.get();
                if (config.getQuantidadeMaximaTentativasPorCanal() > 1) {
                    config.atualizar(
                        config.getTempoLimiteEsperaMinutos(),
                        config.getCanaisHabilitados(),
                        config.getIntervaloEntreTentativasMinutos(),
                        1,
                        config.getNiveisEscalonamento());
                    configuracaoRepositorio.salvar(config);
                    log.info("[SEED] quantidadeMaximaTentativasPorCanal corrigida para 1.");
                }
            }
        };
    }

    @Bean
    public CommandLineRunner seedResponsaveisSecundarios(
            ResponsavelSecundarioJpaRepository responsavelJpaRepository) {
        return args -> {
            if (responsavelJpaRepository.findByPacienteId("p-001").isEmpty()) {
                responsavelJpaRepository.save(ResponsavelSecundarioJpa.fromDomain("p-001",
                    new ResponsavelSecundario(ResponsavelSecundarioId.gerar(),
                        "Maria Silva (mãe)", 1,
                        List.of(CanalContato.TELEFONE, CanalContato.SMS))));
                responsavelJpaRepository.save(ResponsavelSecundarioJpa.fromDomain("p-001",
                    new ResponsavelSecundario(ResponsavelSecundarioId.gerar(),
                        "João Silva (pai)", 2,
                        List.of(CanalContato.TELEFONE))));
                log.info("[SEED] Responsáveis secundários criados para paciente p-001.");
            }
            if (responsavelJpaRepository.findByPacienteId("p-002").isEmpty()) {
                responsavelJpaRepository.save(ResponsavelSecundarioJpa.fromDomain("p-002",
                    new ResponsavelSecundario(ResponsavelSecundarioId.gerar(),
                        "Ana Costa (tutora)", 1,
                        List.of(CanalContato.EMAIL, CanalContato.SMS))));
                log.info("[SEED] Responsáveis secundários criados para paciente p-002.");
            }
        };
    }

    @Bean
    public CommandLineRunner seedDiretivasConsentimento(
            DiretivaConsentimentoJpaRepository diretivaJpaRepository) {
        return args -> {

            if (diretivaJpaRepository.findByPacienteId("p-001").isEmpty()) {
                diretivaJpaRepository.save(DiretivaConsentimentoJpa.criar("p-001",
                    List.of(TipoConduta.PROCEDIMENTO_INVASIVO,
                            TipoConduta.MEDICACAO_CONTROLADA,
                            TipoConduta.INTERNACAO,
                            TipoConduta.PROCEDIMENTO_ELETIVO)));
                log.info("[SEED] Diretivas de consentimento criadas para paciente p-001.");
            }

            if (diretivaJpaRepository.findByPacienteId("p-002").isEmpty()) {
                diretivaJpaRepository.save(DiretivaConsentimentoJpa.criar("p-002",
                    List.of(TipoConduta.MEDICACAO_CONTROLADA)));
                log.info("[SEED] Diretivas de consentimento criadas para paciente p-002.");
            }
        };
    }
}
