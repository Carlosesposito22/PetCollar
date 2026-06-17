package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.IConfiguracaoProtocoloRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelEscalonamento;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.ConfiguracaoProtocoloDTO;
import br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto.RequisicaoConfigurarProtocoloDTO;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/configuracoes-protocolo")
public class ConfiguracaoProtocoloController {

    private final IConfiguracaoProtocoloRepositorio configuracaoRepositorio;

    public ConfiguracaoProtocoloController(IConfiguracaoProtocoloRepositorio configuracaoRepositorio) {
        this.configuracaoRepositorio = configuracaoRepositorio;
    }

    @GetMapping("/vigente")
    public ConfiguracaoProtocoloDTO vigente() {
        return configuracaoRepositorio.buscarVigente()
            .map(ConfiguracaoProtocoloDTO::de)
            .orElseThrow(() -> new IllegalStateException("Não há configuração de protocolo vigente."));
    }

    @GetMapping("/historico")
    public List<ConfiguracaoProtocoloDTO> historico() {
        return configuracaoRepositorio.listarHistorico().stream()
            .map(ConfiguracaoProtocoloDTO::de)
            .toList();
    }

    @PutMapping
    public ConfiguracaoProtocoloDTO configurar(@RequestBody RequisicaoConfigurarProtocoloDTO req) {
        List<CanalContato> canais = req.canaisHabilitados() == null ? List.of()
            : req.canaisHabilitados().stream().map(CanalContato::valueOf).toList();
        List<NivelEscalonamento> niveis = req.niveisEscalonamento() == null ? List.of()
            : req.niveisEscalonamento().stream().map(NivelEscalonamento::valueOf).toList();

        ConfiguracaoProtocolo vigente = configuracaoRepositorio.buscarVigente().orElse(null);
        ConfiguracaoProtocolo nova;
        if (vigente == null) {
            nova = new ConfiguracaoProtocolo(ConfiguracaoProtocoloId.gerar(),
                req.tempoLimiteEsperaMinutos(), canais, req.intervaloEntreTentativasMinutos(),
                req.quantidadeMaximaTentativasPorCanal(), niveis);
        } else {

            nova = new ConfiguracaoProtocolo(ConfiguracaoProtocoloId.gerar(),
                req.tempoLimiteEsperaMinutos(), canais, req.intervaloEntreTentativasMinutos(),
                req.quantidadeMaximaTentativasPorCanal(), niveis,
                vigente.getVersao() + 1, LocalDateTime.now(), LocalDateTime.now());
            nova.validarConsistencia();
        }
        configuracaoRepositorio.salvar(nova);
        return ConfiguracaoProtocoloDTO.de(nova);
    }
}
