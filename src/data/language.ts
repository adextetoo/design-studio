/**
 * Language corpus.
 *
 * Everything here is written to be read aloud. Short sentences, concrete
 * nouns, no consultancy vocabulary — the generator's job is to arrange these
 * around the client's own answers, not to sound clever on top of them.
 */

export interface Archetype {
  name: string;
  pairsWith: string[];
  behaves: string;
  says: string;
  avoids: string;
  traits: string[];
  peers: string[];
}

export const ARCHETYPES: Archetype[] = [
  { name: "The Sage", pairsWith: ["The Magician", "The Ruler", "The Creator"], behaves: "Explains before it sells. Would rather be right than quick.", says: "Here is what we found, and here is what it means for you.", avoids: "Hype, urgency countdowns, anything that requires you not to think.", traits: ["Strategic", "Trusted", "Precise", "Transparent", "Proven"], peers: ["Bloomberg", "The Economist", "Ōura"] },
  { name: "The Magician", pairsWith: ["The Sage", "The Creator", "The Explorer"], behaves: "Makes the difficult thing look effortless, then shows the working.", says: "You do not need to understand it. You do need to see it work.", avoids: "Feature lists, spec sheets, explaining the trick badly.", traits: ["Innovative", "Visionary", "Future-proof", "Disruptive", "Empowering"], peers: ["Apple", "Dyson", "Figma"] },
  { name: "The Ruler", pairsWith: ["The Sage", "The Creator"], behaves: "Sets the standard and holds it, publicly, even when it costs something.", says: "This is how it should be done. We will show you.", avoids: "Discounting, apologising for the price, chasing trends.", traits: ["Premium", "Refined", "Proven", "Systematic", "Trusted"], peers: ["Rolex", "Mercedes-Benz", "Aman"] },
  { name: "The Creator", pairsWith: ["The Magician", "The Ruler", "The Jester"], behaves: "Builds the thing it wishes existed, then shares how it was made.", says: "We made this because nothing else was good enough.", avoids: "Committee language, safe choices, borrowed style.", traits: ["Creative", "Innovative", "Bold", "Adventurous", "Refined"], peers: ["Lego", "Adobe", "Aesop"] },
  { name: "The Caregiver", pairsWith: ["The Everyman", "The Sage"], behaves: "Notices what is difficult and quietly removes it.", says: "We have got this bit. You do the part only you can do.", avoids: "Pity, condescension, over-promising on outcomes.", traits: ["Warm", "Human", "Trusted", "Approachable", "Purpose-driven"], peers: ["Johnson & Johnson", "Bupa", "Headspace"] },
  { name: "The Everyman", pairsWith: ["The Caregiver", "The Jester"], behaves: "Talks like a neighbour, prices like a friend, never performs status.", says: "No tricks. Here is what it costs and what you get.", avoids: "Exclusivity, jargon, anything that makes someone feel outside.", traits: ["Approachable", "Transparent", "Human", "Authentic", "Collaborative"], peers: ["IKEA", "Monzo", "Greggs"] },
  { name: "The Explorer", pairsWith: ["The Magician", "The Outlaw"], behaves: "Goes first, reports back honestly, including when it did not work.", says: "We went and looked. Here is what is actually out there.", avoids: "Comfort, routine, pretending the map is finished.", traits: ["Adventurous", "Bold", "Resilient", "Optimistic", "Scrappy"], peers: ["Patagonia", "Jeep", "Airbnb"] },
  { name: "The Outlaw", pairsWith: ["The Explorer", "The Jester"], behaves: "Names the thing the category will not name, then does the opposite.", says: "The way this industry works is broken. We are not doing it.", avoids: "Consensus, permission, being liked by the incumbents.", traits: ["Disruptive", "Provocative", "Relentless", "Bold", "Scrappy"], peers: ["Oatly", "Liquid Death", "BrewDog"] },
  { name: "The Jester", pairsWith: ["The Everyman", "The Outlaw", "The Creator"], behaves: "Refuses to be boring about a boring category, and gets away with it.", says: "This should not be this miserable. Watch.", avoids: "Solemnity, self-importance, jokes that punch downward.", traits: ["Playful", "Optimistic", "Energetic", "Creative", "Human"], peers: ["Innocent", "Mailchimp", "Duolingo"] },
  { name: "The Lover", pairsWith: ["The Creator", "The Ruler"], behaves: "Treats craft and sensation as the product, not the packaging.", says: "You will feel the difference before you can explain it.", avoids: "Specifications, comparison tables, speed as a virtue.", traits: ["Luxurious", "Refined", "Warm", "Premium", "Creative"], peers: ["Diptyque", "Le Labo", "Loewe"] },
  { name: "The Hero", pairsWith: ["The Ruler", "The Explorer"], behaves: "Sets a hard target in public and does the work in front of you.", says: "Here is the standard. Here is us hitting it.", avoids: "Excuses, soft targets, hiding the numbers.", traits: ["Relentless", "Bold", "Resilient", "Proven", "Empowering"], peers: ["Nike", "Strava", "Whoop"] },
  { name: "The Innocent", pairsWith: ["The Everyman", "The Caregiver"], behaves: "Keeps it simple on purpose and never adds a step for its own sake.", says: "One thing, done properly, with nothing hidden.", avoids: "Complexity, small print, clever pricing.", traits: ["Minimal", "Transparent", "Optimistic", "Human", "Authentic"], peers: ["Muji", "Method", "Tony's Chocolonely"] },
];

export interface VoicePillar {
  name: string;
  claim: string;
  explain: string;
  doThis: string;
  notThis: string;
  traits: string[];
}

export const VOICE_PILLARS: VoicePillar[] = [
  { name: "Direct", claim: "Clear but not cold, sharp but not rude, simple but not dumbed down.", explain: "Direct means putting the point in the first sentence. If a paragraph can be a line, make it a line. We do not warm up before we say the thing.", doThis: "Your money is safe. Here is where it is held.", notThis: "We are committed to ensuring the security of your assets at all times.", traits: ["Bold", "Transparent", "Precise", "Minimal"] },
  { name: "Confident", claim: "Certain about the work, honest about the limits.", explain: "Confidence is knowing the answer and not needing to decorate it. It never becomes bragging, because we say the parts we are unsure about too.", doThis: "This takes eight weeks. It cannot be done well in four.", notThis: "We can turn this around in whatever timeframe works for you.", traits: ["Proven", "Strategic", "Premium", "Relentless"] },
  { name: "Warm", claim: "Human but not soft, friendly but not familiar.", explain: "Warmth is remembering there is a person on the other end who has had a day. It sounds like respect, not like a mascot.", doThis: "That is a fair question, and the honest answer is no.", notThis: "Hey there! Great question!! We love your energy 🙌", traits: ["Warm", "Human", "Approachable", "Collaborative"] },
  { name: "Plain", claim: "Ordinary words, used precisely.", explain: "We use the word a customer would use. If an industry term is unavoidable we explain it once, in the same sentence, and never again.", doThis: "A pension is money your employer put aside for you.", notThis: "A defined contribution vehicle accrues on a tax-advantaged basis.", traits: ["Transparent", "Human", "Trusted", "Minimal"] },
  { name: "Specific", claim: "Numbers, names and dates instead of adjectives.", explain: "One real detail does more than three claims. If we cannot say how much, how long, or how many, we do not make the claim.", doThis: "Four hundred and twelve people moved across last month.", notThis: "Thousands of happy customers have made the switch.", traits: ["Precise", "Proven", "Systematic", "Strategic"] },
  { name: "Steady", claim: "The same voice on the good day and the bad one.", explain: "When something breaks we write shorter, not vaguer. The tone does not change, the amount of detail goes up.", doThis: "It has been down for nine minutes. Two engineers are on it.", notThis: "We are experiencing some issues and are working hard to resolve them.", traits: ["Resilient", "Trusted", "Proven", "Systematic"] },
  { name: "Curious", claim: "Asks first, then answers.", explain: "We start from the customer's situation rather than our own capability. Questions in our writing are real questions, not rhetorical setups.", doThis: "What are you actually trying to protect here?", notThis: "Are you ready to transform the way you think about protection?", traits: ["Collaborative", "Human", "Creative", "Partner-driven"] },
  { name: "Sharp", claim: "Cuts through, never decorative.", explain: "Every word earns its place. Nothing vague, nothing hedged, no sentence that exists to sound impressive.", doThis: "It costs £40 a month. There is no tie-in.", notThis: "Our flexible pricing is designed to suit a range of needs.", traits: ["Precise", "Minimal", "Bold", "Refined"] },
  { name: "Playful", claim: "Light on its feet, serious about the work.", explain: "The joke is never at the customer's expense and never in the way of the information. Wit lives in the headline; the detail stays straight.", doThis: "Four pensions, one screen, no spreadsheet in sight.", notThis: "Pensions? Boring! Not anymore!!", traits: ["Playful", "Optimistic", "Energetic", "Creative"] },
  { name: "Generous", claim: "Gives the answer away.", explain: "We publish the thing a competitor would gate. If someone can solve it without us, we tell them how.", doThis: "If you are under forty, you probably do not need us yet.", notThis: "Book a call to find out whether you qualify.", traits: ["Transparent", "Purpose-driven", "Human", "Partner-driven"] },
];

/** Openers for the origin story. Each one starts on a person or a moment. */
export const STORY_OPENERS = [
  "{name} started with a problem nobody wanted to sit with.",
  "The first version of {name} was a favour for one person.",
  "Before there was a company, there was an argument that would not go away.",
  "{name} began in {location}, in the ordinary way — someone got tired of the workaround.",
  "There is a version of this story with a whiteboard in it. This is not that version.",
  "The idea for {name} arrived late, in the middle of doing something else.",
];

export const STORY_TURNS = [
  "The workaround kept working, which was the surprising part.",
  "What started as a favour turned into a waiting list.",
  "Word travelled the slow way — one person telling another over lunch.",
  "The second customer arrived before there was anything to sell them.",
  "Nothing about it scaled, and it kept growing anyway.",
  "It took a year to admit this was the business.",
];

export const STORY_LANDINGS = [
  "That is still the whole idea. Everything since has been an attempt not to ruin it.",
  "The company has grown. The reason has not moved.",
  "Nothing about the ambition has changed. The scale has.",
  "It is the same promise, made to more people, more carefully.",
  "That first conversation is still the standard everything gets held against.",
];

export const MISSION_SHAPES = [
  "{verb} {object}.",
  "{verb} {object}, and make it obvious.",
  "{verb} {object} — for people who were never given a straight answer.",
];

export const MISSION_VERBS = [
  "Make", "Give", "Take", "Put", "Open up", "Settle", "Hand back", "Rebuild",
];

export const VISION_SHAPES = [
  "A world where {claim}.",
  "{claim} — everywhere, as standard, without anyone having to ask.",
  "The day {claim} is the day this is finished.",
];

/** Section kickers used across the website copy and marketing deliverables. */
export const KICKERS = [
  "How it works", "Why it exists", "What you get", "Where it fits",
  "The short version", "In practice", "What we will not do", "Who it is for",
];

export const CTA_VERBS = [
  "See how it works", "Start here", "Talk to a person", "Get the numbers",
  "Book twenty minutes", "Try it on one account", "Read the detail",
];

export const MOODBOARD_DIRECTIONS = [
  { name: "The Confident", adjectives: ["Warm", "Cinematic", "Intimate"], description: "Low light, long lenses, one person in frame. Type sits quietly in the corner and lets the picture carry the feeling.", surfaces: ["Uncoated stock", "Deep matte panels", "Soft-focus photography"], photography: "Portraits at golden hour, shot close, always someone real doing something ordinary.", motion: "Slow. Nothing cuts on the beat. Fades rather than wipes." },
  { name: "The Architect", adjectives: ["Dynamic", "Scientific", "Precise"], description: "Visible grid, hard edges, data as decoration. Everything aligns to something, and the alignment is the aesthetic.", surfaces: ["Coated white", "Anodised metal", "Screen-first gradients"], photography: "Detail shots, top-down, hard shadow. Objects, not faces.", motion: "Fast and mechanical. Things snap into position on a grid." },
  { name: "The Optimist", adjectives: ["Light", "Elevated", "Pristine"], description: "Air everywhere. Huge margins, small type, one colour doing all the work. Confidence expressed as restraint.", surfaces: ["Bright white", "Frosted glass", "Pale washes"], photography: "Overexposed, airy, plenty of negative space above the subject.", motion: "Almost none. Things appear rather than move." },
  { name: "The Workshop", adjectives: ["Made", "Honest", "Tactile"], description: "Evidence of the hand. Visible texture, real materials, layouts that look set rather than generated.", surfaces: ["Kraft board", "Recycled stock", "Exposed stitching"], photography: "Process shots. Hands, tools, unfinished things on a bench.", motion: "Stop-frame feel. Slightly uneven timing, on purpose." },
  { name: "The Broadcast", adjectives: ["Loud", "Immediate", "Graphic"], description: "Type at billboard scale, colour edge to edge, no gradients. Built to be read from a moving car.", surfaces: ["Vinyl", "Flat colour panels", "Full-bleed print"], photography: "High contrast, cropped tight, subject filling the frame.", motion: "Cut on the beat. Nothing lingers." },
  { name: "The Archive", adjectives: ["Editorial", "Considered", "Enduring"], description: "Runs like a good magazine. Columns, footnotes, real hierarchy, and the patience to let a page be mostly text.", surfaces: ["Book stock", "Cloth binding", "Single-colour foil"], photography: "Documentary. Wide, unposed, captioned properly.", motion: "Page turns. Horizontal, unhurried." },
];
