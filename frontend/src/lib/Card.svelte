<script>
  import { createEventDispatcher } from 'svelte';
  import { cardImagePath } from '$lib/utils/cards.js';

  export let rank = null;
  export let suit = null;
  export let back = false;
  export let selected = false;
  export let isJoker = false;
    export let style = '';
      export let card;

  const dispatch = createEventDispatcher();

  $: src = back ? '/cards/back.png' : cardImagePath(card);

  // ensure a consistent event is emitted from the component (safe on all platforms)
  function handleClick(e) {
    // dispatch a component-level 'click' — parent can listen with on:click
    dispatch('click', { originalEvent: e });
  }
  function handlePointerDown(e) {
      dispatch('pointerdown', e); // forward event
    }
</script>
<div class="card {selected ? 'selected' : ''} {isJoker ? 'joker-card' : ''}">
<img
  src={src}
  alt={back ? 'Back of card' : `${rank} of ${suit}`}
  class:selected={selected}
  on:click={handleClick}
   draggable="false"
   on:mousedown={handlePointerDown}
     on:touchstart={handlePointerDown}
   style={style}
/>
</div>

<style>
  img {
    width: 60px;
    margin: 4px;
    border-radius: 6px;
    cursor: pointer;
    transition: transform 0.12s ease, border 0.12s ease;
    user-select: none;
    -webkit-user-drag: none;
  }
  img:hover { transform: translateY(-4px); }
  .selected { border: 2px solid gold; transform: translateY(-10px); }
  .card {
    width: 100%;
    height: 100%;
    border-radius: 6px;
    overflow: hidden;
    transition: transform 0.2s, box-shadow 0.3s;
  }

  .card.selected {
    transform: translateY(-10px);
  }

.card.joker-card::before {
  content: '';
    position: absolute;
    inset: 0;
    background-color: rgba(200, 150, 255, 0.3);
    border-radius: 6px;
}
</style>
