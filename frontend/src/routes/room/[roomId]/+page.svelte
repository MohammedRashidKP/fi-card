<script>
  import { page } from '$app/stores';
  import { onMount } from 'svelte';
  import { gameSnapshot, handInfo } from '$lib/stores';
  import { connectWebSocket } from '$lib/api';
  import GameTable from '$lib/GameTable.svelte';

  let roomId;
  let playerId = '';
  let playerName = '';
  let copied = false;
  let joinUrl = '';

  onMount(() => {
    roomId = $page.params.roomId;
    const query = $page.url.searchParams;
    playerId = query.get('playerId') || '';
    playerName = query.get('playerName') || '';

    // Recreate join URL dynamically
    joinUrl = `${location.origin}/room/${roomId}`;

    if (playerId && playerName) {
      connectWebSocket(roomId, playerId, playerName);
    }
  });

  function handleJoinForm(e) {
    e.preventDefault();
    const pid = document.getElementById('pid').value;
    const pname = document.getElementById('pname').value;
    location.href = `/room/${roomId}?playerId=${encodeURIComponent(pid)}&playerName=${encodeURIComponent(pname)}`;
  }

  async function copyJoinUrl() {
    if (joinUrl) {
      await navigator.clipboard.writeText(joinUrl);
      copied = true;
      setTimeout(() => (copied = false), 2000);
    }
  }

  async function handleStartGame() {
    try {
      const response = await fetch(`/api/rooms/${roomId}/start`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });

      if (!response.ok) {
        console.error('Failed to start game', await response.text());
      } else {
        console.log('Game started successfully');
      }
    } catch (err) {
      console.error('Error starting game', err);
    }
  }
</script>

{#if !playerId || !playerName}
  <div class="join-form">
    <h2>Join Room {roomId}</h2>
    <form on:submit={handleJoinForm}>
      <input id="pid" placeholder="Enter Player ID" required />
      <input id="pname" placeholder="Enter Player Name" required />
      <button type="submit">Join</button>
    </form>
  </div>
{:else}
  {#if $gameSnapshot?.state === 'LOBBY'}
    <div class="lobby">
      <h1 class="room-title">Room: {roomId}</h1>

      <!-- Join URL -->
      <div class="join-url">
        <span>{joinUrl}</span>
        <button on:click={copyJoinUrl}>
          {#if copied} ✅ Copied! {:else} Copy Link {/if}
        </button>
      </div>

      <!-- Player Cards -->
      <div class="players">
        {#each $gameSnapshot.players as player}
          <div class="player-card">
            <div class="avatar">👤</div>
            <div class="player-info">
              <p class="name">{player.name}</p>
              <p class="id">ID: {player.id}</p>
            </div>
          </div>
        {/each}
      </div>

      <!-- Start Button -->
      {#if $gameSnapshot.players.length > 0 && $gameSnapshot.players[0].id === playerId}
        <div class="start-container">
          <button class="start-btn" on:click={handleStartGame}>
            🚀 Start Game
          </button>
        </div>
      {/if}
    </div>
  {:else}
    <GameTable />
  {/if}
{/if}

<style>
  body {
    margin: 0;
    font-family: sans-serif;
  }

  .lobby {
    background: #f0f0f0;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 2rem;
  }

  .room-title {
    margin: 1rem 0;
    font-size: 2rem;
    text-align: center;
    color: #333;
  }

  .join-url {
    display: flex;
    gap: 1rem;
    margin-bottom: 2rem;
    background: white;
    padding: 0.8rem 1rem;
    border-radius: 8px;
    box-shadow: 0 2px 6px rgba(0,0,0,0.1);
  }

  .join-url span {
    font-family: monospace;
    font-size: 0.9rem;
  }

  .join-url button {
    background: #007bff;
    color: white;
    border: none;
    padding: 0.4rem 0.8rem;
    border-radius: 6px;
    cursor: pointer;
  }

.players {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 1.5rem; /* spacing before button */
}

  .player-card {
    background: white;
    width: 160px;
    padding: 1rem;
    border-radius: 12px;
    box-shadow: 0 4px 10px rgba(0,0,0,0.1);
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .avatar {
    font-size: 2rem;
    margin-bottom: 0.5rem;
  }

  .player-info {
    text-align: center;
  }

  .player-info .name {
    font-weight: bold;
    margin-bottom: 0.2rem;
  }

  .player-info .id {
    font-size: 0.85rem;
    color: #666;
  }

.start-container {
  display: flex;
  justify-content: center;
  margin-top: 1rem;
}

.start-btn {
  padding: 0.8rem 1.5rem;
  background: gold;
  border: none;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0,0,0,0.15);
}

.start-btn:hover {
  background: orange;
}

  /* Join form styling */
  .join-form {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 3rem;
    background: #f0f0f0;
    min-height: 100vh;
  }

  .join-form form {
    display: flex;
    flex-direction: column;
    gap: 0.8rem;
    background: white;
    padding: 2rem;
    border-radius: 12px;
    box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  }

  .join-form input {
    padding: 0.6rem;
    border: 1px solid #ccc;
    border-radius: 6px;
  }

  .join-form button {
    background: #007bff;
    color: white;
    border: none;
    padding: 0.6rem;
    border-radius: 6px;
    cursor: pointer;
  }

  .join-form button:hover {
    background: #0056b3;
  }
</style>
