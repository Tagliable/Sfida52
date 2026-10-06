package it.sfida52.app

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToLong

enum class Frequency(val draws: Int, val label: String) {
    WEEKLY(52, "Settimanale"),
    BIWEEKLY(26, "Ogni 2 settimane"),
    MONTHLY(12, "Mensile"),
}

data class Draw(
    /** Posizione dell'importo nella lista [Challenge.amounts]. */
    val index: Int,
    val date: LocalDate,
    val paid: Boolean,
)

data class Challenge(
    val targetCents: Long,
    val frequency: Frequency,
    val startDate: LocalDate,
    val amounts: List<Long>,
    val draws: List<Draw>,
) {
    val drawnIndexes: Set<Int> get() = draws.map { it.index }.toSet()
    val remainingIndexes: List<Int> get() = amounts.indices.filter { it !in drawnIndexes }
    val isComplete: Boolean get() = draws.size >= amounts.size
    val paidCents: Long get() = draws.filter { it.paid }.sumOf { amounts[it.index] }
    val drawnCents: Long get() = draws.sumOf { amounts[it.index] }

    fun dueDate(drawNumber: Int): LocalDate = when (frequency) {
        Frequency.WEEKLY -> startDate.plusWeeks(drawNumber.toLong())
        Frequency.BIWEEKLY -> startDate.plusWeeks(2L * drawNumber)
        Frequency.MONTHLY -> startDate.plusMonths(drawNumber.toLong())
    }

    /** Data della prossima estrazione, oppure null se la sfida è finita. */
    val nextDue: LocalDate? get() = if (isComplete) null else dueDate(draws.size)
}

object Amounts {
    /**
     * Crea [n] importi proporzionali a 1, 2, 3 … n la cui somma è esattamente [targetCents]
     * (arrotondato all'euro se [roundEuro]). Restituisce null se l'obiettivo è troppo basso.
     */
    fun build(targetCents: Long, n: Int, roundEuro: Boolean): List<Long>? {
        val step = if (roundEuro) 100L else 1L
        val units = (targetCents.toDouble() / step).roundToLong()
        if (units < n) return null
        val tri = n * (n + 1) / 2.0
        val a = LongArray(n) { k -> max(1L, ((k + 1) * units / tri).roundToLong()) }
        var diff = units - a.sum()
        var i = n - 1
        while (diff != 0L) {
            val d = if (diff > 0) 1L else -1L
            if (a[i] + d >= 1) {
                a[i] += d
                diff -= d
            }
            i = (i - 1 + n) % n
        }
        a.sort()
        return a.map { it * step }
    }
}

object Fmt {
    private val currency = NumberFormat.getCurrencyInstance(Locale.ITALY)
    private val date = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)

    fun money(cents: Long): String {
        val decimals = if (cents % 100 == 0L) 0 else 2
        currency.minimumFractionDigits = decimals
        currency.maximumFractionDigits = decimals
        return currency.format(cents / 100.0)
    }

    fun date(d: LocalDate): String = d.format(date)
}
