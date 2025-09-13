<script>
  export let discardedCards = [];
  export let openCard = null;
</script>

<div class="discard">
  {#if discardedCards.length > 0}
    <img
      src={`/cards/${discardedCards[discardedCards.length - 1].rank}_of_${discardedCards[discardedCards.length - 1].suit}.png`}
      alt="Discard"
    />
  {/if}

  {#if openCard}
    <div class="open">
      <img src={`/cards/${openCard.rank}_of_${openCard.suit}.png`} alt="Open card" />
    </div>
  {/if}
</div>

<style>
.discard { position: relative; display: inline-block; margin-left: 20px; }
.discard img { width: 60px; border-radius: 6px; }
.open { position: absolute; top: 0; left: 70px; }
.open img { width: 50px; border-radius: 6px; }
</style>
