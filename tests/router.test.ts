import { test } from "node:test";
import assert from "node:assert/strict";

/**
 * The standalone build routes on the URL hash, and every component that reads
 * the route subscribes independently. Shell subscribes for the whole session;
 * the studio page subscribes only while it is mounted.
 *
 * That makes unsubscribe the dangerous path: the window listener is shared, so
 * one component going away must not take routing down for the components that
 * are still mounted. Leaving the studio used to do exactly that, and every
 * later navigation changed the URL while the page stayed put.
 */

type Handler = () => void;

/** Minimal window stand-in with the real addEventListener dedupe semantics. */
function fakeWindow() {
  const registered: Handler[] = [];
  return {
    location: { hash: "#/" },
    addEventListener(type: string, fn: Handler) {
      if (type !== "hashchange") return;
      if (registered.includes(fn)) return; // the browser dedupes identical registrations
      registered.push(fn);
    },
    removeEventListener(type: string, fn: Handler) {
      if (type !== "hashchange") return;
      const i = registered.indexOf(fn);
      if (i !== -1) registered.splice(i, 1);
    },
    dispatchHashChange() {
      for (const fn of [...registered]) fn();
    },
    get listenerCount() {
      return registered.length;
    },
  };
}

async function loadRouter() {
  const win = fakeWindow();
  (globalThis as Record<string, unknown>).window = win;
  // Fresh module instance per test — the listener set is module state.
  const mod = await import(`../src/standalone/router.tsx?t=${Math.random()}`);
  return { win, ...mod };
}

test("a still-mounted subscriber keeps receiving hash changes after another unsubscribes", async () => {
  const { win, subscribe } = await loadRouter();

  let shellNotified = 0;
  const unsubShell = subscribe(() => shellNotified++);

  let studioNotified = 0;
  const unsubStudio = subscribe(() => studioNotified++);

  win.dispatchHashChange();
  assert.equal(shellNotified, 1, "shell should hear the first hash change");
  assert.equal(studioNotified, 1, "studio should hear the first hash change");

  // Navigating away from /studio unmounts the studio page.
  unsubStudio();

  win.dispatchHashChange();
  assert.equal(
    shellNotified,
    2,
    "shell is still mounted, so it must still hear hash changes after the studio unmounts",
  );
  assert.equal(studioNotified, 1, "the unsubscribed studio must not be notified again");

  unsubShell();
});

test("the window listener is released once the last subscriber goes away", async () => {
  const { win, subscribe } = await loadRouter();

  const unsubA = subscribe(() => {});
  const unsubB = subscribe(() => {});
  assert.equal(win.listenerCount, 1, "one shared window listener serves every subscriber");

  unsubA();
  assert.equal(win.listenerCount, 1, "a subscriber remains, so the listener must stay");

  unsubB();
  assert.equal(win.listenerCount, 0, "no subscribers left, so nothing should stay attached");
});

test("resubscribing after a full teardown reattaches the window listener", async () => {
  const { win, subscribe } = await loadRouter();

  subscribe(() => {})();
  assert.equal(win.listenerCount, 0);

  let notified = 0;
  subscribe(() => notified++);
  assert.equal(win.listenerCount, 1, "a new subscriber must reattach the listener");

  win.dispatchHashChange();
  assert.equal(notified, 1, "the reattached listener must deliver hash changes");
});
