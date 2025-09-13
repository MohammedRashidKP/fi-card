<script>
  import { goto } from '$app/navigation';
  import { getApiHost, getFrontendHost } from '$lib/url';

  let playerId = '';
  let playerName = '';
  let roomId = '';

  async function handleCreateRoom() {
    const res = await fetch(`${getApiHost()}/rooms`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ playerId, name: playerName })
    });

    if (res.ok) {
      const data = await res.json();
      const joinUrl = `${getFrontendHost()}/room/${data.roomId}?playerId=${playerId}&playerName=${playerName}`;
      location.href = joinUrl;
    } else {
      console.error('Failed to create room', await res.text());
    }
  }

  function handleJoinRoom() {
    const joinUrl = `${getFrontendHost()}/room/${roomId}`;
    goto(joinUrl);
  }
</script>

<h1>Card Game Lobby</h1>

<div>
  <input placeholder="Player Name" bind:value={playerId} />
  <input placeholder="Nick Name" bind:value={playerName} />
  <button on:click={handleCreateRoom}>Create Room</button>
</div>