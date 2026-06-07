import { useParams } from "react-router-dom";
import { formatarDataHora } from "../../../../utils/formato";
import { BannerErro, EstadoVazio, Skeleton } from "../../components/compartilhados/Primitivos";
import { IndicadorEtapaAtual } from "../../components/compartilhados/IndicadorEtapaAtual";
import { TimelineProtocolo } from "../../components/compartilhados/TimelineProtocolo";
import { BannerAlertaProtocolo } from "../../components/tutor/BannerAlertaProtocolo";
import { CardStatusProtocolo } from "../../components/tutor/CardStatusProtocolo";
import { useProtocoloDoAtendimento } from "../../hooks/useProtocoloDoAtendimento";
import { isProtocoloAtivo } from "../../tipos";

/**
 * Acompanhamento ao vivo do protocolo pelo tutor (RN 15). Visão resumida e
 * tranquilizadora — nunca operacional: não expõe ids de responsáveis nem dados
 * técnicos internos. Polling de 10s, encerrado automaticamente em estado terminal.
 */
export function AcompanhamentoProtocoloPage() {
  const { atendimentoId = "" } = useParams();
  const { dados, carregando, atualizando, erro, ultimaAtualizacao, recarregar } =
    useProtocoloDoAtendimento(atendimentoId);

  if (carregando) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-20" />
        <Skeleton className="h-40" />
      </div>
    );
  }

  if (erro) {
    return <BannerErro mensagem={erro.message} onTentarNovamente={recarregar} />;
  }

  // 404 → sem protocolo ativo: mensagem tranquilizadora, não é erro.
  if (!dados) {
    return (
      <EstadoVazio
        icone="🟢"
        titulo="Nenhum protocolo de contato ativo no momento."
        descricao="Está tudo certo. Caso a clínica precise falar com você com urgência, você verá o acompanhamento por aqui."
      />
    );
  }

  const ativo = isProtocoloAtivo(dados.status);

  return (
    <div className="space-y-5">
      {ativo && <BannerAlertaProtocolo status={dados.status} />}

      {dados.status === "ENCERRADO_COM_SUCESSO" && (
        <div className="rounded-2xl border border-emerald-200 bg-emerald-50 px-5 py-4 text-emerald-900">
          <p className="text-base font-semibold">✓ Contato resolvido</p>
          <p className="mt-1 text-sm">
            Obrigado por retornar o contato. O protocolo foi encerrado com sucesso em{" "}
            {formatarDataHora(dados.encerradoEm)}.
          </p>
        </div>
      )}

      {dados.status === "ENCERRADO_POR_ESGOTAMENTO" && (
        <div className="rounded-2xl border border-paw-200 bg-paw-50 px-5 py-4 text-paw-700">
          <p className="text-base font-semibold">A clínica está em contato direto</p>
          <p className="mt-1 text-sm">
            Não conseguimos falar com você pelos canais cadastrados. Nossa equipe seguirá
            tentando por outros meios. O atendimento do seu pet continua normalmente.
          </p>
        </div>
      )}

      <CardStatusProtocolo
        visao={dados}
        atualizando={atualizando}
        ultimaAtualizacao={ultimaAtualizacao}
      />

      <section className="card p-6">
        <h2 className="mb-4 text-sm font-semibold text-ink-700">Etapa atual</h2>
        <IndicadorEtapaAtual status={dados.status} />
      </section>

      <section className="card p-6">
        <h2 className="mb-4 text-sm font-semibold text-ink-700">Acompanhamento</h2>
        <TimelineProtocolo
          tentativas={dados.tentativas}
          eventos={dados.eventosEscalonamento}
          modo="compacto"
        />
      </section>
    </div>
  );
}
