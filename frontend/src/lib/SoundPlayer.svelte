<script>
  import { currentCue, soundMap } from "$lib/stores";
  import { get } from "svelte/store";

  let audio;

  $: if ($currentCue) {
    const cue = $currentCue;
    const soundFile = soundMap[cue.cue || cue]; // support {cue, volume, loop} or just "CALL"
    if (soundFile) {
      if (audio) {
        audio.pause();
        audio.currentTime = 0; // reset
      }
      audio = new Audio(soundFile);
      audio.volume = cue.volume ? cue.volume / 100 : 1.0;
      audio.loop = cue.loop ?? false;
      audio.play().catch(err => {
        console.warn("Autoplay blocked:", err);
      });
    }
    // reset so it doesn’t replay on refresh
    currentCue.set(null);
  }
</script>
