"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import {
  FONT_CLASS_OPTIONS, PHASES, PRICE_STANCE_OPTIONS, STEPS, phaseNumber, phaseOf,
} from "@/data/workshop";
import { useStudio } from "@/lib/store";
import { PageHead } from "@/components/Shell";
import { TraitBallot } from "@/components/TraitBallot";
import { Button, Kicker, Panel, Pill, ProgressBar, inputClass } from "@/components/ui";

type Tab = "workshop" | "answers";

export default function DiscoveryPage() {
  const { project, answer, goToStep, setBallots, setRevealed } = useStudio();
  const [tab, setTab] = useState<Tab>("workshop");

  const index = project.workshop.stepIndex;
  const step = STEPS[index];
  const phase = phaseOf(step);
  const value = project.workshop.answers[step.id] ?? "";

  const answered = useMemo(
    () =>
      STEPS.filter(
        (s) =>
          (project.workshop.answers[s.id] ?? "").trim() ||
          (s.kind === "traits" && project.brief.traits.length > 0),
      ).length,
    [project.workshop.answers, project.brief.traits],
  );

  return (
    <>
      <PageHead
        eyebrow={`${project.brief.brandName || "Client"} · Discovery`}
        title="Discovery & Strategy"
        note="One question at a time, the way a workshop actually runs. Everything typed here feeds the generators — a thin brief produces thin work, and it will be obvious."
        status={{
          label: `${answered} of ${STEPS.length} answered`,
          tone: answered === STEPS.length ? "go" : "neutral",
        }}
        actions={
          <div className="flex rounded-md border border-line p-0.5">
            {(["workshop", "answers"] as Tab[]).map((t) => (
              <button
                key={t}
                type="button"
                onClick={() => setTab(t)}
                aria-pressed={tab === t}
                className={`rounded px-3 py-1 text-body capitalize transition-colors ${
                  tab === t ? "bg-ink text-panel" : "text-ink-soft hover:text-ink"
                }`}
              >
                {t}
              </button>
            ))}
          </div>
        }
      />

      {tab === "workshop" ? (
        <div className="mx-auto max-w-4xl px-5 py-6 md:px-8">
          <div className="mb-3 flex flex-wrap items-center justify-between gap-3">
            <Kicker>
              Phase {phaseNumber(phase.id)} of {PHASES.length} · {phase.name}
            </Kicker>
            <Kicker>
              Step {index + 1} of {STEPS.length}
            </Kicker>
          </div>
          <ProgressBar value={index + 1} total={STEPS.length} />

          <Panel className="mt-6 min-h-[26rem] rise" key={step.id}>
            <p className="mb-6 text-body italic text-ink-faint">{phase.intent}</p>

            <h2 className="mx-auto max-w-2xl text-center font-display text-hero leading-tight tracking-[-0.02em]">
              {step.question}
            </h2>
            {step.helper ? (
              <p className="mx-auto mt-2 max-w-xl text-center text-body leading-relaxed text-ink-soft">
                {step.helper}
              </p>
            ) : null}

            <div className="mx-auto mt-7 max-w-2xl">
              {step.kind === "traits" ? (
                <TraitBallot
                  ballots={project.workshop.ballots}
                  revealed={project.workshop.revealed}
                  onChange={setBallots}
                  onReveal={setRevealed}
                />
              ) : step.kind === "choice" ? (
                <ChoiceGrid
                  stepId={step.id}
                  options={step.options ?? []}
                  value={value}
                  onPick={(v) => answer(step.id, v)}
                />
              ) : step.kind === "text" ? (
                <input
                  value={value}
                  onChange={(e) => answer(step.id, e.target.value)}
                  placeholder={step.placeholder}
                  className={inputClass}
                />
              ) : (
                <textarea
                  value={value}
                  onChange={(e) => answer(step.id, e.target.value)}
                  placeholder={step.placeholder}
                  rows={step.kind === "list" ? 5 : 4}
                  className={`${inputClass} resize-y leading-relaxed`}
                />
              )}
              {step.kind === "list" ? (
                <p className="mt-1.5 text-micro text-ink-faint">One per line.</p>
              ) : null}
            </div>
          </Panel>

          <div className="mt-4 flex items-center justify-between gap-3">
            <Button onClick={() => goToStep(index - 1)} disabled={index === 0}>
              ‹ Back
            </Button>
            <p className="text-body text-ink-faint">
              {value.trim() || (step.kind === "traits" && project.workshop.ballots.length > 0)
                ? "Saved"
                : "Not answered"}
            </p>
            {index === STEPS.length - 1 ? (
              <Link
                href="/studio"
                className="inline-flex items-center gap-2 rounded-md bg-ink px-3 py-1.5 text-body font-medium text-panel hover:bg-ink/90"
              >
                Open the design studio ›
              </Link>
            ) : (
              <Button variant="primary" onClick={() => goToStep(index + 1)}>
                Next ›
              </Button>
            )}
          </div>

          <nav className="mt-8 flex flex-wrap gap-1" aria-label="Workshop steps">
            {STEPS.map((s, i) => {
              const done =
                (project.workshop.answers[s.id] ?? "").trim() ||
                (s.kind === "traits" && project.brief.traits.length > 0);
              return (
                <button
                  key={s.id}
                  type="button"
                  onClick={() => goToStep(i)}
                  title={`${i + 1}. ${s.question}`}
                  aria-label={`Step ${i + 1}: ${s.question}`}
                  aria-current={i === index ? "step" : undefined}
                  className={`h-1.5 flex-1 rounded-full transition-colors ${
                    i === index ? "bg-ink" : done ? "bg-go/50" : "bg-line"
                  }`}
                />
              );
            })}
          </nav>
        </div>
      ) : (
        <AnswersView />
      )}
    </>
  );
}

function ChoiceGrid({
  stepId, options, value, onPick,
}: {
  stepId: string;
  options: string[];
  value: string;
  onPick: (v: string) => void;
}) {
  const notes: Record<string, string> = Object.fromEntries([
    ...FONT_CLASS_OPTIONS.map((o) => [o.value, o.note]),
    ...PRICE_STANCE_OPTIONS.map((o) => [o.value, o.note]),
  ]);
  const labels: Record<string, string> = Object.fromEntries([
    ...FONT_CLASS_OPTIONS.map((o) => [o.value, o.label]),
    ...PRICE_STANCE_OPTIONS.map((o) => [o.value, o.label]),
  ]);

  return (
    <div className="grid gap-2 sm:grid-cols-2">
      {options.map((option) => {
        const selected = value === option;
        return (
          <button
            key={`${stepId}-${option}`}
            type="button"
            onClick={() => onPick(option)}
            aria-pressed={selected}
            className={`rounded-lg border p-3 text-left transition-[border-color,background-color,transform] duration-150 ease-[var(--ease-out-quint)] active:scale-[0.99] ${
              selected ? "border-ink bg-sunken" : "border-line hover:border-ink/40"
            }`}
          >
            <span className="flex items-center justify-between gap-2">
              <span className="text-lead font-medium">{labels[option] ?? option}</span>
              <span
                className={`grid size-4 shrink-0 place-items-center rounded-full border text-micro ${
                  selected ? "border-ink bg-ink text-panel" : "border-line"
                }`}
                aria-hidden
              >
                {selected ? "✓" : ""}
              </span>
            </span>
            {notes[option] ? (
              <span className="mt-1 block text-body leading-relaxed text-ink-soft">{notes[option]}</span>
            ) : null}
          </button>
        );
      })}
    </div>
  );
}

function AnswersView() {
  const { project, goToStep } = useStudio();
  return (
    <div className="mx-auto max-w-4xl space-y-5 px-5 py-6 md:px-8">
      {PHASES.map((phase) => {
        const steps = STEPS.filter((s) => s.phaseId === phase.id);
        return (
          <Panel key={phase.id}>
            <div className="mb-3 flex items-baseline justify-between gap-3">
              <h2 className="text-subhead">
                <span className="text-ink-faint">{String(phaseNumber(phase.id)).padStart(2, "0")}</span>{" "}
                {phase.name}
              </h2>
              <Pill>
                {steps.filter((s) => (project.workshop.answers[s.id] ?? "").trim()).length}/{steps.length}
              </Pill>
            </div>
            <dl className="divide-y divide-line-soft">
              {steps.map((s) => {
                const answerText = (project.workshop.answers[s.id] ?? "").trim();
                const isTraits = s.kind === "traits";
                const stepIndex = STEPS.indexOf(s);
                return (
                  <div key={s.id} className="grid gap-1 py-3 sm:grid-cols-[1fr_1.4fr] sm:gap-4">
                    <dt className="text-body text-ink-soft">{s.question}</dt>
                    <dd className="text-body">
                      {isTraits ? (
                        project.brief.traits.length > 0 ? (
                          <span className="flex flex-wrap gap-1.5">
                            {project.brief.traits.map((t) => (
                              <Pill key={t}>{t}</Pill>
                            ))}
                          </span>
                        ) : (
                          <button
                            type="button"
                            onClick={() => goToStep(stepIndex)}
                            className="text-ink-faint underline"
                          >
                            Run the ballot
                          </button>
                        )
                      ) : answerText ? (
                        <span className="whitespace-pre-line">{answerText}</span>
                      ) : (
                        <button
                          type="button"
                          onClick={() => goToStep(stepIndex)}
                          className="text-ink-faint underline"
                        >
                          Not answered
                        </button>
                      )}
                    </dd>
                  </div>
                );
              })}
            </dl>
          </Panel>
        );
      })}
    </div>
  );
}
