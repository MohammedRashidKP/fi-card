import { writable } from "svelte/store";

export const localStream = writable(null);
export const remoteStreams = writable({}); // { playerId: MediaStream }

const peers = {}; // { playerId: RTCPeerConnection }

export async function initLocalStream() {
  const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
  localStream.set(stream);
  return stream;
}

export function createPeer(targetId, myId, roomId, ws) {
  if (peers[targetId]) return peers[targetId];

  const pc = new RTCPeerConnection({
    iceServers: [
      { urls: "stun:stun.l.google.com:19302" } // free public STUN
    ]
  });

  // Attach local tracks
  localStream.subscribe(stream => {
    if (stream) {
      stream.getTracks().forEach(track => pc.addTrack(track, stream));
    }
  });

  // Handle remote tracks
  pc.ontrack = (event) => {
    remoteStreams.update(r => {
      r[targetId] = event.streams[0];
      return { ...r };
    });
  };

  // ICE candidates → send to target
  pc.onicecandidate = (event) => {
    if (event.candidate) {
      ws.send(JSON.stringify({
        action: "webrtc-ice",
        roomId,
        playerId: myId,
        targetId,
        payload: event.candidate
      }));
    }
  };

  peers[targetId] = pc;
  return pc;
}

export async function makeOffer(targetId, myId, roomId, ws) {
  const pc = createPeer(targetId, myId, roomId, ws);
  const offer = await pc.createOffer();
  await pc.setLocalDescription(offer);

  ws.send(JSON.stringify({
    action: "webrtc-offer",
    roomId,
    playerId: myId,
    targetId,
    payload: offer
  }));
}

export async function handleSignal(msg, myId, roomId, ws) {
  const { action, playerId, targetId, payload } = msg;
  if (playerId === myId) return; // ignore own

  if (action === "webrtc-offer" && targetId === myId) {
    const pc = createPeer(playerId, myId, roomId, ws);
    await pc.setRemoteDescription(payload);
    const answer = await pc.createAnswer();
    await pc.setLocalDescription(answer);
    ws.send(JSON.stringify({
      action: "webrtc-answer",
      roomId,
      playerId: myId,
      targetId: playerId,
      payload: answer
    }));
  } else if (action === "webrtc-answer" && targetId === myId) {
    const pc = createPeer(playerId, myId, roomId, ws);
    await pc.setRemoteDescription(payload);
  } else if (action === "webrtc-ice" && targetId === myId) {
    const pc = createPeer(playerId, myId, roomId, ws);
    pc.addIceCandidate(new RTCIceCandidate(payload));
  }
}
