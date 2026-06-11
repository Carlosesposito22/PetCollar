import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../../auth/AuthContext";
import {
  criarNutricaoMedicoService,
  criarNutricaoTutorService,
  fmt,
  ROTULOS_COMORBIDADE,
  ROTULOS_NIVEL_ATIVIDADE,
  ROTULOS_TIPO_CRONOGRAMA,
  type PlanoNutricionalDTO,
  type RacaoDTO,
} from "../medico/nutricaoService";

/**
 * F-11 — visão do tutor sobre os planos nutricionais finalizados pelos médicos
 * dos seus pets. Read-only: o tutor não edita nada, apenas consulta a
 * prescrição e a assinatura digital.
 */
export function TutorNutricao() {
  const { apiFetch } = useAuth();
  const service = useMemo(() => criarNutricaoTutorService(apiFetch), [apiFetch]);

  const [planos, setPlanos] = useState<PlanoNutricionalDTO[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [planoSelecionado, setPlanoSelecionado] = useState<PlanoNutricionalDTO | null>(null);

  useEffect(() => {
    service.listarMeusPlanos()
      .then(setPlanos)
      .catch((e: Error) => { setErro(e.message); setPlanos([]); });
  }, [service]);

  if (planoSelecionado) {
    return <DetalhePlano plano={planoSelecionado} onVoltar={() => setPlanoSelecionado(null)} />;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-ink-900">Nutrição</h1>
        <p className="mt-1 text-sm text-ink-500">
          Acompanhe os planos nutricionais prescritos pelos médicos dos seus pets.
        </p>
      </div>

      {erro && (
        <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          {erro}
        </div>
      )}

      {planos === null ? (
        <div className="card h-40 animate-pulse" />
      ) : planos.length === 0 ? (
        <div className="card p-8 text-center">
          <p className="text-2xl">🥣</p>
          <p className="mt-2 text-sm text-ink-600">
            Nenhum plano nutricional foi prescrito ainda.
          </p>
          <p className="mt-1 text-xs text-ink-400">
            Os planos aparecem aqui assim que o médico finaliza a prescrição.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          {planos.map(plano => (
            <button
              key={plano.id}
              type="button"
              onClick={() => setPlanoSelecionado(plano)}
              className="card p-5 text-left transition hover:border-brand-400 hover:shadow"
            >
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="text-xs text-ink-500">Plano</p>
                  <p className="font-medium text-ink-900">
                    {ROTULOS_TIPO_CRONOGRAMA[plano.cronograma.tipo]}
                  </p>
                </div>
                <span className="inline-flex items-center rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-medium text-brand-700 ring-1 ring-brand-100">
                  Finalizado
                </span>
              </div>
              <dl className="mt-3 grid grid-cols-2 gap-x-4 gap-y-1 text-xs">
                <Mini rotulo="Peso ideal" valor={`${plano.parametros.pesoIdealKg} kg`} />
                <Mini
                  rotulo="Ração/dia"
                  valor={
                    plano.resultadoFinalizado
                      ? `${fmt(plano.resultadoFinalizado.quantidadeRecomendadaGramasPorDia, 0)} g`
                      : "—"
                  }
                />
                <Mini
                  rotulo="NEM total"
                  valor={
                    plano.resultadoFinalizado
                      ? `${fmt(plano.resultadoFinalizado.nemTotal, 0)} kcal`
                      : "—"
                  }
                />
                <Mini rotulo="Emitido em" valor={new Date(plano.atualizadoEm).toLocaleDateString("pt-BR")} />
              </dl>
              <p className="mt-3 text-xs text-brand-600">Ver detalhes →</p>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

// ── Detalhe ──────────────────────────────────────────────────────────────────

function DetalhePlano({ plano, onVoltar }: { plano: PlanoNutricionalDTO; onVoltar: () => void }) {
  const resultado = plano.resultadoFinalizado;
  const assinatura = plano.assinatura;
  const dataAssinatura = assinatura ? new Date(assinatura.assinadoEm).toLocaleString("pt-BR") : "—";

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <button onClick={onVoltar} className="btn-ghost text-sm">← Voltar</button>
        <h1 className="text-xl font-bold text-ink-900">Plano Nutricional</h1>
        <div className="w-24" />
      </div>

      <div className="card p-6 space-y-5">
        <header>
          <p className="text-xs text-ink-500">Plano nº <code>{plano.id}</code></p>
          <p className="text-sm text-ink-500">Emitido em {dataAssinatura}</p>
        </header>

        <section>
          <h2 className="mb-2 text-base font-semibold text-ink-900">Parâmetros</h2>
          <dl className="grid grid-cols-2 gap-x-6 gap-y-2 text-sm sm:grid-cols-3">
            <Linha rotulo="Peso atual" valor={`${plano.parametros.pesoAtualKg} kg`} />
            <Linha rotulo="Peso ideal" valor={`${plano.parametros.pesoIdealKg} kg`} />
            <Linha rotulo="Nível de atividade" valor={ROTULOS_NIVEL_ATIVIDADE[plano.parametros.nivelAtividade]} />
            <Linha rotulo="Comorbidade" valor={ROTULOS_COMORBIDADE[plano.parametros.comorbidade]} />
            <Linha rotulo="Densidade calórica" valor={`${plano.parametros.densidadeCaloricaKcalPorKg} kcal/kg`} />
          </dl>
        </section>

        {resultado && (
          <section>
            <h2 className="mb-2 text-base font-semibold text-ink-900">Recomendação</h2>
            <dl className="grid grid-cols-2 gap-x-6 gap-y-2 text-sm sm:grid-cols-3">
              <Linha rotulo="NEM total" valor={`${fmt(resultado.nemTotal, 0)} kcal/dia`} destaque />
              <Linha
                rotulo="Quantidade recomendada"
                valor={`${fmt(resultado.quantidadeRecomendadaGramasPorDia, 0)} g/dia`}
                destaque
              />
            </dl>
          </section>
        )}

        <section>
          <h2 className="mb-2 text-base font-semibold text-ink-900">
            Transição alimentar — {ROTULOS_TIPO_CRONOGRAMA[plano.cronograma.tipo]}
          </h2>
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-ink-200 text-left text-xs text-ink-500">
                <th className="py-2 pr-3">Faixa</th>
                <th className="py-2 pr-3">% Ração Atual</th>
                <th className="py-2">% Ração Nova</th>
              </tr>
            </thead>
            <tbody>
              {plano.cronograma.dias.map((dia, i) => (
                <tr key={i} className="border-b border-ink-100">
                  <td className="py-1.5 pr-3">{dia.faixaDias}</td>
                  <td className="py-1.5 pr-3">{dia.percentualRacaoAtual}%</td>
                  <td className="py-1.5">{dia.percentualRacaoNova}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>

        {plano.observacoes.length > 0 && (
          <section>
            <h2 className="mb-2 text-base font-semibold text-ink-900">Observações do médico</h2>
            <ul className="list-disc space-y-1 pl-5 text-sm">
              {plano.observacoes.map((o, i) => <li key={i}>{o}</li>)}
            </ul>
          </section>
        )}

        {assinatura && (
          <section className="border-t border-ink-200 pt-4">
            <h2 className="mb-2 text-base font-semibold text-ink-900">Assinatura do veterinário</h2>
            <img
              src={assinatura.imagemBase64}
              alt="Assinatura do médico"
              className="h-24 w-auto rounded border border-ink-200 bg-white p-2"
            />
            <p className="mt-2 text-xs text-ink-500">Hash SHA-256: <code>{assinatura.hashConteudo}</code></p>
          </section>
        )}
      </div>
    </div>
  );
}

function Mini({ rotulo, valor }: { rotulo: string; valor: string }) {
  return (
    <div>
      <dt className="text-ink-500">{rotulo}</dt>
      <dd className="font-medium text-ink-800">{valor}</dd>
    </div>
  );
}

function Linha({ rotulo, valor, destaque = false }: { rotulo: string; valor: string; destaque?: boolean }) {
  return (
    <div>
      <dt className="text-xs text-ink-500">{rotulo}</dt>
      <dd className={"mt-0.5 " + (destaque ? "text-brand-700 font-semibold" : "text-ink-900 font-medium")}>
        {valor}
      </dd>
    </div>
  );
}
