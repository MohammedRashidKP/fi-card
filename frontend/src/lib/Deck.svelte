<script>
  export let deckCount = 0;
  export let joker = null;
</script>

<div class="deck">
  <img src="/cards/back.png" alt="Deck" />
  <div class="count">{deckCount}</div>
  {#if joker}
    <div class="joker">
      <img src={`/cards/${joker.rank}_of_${joker.suit}.png`} alt="Joker" />
    </div>
  {/if}
</div>

<style>
.deck { position: relative; display: inline-block; }
.count {
  position: absolute;
  top: 0; right: 0;
  background: white; padding: 2px 5px;
  border-radius: 4px;
}
.joker img { width: 40px; margin-left: 10px; }
</style>
