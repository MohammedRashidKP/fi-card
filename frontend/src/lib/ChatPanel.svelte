<script>
  import { onMount } from "svelte";
  import { chatMessages, playerInfo } from "$lib/stores";
  import { sendMessage } from "$lib/api";

  let newMessage = "";
  let showInput = false;

  // Position of chat bubble (defaults before window exists)
  let pos = { x: 0, y: 0 };
  let dragging = false;
  let offset = { x: 0, y: 0 };

  // Set initial position safely on client
  onMount(() => {
    pos = { x: window.innerWidth - 70, y: window.innerHeight - 70 };
  });

  function sendChat() {
    if (!newMessage.trim()) return;
    sendMessage({
      action: "chat",
      roomId: $playerInfo.roomId,
      playerId: $playerInfo.id,
      message: newMessage
    });
    newMessage = "";
    showInput = false;
  }

  // Auto-remove messages after 4s
  $: if ($chatMessages.length) {
    const last = $chatMessages[$chatMessages.length - 1];
    setTimeout(() => {
      chatMessages.update(msgs => msgs.filter(m => m !== last));
    }, 4000);
  }

  // --- Drag (mouse) ---
  function startDrag(e) {
    dragging = true;
    offset = { x: e.clientX - pos.x, y: e.clientY - pos.y };
    window.addEventListener("mousemove", onDrag);
    window.addEventListener("mouseup", stopDrag);
  }
  function onDrag(e) {
    if (!dragging) return;
    pos = { x: e.clientX - offset.x, y: e.clientY - offset.y };
  }
  function stopDrag() {
    dragging = false;
    window.removeEventListener("mousemove", onDrag);
    window.removeEventListener("mouseup", stopDrag);
  }

  // --- Drag (touch) ---
  function startTouch(e) {
    const t = e.touches[0];
    dragging = true;
    offset = { x: t.clientX - pos.x, y: t.clientY - pos.y };
    window.addEventListener("touchmove", onTouchDrag, { passive: false });
    window.addEventListener("touchend", stopTouch);
  }
  function onTouchDrag(e) {
    if (!dragging) return;
    const t = e.touches[0];
    pos = { x: t.clientX - offset.x, y: t.clientY - offset.y };
    e.preventDefault(); // stop screen scrolling while dragging
  }
  function stopTouch() {
    dragging = false;
    window.removeEventListener("touchmove", onTouchDrag);
    window.removeEventListener("touchend", stopTouch);
  }
</script>

<!-- Floating chat bubble -->
<div
  class="chat-bubble"
  style="left:{pos.x}px; top:{pos.y}px;"
  on:mousedown={startDrag}
  on:touchstart={startTouch}
  on:click={() => !dragging && (showInput = !showInput)}
>
  💬
</div>

<!-- Chat input expands when bubble is clicked -->
{#if showInput}
  <div class="chat-input" style="left:{pos.x - 150}px; top:{pos.y - 60}px;">
    <input
      bind:value={newMessage}
      placeholder="Type a message..."
      on:keydown={(e) => e.key === "Enter" && sendChat()}
    />
    <button on:click={sendChat}>📨</button>
  </div>
{/if}

<!-- Ephemeral messages overlay -->
<div class="chat-overlay">
  {#each $chatMessages as msg (msg)}
    <div class="chat-msg">
      <strong>{msg.playerId}:</strong> {msg.message}
    </div>
  {/each}
</div>

<style>
.chat-bubble {
  position: fixed;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: #007bff;
  color: white;
  font-size: 1.5rem;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: grab;
  user-select: none;
  box-shadow: 0 0 10px rgba(0,0,0,0.4);
  z-index: 3000;
}
.chat-bubble:active { cursor: grabbing; }

.chat-input {
  position: fixed;
  width: 200px;
  background: rgba(0,0,0,0.85);
  padding: 0.5rem;
  border-radius: 12px;
  display: flex;
  gap: 0.5rem;
  z-index: 3000;
}
.chat-input input {
  flex: 1;
  padding: 0.4rem;
  border: none;
  border-radius: 6px;
  outline: none;
}
.chat-input button {
  background: #28a745;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 0.4rem 0.6rem;
  cursor: pointer;
}

.chat-overlay {
  position: fixed;
  top: 100px;          /* instead of bottom */
  left: 50%;
  transform: translateX(-50%);
  z-index: 2500;
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: center;
  pointer-events: none;
  width: 300px;
  max-width: 90vw;
}
.chat-msg {
  background: rgba(0,0,0,0.8);
  color: white;
  padding: 6px 10px;
  border-radius: 10px;
  animation: fadeInOut 4s forwards;
}
@keyframes fadeInOut {
  0% { opacity: 0; transform: translateY(20px); }
  10% { opacity: 1; transform: translateY(0); }
  90% { opacity: 1; }
  100% { opacity: 0; transform: translateY(-20px); }
}
</style>
