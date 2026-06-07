package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.IConfiguracaoProtocoloRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelEscalonamento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IConsultaAtendimento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IDiretivaConsentimentoRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IResponsavelSecundarioRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoCanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResponsavelSecundario;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResponsavelSecundarioId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResumoAtendimento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.TipoConduta;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa.EtapaContatoResponsaveisSecundariosService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa.EtapaContatoTutorService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa.EtapaEscalonamentoService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa.OrquestradorEtapasProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.AtivacaoProtocoloService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.ConsultaDiretivaConsentimentoService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.ConsultaStatusProtocoloService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Wiring (§6.5) dos serviços de domínio do ProtocoloInacessibilidade como beans, a
 * partir dos adapters em memória/fakes deste módulo, e seed de dados de
 * demonstração. Espelha o {@code ProtocoloInacessibilidadeConfig} da infraestrutura
 * (que entra em cena quando o banco é ligado).
 */
@Configuration
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

    /**
     * Seed: cria a configuração padrão (RN 1/2/6) se nenhuma existir e registra um
     * atendimento de demonstração (com tutor "inacessível") + responsável secundário
     * + diretiva autorizada, para exercitar os endpoints e o scheduler de timeout.
     */
    @Bean
    public CommandLineRunner seedProtocoloInacessibilidade(
            IConfiguracaoProtocoloRepositorio configuracaoRepositorio,
            AtendimentoConsultaEmMemoria atendimentos,
            ResponsavelSecundarioRepositorioEmMemoria responsaveis,
            DiretivaConsentimentoRepositorioEmMemoria diretivas) {
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
            }

            AtendimentoId atendimentoId = AtendimentoId.gerar();
            PacienteId pacienteId = PacienteId.gerar();
            TutorId tutorId = TutorId.gerar();

            // Atendimento em andamento, com tutor sem interação há 30 min (estoura o timeout de 15).
            atendimentos.registrar(new ResumoAtendimento(
                atendimentoId, pacienteId, tutorId, LocalDateTime.now().minusMinutes(30), true));

            responsaveis.cadastrar(pacienteId, new ResponsavelSecundario(
                ResponsavelSecundarioId.gerar(), "Maria (responsável secundária)", 1,
                List.of(CanalContato.TELEFONE, CanalContato.WHATSAPP)));

            diretivas.autorizar(pacienteId, TipoConduta.MEDICACAO_CONTROLADA);
            diretivas.autorizar(pacienteId, TipoConduta.INTERNACAO);

            log.info("[SEED F-03] atendimento de demonstração: atendimentoId={} pacienteId={} tutorId={}",
                atendimentoId.getValor(), pacienteId.getValor(), tutorId.getValor());
        };
    }
}
