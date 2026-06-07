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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring canônico (§6.5 do guia) dos serviços de domínio do ProtocoloInacessibilidade
 * como beans, a partir das interfaces {@code IXxxRepositorio} e das portas de saída,
 * que o Spring resolve para os adapters JPA deste módulo.
 *
 * <p>Enquanto o DataSource/JPA estiver desligado (ver application.yml), esta
 * configuração permanece como referência: a infraestrutura não é varrida pelo
 * component-scan do backend, cujos beans em memória sobem a API. Ao ligar a
 * persistência, importe/escaneie este pacote para ativar os adapters JPA.
 */
@Configuration
public class ProtocoloInacessibilidadeConfig {

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
}
