"use client";

import { useState } from "react";
import { TRAIT_BANK } from "@/data/workshop";
import type { Ballot } from "@/lib/types";
import { Button, Kicker, Pill } from "./ui";

const MAX_PICKS = 5;

/**
 * The brand personality exercise, run the way the reference studio runs it.
 *
 * Everyone in the room gets five picks and one favourite, in private. Nothing
 * is visible until the reveal — which is the entire point of the exercise. If
 * the loudest person's picks are on screen while everyone else is choosing,
 * you have measured that person, not the brand.
 */
export function TraitBallot({
  ballots, revealed, onChange, onReveal,
}: {
  ballots: Ballot[];
  revealed: boolean;
  onChange: (ballots: Ballot[]) => void;
  onReveal: (revealed: boolean) => void;
}) {
  const [name, setName] = useState("");
  const [picks, setPicks] = useState<string[]>([]);
  const [favourite, setFavourite] = useState<string | null>(null);

  const toggle = (trait: string) => {
    setPicks((current) => {
      if (current.includes(trait)) {
        if (favourite === trait) setFavourite(null);
        return current.filter((t) => t !== trait);
      }
      if (current.length >= MAX_PICKS) return current;
      return [...current, trait];
    });
  };

  const submit = () => {
    if (picks.length !== MAX_PICKS) return;
    onChange([...ballots, { participant: name.trim() || `Participant ${ballots.length + 1}`, picks, favourite }]);
    setName("");
    setPicks([]);
    setFavourite(null);
  };

  const tally = new Map<string, { count: number; favourites: number }>();
  for (const ballot of ballots) {
    for (const pick of ballot.picks) {
      const entry = tally.get(pick) ?? { count: 0, favourites: 0 };
      entry.count += 1;
      tally.set(pick, entry);
    }
    if (ballot.favourite) {
      const entry = tally.get(ballot.favourite) ?? { count: 0, favourites: 0 };
      entry.favourites += 1;
      tally.set(ballot.favourite, entry);
    }
  }
  const ranked = [...tally.entries()].sort(
    (a, b) => b[1].favourites - a[1].favourites || b[1].count - a[1].count,
  );

  return (
    <div className="space-y-6">
      <div>
        <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
          <Kicker>Your ballot — {picks.length} of {MAX_PICKS} picked</Kicker>
          {picks.length === MAX_PICKS ? (
            <p className="text-body text-ink-soft">Now star the one that matters most.</p>
          ) : null}
        </div>

        <div className="flex flex-wrap gap-1.5">
          {TRAIT_BANK.map((trait) => {
            const picked = picks.includes(trait);
            const isFavourite = favourite === trait;
            const full = picks.length >= MAX_PICKS && !picked;
            return (
              <span key={trait} className="inline-flex">
                <button
                  type="button"
                  onClick={() => toggle(trait)}
                  disabled={full}
                  aria-pressed={picked}
                  className={`rounded-full px-3 py-1 text-body transition-[background-color,border-color,color,transform] duration-150 ease-[var(--ease-out-quint)] active:scale-[0.96] ${
                    picked
                      ? "bg-ink text-panel"
                      : full
                        ? "cursor-not-allowed border border-line-soft text-ink-faint/60"
                        : "border border-line text-ink-soft hover:border-ink hover:text-ink"
                  } ${isFavourite ? "ring-2 ring-signal ring-offset-1" : ""}`}
                >
                  {trait}
                </button>
                {picked ? (
                  <button
                    type="button"
                    onClick={() => setFavourite(isFavourite ? null : trait)}
                    aria-label={`Mark ${trait} as your favourite`}
                    aria-pressed={isFavourite}
                    className={`-ml-1 rounded-full px-1.5 text-body ${
                      isFavourite ? "text-signal" : "text-ink-faint hover:text-signal"
                    }`}
                  >
                    ★
                  </button>
                ) : null}
              </span>
            );
          })}
        </div>

        <div className="mt-4 flex flex-wrap items-center gap-2">
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Your name"
            className="w-44 rounded-md border border-line bg-panel px-3 py-1.5 text-body focus:border-ink focus:outline-none"
          />
          <Button variant="primary" onClick={submit} disabled={picks.length !== MAX_PICKS}>
            Submit in private
          </Button>
          <p className="text-body text-ink-faint">
            {ballots.length} ballot{ballots.length === 1 ? "" : "s"} in. Nobody sees anything until the reveal.
          </p>
        </div>
      </div>

      <div className="rounded-lg border border-line bg-sunken/50 p-4">
        <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
          <Kicker>The room</Kicker>
          <Button
            onClick={() => onReveal(!revealed)}
            disabled={ballots.length === 0}
            variant={revealed ? "secondary" : "primary"}
          >
            {revealed ? "Hide again" : `Reveal all ${ballots.length}`}
          </Button>
        </div>

        {ballots.length === 0 ? (
          <p className="text-body text-ink-soft">
            Nothing submitted yet. Get everyone on the call to fill this in before anyone says a word out loud.
          </p>
        ) : !revealed ? (
          <div className="flex flex-wrap gap-2">
            {ballots.map((b, i) => (
              <span
                key={`${b.participant}-${i}`}
                className="rounded-md border border-line bg-panel px-3 py-1.5 text-body"
              >
                {b.participant} <span className="text-ink-faint">— sealed</span>
              </span>
            ))}
          </div>
        ) : (
          <div className="space-y-4">
            <div>
              <p className="mb-2 text-body text-ink-soft">
                Ranked by favourites first, then by how many people picked it. The top five become the brand personality.
              </p>
              <div className="flex flex-wrap gap-1.5">
                {ranked.map(([trait, { count, favourites }], i) => (
                  <span
                    key={trait}
                    className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-body ${
                      i < 5 ? "bg-ink text-panel" : "border border-line text-ink-soft"
                    }`}
                  >
                    {trait}
                    <span className="tabular-nums opacity-70">{count}</span>
                    {favourites > 0 ? <span className="text-signal">{"★".repeat(Math.min(favourites, 3))}</span> : null}
                  </span>
                ))}
              </div>
            </div>

            <ul className="space-y-1.5 border-t border-line pt-3 text-body">
              {ballots.map((b, i) => (
                <li key={`${b.participant}-${i}`} className="flex flex-wrap items-baseline gap-x-2">
                  <span className="font-medium">{b.participant}</span>
                  <span className="text-ink-soft">{b.picks.join(", ")}</span>
                  {b.favourite ? <Pill tone="signal">★ {b.favourite}</Pill> : null}
                </li>
              ))}
            </ul>

            {ranked.length > 0 && ranked[0][1].favourites === ballots.length && ballots.length > 1 ? (
              <p className="text-body text-ink-soft">
                Everyone starred the same word. That is either real alignment or one person talked before the ballot.
              </p>
            ) : null}

            {ballots.length > 1 && ranked.filter(([, v]) => v.count === 1).length > ranked.length / 2 ? (
              <p className="text-body text-ink-soft">
                Over half these words were picked by one person only. The room does not agree yet — that disagreement is
                the most useful thing on this screen, and it is worth twenty minutes before moving on.
              </p>
            ) : null}
          </div>
        )}
      </div>
    </div>
  );
}
