package game

import com.closemates.games.game.GameRoom
import com.closemates.games.models.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

data class SessionInfo(
    val session: DefaultWebSocketServerSession,
    val playerId: String
)

object SessionRegistry {
    // roomId -> list of sessions (with playerId)
    private val sessions: MutableMap<String, CopyOnWriteArrayList<SessionInfo>> = ConcurrentHashMap()

    fun addSession(roomId: String, session: DefaultWebSocketServerSession, playerId: String) {
        val list = sessions.computeIfAbsent(roomId) { CopyOnWriteArrayList() }
        list.add(SessionInfo(session, playerId))
    }

    fun removeSession(roomId: String, session: DefaultWebSocketServerSession) {
        sessions[roomId]?.removeIf { it.session == session }
        if (sessions[roomId]?.isEmpty() == true) {
            sessions.remove(roomId)
        }
    }

    private fun getSession(roomId: String, playerId: String): DefaultWebSocketServerSession? {
        return sessions[roomId]?.find { it.playerId == playerId }?.session
    }

    suspend fun sendToAll(room: GameRoom) {
        val json = Json { prettyPrint = true }

        // --- GameSnapshot (broadcast to all) ---
        val snapshot = GameSnapshot(
            roomId = room.id,
            state = room.gameState,
            players = room.players.map { PlayerInfo(it.id, it.name, it.hand.size, it.openCard) },
            currentPlayerId = if (room.currentIndex >= 0) room.currentPlayer().id else null,
            currentPlayerName = if (room.currentIndex >= 0) room.currentPlayer().name else null,
            currentDealerId = if (room.currentIndex >= 0) room.dealer().id else null,
            currentDealerName = if (room.currentIndex >= 0) room.dealer().name else null,
            deckCount = room.deck.size,
            jokerCard = room.jokerCard,
            callerId = room.callerId,
            callValue = room.callValue,
            scoreCard = if (room.gameState == GameState.FINISHED) room.getScoreCard() else null,
            roundHistory = room.roundHistory,
            globalLeaderBoard = room.globalLeaderBoard
        )

        val snapshotJson = json.encodeToString(snapshot)
        println(snapshotJson)
        sessions[room.id]?.forEach { info ->
            try {
                info.session.send(Frame.Text(snapshotJson))
            } catch (e: Exception) {
                println("Failed to send GameSnapshot to ${info.playerId}: ${e.message}")
            }
        }

        // --- HandInfo (private, per player) ---
        room.players.forEach { player ->
            var canCall = false
            if (room.jokerCard != null) {
                val sumRanks = player.hand
                    .filter { room.jokerCard != null && it.rank != room.jokerCard?.rank }
                    .sumOf { it.rank.value }
                canCall = sumRanks <= 5
            }
            val handInfo = HandInfo(player.id, player.hand, player.openCard, canCall)
            val handJson = json.encodeToString(handInfo)
            val session = getSession(room.id, player.id)
            try {
                session?.send(Frame.Text(handJson))
            } catch (e: Exception) {
                println("Failed to send HandInfo to ${player.id}: ${e.message}")
            }
        }
    }
}
