"use client";

import {
  createContext, useCallback, useContext, useEffect, useMemo, useReducer, type ReactNode,
} from "react";
import type {
  Ballot, Brief, ModuleId, ModuleState, Project, Variant,
} from "@/lib/types";
import { MODULE_IDS } from "@/data/modules";
import { STEPS } from "@/data/workshop";
import { generate, seedFor } from "@/lib/generators";

const STORAGE_KEY = "design-studio/project/v1";

/* ------------------------------------------------------------------ */
/* Initial state                                                       */
/* ------------------------------------------------------------------ */

export const EMPTY_BRIEF: Brief = {
  brandName: "",
  offering: "",
  sector: "",
  location: "",
  foundedYear: "",
  audienceNote: "",
  competitors: [],
  priceStance: "considered",
  traits: [],
  fontClass: "geometric-sans",
  answers: {},
};

/** A worked example so the studio is never a blank screen on first open. */
export const SAMPLE_BRIEF: Brief = {
  brandName: "Monty",
  offering: "We help people close to retirement see everything they own in one place.",
  sector: "Personal finance",
  location: "Manchester",
  foundedYear: "2021",
  audienceNote: "Fifty-eight, has just sold a business, does not trust anyone in a suit.",
  competitors: ["Nutmeg", "Moneybox", "A spreadsheet"],
  priceStance: "considered",
  traits: ["Trusted", "Human", "Precise", "Transparent", "Warm"],
  fontClass: "geometric-sans",
  answers: {
    name: "Monty",
    offering: "We help people close to retirement see everything they own in one place.",
    sector: "Personal finance",
    "why-name": "It was my grandfather's name. He kept every receipt he ever got, in a tin, for forty years.",
    founded: "2021",
    location: "Manchester",
    annoyed: "Everyone I knew had four pensions and no idea what any of them were worth.",
    "first-client": "A retiring teacher. She said we were the first people who did not talk down to her.",
    "ten-years": "Forty people, still one product, known for being the honest one",
    refuse: "We will never sell customer data, and we will never run a referral scheme",
    success: "Our sales team stops apologising for the website.",
    audience: "Fifty-eight, has just sold a business, does not trust anyone in a suit.",
    before: "a spreadsheet their son-in-law built in 2016",
    objection: "It sounds expensive for something I could probably do myself.",
    moment: "the first time they open it and every account is already there",
    "not-traits": "Trusted. Everyone in finance says trusted.",
    person: "Arrives early, says the uncomfortable thing kindly, leaves on time.",
    competitors: "Nutmeg\nMoneybox\nA spreadsheet",
    "rival-good": "Their onboarding takes four minutes. Ours takes twenty.",
    price: "considered",
    "sound-like": "The way a good GP explains a diagnosis",
    "never-say": "solutions\nleverage\njourney\nempower",
    "hard-truth": "If you are under forty, you probably do not need us yet.",
    "font-class": "geometric-sans",
    admire: "Aesop. Nothing to do with pensions, but they never shout.",
    inherit: "The blue. Twenty years of paperwork is that blue.",
  },
};

function emptyModules(): Record<ModuleId, ModuleState> {
  const modules = {} as Record<ModuleId, ModuleState>;
  for (const id of MODULE_IDS) {
    modules[id] = {
      status: "empty",
      variants: [],
      activeVariantId: null,
      approvedVariantId: null,
      approvedAt: null,
      note: "",
    };
  }
  return modules;
}

export function newProject(brief: Brief = SAMPLE_BRIEF): Project {
  const now = new Date().toISOString();
  return {
    id: `p_${Math.random().toString(36).slice(2, 10)}`,
    brief,
    workshop: { stepIndex: 0, answers: { ...brief.answers }, ballots: [], revealed: false },
    modules: emptyModules(),
    createdAt: now,
    updatedAt: now,
  };
}

/* ------------------------------------------------------------------ */
/* Reducer                                                             */
/* ------------------------------------------------------------------ */

type Action =
  | { type: "hydrate"; project: Project }
  | { type: "patchBrief"; patch: Partial<Brief> }
  | { type: "answer"; id: string; value: string }
  | { type: "step"; index: number }
  | { type: "setBallots"; ballots: Ballot[] }
  | { type: "reveal"; revealed: boolean }
  | { type: "roll"; moduleId: ModuleId }
  | { type: "chooseVariant"; moduleId: ModuleId; variantId: string }
  | { type: "approve"; moduleId: ModuleId }
  | { type: "unapprove"; moduleId: ModuleId }
  | { type: "note"; moduleId: ModuleId; note: string }
  | { type: "patchPayload"; moduleId: ModuleId; mutate: (v: Variant) => Variant }
  | { type: "reset"; project: Project };

function touch(project: Project): Project {
  return { ...project, updatedAt: new Date().toISOString() };
}

function reducer(project: Project, action: Action): Project {
  switch (action.type) {
    case "hydrate":
    case "reset":
      return action.project;

    case "patchBrief":
      return touch({ ...project, brief: { ...project.brief, ...action.patch } });

    case "answer": {
      const answers = { ...project.workshop.answers, [action.id]: action.value };
      const step = STEPS.find((s) => s.id === action.id);
      let brief = { ...project.brief, answers };

      // Bound answers flow straight into the brief so the generators see them.
      if (step?.binds === "competitors") {
        brief = { ...brief, competitors: action.value.split("\n").map((s) => s.trim()).filter(Boolean) };
      } else if (step?.binds) {
        brief = { ...brief, [step.binds]: action.value } as Brief;
      }
      return touch({ ...project, brief, workshop: { ...project.workshop, answers } });
    }

    case "step":
      return touch({
        ...project,
        workshop: { ...project.workshop, stepIndex: Math.max(0, Math.min(STEPS.length - 1, action.index)) },
      });

    case "setBallots": {
      // The traits on the brief are whatever the room actually agreed on.
      const tally = new Map<string, number>();
      for (const ballot of action.ballots) {
        for (const pick of ballot.picks) tally.set(pick, (tally.get(pick) ?? 0) + 1);
        if (ballot.favourite) tally.set(ballot.favourite, (tally.get(ballot.favourite) ?? 0) + 1.5);
      }
      const traits = [...tally.entries()].sort((a, b) => b[1] - a[1]).slice(0, 5).map(([t]) => t);
      return touch({
        ...project,
        brief: { ...project.brief, traits: traits.length > 0 ? traits : project.brief.traits },
        workshop: { ...project.workshop, ballots: action.ballots },
      });
    }

    case "reveal":
      return touch({ ...project, workshop: { ...project.workshop, revealed: action.revealed } });

    case "roll": {
      const state = project.modules[action.moduleId];
      const round = state.variants.length;
      const seed = seedFor(project.brief, action.moduleId, round);
      const variant: Variant = {
        id: `${action.moduleId}_r${round}_${seed.toString(36)}`,
        round: round + 1,
        seed,
        createdAt: new Date().toISOString(),
        payload: generate(project.brief, action.moduleId, seed),
      };
      return touch({
        ...project,
        modules: {
          ...project.modules,
          [action.moduleId]: {
            ...state,
            // Refreshing an approved module drops it back to draft on purpose:
            // the thing that was approved is not the thing on screen any more.
            status: "draft",
            approvedVariantId: null,
            approvedAt: null,
            variants: [...state.variants, variant],
            activeVariantId: variant.id,
          },
        },
      });
    }

    case "chooseVariant":
      return touch({
        ...project,
        modules: {
          ...project.modules,
          [action.moduleId]: { ...project.modules[action.moduleId], activeVariantId: action.variantId },
        },
      });

    case "approve": {
      const state = project.modules[action.moduleId];
      if (!state.activeVariantId) return project;
      return touch({
        ...project,
        modules: {
          ...project.modules,
          [action.moduleId]: {
            ...state,
            status: "approved",
            approvedVariantId: state.activeVariantId,
            approvedAt: new Date().toISOString(),
          },
        },
      });
    }

    case "unapprove":
      return touch({
        ...project,
        modules: {
          ...project.modules,
          [action.moduleId]: {
            ...project.modules[action.moduleId],
            status: "draft", approvedVariantId: null, approvedAt: null,
          },
        },
      });

    case "note":
      return touch({
        ...project,
        modules: { ...project.modules, [action.moduleId]: { ...project.modules[action.moduleId], note: action.note } },
      });

    case "patchPayload": {
      const state = project.modules[action.moduleId];
      if (!state.activeVariantId) return project;
      return touch({
        ...project,
        modules: {
          ...project.modules,
          [action.moduleId]: {
            ...state,
            variants: state.variants.map((v) => (v.id === state.activeVariantId ? action.mutate(v) : v)),
          },
        },
      });
    }

    default:
      return project;
  }
}

/* ------------------------------------------------------------------ */
/* Context                                                             */
/* ------------------------------------------------------------------ */

interface StoreValue {
  project: Project;
  ready: boolean;
  patchBrief: (patch: Partial<Brief>) => void;
  answer: (id: string, value: string) => void;
  goToStep: (index: number) => void;
  setBallots: (ballots: Ballot[]) => void;
  setRevealed: (revealed: boolean) => void;
  roll: (moduleId: ModuleId) => void;
  chooseVariant: (moduleId: ModuleId, variantId: string) => void;
  approve: (moduleId: ModuleId) => void;
  unapprove: (moduleId: ModuleId) => void;
  setNote: (moduleId: ModuleId, note: string) => void;
  patchPayload: (moduleId: ModuleId, mutate: (v: Variant) => Variant) => void;
  reset: (brief?: Brief) => void;
  activeVariant: (moduleId: ModuleId) => Variant | null;
  approvedCount: number;
}

const StoreContext = createContext<StoreValue | null>(null);

export function StoreProvider({ children }: { children: ReactNode }) {
  const [project, dispatch] = useReducer(reducer, undefined, () => newProject());
  const [ready, markReady] = useReducer(() => true, false);

  // Load once on the client. Server render always starts from the sample so
  // markup matches; the stored project swaps in immediately after mount.
  useEffect(() => {
    try {
      const raw = window.localStorage.getItem(STORAGE_KEY);
      if (raw) {
        const parsed = JSON.parse(raw) as Project;
        if (parsed?.brief && parsed?.modules) {
          // Tolerate projects saved before a module was added.
          const modules = { ...emptyModules(), ...parsed.modules };
          dispatch({ type: "hydrate", project: { ...parsed, modules } });
        }
      }
    } catch {
      // A corrupt or unreadable store should never stop the studio opening.
    }
    markReady();
  }, []);

  useEffect(() => {
    if (!ready) return;
    try {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(project));
    } catch {
      // Private browsing or a full quota. Work continues in memory.
    }
  }, [project, ready]);

  const activeVariant = useCallback(
    (moduleId: ModuleId) => {
      const state = project.modules[moduleId];
      if (!state?.activeVariantId) return null;
      return state.variants.find((v) => v.id === state.activeVariantId) ?? null;
    },
    [project],
  );

  const value = useMemo<StoreValue>(
    () => ({
      project,
      ready,
      patchBrief: (patch) => dispatch({ type: "patchBrief", patch }),
      answer: (id, v) => dispatch({ type: "answer", id, value: v }),
      goToStep: (index) => dispatch({ type: "step", index }),
      setBallots: (ballots) => dispatch({ type: "setBallots", ballots }),
      setRevealed: (revealed) => dispatch({ type: "reveal", revealed }),
      roll: (moduleId) => dispatch({ type: "roll", moduleId }),
      chooseVariant: (moduleId, variantId) => dispatch({ type: "chooseVariant", moduleId, variantId }),
      approve: (moduleId) => dispatch({ type: "approve", moduleId }),
      unapprove: (moduleId) => dispatch({ type: "unapprove", moduleId }),
      setNote: (moduleId, note) => dispatch({ type: "note", moduleId, note }),
      patchPayload: (moduleId, mutate) => dispatch({ type: "patchPayload", moduleId, mutate }),
      reset: (brief) => dispatch({ type: "reset", project: newProject(brief ?? EMPTY_BRIEF) }),
      activeVariant,
      approvedCount: MODULE_IDS.filter((id) => project.modules[id]?.status === "approved").length,
    }),
    [project, ready, activeVariant],
  );

  return <StoreContext.Provider value={value}>{children}</StoreContext.Provider>;
}

export function useStudio(): StoreValue {
  const value = useContext(StoreContext);
  if (!value) throw new Error("useStudio must be used inside <StoreProvider>");
  return value;
}
