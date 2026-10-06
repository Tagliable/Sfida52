package it.sfida52.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Salva la sfida sul telefono (SharedPreferences, formato JSON). */
class Storage(context: Context) {
    private val prefs = context.getSharedPreferences("sfida52", Context.MODE_PRIVATE)

    fun load(): Challenge? {
        val raw = prefs.getString(KEY, null) ?: return null
        return try {
            val o = JSONObject(raw)
            val amounts = o.getJSONArray("amounts").let { arr -> List(arr.length()) { arr.getLong(it) } }
            val draws = o.getJSONArray("draws").let { arr ->
                List(arr.length()) {
                    val d = arr.getJSONObject(it)
                    Draw(d.getInt("index"), LocalDate.parse(d.getString("date")), d.getBoolean("paid"))
                }
            }
            Challenge(
                targetCents = o.getLong("target"),
                frequency = Frequency.valueOf(o.getString("frequency")),
                startDate = LocalDate.parse(o.getString("start")),
                amounts = amounts,
                draws = draws,
            )
        } catch (e: Exception) {
            null
        }
    }

    fun save(c: Challenge?) {
        if (c == null) {
            prefs.edit().remove(KEY).apply()
            return
        }
        val o = JSONObject()
            .put("target", c.targetCents)
            .put("frequency", c.frequency.name)
            .put("start", c.startDate.toString())
            .put("amounts", JSONArray(c.amounts))
            .put("draws", JSONArray().apply {
                c.draws.forEach {
                    put(JSONObject().put("index", it.index).put("date", it.date.toString()).put("paid", it.paid))
                }
            })
        prefs.edit().putString(KEY, o.toString()).apply()
    }

    private companion object {
        const val KEY = "challenge"
    }
}
