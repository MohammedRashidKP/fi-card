<script>
  import { onMount } from "svelte";
  import { localStream, remoteStreams, initLocalStream, makeOffer, handleSignal } from "./webrtc.js";

  export let ws;
  export let roomId;
  export let myId;
  export let players = []; // from gameSnapshot

  let localAudio;
  let remotes = {};

  onMount(async () => {
    const stream = await initLocalStream();
    if (localAudio) localAudio.srcObject = stream;

    ws.addEventListener("message", async (ev) => {
      const msg = JSON.parse(ev.data);
      if (msg.action?.startsWith("webrtc-")) {
        await handleSignal(msg, myId, roomId, ws);
      }
    });
  });

  // Start call with all others (mesh)
  function startCall() {
    players.filter(p => p.id !== myId).forEach(p => {
      makeOffer(p.id, myId, roomId, ws);
    });
  }
</script>

<button on:click={startCall}>Start Call</button>

<h3>My Audio</h3>
<audio bind:this={localAudio} autoplay muted></audio>

<h3>Others</h3>
{#each Object.entries($remoteStreams) as [id, stream]}
  <div>
    <span>{id}</span>
    <audio bind:this={remotes[id]} autoplay {stream}></audio>
    <script>
      $: if (remotes[id] && stream) remotes[id].srcObject = stream;
    </script>
  </div>
{/each}
