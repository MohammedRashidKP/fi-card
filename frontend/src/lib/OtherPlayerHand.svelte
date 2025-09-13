<script>
  import Card from './Card.svelte';
  import { createEventDispatcher } from 'svelte';

  export let hand = [];
  export let selectedCards = [];
  export let onCardClick = () => {};

  const dispatch = createEventDispatcher();
  let dragStartY = null;
  let isDragging = false;
  let isOverDiscardZone = false;

  function startDrag(e) {
    dragStartY = e.touches ? e.touches[0].clientY : e.clientY;
    isDragging = true;

    // Attach global move/end listeners
    window.addEventListener(e.touches ? 'touchmove' : 'mousemove', onDragMove);
    window.addEventListener(e.touches ? 'touchend' : 'mouseup', onDragEnd);
  }

  function onDragMove(e) {
    if (!isDragging) return;
    const currentY = e.touches ? e.touches[0].clientY : e.clientY;
    isOverDiscardZone = currentY < window.innerHeight * 0.2; // top 20%
  }

  function onDragEnd(e) {
    if (!isDragging) return;
    const endY = e.changedTouches ? e.changedTouches[0].clientY : e.clientY;

    if (endY < window.innerHeight * 0.2 && selectedCards.length > 0) {
      dispatch('discard');
    }

    isDragging = false;
    isOverDiscardZone = false;
    dragStartY = null;

    // Remove global listeners
    window.removeEventListener(e.changedTouches ? 'touchmove' : 'mousemove', onDragMove);
    window.removeEventListener(e.changedTouches ? 'touchend' : 'mouseup', onDragEnd);
  }
</script>

<div
  class="player-hand"
  class:is-over-discard={isOverDiscardZone}
  on:touchstart={startDrag}
  on:mousedown={startDrag}
>
  {#each hand as card, i (card.rank + '-' + card.suit + '-' + i)}
    <Card
      rank={card.rank}
      suit={card.suit}
      selected={selectedCards.includes(i)}
      onClick={() => onCardClick(i)}
    />
  {/each}
</div>

<style>
.player-hand {
  display: flex;
  justify-content: center;
  flex-wrap: nowrap;
  overflow-x: auto;
  padding: 10px;
  transition: background-color 0.2s;
}

.player-hand.is-over-discard {
  background-color: rgba(255, 0, 0, 0.2);
}
</style>
