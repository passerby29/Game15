package dev.passerby.game15

import android.content.Context
import android.content.Context.MODE_PRIVATE

class GamePreferences(context: Context) {

    private val preferences = context.getSharedPreferences(PREFERENCES, MODE_PRIVATE)
    private val editor = preferences.edit()

    var numbers: String?
        get() = preferences.getString(NUMBERS, "1#2#3#4#5#6#7#8#9#10#11#12#13#14#15##")
        set(value) = editor.putString(NUMBERS, value).apply()

    var isPlaying: Boolean
        get() = preferences.getBoolean(IS_PLAYING, false)
        set(value) = editor.putBoolean(IS_PLAYING, value).apply()

    var moves: Int
        get() = preferences.getInt(MOVES, 0)
        set(value) = editor.putInt(MOVES, value).apply()

    var pauseTime: Long
        get() = preferences.getLong(PAUSE_TIME, 0)
        set(value) = editor.putLong(PAUSE_TIME, value).apply()

    companion object {

        const val PREFERENCES = "shared_preferences"
        const val NUMBERS = "numbers"
        const val IS_PLAYING = "is_playing"
        const val MOVES = "moves"
        const val PAUSE_TIME = "pause_time"

        private lateinit var instance: GamePreferences

        fun init(context: Context) {
            instance = GamePreferences(context)
        }

        fun getInstance() = instance
    }
}