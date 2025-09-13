<script>
  import { gameSnapshot, handInfo, showScores } from '$lib/stores';
  import { get } from 'svelte/store';
  import { fly, scale } from 'svelte/transition';
  import { createEventDispatcher } from 'svelte';

  let snapshot;

  $: snapshot = $gameSnapshot;

  export let onClose;
  export let scoreCard;
  export let globalLeaderBoard;
  export let roundHistory;

  const dispatch = createEventDispatcher();
  const myId = get(handInfo).playerId;

  // End-of-game flags
  $: isFinished = snapshot?.state === "FINISHED";
  $: isCaller = snapshot?.callerId === myId;
  $: didCallerWin = snapshot?.callerId === snapshot?.scoreCard?.winnerId;

  // Manual scoreboard
  $: showModal = $showScores;

  function toggleModal() {
    showScores.update(v => !v);
  }

    function handleNextRound() {
      dispatch('nextRound');
    }
</script>

{#if isFinished || showModal}
  <div class="overlay">
    <!-- End-of-game animation -->
    {#if isFinished}
      <div class="animation">
        {#if didCallerWin}
          <h2 in:scale>🎆 Caller Won! 🎆</h2>
        {:else}
          <h2 in:scale>💀 Caller Lost! 💀</h2>
        {/if}
      </div>
    {/if}

    <!-- Scoreboard -->
    <div class="scorecard" in:fly={{ y: 50, duration: 300 }}>
      <h3>Scoreboard</h3>

      <!-- Current Game ScoreCard -->
      {#if snapshot?.scoreCard}
        <h4>Current Round</h4>
        <ul>
          {#each snapshot.scoreCard.roundScore as row}
            <li class:caller={row.playerId === snapshot.callerId}
                class:winner={row.playerId === snapshot.scoreCard.winnerId}>
              {row.playerName}: {row.score}
            </li>
          {/each}
        </ul>
      {/if}

 {#if snapshot?.roundHistory?.length}
   <h4>Round History</h4>
   <table>
     <thead>
       <tr>
         <th>Round</th>
         {#each snapshot.players as player}
           <th>{player.name}</th>
         {/each}
       </tr>
     </thead>
     <tbody>
       {#each snapshot.roundHistory as round}
         <tr>
           <td>{round.roundNumber}</td>
           {#each snapshot.players as player}
             <td>{round.scores.find(s => s.playerId === player.id)?.score ?? 0}</td>
           {/each}
         </tr>
       {/each}
       <!-- Totals row -->
       <tr>
               <td><strong>Total</strong></td>
               {#each snapshot.players as player}
                 <td>
                   <strong>
                     {snapshot.roundHistory.reduce(
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



      <!-- globalLeaderBoard Scores -->
{#if snapshot?.globalLeaderBoard?.length && snapshot?.state === "IN_PROGRESS"}
  <h4>Leader Board</h4>
  <ul>
    {#each snapshot.globalLeaderBoard as player}
      <li>{player.playerName}: {player.score}</li>
    {/each}
  </ul>
{/if}


    </div>
    <!-- Footer buttons -->
    <div class="footer-buttons">
      {#if isFinished}
        {#if snapshot?.currentDealerId === myId}
          <button class="next-round-btn" on:click={handleNextRound}>
            ⏭️ Next Round
          </button>
        {:else}
          <button class="close-btn" on:click={toggleModal}>
            ✖ Close
          </button>
        {/if}
      {:else}
        <button class="close-btn" on:click={toggleModal}>
          ✖ Close
        </button>
      {/if}
    </div>

  </div>
{/if}

<style>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  z-index: 2000;
  padding: 20px;
  overflow-y: auto;
  font-family: 'Segoe UI', Roboto, sans-serif;
}

.scorecard {
  background: #1e1e1e;
  padding: 30px 40px;
  border-radius: 16px;
  margin-top: 20px;
  width: 90%;
  max-width: 600px;
  text-align: center;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.7);
}

.scorecard h3 {
  font-size: 2rem;
  margin-bottom: 10px;
  letter-spacing: 1px;
}

.scorecard h4 {
  font-size: 1.4rem;
  margin-top: 20px;
  margin-bottom: 10px;
  font-weight: 500;
  border-bottom: 1px solid #555;
  padding-bottom: 4px;
}

ul {
  list-style: none; /* remove bullets */
  padding: 0;
  margin: 0;
  font-size: 1.2rem;
}

ul li {
  margin: 6px 0;
}

.caller {
  font-weight: bold;
  color: orange;
}

.winner {
  color: lime;
  font-weight: bold;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
  font-size: 1.1rem;
}
th, td {
  border: 1px solid #555;
  padding: 8px;
  text-align: center;
}
th {
  background: #333;
  color: #fff;
}
tr:nth-child(even) {
  background: rgba(255, 255, 255, 0.05);
}
tr:hover {
  background: rgba(255, 255, 255, 0.1);
}

.leader {
  color: gold;
  font-weight: bold;
}

.footer-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 12px;
}

.close-btn,
.next-round-btn {
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  font-size: 1.1rem;
  cursor: pointer;
}

.close-btn {
  background: #dc3545;
  color: white;
}

.next-round-btn {
  background: #28a745;
  color: white;
}

.close-btn:hover {
  background: #c82333;
}

.next-round-btn:hover {
  background: #218838;
}

</style>