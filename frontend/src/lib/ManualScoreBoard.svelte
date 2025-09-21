<script>
  import { createEventDispatcher } from 'svelte';
  export let scoreCard;
  export let roundHistory;
  export let globalLeaderBoard;
  export let players;

  const dispatch = createEventDispatcher();
  let view = 'roundHistory';

  function closeBoard() {
    dispatch('close');
  }
</script>

<div class="overlay">
  <div class="scorecard">
    <button class="close-btn" on:click={closeBoard}>✖</button>
<div class="buttons">
  <button class:active={view==='roundHistory'} on:click={() => view='roundHistory'}>
    Round History
  </button>
  <button class:active={view==='globalLeaderBoard'} on:click={() => view='globalLeaderBoard'}>
    Global Leaderboard
  </button>
</div>

    {#if view === 'roundHistory' && roundHistory?.length}
      <table>
        <thead>
          <tr>
            <th>Round</th>
            {#each players as p}<th>{p.id}</th>{/each}
          </tr>
        </thead>
        <tbody>
          {#each roundHistory as round}
            <tr>
              <td>{round.roundNumber}</td>
              {#each players as p}
                <td>{round.scores.find(s => s.playerId === p.id)?.score ?? 0}</td>
              {/each}
            </tr>
          {/each}
          <!-- Totals row -->
                           <tr>
                                   <td><strong>Total</strong></td>
                                   {#each players as player}
                                     <td>
                                       <strong>
                                         {roundHistory.reduce(
                                           (sum, round) => sum + (round.scores.find(s => s.playerId === player.id)?.score ?? 0),
                                           0
                                         )}
                                       </strong>
                                     </td>
                                   {/each}
                                 </tr>
        </tbody>
      </table>
    {/if}

{#if view === 'globalLeaderBoard' && globalLeaderBoard?.length}
  <div class="leaderboard-container">
    <h3>Global Leaderboard</h3>
    <ul class="leaderboard-list">
      {#each globalLeaderBoard as p, i}
        <li class="leaderboard-item {i === 0 ? 'top-player' : ''}">
          <span class="player-name">{p.playerId}</span>
          <span class="player-points">{p.points}</span>
        </li>
      {/each}
    </ul>
  </div>
{/if}
  </div>
</div>

<style>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.85);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}
.scorecard {
  background: #1e1e1e;
  color: white;
  padding: 2rem;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  position: relative;

  /* NEW: restrict height to viewport and allow scroll */
  max-height: 90vh;
  overflow-y: auto;
  box-sizing: border-box;
}
.close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  border: none;
  background: none;
  color: white;
  font-size: 1.3rem;
  cursor: pointer;
}
.buttons {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 4px;
  margin: 1rem 0;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.buttons button {
  flex: 1;
  padding: 0.6rem 1rem;
  border: none;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.2);
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.buttons button.active {
  background: #28a745; /* active color */
  color: white;
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
}

.buttons button:hover:not(.active) {
  background: rgba(0,0,0,0.35);
}
table {
  width: 100%;
  border-collapse: collapse;
}
th, td {
  border: 1px solid #555;
  padding: 6px;
  text-align: center;
}
tr:nth-child(even) {
  background: rgba(255,255,255,0.05);
}
ul {
  margin-top: 1rem;
  list-style: none;
  padding: 0;
}

.leaderboard-container {
  text-align: center;
  margin-top: 1rem;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.leaderboard-container h3 {
  font-size: 1.8rem;
  font-weight: bold;
  color: #ffd700;
  text-shadow: 0 0 8px rgba(255, 215, 0, 0.8);
  margin-bottom: 1rem;
}

.leaderboard-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.leaderboard-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  padding: 8px 12px;
  margin: 6px 0;
  font-size: 1.2rem;
  transition: transform 0.2s, background 0.2s;
}

.leaderboard-item.top-player {
  background: rgba(255, 215, 0, 0.3);
  color: gold;
  font-weight: bold;
  transform: scale(1.05);
  box-shadow: 0 0 10px rgba(255, 215, 0, 0.6);
}

.player-name {
  flex: 1;
  text-align: left;
}

.player-points {
  flex: 0;
  font-weight: bold;
}
</style>
