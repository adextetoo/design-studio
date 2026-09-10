"use client";

import { Suspense, useCallback, useMemo } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { DELIVERABLES, DELIVERABLE_BY_ID, STAGES, deliverablesInStage } from "@/data/deliverables";
import type { DeliverableId, PalettePayload } from "@/lib/types";
import { auditStrings } from "@/lib/jargon";
import { useStudio } from "@/lib/store";
import { PageHead } from "@/components/Shell";
import { DeliverableView } from "@/components/deliverables";
import { Button, EmptyState, Kicker, Panel, Pill, ProgressBar } from "@/components/ui";

export default function StudioPage() {
  return (
    <Suspense fallback={<div className="p-8 text-body text-ink-faint">Opening the studio…</div>}>
      <Studio />
    </Suspense>
  );
}

function Studio() {
  const router = useRouter();
  const params = useSearchParams();
  const store = useStudio();
  const { project, approvedCount, roll, approve, unapprove, chooseVariant, setNote, patchPayload } = store;

  /*
   * The URL is the only source of truth for which deliverable is open. That keeps
   * a link to a deliverable shareable, makes the back button work, and means there
   * is no second copy of this state to fall out of sync.
   */
  const requested = params.get("deliverable") as DeliverableId | null;
  const active: DeliverableId = requested && DELIVERABLE_BY_ID[requested] ? requested : "story";

  const select = useCallback(
    (id: DeliverableId) => {
      router.replace(`/studio?deliverable=${id}`, { scroll: false });
    },
    [router],
  );

  const meta = DELIVERABLE_BY_ID[active];
  const state = project.deliverables[active];
  const variant = store.activeVariant(active);

  /* Visual deliverables preview in the approved brand colours where they exist. */
  const palette = useMemo<PalettePayload | null>(() => {
    const paletteState = project.deliverables.palette;
    const id = paletteState.approvedVariantId ?? paletteState.activeVariantId;
    const found = paletteState.variants.find((v) => v.id === id);
    return found && found.payload.kind === "palette" ? found.payload.data : null;
  }, [project.deliverables.palette]);

  const typeStack = useMemo(() => {
    const typeState = project.deliverables.typography;
    const id = typeState.approvedVariantId ?? typeState.activeVariantId;
    const found = typeState.variants.find((v) => v.id === id);
    return found && found.payload.kind === "typography" ? found.payload.data.primary.stack : null;
  }, [project.deliverables.typography]);

  const toneHits = useMemo(
    () => (variant ? auditStrings(variant.payload) : []),
    [variant],
  );

  return (
    <>
      <PageHead
        eyebrow={`${project.brief.brandName || "Client"} · Design Studio`}
        title={meta.title}
        note={meta.purpose}
        status={
          state.status === "approved"
            ? { label: "Approved", tone: "go" }
            : state.status === "draft"
              ? { label: `Draft · round ${variant?.round ?? 1}`, tone: "neutral" }
              : { label: "Not started", tone: "neutral" }
        }
        actions={
          <div className="flex items-center gap-2">
            <span className="hidden text-body tabular-nums text-ink-faint sm:inline">
              {approvedCount} of {DELIVERABLES.length} approved
            </span>
            <Link
              href="/handoff"
              className="rounded-md border border-line px-3 py-1.5 text-body font-medium hover:bg-sunken"
            >
              Handoff ›
            </Link>
          </div>
        }
      />

      <div className="mx-auto grid max-w-7xl gap-5 px-5 py-6 lg:grid-cols-[15rem_minmax(0,1fr)_16rem] md:px-8">
        <DeliverableRail active={active} onSelect={select} />

        <div className="min-w-0">
          <Panel className="min-h-[30rem] pb-16">
            {!variant ? (
              <EmptyState
                title="Nothing drafted yet"
                note="Generate a first round from the discovery answers. It is a starting point to react to — the studio still makes the decisions."
                action={<Button variant="primary" onClick={() => roll(active)}>Generate first round</Button>}
              />
            ) : (
              <div key={variant.id} className="rise">
                <DeliverableView
                  deliverableId={active}
                  payload={variant.payload}
                  palette={palette}
                  typeStack={typeStack}
                  onPatch={(mutate) => patchPayload(active, mutate)}
                />
              </div>
            )}
          </Panel>

          {variant ? (
            <div className="sticky bottom-0 z-20 mt-4 flex flex-wrap items-center justify-between gap-3 rounded-lg border border-line bg-panel px-4 py-3 shadow-[0_-8px_24px_-12px_rgba(0,0,0,0.18)]">
              <div className="flex items-center gap-2">
                <Button onClick={() => roll(active)}>
                  <span aria-hidden>↻</span> Refresh
                </Button>
                <p className="hidden max-w-[22rem] text-body leading-snug text-ink-faint sm:block">
                  A new round, generated from the same brief. Every previous round stays available.
                </p>
              </div>
              {state.status === "approved" ? (
                <div className="flex items-center gap-2">
                  <Pill tone="go" dot>
                    Approved{" "}
                    {state.approvedAt
                      ? new Date(state.approvedAt).toLocaleString("en-GB", {
                          day: "numeric", month: "short", hour: "2-digit", minute: "2-digit",
                        })
                      : ""}
                  </Pill>
                  <Button onClick={() => unapprove(active)}>Withdraw approval</Button>
                </div>
              ) : (
                <Button variant="primary" onClick={() => approve(active)}>
                  <span aria-hidden>✓</span> Approve — send to {meta.exports === "both" ? "both files" : meta.exports === "brand" ? "Brand.md" : "Design.md"}
                </Button>
              )}
            </div>
          ) : null}
        </div>

        <aside className="space-y-4">
          <Panel>
            <Kicker className="mb-2">Rounds</Kicker>
            {state.variants.length === 0 ? (
              <p className="text-body text-ink-soft">None yet.</p>
            ) : (
              <div className="space-y-1.5">
                {state.variants
                  .slice()
                  .reverse()
                  .map((v) => {
                    const isActive = v.id === state.activeVariantId;
                    const isApproved = v.id === state.approvedVariantId;
                    return (
                      <button
                        key={v.id}
                        type="button"
                        onClick={() => chooseVariant(active, v.id)}
                        aria-pressed={isActive}
                        className={`flex w-full items-center justify-between gap-2 rounded-md border px-2.5 py-1.5 text-left text-body transition-colors ${
                          isActive ? "border-ink bg-sunken" : "border-line hover:bg-sunken/60"
                        }`}
                      >
                        <span>Round {v.round}</span>
                        <span className="flex items-center gap-1.5">
                          {isApproved ? <span className="text-micro text-go">✓</span> : null}
                          <span className="text-micro tabular-nums text-ink-faint">
                            {new Date(v.createdAt).toLocaleTimeString("en-GB", { hour: "2-digit", minute: "2-digit" })}
                          </span>
                        </span>
                      </button>
                    );
                  })}
              </div>
            )}
            <p className="mt-3 border-t border-line-soft pt-3 text-micro leading-relaxed text-ink-faint">
              Rounds are kept so you can go back to the one from twenty minutes ago. That is usually the good one.
            </p>
          </Panel>

          {variant ? (
            <Panel>
              <Kicker className="mb-2">Human tone check</Kicker>
              {toneHits.length === 0 ? (
                <div className="flex items-start gap-2">
                  <span className="mt-0.5 text-go" aria-hidden>✓</span>
                  <p className="text-body leading-relaxed text-ink-soft">
                    Clean. No AI tells, no consultancy filler.
                  </p>
                </div>
              ) : (
                <div>
                  <p className="text-body leading-relaxed text-ink-soft">
                    {toneHits.length} phrase{toneHits.length === 1 ? "" : "s"} to rewrite before this ships:
                  </p>
                  <ul className="mt-2 flex flex-wrap gap-1.5">
                    {toneHits.slice(0, 8).map((h, i) => (
                      <li key={i} className="rounded bg-signal-soft px-2 py-0.5 text-body text-signal">
                        {h.hit.phrase}
                      </li>
                    ))}
                  </ul>
                </div>
              )}
              <p className="mt-3 border-t border-line-soft pt-3 text-micro leading-relaxed text-ink-faint">
                Every string on this screen is checked against a blocklist of machine tells and consultancy filler.
                Quoted counter-examples in the voice guidelines are exempt — they are supposed to be bad.
              </p>
            </Panel>
          ) : null}

          <Panel>
            <Kicker className="mb-2">Studio note</Kicker>
            <textarea
              value={state.note}
              onChange={(e) => setNote(active, e.target.value)}
              rows={4}
              placeholder="What you would say out loud when presenting this."
              className="w-full resize-y rounded-md border border-line bg-panel px-2.5 py-2 text-body leading-relaxed placeholder:text-ink-faint focus:border-ink focus:outline-none"
            />
          </Panel>
        </aside>
      </div>
    </>
  );
}

function DeliverableRail({ active, onSelect }: { active: DeliverableId; onSelect: (id: DeliverableId) => void }) {
  const { project, approvedCount } = useStudio();
  return (
    <nav className="lg:sticky lg:top-6 lg:self-start" aria-label="Brand deliverables">
      <div className="mb-3">
        <div className="mb-1.5 flex items-baseline justify-between gap-2">
          <Kicker>Deliverables</Kicker>
          <span className="text-micro tabular-nums text-ink-faint">{approvedCount}/{DELIVERABLES.length}</span>
        </div>
        <ProgressBar value={approvedCount} total={DELIVERABLES.length} />
      </div>

      <div className="space-y-4">
        {STAGES.map((stage) => (
          <div key={stage.id}>
            <p className="mb-1 px-1 text-micro font-medium text-ink-faint">{stage.label}</p>
            <ul>
              {deliverablesInStage(stage.id).map((m) => {
                const status = project.deliverables[m.id].status;
                const isActive = m.id === active;
                return (
                  <li key={m.id}>
                    <button
                      type="button"
                      onClick={() => onSelect(m.id)}
                      aria-current={isActive ? "true" : undefined}
                      className={`mb-0.5 flex w-full items-center gap-2 rounded-md px-2.5 py-1.5 text-left text-body transition-colors ${
                        isActive ? "bg-ink text-panel" : "text-ink-soft hover:bg-sunken"
                      }`}
                    >
                      <span
                        className={`size-1.5 shrink-0 rounded-full ${
                          status === "approved"
                            ? "bg-go"
                            : status === "draft"
                              ? isActive ? "bg-panel/60" : "bg-ink-faint"
                              : isActive ? "bg-panel/25" : "bg-line"
                        }`}
                        aria-hidden
                      />
                      <span className="truncate">{m.title}</span>
                    </button>
                  </li>
                );
              })}
            </ul>
          </div>
        ))}
      </div>
    </nav>
  );
}
