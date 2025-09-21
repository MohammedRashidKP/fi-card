import { writable, derived } from 'svelte/store';
export const wsConnection = writable(null); // optional store to hold WS object

// Debug log to show raw messages
export const debugLog = writable('');

export const gameSnapshot = writable({
  roomId: null,
  state: null,               // "LOBBY" | "IN_PROGRESS"
  players: [],               // [{id, name, cardCount}]
  currentPlayerId: null,
  currentPlayerName: null,
  currentDealerId: null,
  currentDealerName: null,
  jokerCard: null,
  openCard: null,
  deckCount: 0,
  callerId: null,
  callValue: null,
  strikerId: null,
  strikeValue: null,
  scoreCard: null,
  roundHistory: null,
  globalLeaderBoard: null
});

export const handInfo = writable({
  playerId: null,
  hand: [],
  openCard: null,
  canCall: false,
  canStrike: false
});

export const soundMap = {
  PLAYER_TURN: "/sounds/notification.wav",
  CALL: "/sounds/call_sound.mp3",
  STRIKE: "/sounds/strike_sound.mp3",
  CALLER_WIN: "/sounds/caller_win.mp3",
  STRIKER_WIN: "/sounds/striker_win.mp3"
};
export const currentCue = writable(null);

export const selectedCards = writable([]);
export const discardedCards = writable([]); // for animation
export const hasDiscarded = writable(false);
export const chatMessages = writable([]);
export const showScores = writable(false); // controls panel visibility
export const isMyTurn = derived(
  [gameSnapshot, handInfo, hasDiscarded],
  ([$gameSnapshot, $handInfo, $hasDiscarded]) => {
    return (
      $gameSnapshot.currentPlayerId === $handInfo.playerId &&
      !$hasDiscarded
    );
  }
);

export const playerInfo = writable({
  id: null,
  name: null,
  roomId: null
});