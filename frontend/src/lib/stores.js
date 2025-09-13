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
  scoreCard: null,
  roundHistory: null,
  globalLeaderBoard: null
});

export const handInfo = writable({
  playerId: null,
  hand: [],
  openCard: null,
  canCall: false
});

export const selectedCards = writable([]);
export const discardedCards = writable([]); // for animation
export const hasDiscarded = writable(false);

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