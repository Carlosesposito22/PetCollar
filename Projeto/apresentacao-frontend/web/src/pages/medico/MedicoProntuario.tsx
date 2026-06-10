import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { criarMedicoService, type ProntuarioDTO } from "./medicoService";
import { gerarPdfRelatorio, listarRelatorios, type RelatorioSalvo } from "./relatorioStorage";

export function MedicoProntuario() {
  const { pacienteId } = useParams<{ pacienteId: string }>();
  const navigate = useNavigate();
  const { apiFetch } = useAuth();
  const service = useMemo(() => criarMedicoService(apiFetch), [apiFetch]);

  const [prontuario, setProntuario] = useState<ProntuarioDTO | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [relatorios, setRelatorios] = useState<RelatorioSalvo[]>([]);

  useEffect(() => {
    if (!pacienteId) return;
    setRelatorios(listarRelatorios(pacienteId));
    service
      .buscarProntuario(pacienteId)
      .then(setProntuario)
      .catch((e: Error) => setErro(e.message))
      .finally(() => setCarregando(false));
  }, [service, pacienteId]);

  if (carregando) {
    return (
      <div className="space-y-4">
        <div className="h-8 w-48 animate-pulse rounded-xl bg-ink-100" />
        <div className="card h-40 animate-pulse" />
        <div className="card h-60 animate-pulse" />
      </div>
    );
  }

  if (erro || !prontuario) {
    return (
      <div>
        <button onClick={() => navigate("/medico")} className="btn-ghost mb-4 text-sm">
          ← Voltar ao Painel
        </button>
        <div
          role="alert"
          className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900"
        >
          {erro ?? "Prontuário não encontrado."}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Navegação de volta */}
      <div className="flex items-center justify-between">
        <button onClick={() => navigate("/medico")} className="btn-ghost text-sm">
          ← Voltar ao Painel
        </button>
        <h1 className="text-xl font-bold text-ink-900">Prontuário do Paciente</h1>
        <div className="w-32" /> {/* espaçador para centralizar o título */}
      </div>

      {/* ── Seção 1: Alerta de Alergias (condicional) ──────────────────────── */}
      {prontuario.alergias.length > 0 && (
        <div
          role="alert"
          className="rounded-2xl border-2 border-red-300 bg-red-50 p-5"
        >
          <div className="flex items-center gap-2 mb-3">
            <span className="text-xl">⚠️</span>
            <h2 className="font-bold text-red-800 text-base">Alergias Conhecidas</h2>
          </div>
          <p className="text-sm text-red-700">
            {prontuario.alergias.map((a, i) => (
              <span key={i}>
                {i > 0 && <span className="mx-2 text-red-400">|</span>}
                • {a}
              </span>
            ))}
          </p>
        </div>
      )}

      {/* ── Seção 2: Dados do Paciente ─────────────────────────────────────── */}
      <div className="card p-6">
        <h2 className="mb-4 text-base font-semibold text-ink-900">Dados do Paciente</h2>

        {/* TODO: todos os campos abaixo vêm do stub até que
            GET /api/medico/pacientes/:pacienteId/prontuario seja implementado (F-10) */}
        <dl className="grid grid-cols-2 gap-x-8 gap-y-3 sm:grid-cols-3">
          <CampoInfo rotulo="Nome" valor={prontuario.nomePet} />
          <CampoInfo rotulo="Espécie" valor={prontuario.especie} />
          <CampoInfo rotulo="Raça" valor={prontuario.raca} />
          <CampoInfo
            rotulo="Idade"
            valor={prontuario.idadeAnos > 0 ? `${prontuario.idadeAnos} anos` : "—"}
          />
          <CampoInfo
            rotulo="Peso"
            valor={prontuario.pesoKg > 0 ? `${prontuario.pesoKg} kg` : "—"}
          />
          <CampoInfo rotulo="Sexo" valor={prontuario.sexo} />
        </dl>

        {/* Tags de perfil */}
        {prontuario.tags.length > 0 && (
          <div className="mt-5 flex flex-wrap gap-2">
            {prontuario.tags.map((tag, i) => (
              <span
                key={i}
                className={
                  "inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ring-1 " +
                  (tag.alerta
                    ? "bg-amber-50 text-amber-800 ring-amber-300"
                    : "bg-brand-50 text-brand-700 ring-brand-100")
                }
              >
                {tag.rotulo}
              </span>
            ))}
          </div>
        )}
      </div>

      {/* ── Seção 3: Histórico de Triagens ────────────────────────────────── */}
      <div className="card p-6">
        <h2 className="mb-4 text-base font-semibold text-ink-900">Histórico de Triagens</h2>

        {/* TODO: triagens vêm do stub até que endpoint de prontuário seja implementado (F-01 / F-10) */}
        {prontuario.triagens.length === 0 ? (
          <p className="text-sm text-ink-500">Nenhuma triagem registrada para este paciente.</p>
        ) : (
          <div className="space-y-3">
            {prontuario.triagens.map((t, i) => (
              <TriagemItem
                key={t.id ?? i}
                triagem={t}
                relatorio={relatorios.find((r) => r.triagemId === t.id) ?? null}
                onBaixarRelatorio={(r) => gerarPdfRelatorio(r)}
                onEmitirRelatorio={() =>
                  navigate(`/medico/prontuario/${pacienteId}/relatorio?triagem=${t.id}`)
                }
              />
            ))}
          </div>
        )}
      </div>

      {/* ── Seção 4: Ações do Prontuário ──────────────────────────────────── */}
      <div className="card p-6">
        <h2 className="mb-4 text-base font-semibold text-ink-900">Acesso às Seções</h2>
        <div className="grid grid-cols-2 gap-3">
          <BotaoAcao
            titulo="Relatório Clínico"
            onClick={() => {
              const ultima = prontuario.triagens[0];
              if (!ultima) {
                alert("Nenhum atendimento (triagem) registrado. O relatório é emitido por atendimento.");
                return;
              }
              navigate(`/medico/prontuario/${pacienteId}/relatorio?triagem=${ultima.id}`);
            }}
            destaque
          />
          <BotaoAcao
            titulo="Prescrição"
            // TODO: navegar para /medico/prontuario/:id/prescricao quando F-12 (Farmacovigilância) for implementado
            onClick={() => alert("Prescrição — funcionalidade em desenvolvimento (F-12).")}
          />
          <BotaoAcao
            titulo="Gestão Nutricional"
            // TODO: navegar para /medico/prontuario/:id/nutricional quando F-11 (NEM) for implementado
            onClick={() => alert("Gestão Nutricional (NEM) — funcionalidade em desenvolvimento (F-11).")}
          />
          <BotaoAcao
            titulo="Vacinação"
            // TODO: integrar com dados reais de vacinação do paciente via CicloVacinalService (F-06)
            onClick={() => alert("Vacinação — funcionalidade em desenvolvimento (F-06).")}
          />
        </div>
      </div>
    </div>
  );
}

// ── Subcomponentes ─────────────────────────────────────────────────────────────

function CampoInfo({ rotulo, valor }: { rotulo: string; valor: string }) {
  return (
    <div>
      <dt className="text-xs text-ink-500">{rotulo}</dt>
      <dd className="mt-0.5 text-sm font-medium text-ink-900">{valor}</dd>
    </div>
  );
}

function TriagemItem({
  triagem, relatorio, onBaixarRelatorio, onEmitirRelatorio,
}: {
  triagem: { id: string; data: string; motivo: string; corDeRisco: "VERMELHO" | "AMARELO" | "VERDE"; pesoTotal: number };
  relatorio: RelatorioSalvo | null;
  onBaixarRelatorio: (r: RelatorioSalvo) => void;
  onEmitirRelatorio: () => void;
}) {
  const corConfig = {
    VERMELHO: { bg: "bg-red-50",    ring: "ring-red-200",    text: "text-red-700",    ponto: "🔴", rotulo: "Vermelho" },
    AMARELO:  { bg: "bg-amber-50",  ring: "ring-amber-200",  text: "text-amber-700",  ponto: "🟡", rotulo: "Amarelo"  },
    VERDE:    { bg: "bg-green-50",  ring: "ring-green-200",  text: "text-green-700",  ponto: "🟢", rotulo: "Verde"    },
  }[triagem.corDeRisco];

  const dataFormatada = (() => {
    try {
      return new Date(triagem.data).toLocaleDateString("pt-BR");
    } catch {
      return triagem.data;
    }
  })();

  return (
    <div className="flex flex-wrap items-start justify-between gap-3 rounded-xl border border-ink-200/60 bg-ink-50/40 px-4 py-3">
      <div className="space-y-0.5 min-w-0">
        <p className="text-xs text-ink-500">
          <span className="font-medium text-ink-700">Data:</span> {dataFormatada}
        </p>
        <p className="text-xs text-ink-500">
          <span className="font-medium text-ink-700">Motivo:</span> {triagem.motivo}
        </p>
      </div>
      <div className="flex flex-wrap items-center gap-2 shrink-0">
        <span
          className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ${corConfig.bg} ${corConfig.ring} ${corConfig.text}`}
        >
          {corConfig.ponto} {corConfig.rotulo}
        </span>
        <span className="text-xs text-ink-500">
          | PesoTotal: <strong className="text-ink-700">{triagem.pesoTotal}</strong>
        </span>
        {relatorio ? (
          <button
            type="button"
            onClick={() => onBaixarRelatorio(relatorio)}
            className="inline-flex items-center gap-1 rounded-lg border border-brand-300 bg-brand-50 px-2.5 py-1 text-xs font-medium text-brand-700 transition hover:bg-brand-100"
            title="Ver/baixar o relatório clínico já emitido para este atendimento"
          >
            📄 Ver Relatório
          </button>
        ) : (
          <button
            type="button"
            onClick={onEmitirRelatorio}
            className="inline-flex items-center gap-1 rounded-lg border border-ink-300 bg-white px-2.5 py-1 text-xs font-medium text-ink-700 transition hover:border-brand-400 hover:bg-brand-50 hover:text-brand-700"
            title="Emitir o relatório clínico deste atendimento"
          >
            ✍ Emitir Relatório
          </button>
        )}
      </div>
    </div>
  );
}

function BotaoAcao({ titulo, onClick, destaque = false }: { titulo: string; onClick: () => void; destaque?: boolean }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={
        "rounded-xl border px-4 py-4 text-sm font-medium transition focus:outline-none focus:ring-4 focus:ring-brand-100 " +
        (destaque
          ? "border-brand-400 bg-brand-50 text-brand-700 hover:bg-brand-100"
          : "border-ink-300 bg-white text-ink-700 hover:border-brand-400 hover:bg-brand-50 hover:text-brand-700")
      }
    >
      {titulo}
    </button>
  );
}
