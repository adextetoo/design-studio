"use client";

import { useRef, type ReactElement } from "react";
import type {
  DeliverableId, DeliverablePayload, PalettePayload, PayloadKind, PayloadOf, Swatch, Variant,
} from "@/lib/types";
import { readableOn } from "@/lib/color";
import { Kicker, Pill, RuledHead } from "@/components/ui";
import { LogoMark } from "./LogoMark";

export interface DeliverableViewProps {
  deliverableId: DeliverableId;
  payload: DeliverablePayload;
  /** Approved palette, so visual modules preview in the real brand colours. */
  palette: PalettePayload | null;
  typeStack: string | null;
  onPatch: (mutate: (v: Variant) => Variant) => void;
}

/**
 * How each payload kind is drawn on screen.
 *
 * Declared over every `PayloadKind` so a new kind cannot reach the export
 * without also reaching the studio — the compiler holds the pair together even
 * though the Markdown side lives in `lib/markdown` to stay free of React.
 */
const VIEWS: {
  [K in PayloadKind]: (props: { data: PayloadOf<K>; ctx: DeliverableViewProps }) => ReactElement | null;
} = {
  prose: ({ data }) => <ProseView data={data} />,
  statements: ({ data }) => <StatementsView data={data} />,
  palette: ({ data }) => <PaletteView data={data} />,
  typography: ({ data }) => <TypographyView data={data} />,
  logo: ({ data, ctx }) => <LogoView {...ctx} data={data} />,
  lookfeel: ({ data, ctx }) => <LookFeelView {...ctx} data={data} />,
  audience: ({ data }) => <AudienceView data={data} />,
  market: ({ data }) => <MarketView data={data} />,
  competition: ({ data }) => <CompetitionView data={data} />,
  differentiation: ({ data }) => <DifferentiationView data={data} />,
  offering: ({ data }) => <OfferingView data={data} />,
  packaging: ({ data, ctx }) => <PackagingView data={data} palette={ctx.palette} />,
  marketing: ({ data, ctx }) => <MarketingView data={data} palette={ctx.palette} />,
  social: ({ data, ctx }) => <SocialView data={data} palette={ctx.palette} />,
  website: ({ data, ctx }) => <WebsiteView data={data} palette={ctx.palette} />,
  expose: ({ data }) => <ExposeView data={data} />,
};

export function DeliverableView(props: DeliverableViewProps) {
  const View = VIEWS[props.payload.kind] as (p: {
    data: DeliverablePayload["data"];
    ctx: DeliverableViewProps;
  }) => ReactElement | null;
  return <View data={props.payload.data} ctx={props} />;
}

/* ---------------------------------------------------------------- */

function ProseView({ data }: { data: Extract<DeliverablePayload, { kind: "prose" }>["data"] }) {
  return (
    <article className="max-w-2xl">
      {data.heading ? <Kicker className="mb-4">{data.heading}</Kicker> : null}
      {data.paragraphs.map((p, i) => (
        <p
          key={i}
          className={`mb-4 font-display leading-[1.7] ${i === 0 ? "text-subhead text-ink" : "text-lead text-ink-soft"}`}
        >
          {p}
        </p>
      ))}
    </article>
  );
}

function StatementsView({ data }: { data: Extract<DeliverablePayload, { kind: "statements" }>["data"] }) {
  return (
    <div className="max-w-3xl">
      <p className="mb-6 max-w-2xl text-lead leading-relaxed text-ink-soft">{data.intro}</p>
      <div className="space-y-5">
        {data.items.map((item) => (
          <div key={item.label} className="grid gap-2 border-t border-line pt-4 sm:grid-cols-[9rem_1fr] sm:gap-6">
            <Kicker className="pt-0.5">{item.label}</Kicker>
            <div>
              <p className="font-display text-subhead leading-snug">{item.statement}</p>
              <p className="mt-2 text-lead leading-relaxed text-ink-soft">{item.detail}</p>
              {item.sayThis || item.counterExample ? (
                <div className="mt-3 space-y-1.5 text-body">
                  {item.sayThis ? (
                    <p className="flex gap-2">
                      <span className="shrink-0 font-medium text-go">Say</span>
                      <span className="text-ink">“{item.sayThis}”</span>
                    </p>
                  ) : null}
                  {item.counterExample ? (
                    <p className="flex gap-2">
                      <span className="shrink-0 font-medium text-signal">Not</span>
                      <span className="text-ink-faint line-through decoration-signal/40">
                        “{item.counterExample}”
                      </span>
                    </p>
                  ) : null}
                </div>
              ) : null}
              {item.bannedWords?.length ? (
                <div className="mt-3 flex flex-wrap gap-1.5">
                  {item.bannedWords.map((w) => (
                    <span
                      key={w}
                      className="rounded bg-signal-soft px-2 py-0.5 font-mono text-body text-signal line-through"
                    >
                      {w}
                    </span>
                  ))}
                </div>
              ) : null}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

/* ---------------------------------------------------------------- */

function PaletteView({ data }: { data: PalettePayload }) {
  return (
    <div>
      <div className="mb-6 max-w-2xl">
        <h3 className="text-title tracking-[-0.01em]">{data.name}</h3>
        <p className="mt-1.5 text-lead leading-relaxed text-ink-soft">{data.rationale}</p>
      </div>

      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {data.swatches.map((s) => <SwatchCard key={`${s.name}-${s.hex}`} swatch={s} />)}
      </div>

      <div className="mt-6 rounded-lg border border-line bg-sunken/50 p-4">
        <Kicker className="mb-1.5">Pairing rule</Kicker>
        <p className="text-lead leading-relaxed">{data.pairingRule}</p>
      </div>

      <div className="mt-4">
        <RuledHead>Contrast, measured</RuledHead>
        <p className="mb-3 mt-2 text-body text-ink-soft">
          WCAG 2.1 ratios against white and against ink. Anything under 4.5 is a decorative colour, not a text colour —
          the guidelines should say so before someone finds out in a support ticket.
        </p>
        <div className="overflow-x-auto">
          <table className="w-full min-w-[34rem] text-left text-body">
            <thead>
              <tr className="border-b border-line text-ink-faint">
                <th className="py-2 font-medium">Colour</th>
                <th className="py-2 font-medium">On white</th>
                <th className="py-2 font-medium">On ink</th>
                <th className="py-2 font-medium">Safe for body text</th>
              </tr>
            </thead>
            <tbody>
              {data.swatches.map((s) => (
                <tr key={`c-${s.hex}`} className="border-b border-line-soft">
                  <td className="py-2">
                    <span className="inline-flex items-center gap-2">
                      <span className="size-3.5 rounded-sm border border-line" style={{ background: s.hex }} />
                      {s.name}
                    </span>
                  </td>
                  <td className="py-2 tabular-nums">
                    <span className={s.contrastOnWhite >= 4.5 ? "" : "text-ink-faint"}>{s.contrastOnWhite}:1</span>
                    <span className="ml-1.5 text-micro text-ink-faint">{gradeOf(s.contrastOnWhite)}</span>
                  </td>
                  <td className="py-2 tabular-nums">
                    <span className={s.contrastOnInk >= 4.5 ? "" : "text-ink-faint"}>{s.contrastOnInk}:1</span>
                    <span className="ml-1.5 text-micro text-ink-faint">{gradeOf(s.contrastOnInk)}</span>
                  </td>
                  <td className="py-2">
                    <Pill tone={bodyVerdict(s).tone}>{bodyVerdict(s).label}</Pill>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

/** WCAG 2.1 grade for normal-size body text at a given ratio. */
function gradeOf(ratio: number): string {
  if (ratio >= 7) return "AAA";
  if (ratio >= 4.5) return "AA";
  return "fail";
}

/**
 * Which ground a colour can carry body text on.
 *
 * A single "AAA" badge is the trap here: lime clears AAA against ink and fails
 * badly against white, and a guideline that says only "AAA" invites someone to
 * set body copy in lime on a white page.
 */
function bodyVerdict(s: Swatch): { label: string; tone: "go" | "neutral" | "signal" } {
  const onWhite = s.contrastOnWhite >= 4.5;
  const onInk = s.contrastOnInk >= 4.5;
  if (onWhite && onInk) return { label: "Either ground", tone: "go" };
  if (onWhite) return { label: "On white only", tone: "neutral" };
  if (onInk) return { label: "On ink only", tone: "neutral" };
  return { label: "Never — fills and large type only", tone: "signal" };
}

function SwatchCard({ swatch }: { swatch: Swatch }) {
  return (
    <div className="overflow-hidden rounded-lg border border-line">
      <div
        className="flex h-28 items-end p-3"
        style={{ background: swatch.hex, color: readableOn(swatch.hex) }}
      >
        <span className="text-body font-medium">{swatch.name}</span>
      </div>
      <div className="space-y-1 p-3 font-data text-micro text-ink-soft">
        <p className="flex justify-between"><span className="text-ink-faint">HEX</span><span>{swatch.hex}</span></p>
        <p className="flex justify-between"><span className="text-ink-faint">RGB</span><span>{swatch.rgb.join(" ")}</span></p>
        <p className="flex justify-between"><span className="text-ink-faint">CMYK</span><span>{swatch.cmyk.join(" ")}</span></p>
      </div>
      <p className="border-t border-line-soft px-3 py-2 text-body leading-relaxed text-ink-soft">{swatch.usage}</p>
    </div>
  );
}

/* ---------------------------------------------------------------- */

function TypographyView({ data }: { data: Extract<DeliverablePayload, { kind: "typography" }>["data"] }) {
  return (
    <div>
      <div className="mb-6 max-w-2xl">
        <Kicker className="mb-1.5">{data.className}</Kicker>
        <p className="text-lead leading-relaxed text-ink-soft">{data.classNote}</p>
      </div>

      <div className="mb-6 grid gap-3 sm:grid-cols-2">
        {[data.primary, data.secondary].map((face, i) => (
          <div key={face.name} className="rounded-lg border border-line p-4">
            <Kicker className="mb-2">{i === 0 ? "Primary" : "Secondary"}</Kicker>
            <p className="text-hero leading-none tracking-[-0.02em]" style={{ fontFamily: face.stack }}>
              {face.name}
            </p>
            <p className="mt-3 text-body text-ink-soft">{face.weights.join(" · ")}</p>
            <p
              className="mt-3 border-t border-line-soft pt-3 text-body leading-relaxed"
              style={{ fontFamily: face.stack }}
            >
              ABCDEFGHIJKLMNOPQRSTUVWXYZ<br />
              abcdefghijklmnopqrstuvwxyz<br />
              0123456789 ?! () [] @ £ &amp;
            </p>
          </div>
        ))}
      </div>

      <RuledHead>Hierarchy · {data.scaleRatio}</RuledHead>
      <div className="mt-3 overflow-x-auto">
        <table className="w-full min-w-[38rem] text-left text-body">
          <thead>
            <tr className="border-b border-line text-ink-faint">
              <th className="py-2 pr-4 font-medium">Level</th>
              <th className="py-2 pr-4 font-medium">Weight</th>
              <th className="py-2 pr-4 font-medium">Size</th>
              <th className="py-2 pr-4 font-medium">Leading</th>
              <th className="py-2 pr-4 font-medium">Tracking</th>
              <th className="py-2 font-medium">Use</th>
            </tr>
          </thead>
          <tbody>
            {data.levels.map((l) => (
              <tr key={l.level} className="border-b border-line-soft align-top">
                <td className="py-2.5 pr-4 font-medium">{l.level}</td>
                <td className="py-2.5 pr-4 text-ink-soft">{l.weight}</td>
                <td className="py-2.5 pr-4 tabular-nums text-ink-soft">{l.size}</td>
                <td className="py-2.5 pr-4 tabular-nums text-ink-soft">{l.lineHeight}</td>
                <td className="py-2.5 pr-4 tabular-nums text-ink-soft">{l.tracking}</td>
                <td className="min-w-[13rem] py-2.5 text-ink-soft">{l.use}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="mt-6 space-y-3 rounded-lg border border-line p-5">
        <Kicker>Specimen</Kicker>
        {data.levels.map((l) => (
          <p
            key={`spec-${l.level}`}
            className="truncate"
            style={{
              fontFamily: l.level === "Label" || l.level === "Caption" ? data.secondary.stack : data.primary.stack,
              fontSize: l.size,
              lineHeight: l.lineHeight,
              letterSpacing: l.tracking,
              fontWeight: /Bold|SemiBold|700|600/.test(l.weight) ? 700 : /Medium|500/.test(l.weight) ? 500 : 400,
            }}
          >
            {l.level} — the quick brown fox
          </p>
        ))}
      </div>

      <div className="mt-6">
        <RuledHead>Rules</RuledHead>
        <ul className="mt-2 space-y-2 text-lead leading-relaxed text-ink-soft">
          {data.rules.map((r, i) => (
            <li key={i} className="flex gap-2.5">
              <span className="mt-2 size-1 shrink-0 rounded-full bg-ink-faint" aria-hidden />
              {r}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

/* ---------------------------------------------------------------- */

function LogoView({
  payload, palette, onPatch,
}: DeliverableViewProps & { data: Extract<DeliverablePayload, { kind: "logo" }>["data"] }) {
  const fileRef = useRef<HTMLInputElement>(null);
  if (payload.kind !== "logo") return null;
  const data = payload.data;
  const brandColor = palette?.swatches.find((s) => s.role === "primary")?.hex ?? "#17150F";

  const setChosen = (id: string) =>
    onPatch((v) => (v.payload.kind === "logo"
      ? { ...v, payload: { ...v.payload, data: { ...v.payload.data, chosenRouteId: id } } }
      : v));

  const onUpload = (file: File | undefined) => {
    if (!file) return;
    if (file.size > 2 * 1024 * 1024) return;
    const reader = new FileReader();
    reader.onload = () => {
      const result = typeof reader.result === "string" ? reader.result : undefined;
      if (!result) return;
      onPatch((v) => (v.payload.kind === "logo"
        ? { ...v, payload: { ...v.payload, data: { ...v.payload.data, uploadedMark: result, uploadedName: file.name } } }
        : v));
    };
    reader.readAsDataURL(file);
  };

  const clearUpload = () =>
    onPatch((v) => (v.payload.kind === "logo"
      ? { ...v, payload: { ...v.payload, data: { ...v.payload.data, uploadedMark: undefined, uploadedName: undefined } } }
      : v));

  return (
    <div>
      <RuledHead>Client artwork</RuledHead>
      <div className="mt-3 mb-8">
        {data.uploadedMark ? (
          <div className="flex flex-wrap items-center gap-5 rounded-lg border border-line p-4">
            <div className="grid size-28 place-items-center rounded-md bg-sunken p-3">
              {/* Client artwork, loaded locally as a data URL — never uploaded anywhere. */}
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img src={data.uploadedMark} alt="Uploaded brand mark" className="max-h-full max-w-full object-contain" />
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-lead font-medium">{data.uploadedName}</p>
              <p className="mt-1 max-w-md text-body leading-relaxed text-ink-soft">
                Working from the client&rsquo;s existing mark. The constructed routes below stay available as a
                comparison — useful when the answer is &ldquo;keep it, but fix it&rdquo;.
              </p>
              <button
                type="button"
                onClick={clearUpload}
                className="mt-2 text-body text-signal underline underline-offset-2"
              >
                Remove
              </button>
            </div>
          </div>
        ) : (
          <div className="rounded-lg border border-dashed border-line p-6 text-center">
            <p className="text-lead font-medium">Upload the existing mark</p>
            <p className="mx-auto mt-1 max-w-md text-body leading-relaxed text-ink-soft">
              PNG or SVG, up to 2 MB. It stays in this browser — nothing is sent anywhere.
            </p>
            <input
              ref={fileRef}
              type="file"
              accept="image/png,image/svg+xml,image/jpeg,image/webp"
              className="sr-only"
              onChange={(e) => onUpload(e.target.files?.[0])}
            />
            <button
              type="button"
              onClick={() => fileRef.current?.click()}
              className="mt-3 rounded-full border border-line bg-panel px-4 py-1.5 text-body font-medium hover:bg-sunken"
            >
              Choose a file
            </button>
          </div>
        )}
      </div>

      <RuledHead>Three routes</RuledHead>
      <p className="mb-4 mt-2 max-w-2xl text-body leading-relaxed text-ink-soft">
        Presented the way routes get presented: three, not seven. Seven means the studio has not made up its mind and is
        asking the client to do it instead.
      </p>

      <div className="grid gap-3 lg:grid-cols-3">
        {data.routes.map((route) => {
          const chosen = route.id === data.chosenRouteId;
          return (
            <button
              key={route.id}
              type="button"
              onClick={() => setChosen(route.id)}
              aria-pressed={chosen}
              className={`rounded-lg border p-4 text-left transition-[border-color,box-shadow,transform] duration-150 ease-[var(--ease-out-quint)] active:scale-[0.995] ${
                chosen ? "border-ink shadow-[0_0_0_1px_var(--color-ink)]" : "border-line hover:border-ink/40"
              }`}
            >
              <div className="mb-4 grid h-32 place-items-center rounded-md bg-sunken">
                <LogoMark
                  shape={route.markShape}
                  size={72}
                  color={brandColor}
                  letter={(data.uploadedName ?? "A").charAt(0).toUpperCase()}
                />
              </div>
              <div className="flex items-center justify-between gap-2">
                <p className="text-lead font-medium">{route.name}</p>
                {chosen ? <Pill tone="go" dot>Chosen</Pill> : null}
              </div>
              <p className="mt-2 text-body leading-relaxed text-ink-soft">{route.construction}</p>
            </button>
          );
        })}
      </div>

      {(() => {
        const chosen = data.routes.find((r) => r.id === data.chosenRouteId) ?? data.routes[0];
        if (!chosen) return null;
        return (
          <div className="mt-6 space-y-5">
            <div className="rounded-lg border border-line p-5">
              <Kicker className="mb-3">Why this one</Kicker>
              <p className="max-w-2xl text-lead leading-relaxed">{chosen.rationale}</p>
            </div>

            <div className="grid gap-3 sm:grid-cols-3">
              {[
                { label: "Clearspace", value: chosen.clearspace },
                { label: "Minimum size", value: chosen.minSize },
                { label: "Variants supplied", value: chosen.variants.join(", ") },
              ].map((row) => (
                <div key={row.label} className="rounded-lg border border-line p-4">
                  <Kicker className="mb-1.5">{row.label}</Kicker>
                  <p className="text-body leading-relaxed text-ink-soft">{row.value}</p>
                </div>
              ))}
            </div>

            <div>
              <RuledHead>Small sizes and reversed</RuledHead>
              <p className="mb-3 mt-2 text-body text-ink-soft">
                Where marks actually fail. Judge it here, not on the presentation slide.
              </p>
              <div className="flex flex-wrap items-end gap-6 rounded-lg border border-line p-5">
                {[16, 24, 32, 48].map((size) => (
                  <div key={size} className="text-center">
                    <LogoMark shape={chosen.markShape} size={size} color={brandColor} />
                    <p className="mt-1.5 text-micro tabular-nums text-ink-faint">{size}px</p>
                  </div>
                ))}
                <div className="grid place-items-center rounded-md bg-ink p-3">
                  <LogoMark shape={chosen.markShape} size={40} color="#FFFFFF" />
                </div>
                <div className="grid place-items-center rounded-md p-3" style={{ background: brandColor }}>
                  <LogoMark shape={chosen.markShape} size={40} color="#FFFFFF" />
                </div>
              </div>
            </div>

            <p className="max-w-2xl text-lead leading-relaxed text-ink-soft">{data.wordmarkNote}</p>
          </div>
        );
      })()}
    </div>
  );
}

/* ---------------------------------------------------------------- */

function LookFeelView({
  payload, onPatch,
}: DeliverableViewProps & { data: Extract<DeliverablePayload, { kind: "lookfeel" }>["data"] }) {
  if (payload.kind !== "lookfeel") return null;
  const data = payload.data;

  const choose = (id: string) =>
    onPatch((v) => (v.payload.kind === "lookfeel"
      ? { ...v, payload: { ...v.payload, data: { ...v.payload.data, chosenId: id } } }
      : v));

  return (
    <div>
      <p className="mb-5 max-w-2xl text-lead leading-relaxed text-ink-soft">
        Three directions, each with three adjectives. The adjectives are the contract — everything downstream gets
        judged against them, so they are worth arguing about now rather than at artwork stage.
      </p>

      <div className="grid gap-3 lg:grid-cols-3">
        {data.directions.map((d, i) => {
          const chosen = d.id === data.chosenId;
          return (
            <button
              key={d.id}
              type="button"
              onClick={() => choose(d.id)}
              aria-pressed={chosen}
              className={`rounded-lg border p-4 text-left transition-[border-color,box-shadow,transform] duration-150 ease-[var(--ease-out-quint)] active:scale-[0.995] ${
                chosen ? "border-ink shadow-[0_0_0_1px_var(--color-ink)]" : "border-line hover:border-ink/40"
              }`}
            >
              <div className="mb-3 flex items-center justify-between gap-2">
                <Kicker>Direction {i + 1}</Kicker>
                {chosen ? <Pill tone="go" dot>Chosen</Pill> : null}
              </div>
              <p className="text-subhead font-semibold tracking-[-0.01em]">{d.name}</p>
              <ul className="mt-2 space-y-0.5">
                {d.adjectives.map((adj) => (
                  <li key={adj} className="flex items-center gap-2 text-body text-ink-soft">
                    <span className="size-1 rounded-full bg-ink-faint" aria-hidden />
                    {adj}
                  </li>
                ))}
              </ul>
              <p className="mt-3 border-t border-line-soft pt-3 text-body leading-relaxed text-ink-soft">
                {d.description}
              </p>
            </button>
          );
        })}
      </div>

      {(() => {
        const chosen = data.directions.find((d) => d.id === data.chosenId) ?? data.directions[0];
        return (
          <div className="mt-6 space-y-3">
            {[
              { label: "Surfaces", value: chosen.surfaces.join(" · ") },
              { label: "Photography", value: chosen.photography },
              { label: "Motion", value: chosen.motion },
              { label: "Grid", value: data.gridNote },
              { label: "Margins", value: data.marginRule },
            ].map((row) => (
              <div key={row.label} className="grid gap-1 border-t border-line pt-3 sm:grid-cols-[8rem_1fr] sm:gap-6">
                <Kicker className="pt-0.5">{row.label}</Kicker>
                <p className="text-lead leading-relaxed text-ink-soft">{row.value}</p>
              </div>
            ))}
          </div>
        );
      })()}
    </div>
  );
}

/* ---------------------------------------------------------------- */

function AudienceView({ data }: { data: Extract<DeliverablePayload, { kind: "audience" }>["data"] }) {
  return (
    <div>
      <p className="mb-6 max-w-2xl text-lead leading-relaxed text-ink-soft">{data.read}</p>
      <div className="grid gap-4 lg:grid-cols-2">
        {data.personas.map((p) => (
          <article key={p.name} className="rounded-lg border border-line p-5">
            <header className="mb-4 border-b border-line-soft pb-3">
              <h3 className="text-title tracking-[-0.01em]">
                {p.name} <span className="font-ui text-lead font-normal text-ink-faint">| {p.archetypeLabel}</span>
              </h3>
              <ol className="mt-3 space-y-0.5 text-lead">
                {p.traits.map((t, i) => (
                  <li key={t} className="flex gap-2">
                    <span className="w-4 shrink-0 tabular-nums text-ink-faint">{i + 1}.</span>
                    {t}
                  </li>
                ))}
              </ol>
            </header>

            <dl className="mb-4 space-y-1.5 text-body">
              <div className="flex gap-2"><dt className="w-20 shrink-0 text-ink-faint">Age</dt><dd>{p.age}</dd></div>
              <div className="flex gap-2"><dt className="w-20 shrink-0 text-ink-faint">Location</dt><dd>{p.location}</dd></div>
              <div className="flex gap-2"><dt className="w-20 shrink-0 text-ink-faint">Also buys</dt><dd>{p.alsoBuys.join(", ")}</dd></div>
            </dl>

            <div className="grid gap-4 sm:grid-cols-2">
              <PersonaList title="Motivations" items={p.motivations} />
              <PersonaList title="Challenges" items={p.challenges} />
              <PersonaList title="Why this brand" items={p.whyThisBrand} />
              <PersonaList title="Buying behaviour" items={p.buyingBehaviour} />
            </div>
            <div className="mt-4 border-t border-line-soft pt-3">
              <PersonaList title="Where they are" items={p.socialBehaviour} />
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}

function PersonaList({ title, items }: { title: string; items: string[] }) {
  return (
    <div>
      <Kicker className="mb-1.5">{title}</Kicker>
      <ul className="space-y-1 text-body leading-relaxed text-ink-soft">
        {items.map((item, i) => (
          <li key={i} className="flex gap-2">
            <span className="mt-1.5 size-1 shrink-0 rounded-full bg-ink-faint" aria-hidden />
            {item}
          </li>
        ))}
      </ul>
    </div>
  );
}

/* ---------------------------------------------------------------- */

function MarketView({ data }: { data: Extract<DeliverablePayload, { kind: "market" }>["data"] }) {
  return (
    <div>
      <h3 className="max-w-2xl font-display text-display leading-snug tracking-[-0.02em]">{data.headline}</h3>
      <p className="mt-3 max-w-2xl text-lead leading-relaxed text-ink-soft">{data.summary}</p>

      <div className="mt-6 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        {data.figures.map((f) => (
          <div key={f.label} className="rounded-lg border border-line p-4">
            <p className="font-display text-display leading-none tracking-[-0.02em] tabular-nums">{f.value}</p>
            <p className="mt-2 text-body font-medium">{f.label}</p>
            <p className="mt-1 text-body leading-relaxed text-ink-faint">{f.note}</p>
          </div>
        ))}
      </div>

      <div className="mt-6">
        <RuledHead>Market segmentation</RuledHead>
        <div className="mt-3 space-y-3">
          {data.segments.map((s) => (
            <div key={s.name}>
              <div className="mb-1 flex items-baseline justify-between gap-3 text-body">
                <span className="font-medium">{s.name}</span>
                <span className="tabular-nums text-ink-faint">{s.share}%</span>
              </div>
              <div className="h-2 overflow-hidden rounded-full bg-sunken">
                <div className="h-full rounded-full bg-ink" style={{ width: `${s.share}%` }} />
              </div>
              <p className="mt-1 text-body leading-relaxed text-ink-soft">{s.note}</p>
            </div>
          ))}
        </div>
      </div>

      <div className="mt-6 grid gap-5 sm:grid-cols-2">
        <div>
          <RuledHead>What is shifting</RuledHead>
          <ul className="mt-2 space-y-2 text-body leading-relaxed text-ink-soft">
            {data.shifts.map((s, i) => (
              <li key={i} className="flex gap-2"><span className="mt-1.5 size-1 shrink-0 rounded-full bg-ink-faint" aria-hidden />{s}</li>
            ))}
          </ul>
        </div>
        <div>
          <RuledHead>Sources</RuledHead>
          <ul className="mt-2 space-y-2 text-body leading-relaxed text-ink-soft">
            {data.sources.map((s, i) => (
              <li key={i} className="flex gap-2"><span className="mt-1.5 size-1 shrink-0 rounded-full bg-ink-faint" aria-hidden />{s}</li>
            ))}
          </ul>
          <p className="mt-3 rounded-md bg-signal-soft px-3 py-2 text-body leading-relaxed text-signal">
            These figures are studio placeholders sized to be plausible for the sector. Replace them with the
            client&rsquo;s own analyst data before this leaves the building — a made-up number in a board pack is worse
            than no number.
          </p>
        </div>
      </div>
    </div>
  );
}

/* ---------------------------------------------------------------- */

function CompetitionView({ data }: { data: Extract<DeliverablePayload, { kind: "competition" }>["data"] }) {
  return (
    <div>
      <p className="mb-6 max-w-2xl text-lead leading-relaxed">{data.read}</p>

      <div className="grid gap-5 lg:grid-cols-[1fr_20rem]">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[40rem] text-left text-body">
            <thead>
              <tr className="border-b border-line text-ink-faint">
                <th className="py-2 pr-4 font-medium">Who</th>
                <th className="py-2 pr-4 font-medium">Position</th>
                <th className="py-2 pr-4 font-medium">Does well</th>
                <th className="py-2 pr-4 font-medium">Leaves open</th>
                <th className="py-2 font-medium">Price</th>
              </tr>
            </thead>
            <tbody>
              {data.rows.map((r) => (
                <tr key={r.name} className="border-b border-line-soft align-top">
                  <td className="py-3 pr-4 font-medium">{r.name}</td>
                  <td className="py-3 pr-4 text-ink-soft">{r.position}</td>
                  <td className="py-3 pr-4 text-ink-soft">{r.doesWell}</td>
                  <td className="py-3 pr-4 text-ink-soft">{r.leavesOpen}</td>
                  <td className="py-3 capitalize text-ink-soft">{r.priceBand}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div>
          <Kicker className="mb-2">Positioning map</Kicker>
          <div className="relative aspect-square rounded-lg border border-line bg-panel">
            <span className="absolute inset-x-0 top-1/2 h-px bg-line" aria-hidden />
            <span className="absolute inset-y-0 left-1/2 w-px bg-line" aria-hidden />
            <span className="absolute left-2 top-1/2 -translate-y-1/2 text-micro text-ink-faint">{data.axes.x[0]}</span>
            <span className="absolute right-2 top-1/2 -translate-y-1/2 text-micro text-ink-faint">{data.axes.x[1]}</span>
            <span className="absolute left-1/2 top-2 -translate-x-1/2 text-micro text-ink-faint">{data.axes.y[1]}</span>
            <span className="absolute bottom-2 left-1/2 -translate-x-1/2 text-micro text-ink-faint">{data.axes.y[0]}</span>
            {data.plot.map((p) => (
              <span
                key={p.name}
                className="absolute -translate-x-1/2 -translate-y-1/2"
                style={{ left: `${p.x * 100}%`, top: `${(1 - p.y) * 100}%` }}
              >
                <span
                  className={`block size-2.5 rounded-full ${p.isUs ? "bg-signal ring-4 ring-signal/20" : "bg-ink-faint"}`}
                  aria-hidden
                />
                <span
                  className={`absolute left-4 top-1/2 -translate-y-1/2 whitespace-nowrap text-micro ${
                    p.isUs ? "font-semibold text-signal" : "text-ink-soft"
                  }`}
                >
                  {p.name}
                </span>
              </span>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

/* ---------------------------------------------------------------- */

function DifferentiationView({ data }: { data: Extract<DeliverablePayload, { kind: "differentiation" }>["data"] }) {
  return (
    <div className="max-w-3xl">
      <p className="font-display text-display leading-snug tracking-[-0.02em]">{data.claim}</p>

      <div className="mt-6">
        <RuledHead>Proof</RuledHead>
        <div className="mt-3 space-y-4">
          {data.proofs.map((p, i) => (
            <div key={i} className="rounded-lg border border-line p-4">
              <p className="text-lead font-medium">{p.proof}</p>
              <p className="mt-1.5 text-body leading-relaxed text-ink-soft">{p.evidence}</p>
            </div>
          ))}
        </div>
      </div>

      <div className="mt-6 rounded-lg border border-line bg-sunken/50 p-5">
        <Kicker className="mb-1.5">Only this company can say it because</Kicker>
        <p className="text-lead leading-relaxed">{data.onlyWeCan}</p>
      </div>

      <div className="mt-6">
        <RuledHead>Not for</RuledHead>
        <p className="mb-3 mt-2 text-body text-ink-soft">
          A position that excludes nobody positions nobody.
        </p>
        <ul className="space-y-2 text-lead text-ink-soft">
          {data.notFor.map((n, i) => (
            <li key={i} className="flex gap-2.5">
              <span className="mt-2 size-1 shrink-0 rounded-full bg-signal" aria-hidden />
              {n}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

/* ---------------------------------------------------------------- */

function OfferingView({ data }: { data: Extract<DeliverablePayload, { kind: "offering" }>["data"] }) {
  return (
    <div>
      <p className="mb-6 max-w-2xl font-display text-title leading-snug">{data.line}</p>
      <div className="grid gap-3 lg:grid-cols-3">
        {data.tiers.map((t, i) => (
          <div
            key={t.name}
            className={`rounded-lg border p-5 ${i === 1 ? "border-ink" : "border-line"}`}
          >
            {i === 1 ? <Pill tone="neutral">Most take this one</Pill> : null}
            <p className="mt-2 text-lead font-semibold">{t.name}</p>
            <p className="mt-1 font-display text-title tracking-[-0.01em] tabular-nums">{t.price}</p>
            <p className="mt-2 text-body italic leading-relaxed text-ink-soft">{t.forWho}</p>
            <ul className="mt-4 space-y-1.5 border-t border-line-soft pt-3 text-body leading-relaxed">
              {t.includes.map((inc) => (
                <li key={inc} className="flex gap-2">
                  <span className="mt-0.5 shrink-0 text-go" aria-hidden>✓</span>
                  {inc}
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
      <div className="mt-6">
        <RuledHead>Where the edges are</RuledHead>
        <ul className="mt-2 space-y-2 text-lead leading-relaxed text-ink-soft">
          {data.boundaries.map((b, i) => (
            <li key={i} className="flex gap-2.5">
              <span className="mt-2 size-1 shrink-0 rounded-full bg-signal" aria-hidden />
              {b}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

/* ---------------------------------------------------------------- */

function PackagingView({
  data, palette,
}: { data: Extract<DeliverablePayload, { kind: "packaging" }>["data"]; palette: PalettePayload | null }) {
  const primary = palette?.swatches.find((s) => s.role === "primary")?.hex ?? "#17150F";
  const paper = palette?.swatches.find((s) => s.role === "surface")?.hex ?? "#F6F5F3";

  return (
    <div>
      <div className="mb-6 grid gap-3 sm:grid-cols-2">
        <div className="rounded-lg border border-line p-4">
          <Kicker className="mb-1.5">Substrate</Kicker>
          <p className="text-lead leading-relaxed">{data.substrate}</p>
        </div>
        <div className="rounded-lg border border-line p-4">
          <Kicker className="mb-1.5">Finish</Kicker>
          <p className="text-lead leading-relaxed">{data.finish}</p>
        </div>
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        {data.items.map((item) => (
          <div key={item.name} className="overflow-hidden rounded-lg border border-line">
            <div
              className="flex h-40 flex-col justify-between p-5"
              style={{ background: primary, color: readableOn(primary) }}
            >
              <span className="text-micro uppercase tracking-[0.16em] opacity-70">{item.format}</span>
              {item.copy ? (
                <div>
                  <p className="text-subhead font-semibold leading-tight tracking-[-0.02em]">{item.copy.headline}</p>
                  {item.copy.sub ? <p className="mt-1 max-w-[24ch] text-body opacity-80">{item.copy.sub}</p> : null}
                </div>
              ) : (
                <span
                  className="self-start rounded px-2 py-1 text-micro"
                  style={{ background: paper, color: primary }}
                >
                  unprinted outer
                </span>
              )}
            </div>
            <div className="p-4">
              <p className="text-lead font-medium">{item.name}</p>
              <ul className="mt-2 space-y-1.5 text-body leading-relaxed text-ink-soft">
                {item.spec.map((s, i) => (
                  <li key={i} className="flex gap-2">
                    <span className="mt-1.5 size-1 shrink-0 rounded-full bg-ink-faint" aria-hidden />
                    {s}
                  </li>
                ))}
              </ul>
            </div>
          </div>
        ))}
      </div>

      <div className="mt-6 rounded-lg border border-line bg-sunken/50 p-5">
        <Kicker className="mb-1.5">Unboxing</Kicker>
        <p className="max-w-2xl text-lead leading-relaxed">{data.unboxingNote}</p>
      </div>
    </div>
  );
}

/* ---------------------------------------------------------------- */

function MarketingView({
  data, palette,
}: { data: Extract<DeliverablePayload, { kind: "marketing" }>["data"]; palette: PalettePayload | null }) {
  const primary = palette?.swatches.find((s) => s.role === "primary")?.hex ?? "#17150F";
  const support = palette?.swatches.find((s) => s.role === "support")?.hex ?? "#EFEDEA";

  return (
    <div className="space-y-5">
      {data.layouts.map((l, i) => (
        <div key={l.name} className="overflow-hidden rounded-lg border border-line">
          <div className="grid md:grid-cols-[1.3fr_1fr]">
            <div
              className="flex min-h-[13rem] flex-col justify-between p-6"
              style={{
                background: i % 2 === 0 ? primary : support,
                color: readableOn(i % 2 === 0 ? primary : support),
              }}
            >
              <span className="text-micro uppercase tracking-[0.16em] opacity-70">{l.name}</span>
              <div>
                <p className="max-w-[18ch] text-[clamp(20px,3vw,32px)] font-semibold leading-[1.05] tracking-[-0.03em]">
                  {l.headline}
                </p>
                {l.sub ? <p className="mt-2 max-w-[46ch] text-body leading-relaxed opacity-80">{l.sub}</p> : null}
              </div>
              <span className="text-micro opacity-60">◆</span>
            </div>
            <div className="border-t border-line p-5 md:border-l md:border-t-0">
              <Kicker className="mb-1.5">{l.ratio}</Kicker>
              <p className="text-body leading-relaxed text-ink-soft">{l.grid}</p>
              <ul className="mt-3 space-y-1.5 border-t border-line-soft pt-3 text-body leading-relaxed text-ink-soft">
                {l.hierarchy.map((h, k) => (
                  <li key={k} className="flex gap-2">
                    <span className="w-4 shrink-0 tabular-nums text-ink-faint">{k + 1}.</span>
                    {h}
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      ))}

      <div>
        <RuledHead>Rules that apply to every format</RuledHead>
        <ul className="mt-2 space-y-2 text-lead leading-relaxed text-ink-soft">
          {data.rules.map((r, i) => (
            <li key={i} className="flex gap-2.5">
              <span className="mt-2 size-1 shrink-0 rounded-full bg-ink-faint" aria-hidden />
              {r}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

/* ---------------------------------------------------------------- */

function SocialView({
  data, palette,
}: { data: Extract<DeliverablePayload, { kind: "social" }>["data"]; palette: PalettePayload | null }) {
  const primary = palette?.swatches.find((s) => s.role === "primary")?.hex ?? "#17150F";
  const support = palette?.swatches.find((s) => s.role === "support")?.hex ?? "#EFEDEA";
  const ink = palette?.swatches.find((s) => s.role === "ink")?.hex ?? "#17150F";
  const fills = [primary, ink, support];

  return (
    <div>
      <div className="mb-6 overflow-hidden rounded-lg border border-line">
        <div className="h-20" style={{ background: primary }} />
        <div className="-mt-7 px-5 pb-5">
          <span
            className="grid size-14 place-items-center rounded-xl border-4 border-panel text-subhead font-bold"
            style={{ background: ink, color: readableOn(ink) }}
            aria-hidden
          >
            ◆
          </span>
          <p className="mt-2 text-lead font-semibold">{data.handle}</p>
          <p className="mt-1 max-w-md text-body leading-relaxed text-ink-soft">{data.bio}</p>
        </div>
      </div>

      <RuledHead>Content pillars</RuledHead>
      <div className="mt-3 space-y-3">
        {data.pillars.map((p) => (
          <div key={p.name}>
            <div className="mb-1 flex items-baseline justify-between gap-3 text-body">
              <span className="font-medium">{p.name}</span>
              <span className="tabular-nums text-ink-faint">{p.share}%</span>
            </div>
            <div className="h-2 overflow-hidden rounded-full bg-sunken">
              <div className="h-full rounded-full" style={{ width: `${p.share}%`, background: primary }} />
            </div>
            <p className="mt-1 text-body leading-relaxed text-ink-soft">{p.example}</p>
          </div>
        ))}
      </div>

      <div className="mt-6">
        <RuledHead>Posts</RuledHead>
        <div className="mt-3 grid gap-3 sm:grid-cols-3">
          {data.posts.map((post, i) => (
            <div key={post.format} className="overflow-hidden rounded-lg border border-line">
              <div
                className="flex aspect-[4/5] flex-col justify-end p-4"
                style={{ background: fills[i % fills.length], color: readableOn(fills[i % fills.length]) }}
              >
                <p className="text-subhead font-semibold leading-tight tracking-[-0.02em]">{post.headline}</p>
              </div>
              <div className="p-3">
                <Kicker className="mb-1">{post.format}</Kicker>
                <p className="text-body leading-relaxed text-ink-soft">{post.caption}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="mt-6 grid gap-3 sm:grid-cols-2">
        <div className="rounded-lg border border-line p-4">
          <Kicker className="mb-1.5">Page banner</Kicker>
          <p className="text-body leading-relaxed text-ink-soft">{data.bannerNote}</p>
        </div>
        <div className="rounded-lg border border-line p-4">
          <Kicker className="mb-1.5">Cadence</Kicker>
          <p className="text-body leading-relaxed text-ink-soft">{data.cadence}</p>
        </div>
      </div>
    </div>
  );
}

/* ---------------------------------------------------------------- */

function WebsiteView({
  data, palette,
}: { data: Extract<DeliverablePayload, { kind: "website" }>["data"]; palette: PalettePayload | null }) {
  const primary = palette?.swatches.find((s) => s.role === "primary")?.hex ?? "#17150F";
  const paper = palette?.swatches.find((s) => s.role === "surface")?.hex ?? "#FFFFFF";
  const ink = palette?.swatches.find((s) => s.role === "ink")?.hex ?? "#17150F";

  return (
    <div>
      <div className="overflow-hidden rounded-lg border border-line" style={{ background: paper, color: ink }}>
        <div className="flex flex-wrap items-center justify-between gap-3 border-b px-5 py-3" style={{ borderColor: `${ink}18` }}>
          <span className="text-lead font-semibold" aria-hidden>◆</span>
          <nav className="flex flex-wrap gap-4 text-body opacity-70">
            {data.navigation.map((n) => <span key={n}>{n}</span>)}
          </nav>
        </div>
        <div className="px-6 py-12 text-center">
          <p className="text-micro uppercase tracking-[0.16em] opacity-50">{data.hero.eyebrow}</p>
          <h3 className="mx-auto mt-3 max-w-[18ch] text-[clamp(26px,4vw,44px)] font-semibold leading-[1.05] tracking-[-0.03em]">
            {data.hero.headline}
          </h3>
          <p className="mx-auto mt-4 max-w-[52ch] text-lead leading-relaxed opacity-70">{data.hero.sub}</p>
          <div className="mt-6 flex flex-wrap justify-center gap-2">
            <span
              className="rounded-full px-4 py-2 text-body font-medium"
              style={{ background: primary, color: readableOn(primary) }}
            >
              {data.hero.primaryCta}
            </span>
            <span className="rounded-full border px-4 py-2 text-body" style={{ borderColor: `${ink}30` }}>
              {data.hero.secondaryCta}
            </span>
          </div>
        </div>
      </div>

      <div className="mt-5 space-y-4">
        {data.sections.map((s) => (
          <div key={s.heading} className="grid gap-2 border-t border-line pt-4 sm:grid-cols-[9rem_1fr] sm:gap-6">
            <Kicker className="pt-0.5">{s.kicker}</Kicker>
            <div>
              <p className="text-lead font-medium">{s.heading}</p>
              <p className="mt-1.5 max-w-2xl text-lead leading-relaxed text-ink-soft">{s.body}</p>
            </div>
          </div>
        ))}
      </div>

      <div className="mt-6 grid gap-3 sm:grid-cols-2">
        <div className="rounded-lg border border-line p-4">
          <Kicker className="mb-1.5">Proof</Kicker>
          <p className="text-lead leading-relaxed text-ink-soft">{data.proof}</p>
        </div>
        <div className="rounded-lg border border-line p-4">
          <Kicker className="mb-1.5">Footer</Kicker>
          <p className="text-lead leading-relaxed text-ink-soft">{data.footerLine}</p>
        </div>
      </div>
    </div>
  );
}

/* ---------------------------------------------------------------- */

function ExposeView({ data }: { data: Extract<DeliverablePayload, { kind: "expose" }>["data"] }) {
  return (
    <div className="max-w-3xl">
      <p className="font-display text-title leading-snug tracking-[-0.01em]">{data.oneLiner}</p>
      <div className="mt-5 space-y-4">
        {data.paragraphs.map((p, i) => (
          <p key={i} className="font-display text-lead leading-[1.7] text-ink-soft">{p}</p>
        ))}
      </div>

      <div className="mt-6 grid gap-x-6 gap-y-2 rounded-lg border border-line p-5 sm:grid-cols-2">
        {data.atAGlance.map((row) => (
          <div key={row.label} className="flex items-baseline justify-between gap-3 border-b border-line-soft py-1.5 text-body">
            <span className="text-ink-faint">{row.label}</span>
            <span className="text-right font-medium">{row.value}</span>
          </div>
        ))}
      </div>

      <p className="mt-5 text-body leading-relaxed text-ink-faint">{data.pressLine}</p>
    </div>
  );
}
