<script>
  import Card from './Card.svelte';
  import { createEventDispatcher } from 'svelte';
  import { isMyTurn, hasDiscarded, handInfo, gameSnapshot } from '$lib/stores';
  import { get } from 'svelte/store';

  export let hand = [];
  export let selectedCards = [];

  const dispatch = createEventDispatcher();

  // Drag state
  let dragStartY = null;
  let isDragging = false;
  let draggingIndex = null;
  let dragDelta = 0;
  let dragStartTop = 0;
  let dragStartLeft = 0;
  const discardThreshold = 60; // px to trigger discard

  // Only allow pointer down if player can discard
  function handleCardPointerDown(e, index) {
    if (!get(isMyTurn) || get(hasDiscarded)) return;
    if (!selectedCards.includes(index)) return;

    const clientY = e.touches?.[0]?.clientY ?? e.clientY;
    dragStartY = clientY;
    isDragging = true;
    draggingIndex = index;

    const rect = e.currentTarget.getBoundingClientRect();
    dragStartTop = rect.top;
    dragStartLeft = rect.left;

    window.addEventListener('touchmove', handlePointerMove, { passive: true });
    window.addEventListener('touchend', handlePointerUp);
    window.addEventListener('mousemove', handlePointerMove);
    window.addEventListener('mouseup', handlePointerUp);
  }

  function handlePointerMove(e) {
    if (!isDragging || dragStartY === null) return;

    let currentY = e.type.startsWith('touch')
      ? e.touches?.[0]?.clientY
      : e.clientY;

    if (currentY === undefined) return;

    dragDelta = dragStartY - currentY;
    if (dragDelta < 0) dragDelta = 0; // only upward drag
  }

  function handlePointerUp(e) {
    if (!isDragging) return;
    const endY = e.changedTouches ? e.changedTouches[0].clientY : e.clientY;
    const delta = dragStartY - endY;

    if (delta > discardThreshold && selectedCards.length > 0 && !get(hasDiscarded)) {
      dispatch('discard');
      hasDiscarded.set(true);
    }

    // Reset drag state
    isDragging = false;
    draggingIndex = null;
    dragStartY = null;
    dragDelta = 0;

    window.removeEventListener('touchmove', handlePointerMove);
    window.removeEventListener('touchend', handlePointerUp);
    window.removeEventListener('mousemove', handlePointerMove);
    window.removeEventListener('mouseup', handlePointerUp);
  }

  function handleCardClick(i) {
    if (!get(isMyTurn) || get(hasDiscarded)) return;
    dispatch('cardClick', { index: i });
  }
  function handleCallClick() {
      dispatch('call');
  }
  function handleStrikeClick() {
      console.log('Dispatching strike')
      dispatch('strike');
  }
</script>

<div class="player-hand">
  {#each hand as card, i (card.rank + '-' + card.suit + '-' + i)}
    <div
      class="card-wrapper"
      style={`position: ${isDragging && selectedCards.includes(i) ? 'fixed' : 'relative'};
             top: ${isDragging && selectedCards.includes(i) ? dragStartTop - dragDelta + 'px' : '0'};
             left: ${isDragging && selectedCards.includes(i) ? dragStartLeft + 'px' : '0'};
             z-index: ${isDragging && selectedCards.includes(i) ? 1000 : 1};
             transition: transform 0.1s;
             pointer-events: ${get(isMyTurn) && !get(hasDiscarded) ? 'auto' : 'none'};`}
    >
      <Card
        card={card}
        rank={card.rank}
        suit={card.suit}
        selected={selectedCards.includes(i)}
        on:click={() => handleCardClick(i)}
        on:pointerdown={(e) => handleCardPointerDown(e.detail, i)}
        isJoker={$gameSnapshot.jokerCard
                        && card.rank === $gameSnapshot.jokerCard.rank}
        style="width: 70px; height: 110px;"
      />
    </div>
  {/each}
 <!-- Call button -->
 {#if $isMyTurn && $handInfo.canCall}
   <div class="call-container">
     <button class="call-btn" on:click={handleCallClick}>
       Call
     </button>
   </div>
 {/if}

   <!-- Strike button -->
   {#if $handInfo.canStrike}
     <div class="call-container">
       <button class="strike-btn" on:click={handleStrikeClick}>
         Strike
       </button>
     </div>
   {/if}
</div>

<style>
.player-hand {
  display: flex;
  justify-content: center;
  flex-wrap: nowrap;
  overflow-x: auto;
  padding: 10px;
  border-radius: 12px;
  background: rgba(0,0,0,0.06);
  touch-action: pan-y;
}

.card-wrapper {
  position: relative; /* normal cards */
}
.call-container {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

.call-btn {
  padding: 6px 14px;
  font-size: 14px;
  font-weight: bold;
  border: none;
  border-radius: 6px;
  background: #0077ff;
  color: white;
  cursor: pointer;
  transition: background 0.2s;
}

.call-btn:hover {
  background: #005fcc;
}

.strike-btn {
  padding: 6px 14px;
  font-size: 14px;
  font-weight: bold;
  border: none;
  border-radius: 6px;
  background: #d9534f;
  color: white;
  cursor: pointer;
  transition: background 0.2s;
}

.strike-btn:hover {
  background: #c9302c;
}
</style>
