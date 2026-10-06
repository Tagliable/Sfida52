package it.sfida52.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import java.time.LocalDate
import kotlin.random.Random

class ChallengeViewModel(app: Application) : AndroidViewModel(app) {
    private val storage = Storage(app)

    var challenge by mutableStateOf(storage.load())
        private set

    fun start(amounts: List<Long>, frequency: Frequency, startDate: LocalDate) {
        update(Challenge(amounts.sum(), frequency, startDate, amounts, emptyList()))
    }

    /** Estrae a caso uno degli importi rimasti e restituisce il suo indice. */
    fun draw(): Int? {
        val c = challenge ?: return null
        val left = c.remainingIndexes
        if (left.isEmpty()) return null
        val pick = left[Random.nextInt(left.size)]
        update(c.copy(draws = c.draws + Draw(pick, LocalDate.now(), paid = false)))
        return pick
    }

    fun togglePaid(drawPosition: Int) {
        val c = challenge ?: return
        val list = c.draws.toMutableList()
        val d = list[drawPosition]
        list[drawPosition] = d.copy(paid = !d.paid)
        update(c.copy(draws = list))
    }

    private fun update(c: Challenge?) {
        challenge = c
        storage.save(c)
    }
}
