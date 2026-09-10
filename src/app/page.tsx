"use client";

import Link from "next/link";
import { MODULES, STAGES, modulesInStage } from "@/data/modules";
import { STEPS } from "@/data/workshop";
import { EMPTY_BRIEF, SAMPLE_BRIEF, useStudio } from "@/lib/store";
import { PageHead } from "@/components/Shell";
import { Button, Panel, Pill, ProgressBar, SectionHead, StatTile } from "@/components/ui";

export default function OverviewPage() {
  const { project, approvedCount, reset, roll } = useStudio();
  const answered = Object.values(project.workshop.answers).filter((v) => v.trim()).length;
  const drafted = MODULES.filter((m) => project.modules[m.id].status !== "empty").length;
  const waiting = MODULES.filter((m) => project.modules[m.id].status === "draft").length;

  const recent = MODULES.flatMap((m) =>
    project.modules[m.id].variants.map((v) => ({ module: m, variant: v })),
  )
    .sort((a, b) => b.variant.createdAt.localeCompare(a.variant.createdAt))
    .slice(0, 8);

  return (
    <>
      <PageHead
        eyebrow={new Date().toLocaleDateString("en-GB", { weekday: "long", day: "numeric", month: "long" })}
        title={`Welcome back to the studio`}
        note="One workspace for the whole engagement: discovery, strategy, identity, applications, and a handover file that only contains what somebody actually approved."
        actions={
          <>
            <Button onClick={() => reset(SAMPLE_BRIEF)}>Load worked example</Button>
            <Button variant="primary" onClick={() => reset(EMPTY_BRIEF)}>Start a new client</Button>
          </>
        }
      />

      <div className="mx-auto max-w-6xl space-y-6 px-5 py-6 md:px-8">
        <Panel flush>
          <div className="grid grid-cols-2 md:grid-cols-4">
            <StatTile label="Discovery answered" value={`${answered}`} note={`of ${STEPS.length} steps`} />
            <StatTile label="Modules drafted" value={`${drafted}`} note={`of ${MODULES.length}`} />
            <StatTile label="Waiting on you" value={`${waiting}`} note="drafts not yet approved" />
            <StatTile label="Approved" value={`${approvedCount}`} note="goes into the export" />
          </div>
        </Panel>

        <div className="grid gap-6 lg:grid-cols-[1.6fr_1fr]">
          <div className="space-y-6">
            <Panel>
              <SectionHead
                title="The engagement"
                note="Four passes. Each one is only finished when a person has signed it off."
                action={<Pill tone={approvedCount === MODULES.length ? "go" : "neutral"}>{approvedCount}/{MODULES.length}</Pill>}
              />
              <div className="mb-5">
                <ProgressBar value={approvedCount} total={MODULES.length} />
              </div>
              <ol className="space-y-4">
                {STAGES.map((stage, i) => {
                  const mods = modulesInStage(stage.id);
                  const done = mods.filter((m) => project.modules[m.id].status === "approved").length;
                  return (
                    <li key={stage.id} className="flex gap-3">
                      <span
                        className={`mt-0.5 grid size-5 shrink-0 place-items-center rounded-full text-[10px] font-semibold ${
                          done === mods.length ? "bg-go text-panel" : "border border-line text-ink-faint"
                        }`}
                        aria-hidden
                      >
                        {done === mods.length ? "✓" : i + 1}
                      </span>
                      <div className="min-w-0 flex-1">
                        <div className="flex items-baseline justify-between gap-3">
                          <p className="text-[13px] font-medium">{stage.label}</p>
                          <p className="text-[11px] tabular-nums text-ink-faint">{done}/{mods.length}</p>
                        </div>
                        <p className="text-[12px] text-ink-faint">{stage.note}</p>
                        <div className="mt-2 flex flex-wrap gap-1.5">
                          {mods.map((m) => {
                            const status = project.modules[m.id].status;
                            return (
                              <Link
                                key={m.id}
                                href={{ pathname: "/studio", query: { module: m.id } }}
                                className={`rounded border px-2 py-0.5 text-[11px] transition-colors ${
                                  status === "approved"
                                    ? "border-go/30 bg-go-soft text-go"
                                    : status === "draft"
                                      ? "border-line bg-sunken text-ink-soft"
                                      : "border-line text-ink-faint hover:bg-sunken"
                                }`}
                              >
                                {m.title}
                              </Link>
                            );
                          })}
                        </div>
                      </div>
                    </li>
                  );
                })}
              </ol>
            </Panel>

            <Panel>
              <SectionHead title="Activity" note="Every round the studio has generated, newest first." />
              {recent.length === 0 ? (
                <p className="rounded-md border border-dashed border-line px-4 py-8 text-center text-[13px] text-ink-soft">
                  Nothing generated yet. Run discovery first, then open the design studio.
                </p>
              ) : (
                <ul className="divide-y divide-line-soft text-[13px]">
                  {recent.map(({ module, variant }) => (
                    <li key={variant.id} className="flex items-center justify-between gap-3 py-2">
                      <span className="min-w-0 truncate">
                        <span className="font-medium">{module.title}</span>
                        <span className="text-ink-faint"> — round {variant.round}</span>
                      </span>
                      <span className="shrink-0 text-[11px] tabular-nums text-ink-faint">
                        {new Date(variant.createdAt).toLocaleTimeString("en-GB", { hour: "2-digit", minute: "2-digit" })}
                      </span>
                    </li>
                  ))}
                </ul>
              )}
            </Panel>
          </div>

          <div className="space-y-6">
            <Panel>
              <SectionHead title="Next" note="What is worth doing now." />
              <div className="space-y-3 text-[13px]">
                {answered < STEPS.length ? (
                  <NextItem
                    href="/discovery"
                    title="Finish discovery"
                    note={`${STEPS.length - answered} question${STEPS.length - answered === 1 ? "" : "s"} left. The generators read these answers directly — a thin brief makes thin work.`}
                  />
                ) : null}
                {drafted < MODULES.length ? (
                  <NextItem
                    href="/studio"
                    title="Draft the remaining modules"
                    note={`${MODULES.length - drafted} still empty. Generate a first round, then judge it.`}
                  />
                ) : null}
                {waiting > 0 ? (
                  <NextItem
                    href="/studio"
                    title={`Approve or refresh ${waiting} draft${waiting === 1 ? "" : "s"}`}
                    note="Nothing reaches the export until you approve it."
                  />
                ) : null}
                {approvedCount === MODULES.length ? (
                  <NextItem href="/handoff" title="Export the handover" note="Brand.md and Design.md are ready." />
                ) : null}
              </div>
            </Panel>

            <Panel>
              <SectionHead title="Client" note="Read straight from discovery." />
              <dl className="space-y-2 text-[13px]">
                <Row label="Name" value={project.brief.brandName || "—"} />
                <Row label="Sector" value={project.brief.sector || "—"} />
                <Row label="Based" value={project.brief.location || "—"} />
                <Row label="Price" value={project.brief.priceStance} capitalise />
                <Row label="Type class" value={project.brief.fontClass.replace("-", " ")} capitalise />
              </dl>
              {project.brief.traits.length > 0 ? (
                <div className="mt-3 flex flex-wrap gap-1.5 border-t border-line-soft pt-3">
                  {project.brief.traits.map((t) => (
                    <Pill key={t}>{t}</Pill>
                  ))}
                </div>
              ) : null}
            </Panel>

            <Panel>
              <SectionHead title="Draft everything" note="Generates a first round for every empty module." />
              <Button
                variant="secondary"
                className="w-full"
                onClick={() => {
                  for (const m of MODULES) {
                    if (project.modules[m.id].status === "empty") roll(m.id);
                  }
                }}
              >
                Generate first rounds
              </Button>
              <p className="mt-2 text-[11px] leading-relaxed text-ink-faint">
                A first round is a starting point, not an answer. It exists so nobody has to open a blank page.
              </p>
            </Panel>
          </div>
        </div>
      </div>
    </>
  );
}

function NextItem({ href, title, note }: { href: string; title: string; note: string }) {
  return (
    <Link
      href={href as "/discovery"}
      className="block rounded-md border border-line px-3 py-2.5 transition-colors hover:bg-sunken"
    >
      <p className="font-medium">{title}</p>
      <p className="mt-0.5 text-[12px] leading-relaxed text-ink-soft">{note}</p>
    </Link>
  );
}

/** `capitalise` is opt-in: fields the client typed keep the casing they typed. */
function Row({ label, value, capitalise = false }: { label: string; value: string; capitalise?: boolean }) {
  return (
    <div className="flex items-baseline justify-between gap-3">
      <dt className="text-ink-faint">{label}</dt>
      <dd className={`truncate text-right font-medium ${capitalise ? "first-letter:uppercase" : ""}`}>{value}</dd>
    </div>
  );
}
