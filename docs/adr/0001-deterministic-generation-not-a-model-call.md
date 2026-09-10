# Drafts are generated deterministically, not by a model call

Every round is a pure function of `(brief, seed)` over curated corpora, with no
LLM in the loop. We wanted a studio that runs offline, needs no key, costs
nothing per round, and produces the identical draft on a colleague's machine
from the same project file — none of which survives a model call. The price is
that specificity has to come from the client's own workshop answers rather than
from a model's fluency, so a thin brief produces visibly thin work; the app
says so on screen rather than papering over it.

## Consequences

Adding a deliverable means writing a corpus, not a prompt. Refresh is "advance
the round", so variety is bounded by how much the corpora carry — the tests
assert at least four distinct rounds in eight for every deliverable, which is
the floor that keeps Refresh honest.
