import { useMemo, useState, type ReactNode } from "react";
import {
  TODOS_CANAIS,
  TODOS_NIVEIS,
  type CanalContato,
  type ConfiguracaoProtocoloDTO,
  type NivelEscalonamento,
  type RequisicaoConfigurarProtocolo,
} from "../../tipos";
import { BannerErro } from "../compartilhados/Primitivos";
import { EditorCanaisHabilitados, type EstadoCanal } from "./EditorCanaisHabilitados";
import { EditorNiveisEscalonamento, type EstadoNivel } from "./EditorNiveisEscalonamento";
import { PreviewConfiguracao } from "./PreviewConfiguracao";

type Props = {
  inicial: ConfiguracaoProtocoloDTO | null;
  salvando: boolean;
  erro: string | null;
  onSalvar: (payload: RequisicaoConfigurarProtocolo) => void;
  onVerHistorico: () => void;
};

const PADRAO = {
  tempoLimiteEsperaMinutos: 15,
  intervaloEntreTentativasMinutos: 5,
  quantidadeMaximaTentativasPorCanal: 2,
  canaisHabilitados: ["TELEFONE", "SMS", "EMAIL"] as CanalContato[],
  niveisEscalonamento: TODOS_NIVEIS,
};

function montarCanais(habilitados: CanalContato[]): EstadoCanal[] {
  const habilitadosEmOrdem = habilitados.map((canal) => ({ canal, habilitado: true }));
  const restantes = TODOS_CANAIS.filter((c) => !habilitados.includes(c)).map((canal) => ({
    canal,
    habilitado: false,
  }));
  return [...habilitadosEmOrdem, ...restantes];
}

function montarNiveis(habilitados: NivelEscalonamento[]): EstadoNivel[] {
  return TODOS_NIVEIS.map((nivel) => ({ nivel, habilitado: habilitados.includes(nivel) }));
}

export function FormularioConfiguracao({ inicial, salvando, erro, onSalvar, onVerHistorico }: Props) {
  const base = inicial ?? PADRAO;

  const [tempo, setTempo] = useState(base.tempoLimiteEsperaMinutos);
  const [intervalo, setIntervalo] = useState(base.intervaloEntreTentativasMinutos);
  const [maxTentativas, setMaxTentativas] = useState(base.quantidadeMaximaTentativasPorCanal);
  const [canais, setCanais] = useState<EstadoCanal[]>(montarCanais(base.canaisHabilitados));
  const [niveis, setNiveis] = useState<EstadoNivel[]>(montarNiveis(base.niveisEscalonamento));

  function moverCanal(indice: number, direcao: "cima" | "baixo") {
    const destino = direcao === "cima" ? indice - 1 : indice + 1;
    if (destino < 0 || destino >= canais.length) return;
    const copia = [...canais];
    [copia[indice], copia[destino]] = [copia[destino], copia[indice]];
    setCanais(copia);
  }
  const alternarCanal = (canal: CanalContato) =>
    setCanais((atual) => atual.map((c) => (c.canal === canal ? { ...c, habilitado: !c.habilitado } : c)));
  const alternarNivel = (nivel: NivelEscalonamento) =>
    setNiveis((atual) => atual.map((n) => (n.nivel === nivel ? { ...n, habilitado: !n.habilitado } : n)));

  const canaisHabilitados = canais.filter((c) => c.habilitado).map((c) => c.canal);
  const niveisHabilitados = niveis.filter((n) => n.habilitado).map((n) => n.nivel);

  const erros = useMemo(() => {
    const e: string[] = [];
    if (tempo < 1 || tempo > 60) e.push("O tempo limite deve estar entre 1 e 60 minutos.");
    if (intervalo < 1 || intervalo > 30) e.push("O intervalo entre tentativas deve estar entre 1 e 30 minutos.");
    if (maxTentativas < 1 || maxTentativas > 5) e.push("A quantidade de tentativas por canal deve estar entre 1 e 5.");
    if (canaisHabilitados.length === 0) e.push("Habilite ao menos um canal de contato.");
    if (niveisHabilitados.length === 0) e.push("Habilite ao menos um nível de escalonamento.");
    return e;
  }, [tempo, intervalo, maxTentativas, canaisHabilitados.length, niveisHabilitados.length]);

  function resetar() {
    setTempo(base.tempoLimiteEsperaMinutos);
    setIntervalo(base.intervaloEntreTentativasMinutos);
    setMaxTentativas(base.quantidadeMaximaTentativasPorCanal);
    setCanais(montarCanais(base.canaisHabilitados));
    setNiveis(montarNiveis(base.niveisEscalonamento));
  }

  function salvar() {
    if (erros.length > 0) return;
    onSalvar({
      tempoLimiteEsperaMinutos: tempo,
      canaisHabilitados,
      intervaloEntreTentativasMinutos: intervalo,
      quantidadeMaximaTentativasPorCanal: maxTentativas,
      niveisEscalonamento: niveisHabilitados,
    });
  }

  return (
    <div className="grid grid-cols-1 gap-5 lg:grid-cols-3">
      <div className="space-y-5 lg:col-span-2">
        <Secao titulo="Parâmetros gerais">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <CampoNumero rotulo="Tempo limite (min)" valor={tempo} min={1} max={60} onChange={setTempo} />
            <CampoNumero rotulo="Intervalo (min)" valor={intervalo} min={1} max={30} onChange={setIntervalo} />
            <CampoNumero rotulo="Tentativas/canal" valor={maxTentativas} min={1} max={5} onChange={setMaxTentativas} />
          </div>
        </Secao>

        <Secao titulo="Canais habilitados" descricao="A ordem define a sequência de tentativas (RN 2).">
          <EditorCanaisHabilitados itens={canais} onMover={moverCanal} onAlternar={alternarCanal} />
        </Secao>

        <Secao
          titulo="Níveis de escalonamento"
          descricao="A ordem é canônica (prioridade crescente, RN 6); habilite os níveis aplicáveis."
        >
          <EditorNiveisEscalonamento itens={niveis} onAlternar={alternarNivel} />
        </Secao>

        {erro && <BannerErro mensagem={erro} />}
        {erros.length > 0 && (
          <ul className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
            {erros.map((e) => (
              <li key={e}>• {e}</li>
            ))}
          </ul>
        )}

        <div className="flex flex-wrap justify-end gap-2">
          <button onClick={onVerHistorico} className="btn-ghost mr-auto ring-1 ring-ink-300">
            Ver histórico
          </button>
          <button onClick={resetar} className="btn-ghost ring-1 ring-ink-300">
            Cancelar
          </button>
          <button
            onClick={salvar}
            disabled={salvando || erros.length > 0}
            className="btn-primary w-auto"
          >
            {salvando ? "Salvando…" : "Salvar nova versão"}
          </button>
        </div>
      </div>

      <aside className="card h-fit space-y-3 p-6">
        <h2 className="text-sm font-semibold text-ink-700">Pré-visualização</h2>
        <PreviewConfiguracao
          tempoLimiteEsperaMinutos={tempo}
          canaisHabilitados={canaisHabilitados}
          niveisEscalonamento={niveisHabilitados}
          intervaloEntreTentativasMinutos={intervalo}
          quantidadeMaximaTentativasPorCanal={maxTentativas}
        />
      </aside>
    </div>
  );
}

function Secao({
  titulo,
  descricao,
  children,
}: {
  titulo: string;
  descricao?: string;
  children: ReactNode;
}) {
  return (
    <section className="card p-6">
      <h2 className="text-sm font-semibold text-ink-700">{titulo}</h2>
      {descricao && <p className="mb-3 mt-0.5 text-xs text-ink-500">{descricao}</p>}
      <div className={descricao ? "" : "mt-3"}>{children}</div>
    </section>
  );
}

function CampoNumero({
  rotulo,
  valor,
  min,
  max,
  onChange,
}: {
  rotulo: string;
  valor: number;
  min: number;
  max: number;
  onChange: (v: number) => void;
}) {
  return (
    <label className="block">
      <span className="label">{rotulo}</span>
      <input
        type="number"
        className="input"
        min={min}
        max={max}
        value={valor}
        onChange={(e) => onChange(Number(e.target.value))}
      />
    </label>
  );
}
