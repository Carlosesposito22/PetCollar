import { useCallback, useEffect, useState } from "react";
import { useAuth } from "../../auth/AuthContext";

// ─── Types ────────────────────────────────────────────────────────────────────

type CategoriaBadge = "FIDELIDADE" | "SAUDE_DO_PET" | "ENGAJAMENTO";
type RaridadeBadge  = "COMUM" | "INCOMUM" | "RARA" | "EPICA" | "LENDARIA";

type Badge = {
  badgeId: string;
  nome: string;
  descricao: string;
  categoria: CategoriaBadge;
  raridade: RaridadeBadge;
  conquistadaEm: string | null; // ISO date — null = ainda não conquistada
  eventoDisparador: string | null;
};

type ProgressoBadge = {
  badgeId: string;
  badgeNome: string;
  valorAtual: number;
  metaTotal: number;
  percentualConclusao: number;
};

type ConquistasResponse = {
  tempoAssinaturaMeses: number;
  totalBadges: number;
  badgesDesbloqueadas: number;
  badges: Badge[];
  progressos: ProgressoBadge[];
};

// ─── Mock ─────────────────────────────────────────────────────────────────────

const MOCK: ConquistasResponse = {
  tempoAssinaturaMeses: 6,
  totalBadges: 6,
  badgesDesbloqueadas: 3,
  badges: [
    {
      badgeId: "b1",
      nome: "6 Meses Ativo",
      descricao: "Assinatura ativa por 6 meses consecutivos.",
      categoria: "FIDELIDADE",
      raridade: "INCOMUM",
      conquistadaEm: "2025-10-15",
      eventoDisparador: "assinatura_6_meses",
    },
    {
      badgeId: "b2",
      nome: "1 Ano Ativo",
      descricao: "Assinatura ativa por 12 meses consecutivos.",
      categoria: "FIDELIDADE",
      raridade: "RARA",
      conquistadaEm: null,
      eventoDisparador: "assinatura_12_meses",
    },
    {
      badgeId: "b3",
      nome: "Vacinação em Dia",
      descricao: "Todos os pets com vacinas em dia no ciclo anual.",
      categoria: "SAUDE_DO_PET",
      raridade: "COMUM",
      conquistadaEm: "2026-03-20",
      eventoDisparador: "vacinas_em_dia",
    },
    {
      badgeId: "b4",
      nome: "Check-up Anual",
      descricao: "Realizou o check-up anual completo com o veterinário.",
      categoria: "SAUDE_DO_PET",
      raridade: "INCOMUM",
      conquistadaEm: null,
      eventoDisparador: "checkup_anual",
    },
    {
      badgeId: "b5",
      nome: "Primeira Consulta",
      descricao: "Realizou a primeira consulta pelo plano petCollar.",
      categoria: "ENGAJAMENTO",
      raridade: "COMUM",
      conquistadaEm: "2026-05-05",
      eventoDisparador: "primeira_consulta",
    },
    {
      badgeId: "b6",
      nome: "5 Consultas Realizadas",
      descricao: "Realizou 5 consultas pelo plano petCollar.",
      categoria: "ENGAJAMENTO",
      raridade: "INCOMUM",
      conquistadaEm: null,
      eventoDisparador: "consultas_realizadas",
    },
  ],
  progressos: [
    { badgeId: "b2", badgeNome: "1 Ano Ativo",           valorAtual: 6,  metaTotal: 12, percentualConclusao: 50 },
    { badgeId: "b6", badgeNome: "5 Consultas Realizadas", valorAtual: 3,  metaTotal: 5,  percentualConclusao: 60 },
  ],
};

// ─── Config visual ────────────────────────────────────────────────────────────

const CATEGORIA_CONFIG: Record<CategoriaBadge, { label: string; cor: string }> = {
  FIDELIDADE:    { label: "Fidelidade",    cor: "text-violet-700 bg-violet-50 ring-violet-200" },
  SAUDE_DO_PET:  { label: "Saúde do Pet",  cor: "text-emerald-700 bg-emerald-50 ring-emerald-200" },
  ENGAJAMENTO:   { label: "Engajamento",   cor: "text-brand-700 bg-brand-50 ring-brand-200" },
};

const RARIDADE_CONFIG: Record<RaridadeBadge, { label: string; borda: string; fundo: string; texto: string; emoji: string }> = {
  COMUM:    { label: "Comum",    borda: "border-ink-300",    fundo: "bg-ink-50",     texto: "text-ink-500",    emoji: "⭐" },
  INCOMUM:  { label: "Incomum",  borda: "border-amber-300",  fundo: "bg-amber-50",   texto: "text-amber-600",  emoji: "🏆" },
  RARA:     { label: "Rara",     borda: "border-sky-300",    fundo: "bg-sky-50",     texto: "text-sky-600",    emoji: "💎" },
  EPICA:    { label: "Épica",    borda: "border-violet-400", fundo: "bg-violet-50",  texto: "text-violet-700", emoji: "🔮" },
  LENDARIA: { label: "Lendária", borda: "border-paw-400",    fundo: "bg-paw-50",     texto: "text-paw-700",    emoji: "🌟" },
};

const CATEGORIAS: CategoriaBadge[] = ["FIDELIDADE", "SAUDE_DO_PET", "ENGAJAMENTO"];

// ─── Helpers ─────────────────────────────────────────────────────────────────

function formatarData(iso: string) {
  const [a, m, d] = iso.split("-");
  return `${d}/${m}/${a}`;
}

function rotuloPeriodo(meses: number) {
  if (meses < 12) return `${meses} ${meses === 1 ? "mês" : "meses"}`;
  const anos = Math.floor(meses / 12);
  const resto = meses % 12;
  return resto === 0
    ? `${anos} ${anos === 1 ? "ano" : "anos"}`
    : `${anos} ${anos === 1 ? "ano" : "anos"} e ${resto} ${resto === 1 ? "mês" : "meses"}`;
}

// ─── Componente principal ─────────────────────────────────────────────────────

export function TutorConquistas() {
  const { apiFetch } = useAuth();
  const [dados, setDados] = useState<ConquistasResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [badgeDetalhe, setBadgeDetalhe] = useState<Badge | null>(null);

  const carregar = useCallback(async () => {
    try {
      const res = await apiFetch("/api/tutor/conquistas");
      if (res.ok) {
        setDados(await res.json());
        return;
      }
    } catch { /* API ainda não disponível */ }
    setDados(MOCK);
  }, [apiFetch]);

  useEffect(() => {
    void carregar().finally(() => setCarregando(false));
  }, [carregar]);

  if (carregando || !dados) {
    return (
      <div className="space-y-4">
        <div className="card h-28 animate-pulse bg-white/60" />
        <div className="card h-64 animate-pulse bg-white/60" />
      </div>
    );
  }

  const pctGeral = dados.totalBadges > 0
    ? Math.round((dados.badgesDesbloqueadas / dados.totalBadges) * 100)
    : 0;

  return (
    <div className="space-y-8">

      {/* ── Cabeçalho ── */}
      <div>
        <h1 className="text-2xl font-bold text-ink-900">Programa de Conquistas</h1>
        <p className="mt-1 text-sm text-ink-500">
          Desbloqueie badges completando ações reais no petCollar.
        </p>
      </div>

      {/* ── Card de resumo geral ── */}
      <div className="card p-5">
        <div className="flex flex-wrap items-start justify-between gap-4 mb-4">
          <div>
            <p className="text-xs font-medium uppercase tracking-wide text-ink-400">Tempo de Assinatura Ativa</p>
            <p className="text-2xl font-bold text-ink-900">{rotuloPeriodo(dados.tempoAssinaturaMeses)}</p>
          </div>
          <div className="text-right">
            <p className="text-xs font-medium uppercase tracking-wide text-ink-400">Badges Desbloqueadas</p>
            <p className="text-2xl font-bold text-ink-900">
              {dados.badgesDesbloqueadas}{" "}
              <span className="text-base font-normal text-ink-400">de {dados.totalBadges}</span>
            </p>
          </div>
        </div>

        {/* Barra de progresso geral */}
        <div className="h-3 w-full overflow-hidden rounded-full bg-ink-100">
          <div
            className="h-full rounded-full bg-brand-500 transition-all duration-700"
            style={{ width: `${pctGeral}%` }}
          />
        </div>
        <p className="mt-1.5 text-right text-xs text-ink-400">{pctGeral}% concluído</p>
      </div>

      {/* ── Badges por categoria ── */}
      <section className="space-y-6">
        <h2 className="text-lg font-bold text-ink-900">Badges por Categoria</h2>

        {CATEGORIAS.map(cat => {
          const config = CATEGORIA_CONFIG[cat];
          const badgesDaCat = dados.badges.filter(b => b.categoria === cat);

          return (
            <div key={cat}>
              {/* Separador de categoria */}
              <div className="mb-3 flex items-center gap-3">
                <span
                  className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ${config.cor}`}
                >
                  {config.label}
                </span>
                <div className="flex-1 border-t border-ink-200" />
              </div>

              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {badgesDaCat.map(badge => (
                  <BadgeCard
                    key={badge.badgeId}
                    badge={badge}
                    onClick={() => setBadgeDetalhe(badge)}
                  />
                ))}
              </div>
            </div>
          );
        })}
      </section>

      {/* ── Próximas Conquistas ── */}
      {dados.progressos.length > 0 && (
        <section className="card p-5 space-y-4">
          <h2 className="text-base font-bold text-ink-900">Próximas Conquistas</h2>

          <div className="space-y-4">
            {dados.progressos.map(prog => (
              <ProgressoItem key={prog.badgeId} progresso={prog} />
            ))}
          </div>
        </section>
      )}

      {/* ── Modal de detalhe ── */}
      {badgeDetalhe && (
        <BadgeDetalheModal
          badge={badgeDetalhe}
          onFechar={() => setBadgeDetalhe(null)}
        />
      )}
    </div>
  );
}

// ─── BadgeCard ────────────────────────────────────────────────────────────────

function BadgeCard({ badge, onClick }: { badge: Badge; onClick: () => void }) {
  const conquistada = badge.conquistadaEm !== null;
  const r = RARIDADE_CONFIG[badge.raridade];

  return (
    <button
      onClick={onClick}
      className={[
        "card w-full p-4 text-left transition",
        "hover:shadow-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-400",
        conquistada
          ? `border-2 ${r.borda} ${r.fundo}`
          : "opacity-60 grayscale hover:opacity-80 hover:grayscale-0",
      ].join(" ")}
    >
      {/* Ícone de raridade */}
      <div className="mb-2 text-3xl">{conquistada ? r.emoji : "🔒"}</div>

      {/* Nome */}
      <p className={`text-sm font-bold ${conquistada ? "text-ink-900" : "text-ink-600"}`}>
        {badge.nome}
      </p>

      {/* Data ou dica */}
      {conquistada ? (
        <p className="mt-1 text-xs text-ink-500">
          Desbloqueada em {formatarData(badge.conquistadaEm!)}
        </p>
      ) : (
        <p className="mt-1 text-xs text-ink-400 italic">Ainda não conquistada</p>
      )}

      {/* Raridade */}
      <p className={`mt-2 text-xs font-semibold ${r.texto}`}>{r.label}</p>
    </button>
  );
}

// ─── ProgressoItem ────────────────────────────────────────────────────────────

function ProgressoItem({ progresso }: { progresso: ProgressoBadge }) {
  const pct = Math.min(100, Math.round(progresso.percentualConclusao));

  // cor da barra baseada no progresso
  const corBarra =
    pct >= 75 ? "bg-emerald-500" :
    pct >= 40 ? "bg-amber-400"   :
                "bg-brand-400";

  return (
    <div className="space-y-1.5">
      <div className="flex items-center justify-between text-sm">
        <span className="font-medium text-ink-800">{progresso.badgeNome}</span>
        <span className="text-xs text-ink-500">
          {progresso.valorAtual}/{progresso.metaTotal}{" "}
          {/* rótulo amigável baseado no nome */}
          {progresso.badgeNome.toLowerCase().includes("mês") ||
           progresso.badgeNome.toLowerCase().includes("ano") ||
           progresso.badgeNome.toLowerCase().includes("ativo")
            ? "meses"
            : "consultas"}
        </span>
      </div>
      <div className="h-2.5 w-full overflow-hidden rounded-full bg-ink-100">
        <div
          className={`h-full rounded-full transition-all duration-500 ${corBarra}`}
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  );
}

// ─── Modal de detalhe da badge ────────────────────────────────────────────────

function BadgeDetalheModal({ badge, onFechar }: { badge: Badge; onFechar: () => void }) {
  const conquistada = badge.conquistadaEm !== null;
  const r  = RARIDADE_CONFIG[badge.raridade];
  const c  = CATEGORIA_CONFIG[badge.categoria];

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      onClick={onFechar}
    >
      <div
        className={`w-full max-w-sm card p-6 ${conquistada ? `border-2 ${r.borda}` : ""}`}
        onClick={e => e.stopPropagation()}
      >
        {/* Ícone grande */}
        <div className="mb-4 text-center text-5xl">
          {conquistada ? r.emoji : "🔒"}
        </div>

        {/* Nome */}
        <h3 className="text-center text-lg font-bold text-ink-900">{badge.nome}</h3>

        {/* Categoria + Raridade */}
        <div className="mt-2 flex justify-center gap-2">
          <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ring-1 ${c.cor}`}>
            {c.label}
          </span>
          <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${r.texto} ${r.fundo} ring-1 ${r.borda}`}>
            {r.label}
          </span>
        </div>

        {/* Descrição */}
        <p className="mt-4 text-center text-sm text-ink-600 leading-relaxed">
          {badge.descricao}
        </p>

        {/* Data de conquista ou aviso */}
        <div className={`mt-4 rounded-xl px-4 py-3 text-center text-sm ${conquistada ? `${r.fundo} ${r.texto}` : "bg-ink-100 text-ink-500"}`}>
          {conquistada
            ? <>🎉 Conquistada em <strong>{formatarData(badge.conquistadaEm!)}</strong></>
            : "Continue usando o petCollar para desbloquear esta badge."}
        </div>

        <button onClick={onFechar} className="btn-primary mt-5">
          Fechar
        </button>
      </div>
    </div>
  );
}
