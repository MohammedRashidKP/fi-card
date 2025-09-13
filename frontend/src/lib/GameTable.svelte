<script>
  import { gameSnapshot, handInfo, selectedCards, hasDiscarded } from '$lib/stores';
  import { sendMessage } from '$lib/api';
  import PlayerHand from '$lib/PlayerHand.svelte';
  import ScoreBoard from '$lib/ScoreBoard.svelte';
  import { get } from 'svelte/store';
  import { showScores } from '$lib/stores';
  import CallIndicator from '$lib/CallIndicator.svelte';

$: if ($gameSnapshot.state === 'FINISHED') {
  showScores.update(() => true);
}

  function cardImagePath(card) {
    if (!card) return '';
    return `/cards/${card.rank.toLowerCase()}_of_${card.suit.toLowerCase()}.png`;
  }

  // selection logic (unchanged)
  function onCardClick(index) {
    console.log('Clicked card index:', index, 'Selected before click:', $selectedCards);
    const currentSelection = $selectedCards;
    const clickedCard = $handInfo.hand[index];
    if (!clickedCard) return;

    if (currentSelection.length === 0) {
      selectedCards.set([index]);
    } else {
      const firstCard = $handInfo.hand[currentSelection[0]];
      if (clickedCard.rank === firstCard.rank) {
        if (currentSelection.includes(index)) {
          selectedCards.set(currentSelection.filter(i => i !== index));
        } else {
          selectedCards.set([...currentSelection, index]);
        }
      } else {
        selectedCards.set([index]);
      }
    }
    console.log('Selected after click:', $selectedCards);
  }

  function discardSelected() {
    if ($selectedCards.length === 0) return;
    sendMessage({
      action: 'discard',
      roomId: $gameSnapshot.roomId,
      playerId: $handInfo.playerId,
      cardsToDiscard: $selectedCards.map(i => $handInfo.hand[i])
    });
    selectedCards.set([]);
  }

  // Relative seating: self is implicit at bottom
  $: otherPlayers = $gameSnapshot.players.filter(p => p.id !== $handInfo.playerId);

  // Fixed slot layout (9 available for others)
  const slotOrder = [1, 2, 3, 4, 5, 6, 7, 8, 9];

  $: slottedPlayers = otherPlayers.map((p, i) => ({
    ...p,
    slot: slotOrder[i] ?? null
  }));

  function slotClass(slot) {
    return slot !== null ? `slot-${slot}` : '';
  }
function handleDeckClick() {
  // Only allow if it's this player's turn
  if ($gameSnapshot.currentPlayerId !== $handInfo.playerId) return;
    if (!get(hasDiscarded)) return;
  console.log('Deck clicked');
  sendMessage({
    action: 'pickDeck',
    roomId: $gameSnapshot.roomId,
    playerId: $handInfo.playerId
  });
}

function handleOpenCardClick() {
  if ($gameSnapshot.currentPlayerId !== $handInfo.playerId) return;
    if (!get(hasDiscarded)) return;
  console.log('Open card clicked', $handInfo.openCard);
  sendMessage({
    action: 'pickOpen',
    roomId: $gameSnapshot.roomId,
    playerId: $handInfo.playerId
  });
}
function handleCall() {
    const { playerId, roomId } = get(handInfo);
    sendMessage({
      action: "call",
      roomId: $gameSnapshot.roomId,
      playerId: $handInfo.playerId
    });
  }
  function handleStrike() {
    sendMessage({
      action: "strike",
      roomId: $gameSnapshot.roomId,
      playerId: $handInfo.playerId
    });
  }
  function toggleScores() {
          showScores.update(v => !v);
      }

  function closeScoreBoard() {
    showScores.set(false);
  }
  function handleNextRound() {
    const { roomId, playerId } = get(handInfo);
    sendMessage({
      action: "startNewRound",
      roomId: $gameSnapshot.roomId,
      playerId: $handInfo.playerId
    });
    showScores.set(false);
  }
</script>

<style>
  :root {
    --seat-offset: 70px; /* distance of avatars outside table */
    --card-offset: 80px; /* distance of open cards inside table */
  }

  .game-container {
    display: flex;
    flex-direction: column;
    height: 100vh;
    background: #2e2b23;
    justify-content: space-between;
  }

  .table {
    position: relative;
    margin: 100px auto 0;
    width: 70%;
    height: 65vh;
    max-width: 400px;
    border-radius: 30px;
    background: radial-gradient(circle at center, #006400 60%, #004d00 100%);
    border: 20px solid #5a3825;
    box-shadow: inset 0 0 50px rgba(0, 0, 0, 0.7);
  }

  .player-slot {
    position: absolute;
    display: flex;
    flex-direction: column;
    align-items: center;
    color: white;
    font-size: 0.82rem;
    text-align: center;
    gap: 4px;
  }

  .avatar { font-size: 2rem; line-height: 1; transition: all 0.2s ease; }

  .player-slot.current .avatar {
    border: 3px solid gold;
    border-radius: 50%;
    padding: 2px;
    box-shadow: 0 0 10px gold;
  }

  .avatar-wrapper { position: relative; display: inline-block; }
  .card-count { position: absolute; top: -6px; right: -10px; background: crimson; color: white; font-size: 0.7rem; padding: 2px 6px; border-radius: 100%; min-width: 10px; text-align: center; line-height: 1; }

  .name {
    font-size: 0.8rem;
    font-weight: 600;
    background: rgba(0, 0, 0, 0.6);
    padding: 2px 8px;
    border-radius: 12px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .open-card img { width: 48px; border-radius: 4px; }

  /* slots */
  .slot-1 { right: calc(var(--seat-offset) * -1); top: 70%; transform: translateY(-50%); }
  .slot-2 { right: calc(var(--seat-offset) * -1); top: 50%; transform: translateY(-50%); }
  .slot-3 { right: calc(var(--seat-offset) * -1); top: 30%; transform: translateY(-50%); }
  .slot-4 { right: calc(var(--seat-offset) * -1); top: 10%; transform: translateY(-50%); }
  .slot-5 { left: 50%; top: calc(var(--seat-offset) * -1); transform: translateX(-50%); }
  .slot-6 { left: calc(var(--seat-offset) * -1); top: 10%; transform: translateY(-50%); }
  .slot-7 { left: calc(var(--seat-offset) * -1); top: 30%; transform: translateY(-50%); }
  .slot-8 { left: calc(var(--seat-offset) * -1); top: 50%; transform: translateY(-50%); }
  .slot-9 { left: calc(var(--seat-offset) * -1); top: 70%; transform: translateY(-50%); }

  .player-slot .open-card { position: absolute; }
  .slot-1 .open-card { right: var(--card-offset); top: 70%; transform: translateY(-50%); }
  .slot-2 .open-card { right: var(--card-offset); top: 50%; transform: translateY(-50%); }
  .slot-3 .open-card { right: var(--card-offset); top: 30%; transform: translateY(-50%); }
  .slot-4 .open-card { right: var(--card-offset); top: 10%; transform: translateY(-50%); }
  .slot-5 .open-card { top: var(--card-offset); left: 50%; transform: translateX(-50%); }
  .slot-6 .open-card { left: var(--card-offset); top: 10%; transform: translateY(-50%); }
  .slot-7 .open-card { left: var(--card-offset); top: 30%; transform: translateY(-50%); }
  .slot-8 .open-card { left: var(--card-offset); top: 50%; transform: translateY(-50%); }
  .slot-9 .open-card { left: var(--card-offset); top: 70%; transform: translateY(-50%); }

  .self-open { position: absolute; bottom: 5%; left: 50%; transform: translateX(-50%); }
  .self-open img { width: 56px; border-radius: 6px; }

  .center {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    display: flex;
    gap: 15px;
    align-items: center;
  }

  .deck {
    position: relative; /* needed for absolute label inside */
    width: 56px;
    height: 80px;
    background: url('/cards/back.png') no-repeat center/cover;
    border-radius: 6px;
    cursor: pointer;
  }

.deck-label {
  position: absolute;
  top: -1.5em;          /* space above the deck */
  left: 50%;
  transform: translateX(-50%);
  font-size: 1rem;       /* larger text */
  font-weight: bold;
  color: #ffd700;        /* bright color, e.g., gold */
  text-shadow: 0 0 4px rgba(0,0,0,0.5); /* optional glow for better contrast */
  white-space: nowrap;
  animation: blink 2s infinite;
}
@keyframes blink {
  0%, 50%, 100% {
    opacity: 1;
  }
  25%, 75% {
    opacity: 0.3; /* adjust for subtlety */
  }
}
  .joker { width: 56px; }

  .score-panel {
      background: rgba(255,255,255,0.95);
      padding: 1rem;
      border-radius: 8px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.2);
      max-height: 60vh;
      overflow-y: auto;
      margin-top: 1rem;
  }
  .show-scores-btn {
    margin-top: 10px;      /* moves it slightly lower */
    margin-left: 10px;
    display: block;        /* needed for margin-left: auto to work */
    padding: 0.5rem 1rem;
    border-radius: 6px;
    border: none;
    background: #007bff;
    color: white;
    cursor: pointer;
  }

  .show-scores-btn:hover {
      background: #0056b3;
  }
</style>

<div class="game-container">
  <div class="table">
    {#each slottedPlayers as player (player.id)}
      <div class="player-slot {slotClass(player.slot)} {player.id === $gameSnapshot.currentPlayerId ? 'current' : ''}">
        <div class="avatar-wrapper">
          <div class="avatar">👤</div>
          <div class="card-count">{player.cardCount}</div>
        </div>
        <div class="name">{player.name}</div>

        {#if player.openCard}
          <div class="open-card">
            <img src={cardImagePath(player.openCard)} alt="Open Card" />
          </div>
        {/if}
      </div>
    {/each}

<!-- Own open card -->
{#if $handInfo.openCard}
  <div class="open-card self-open" on:click={handleOpenCardClick}>
    <img src={cardImagePath($handInfo.openCard)} alt="Your Open Card" />
  </div>
{/if}

<button class="show-scores-btn" on:click={toggleScores}>
    {#if $showScores} Hide Scores {:else} Show Scores {/if}
</button>

<div class="center">
  <div class="deck" on:click={handleDeckClick}></div>
  {#if $gameSnapshot.currentPlayerName}
    <span class="deck-label">{$gameSnapshot.currentPlayerName}</span>
    {/if}
  {#if $gameSnapshot.jokerCard}
    <img src={cardImagePath($gameSnapshot.jokerCard)} alt="Joker" class="joker" />
  {/if}
</div>
  </div>

  <!-- Own hand -->
  <PlayerHand
    hand={$handInfo.hand}
    selectedCards={$selectedCards}
    on:cardClick={(e) => onCardClick(e.detail.index)}
    on:discard={discardSelected}
    on:call={handleCall}
    on:strike={handleStrike}
  />



{#if showScores}
  <ScoreBoard
    scoreCard={$gameSnapshot.scoreCard}
    globalLeaderBoard={$gameSnapshot.globalLeaderBoard}
    roundHistory={$gameSnapshot.roundHistory}
    onClose={() => showScores.set(false)}
    on:nextRound={handleNextRound}
  />
{/if}
<CallIndicator />

</div>
