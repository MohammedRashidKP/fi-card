<script>
  import { gameSnapshot } from '$lib/stores';
  import { derived } from 'svelte/store';

  // Derived store to show message only if state === "CALLED"
  const activeCall = derived(gameSnapshot, $snapshot => {
    if ($snapshot?.state === "CALLED" && $snapshot?.callerId) {
      const caller = $snapshot.players.find(p => p.id === $snapshot.callerId);
      const callValue = $snapshot?.callValue
      return caller ? `${caller.name} called ${callValue}` : null;
    }
    return null;
  });

  let message = null;
  $: message = $activeCall;
</script>

{#if message}
  <div class="call-indicator">
    {message}
  </div>
{/if}

<style>
.call-indicator {
  position: fixed;
  top: 10%;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 165, 0, 0.9);
  color: black;
  padding: 12px 20px;
  border-radius: 10px;
  font-weight: bold;
  font-size: 1.2rem;
  text-align: center;
  z-index: 3000;
  box-shadow: 0 0 15px rgba(0,0,0,0.4);
  animation: blink 1s step-start 0s infinite;
}

@keyframes blink {
  50% { opacity: 0; }
}
</style>
