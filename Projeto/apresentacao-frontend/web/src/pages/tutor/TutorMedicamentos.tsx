import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../../auth/AuthContext";
import {
  ROTULOS_FREQUENCIA,
  ROTULOS_TAG,
  ROTULOS_VIA,
  criarFarmacovigilanciaTutorService,
  fmt,
  type Frequencia,
  type HistoricoDTO,
  type PrescricaoDTO,
  type TagClinica,
  type ViaAdministracao,
} from "../medico/farmacovigilanciaService";

/**
 * F-12 — visão do tutor sobre as prescrições farmacológicas vigentes
 * dos seus pets. Tudo read-only.
 */
export function TutorMedicamentos() {
  const { apiFetch } = useAuth();
  const service = useMemo(() => criarFarmacovigilanciaTutorService(apiFetch), [apiFetch]);

  const [dados, setDados] = useState<HistoricoDTO | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [selecionado, setSelecionado] = useState<PrescricaoDTO | null>(null);

  useEffect(() => {
    service.meusTratamentos()
      .then(setDados)
      .catch((e: Error) => { setErro(e.message); setDados({ prescricoes: [], nomesDosMedicamentos: {} }); });
  }, [service]);

  if (selecionado) {
    return <DetalhePrescricao prescricao={selecionado} onVoltar={() => setSelecionado(null)} />;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-ink-900">Medicamentos</h1>
        <p className="mt-1 text-sm text-ink-500">
          Acompanhe as prescrições farmacológicas vigentes dos médicos dos seus pets.
        </p>
      </div>

      {erro && (
        <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          {erro}
        </div>
      )}

      {dados === null ? (
        <div className="card h-40 animate-pulse" />
      ) : dados.prescricoes.length === 0 ? (
        <div className="card p-8 text-center">
          <p className="text-2xl">💊</p>
          <p className="mt-2 text-sm text-ink-600">Nenhuma prescrição farmacológica ativa no momento.</p>
          <p className="mt-1 text-xs text-ink-400">
            As prescrições aparecem aqui assim que o médico finaliza o atendimento.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          {dados.prescricoes.map(p => (
            <button
              key={p.id}
              type="button"
              onClick={() => setSelecionado(p)}
              className="card p-5 text-left transition hover:border-brand-400 hover:shadow"
            >
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="text-xs text-ink-500">Prescrição emitida em</p>
                  <p className="font-medium text-ink-900">
                    {new Date(p.assinatura.assinadoEm).toLocaleDateString("pt-BR")}
                  </p>
                </div>
                <span className="rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-medium text-brand-700 ring-1 ring-brand-100">
                  Vigente
                </span>
              </div>
              <dl className="mt-3 grid grid-cols-2 gap-x-4 gap-y-1 text-xs">
                <div>
                  <dt className="text-ink-500">Medicamentos</dt>
                  <dd className="font-medium text-ink-800">{p.itens.length}</dd>
                </div>
                <div>
                  <dt className="text-ink-500">Período</dt>
                  <dd className="font-medium text-ink-800">
                    {Math.max(...p.itens.map(it => it.duracaoDias))} dias
                  </dd>
                </div>
                <div className="col-span-2">
                  <dt className="text-ink-500">Termina em</dt>
                  <dd className="font-medium text-ink-800">
                    {new Date(p.dataFim).toLocaleDateString("pt-BR")}
                  </dd>
                </div>
              </dl>
              <p className="mt-3 text-xs text-brand-600">Ver detalhes →</p>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

function DetalhePrescricao({ prescricao, onVoltar }: { prescricao: PrescricaoDTO; onVoltar: () => void }) {
  const a = prescricao.assinatura;
  const dataAss = new Date(a.assinadoEm).toLocaleString("pt-BR");

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <button onClick={onVoltar} className="btn-ghost text-sm">← Voltar</button>
        <h1 className="text-xl font-bold text-ink-900">Prescrição Farmacológica</h1>
        <div className="w-24" />
      </div>

      <div className="card p-6 space-y-5">
        {/* Resumo do tratamento (com peso considerado pelo médico) */}
        <header className="space-y-1 border-b border-ink-200 pb-4">
          <h2 className="text-lg font-bold text-ink-900">Resumo do Tratamento</h2>
          <dl className="mt-3 grid grid-cols-2 gap-x-6 gap-y-2 text-sm">
            <Linha rotulo="Emitida em" valor={dataAss} />
            <Linha rotulo="Peso considerado" valor={`${fmt(prescricao.pesoPacienteKg, 2)} kg`} />
            <Linha rotulo="Início do tratamento" valor={new Date(prescricao.dataInicio).toLocaleDateString("pt-BR")} />
            <Linha rotulo="Fim do tratamento" valor={new Date(prescricao.dataFim).toLocaleDateString("pt-BR")} destaque />
          </dl>
        </header>

        {/* Considerações clínicas que o médico levou em conta */}
        {(prescricao.tagsClinicas.length > 0 || prescricao.alergiasConsideradas.length > 0) && (
          <section className="rounded-xl border border-amber-200 bg-amber-50/60 p-4">
            <h2 className="mb-2 text-base font-semibold text-amber-900">Considerações clínicas do paciente</h2>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              {prescricao.tagsClinicas.length > 0 && (
                <div>
                  <p className="text-xs text-amber-800">Tags clínicas consideradas</p>
                  <div className="mt-1 flex flex-wrap gap-1.5">
                    {prescricao.tagsClinicas.map(tag => (
                      <span key={tag} className="rounded-full bg-amber-100 px-2.5 py-0.5 text-xs font-medium text-amber-900">
                        {ROTULOS_TAG[tag as TagClinica]}
                      </span>
                    ))}
                  </div>
                </div>
              )}
              {prescricao.alergiasConsideradas.length > 0 && (
                <div>
                  <p className="text-xs text-amber-800">Alergias consideradas</p>
                  <div className="mt-1 flex flex-wrap gap-1.5">
                    {prescricao.alergiasConsideradas.map(al => (
                      <span key={al} className="rounded-full bg-red-100 px-2.5 py-0.5 text-xs font-medium text-red-800">
                        ⚠ {al}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </section>
        )}

        <section>
          <h2 className="mb-3 text-base font-semibold text-ink-900">
            Medicamentos prescritos ({prescricao.itens.length})
          </h2>
          <div className="space-y-3">
            {prescricao.itens.map((it, i) => (
              <div key={i} className="rounded-xl border-2 border-brand-200 bg-brand-50/30 p-4">
                <div className="flex items-start justify-between gap-3">
                  <p className="text-base font-semibold text-brand-800">
                    {i + 1}. {it.nomeMedicamento}
                  </p>
                </div>
                <dl className="mt-3 grid grid-cols-2 gap-x-6 gap-y-2 text-sm sm:grid-cols-3">
                  <Linha rotulo="Dose total por administração" valor={`${fmt(it.doseTotalMg, 2)} mg`} destaque />
                  <Linha rotulo="Volume aproximado" valor={`${fmt(it.volumeFinalMl, 2)} ml`} />
                  <Linha rotulo="Dose por kg" valor={`${fmt(it.doseMgPorKg, 2)} mg/kg`} />
                  <Linha rotulo="Via" valor={ROTULOS_VIA[it.via as ViaAdministracao]} />
                  <Linha rotulo="Frequência" valor={ROTULOS_FREQUENCIA[it.frequencia as Frequencia]} />
                  <Linha rotulo="Duração" valor={`${it.duracaoDias} dias`} />
                  <Linha rotulo="Horários" valor={it.horarios.join(", ") || "—"} destaque />
                </dl>
                {it.notaCuidado && (
                  <p className="mt-3 rounded-lg bg-amber-50 p-2 text-xs text-amber-800">
                    📝 <strong>Nota de cuidado:</strong> {it.notaCuidado}
                  </p>
                )}
              </div>
            ))}
          </div>
        </section>

        {prescricao.instrucoesGerais.length > 0 && (
          <section className="rounded-xl bg-ink-50 p-4">
            <h2 className="mb-2 text-base font-semibold text-ink-900">📋 Instruções gerais para você</h2>
            <ul className="list-disc space-y-1 pl-5 text-sm text-ink-800">
              {prescricao.instrucoesGerais.map((ins, i) => <li key={i}>{ins}</li>)}
            </ul>
          </section>
        )}

        <section className="border-t border-ink-200 pt-4">
          <h2 className="mb-2 text-base font-semibold text-ink-900">Assinatura do veterinário</h2>
          <img
            src={a.imagemBase64}
            alt="Assinatura do médico"
            className="h-24 w-auto rounded border border-ink-200 bg-white p-2"
          />
          <p className="mt-2 text-xs text-ink-500">
            ✓ Documento assinado digitalmente em {dataAss}.
          </p>
        </section>
      </div>
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
