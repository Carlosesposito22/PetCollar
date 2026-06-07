import { useState } from "react";
import { Link } from "react-router-dom";
import { formatarDataHora } from "../../../../utils/formato";
import { BannerErro, EstadoVazio, Skeleton } from "../../components/compartilhados/Primitivos";
import { PreviewConfiguracao } from "../../components/administrador/PreviewConfiguracao";
import { useHistoricoConfiguracoes } from "../../hooks/useHistoricoConfiguracoes";
import type { ConfiguracaoProtocoloDTO } from "../../tipos";

/**
 * Histórico (somente leitura) das versões da configuração. Configurações são
 * imutáveis após criadas — nunca há ação de editar versão antiga.
 */
export function HistoricoConfiguracaoPage() {
  const { dados, carregando, erro, recarregar } = useHistoricoConfiguracoes();

  const versoes = [...(dados ?? [])].sort((a, b) => b.versao - a.versao);

  return (
    <div className="mx-auto max-w-3xl space-y-5 p-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-ink-900">Histórico de configurações</h1>
        <Link to="/admin/protocolos/configuracao" className="text-sm text-brand-700 hover:underline">
          ← Configuração vigente
        </Link>
      </div>

      {erro && <BannerErro mensagem={erro.message} onTentarNovamente={recarregar} />}

      {carregando ? (
        <Skeleton className="h-40" />
      ) : versoes.length === 0 ? (
        <EstadoVazio titulo="Ainda não há versões registradas." />
      ) : (
        <ul className="space-y-3">
          {versoes.map((v, i) => (
            <CardVersao key={v.id} versao={v} vigente={i === 0} />
          ))}
        </ul>
      )}
    </div>
  );
}

function CardVersao({ versao, vigente }: { versao: ConfiguracaoProtocoloDTO; vigente: boolean }) {
  const [aberto, setAberto] = useState(vigente);
  return (
    <li className="card overflow-hidden">
      <button
        onClick={() => setAberto((a) => !a)}
        className="flex w-full items-center justify-between gap-3 px-5 py-4 text-left"
      >
        <span className="flex items-center gap-3">
          <span className="text-sm font-semibold text-ink-800">Versão {versao.versao}</span>
          {vigente && (
            <span className="rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-medium text-emerald-700 ring-1 ring-emerald-200">
              Vigente
            </span>
          )}
        </span>
        <span className="flex items-center gap-3 text-xs text-ink-500">
          {formatarDataHora(versao.atualizadaEm)}
          <span aria-hidden>{aberto ? "▲" : "▼"}</span>
        </span>
      </button>
      {aberto && (
        <div className="border-t border-ink-300/50 px-5 py-4">
          <PreviewConfiguracao
            tempoLimiteEsperaMinutos={versao.tempoLimiteEsperaMinutos}
            canaisHabilitados={versao.canaisHabilitados}
            niveisEscalonamento={versao.niveisEscalonamento}
            intervaloEntreTentativasMinutos={versao.intervaloEntreTentativasMinutos}
            quantidadeMaximaTentativasPorCanal={versao.quantidadeMaximaTentativasPorCanal}
          />
        </div>
      )}
    </li>
  );
}
