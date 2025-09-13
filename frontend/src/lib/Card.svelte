<script>
  import { createEventDispatcher } from 'svelte';

  export let rank = null;
  export let suit = null;
  export let back = false;
  export let selected = false;
    export let style = '';
  const dispatch = createEventDispatcher();

  $: src = back
    ? '/cards/back.png'
    : `/cards/${rank?.toLowerCase()}_of_${suit?.toLowerCase()}.png`;

  // ensure a consistent event is emitted from the component (safe on all platforms)
  function handleClick(e) {
    // dispatch a component-level 'click' — parent can listen with on:click
    dispatch('click', { originalEvent: e });
  }
  function handlePointerDown(e) {
      dispatch('pointerdown', e); // forward event
    }
</script>

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
</style>
