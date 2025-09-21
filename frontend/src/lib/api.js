import { gameSnapshot, handInfo, debugLog, wsConnection, hasDiscarded, chatMessages } from '$lib/stores';
import { get, writable } from 'svelte/store';
import { getWebSocketHost } from '$lib/url';
import { currentCue } from "$lib/stores";


let socket;
export const gameResult = writable(null);

export function connectWebSocket(roomId, playerId, playerName) {
  if (socket) {
    console.warn("WebSocket already open, closing existing connection...");
    socket.close();
  }
  const host = location.hostname;
  socket = new WebSocket(
           `${getWebSocketHost()}/game?roomId=${roomId}&playerId=${playerId}&playerName=${playerName}`
         );
  wsConnection.set(socket);

  socket.onopen = () => {
    console.log("Connected to WebSocket");
    socket.send(JSON.stringify({
      action: "joinRoom",
      roomId,
      playerId,
      name: playerName
    }));
  };

  socket.onmessage = (event) => {
    const text = event.data;
    let msg;

    try {
      msg = JSON.parse(text);
    } catch {
      debugLog.update(d => `${d}\n[TEXT] ${text}`);
      return;
    }

    debugLog.update(d => `${d}\n[JSON] ${JSON.stringify(msg)}`);

    // --- GameSnapshot updates ---
    if (msg?.state && msg?.players) {
      gameSnapshot.update(prev => ({
        ...prev,
        ...msg,
        players: msg.players
      }));

            // If game finished → capture result in separate store
            if (msg.state === "FINISHED") {
              gameResult.set({
                scorecard: msg.scorecard ?? [],
                winnerId: msg.winnerId,
                state: "FINISHED"
              });
            } else {
              gameResult.set(null); // clear when game is ongoing
            }

      return;
    }

    // --- HandInfo (private to player) ---
if (msg?.hand) {
  const prev = get(handInfo);
const normalizedHand = (msg.hand || []).map(c => ({
  ...c,
  isJoker: c.rank === null || c.suit === null || c.jokerColor != null
}));
  handInfo.set({
    ...prev,
    ...msg,
    hand: normalizedHand,
    openCard: "openCard" in msg ? msg.openCard : null, // force overwrite
    canCall: msg.hasOwnProperty("canCall") ? msg.canCall : false,
    canStrike: msg.hasOwnProperty("canStrike") ? msg.canStrike : false // force overwrite
  });
  return;
}

  // --- SoundCue ---
if (msg?.cue) {
  currentCue.set(msg); // whole object {cue, volume, loop}
   return;
}
if (msg?.message) {

  chatMessages.update(messages => [...messages, msg]);
   return;
}
    console.warn("Unknown WS message", msg);
  };

  socket.onclose = () => {
    console.log("WebSocket closed");
    wsConnection.set(null);
  };

  socket.onerror = (err) => {
    console.error("WebSocket error", err);
  };
}

export function sendMessage(message) {
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send(JSON.stringify(message));
  } else {
    console.warn("WebSocket not connected, cannot send", message);
  }
}

// --- Debug subscriptions ---
handInfo.subscribe(value => {
  console.log('handInfo changed:', value);
});

gameSnapshot.subscribe(value => {
  console.log('gameSnapshot changed:', value);
});

// --- Turn logic: reset hasDiscarded at start of this player's turn ---
let prevPlayerId = null;

gameSnapshot.subscribe(snapshot => {
  if (!snapshot.currentPlayerId) return;

  const myId = get(handInfo).playerId;
  const currentId = snapshot.currentPlayerId;

  // Reset only when **this player’s turn starts**
  if (currentId === myId && prevPlayerId !== myId) {
    console.log("New turn for me → resetting hasDiscarded");
    hasDiscarded.set(false);
  }

  prevPlayerId = currentId;
});

// At the bottom of the file
if (typeof window !== 'undefined') {
  window.sendMessage = sendMessage;
}