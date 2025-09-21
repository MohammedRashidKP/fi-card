<script>
  import { createEventDispatcher } from 'svelte';
  import { gameSnapshot } from '$lib/stores';

  export let scoreCard;
  export let roundHistory;
  export let players;

  const dispatch = createEventDispatcher();

  function handleNextRound() {
    dispatch('nextRound');
  }

</script>

<div class="overlay">
  <div class="scorecard">
    <div class="final-score">
      <h3 class="winner-text">🏆 Winner: {scoreCard.winnerId} 🏆</h3>

      {#if scoreCard}
        <ul class="score-list">
        {#if $gameSnapshot.callerId}
          <li class="score-item caller">
            <span class="player-name">Caller</span>
            <span class="player-score">{$gameSnapshot.callerId}-{$gameSnapshot.callValue}</span>
          </li>
          {/if}
          {#if $gameSnapshot.strikerId}
          <li class="score-item striker">
            <span class="player-name">Striker</span>
            <span class="player-score">{$gameSnapshot.strikerId}-{$gameSnapshot.strikeValue}</span>
          </li>
          {/if}
        </ul>

        <ul class="score-list">
          {#each scoreCard.roundScore as row}
            <li class="score-item {row.playerId === scoreCard.winnerId ? 'winner' : ''}">
              <span class="player-name">{row.playerId}</span>
              <span class="player-score">{row.score}</span>
            </li>
          {/each}
        </ul>
      {/if}
    </div>

    {#if roundHistory?.length}
      <h4 class="round-history-title">Round History</h4>
      <div class="table-wrapper">
        <!-- Scrollable rounds -->
        <div class="rounds-scroll">
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
            </tbody>
          </table>
        </div>

        <!-- Totals row always at bottom -->
        <table class="totals-table">
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
        </table>
      </div>
    {/if}

    <button class="next-round-btn" on:click={handleNextRound}>⏭️ Next Round</button>
  </div>
</div>

<style>
/* Full screen overlay */
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.85);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  padding: 1rem;
  box-sizing: border-box;
}

/* Scorecard */
.scorecard {
  background: #1e1e1e;
  color: white;
  border-radius: 12px;
  width: 100%;
  max-width: 700px;
  max-height: 90vh;
  padding: 2rem;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  overflow: hidden;
  box-sizing: border-box;
}

/* Close button */
.close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  border: none;
  background: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
}

/* Winner text */
.final-score {
  text-align: center;
  margin-bottom: 1.5rem;
}

.winner-text {
  font-size: 1.8rem;
  font-weight: bold;
  color: gold;
  text-shadow: 0 0 8px rgba(255, 215, 0, 0.8);
  margin-bottom: 1rem;
}

/* Score list */
.score-list {
  list-style: none;
  padding: 0;
  margin: 0 auto;
  max-width: 100%;
}

.score-item {
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

.score-item.winner {
  background: rgba(255, 215, 0, 0.3);
  color: gold;
  font-weight: bold;
  transform: scale(1.05);
  box-shadow: 0 0 10px rgba(255, 215, 0, 0.6);
}

.player-name { flex: 1; text-align: left; }
.player-score { flex: 0; font-weight: bold; }

.table-wrapper {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.rounds-scroll {
  max-height: 200px; /* adjust */
  overflow-y: auto;
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

th, td {
  border: 1px solid #555;
  padding: 6px;
  text-align: center;
}

.totals-table {
  margin-top: 4px;
  background: rgba(255,215,0,0.2);
  font-weight: bold;
}
tr:nth-child(even) { background: rgba(255,255,255,0.05); }
.totals-row { background: rgba(255,215,0,0.2); font-weight: bold; }

/* Next round button fixed at bottom */
.next-round-btn {
  margin-top: auto; /* pushes it to the bottom */
  padding: 12px 24px;
  border-radius: 8px;
  border: none;
  font-size: 1rem;
  background: #28a745;
  color: white;
  cursor: pointer;
  align-self: center;
}

.next-round-btn:hover { background: #218838; }

/* Round history heading */
.round-history-title {
  margin-top: 1.5rem;
  text-align: center;
  font-size: 1.2rem;
  text-decoration: underline;
}

/* Responsive adjustments */
@media (max-width: 500px) {
  .scorecard { padding: 1rem; }
  .winner-text { font-size: 1.4rem; }
  .score-item { font-size: 1rem; padding: 6px 8px; }
  table { font-size: 0.8rem; }
  th, td { padding: 3px 4px; }
  .next-round-btn { font-size: 0.9rem; padding: 10px 20px; }
}
.score-item.caller {
  background: rgba(0, 123, 255, 0.25);
  color: #4da3ff;
  font-weight: bold;
}

.score-item.striker {
  background: rgba(255, 50, 50, 0.25);
  color: #ff6666;
  font-weight: bold;
}
</style>
