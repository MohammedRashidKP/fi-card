import { writable } from "svelte/store";

const soundMap = {
  PLAYER_TURN: "/sounds/turn.mp3",
  CALL: "/sounds/call.mp3",
  STRIKE: "/sounds/strike.mp3",
  CALLER_WIN: "/sounds/caller-win.mp3",
  STRIKER_WIN: "/sounds/striker-win.mp3"
};

// Store for currently playing sound
export const currentSound = writable(null);

// Cache for loaded Audio objects
let audios = {};

function getAudio(cue) {
  if (typeof window === "undefined" || typeof Audio === "undefined") return null;

  if (!audios[cue] && soundMap[cue]) {
    audios[cue] = new Audio(soundMap[cue]);
  }
  return audios[cue];
}

export function playSound(cue, { volume = 1.0, loop = false } = {}) {
  const audio = getAudio(cue);
  if (!audio) return;

  audio.pause();
  audio.currentTime = 0;
  audio.volume = volume;
  audio.loop = loop;
  audio.play();

  currentSound.set({ cue, playing: true });
}

export function stopSound(cue) {
  const audio = getAudio(cue);
  if (!audio) return;

  audio.pause();
  audio.currentTime = 0;

  currentSound.set({ cue, playing: false });
}
