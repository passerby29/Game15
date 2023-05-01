package dev.passerby.game15

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dev.passerby.game15.databinding.ActivityMainBinding
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var numbers: ArrayList<Int>
    private lateinit var emptyCoordinate: Coordinate
    private lateinit var buttons: Array<Array<Button>>

    private var moves = 0
    private var pauseTime: Long = 0
    private var isStarted = false
    private var soundButtonStatus = true

    private val preferences = GamePreferences.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadButtons()

        if (savedInstanceState == null) {
            isStarted = preferences.isPlaying
            if (isStarted) {
                moves = preferences.moves
                pauseTime = preferences.pauseTime
                val numbersList: MutableList<String> = ArrayList()
                val numbersText = preferences.numbers
                val numbersArray = numbersText!!.split("#").toTypedArray()
                for (i in numbersArray.indices) {
                    numbersList.add(numbersArray[i])
                }
                loadSavedNumbers(numbersList)
            } else {
                initNumbers()
                loadNumbersToButtons()
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        moves = savedInstanceState.getInt(MOVES, 0)
        pauseTime = savedInstanceState.getLong(PAUSE_TIME, 0)
        isStarted = savedInstanceState.getBoolean(IS_STARTED)
        soundButtonStatus = savedInstanceState.getBoolean(MUSIC_ICON_STATE)
        val numbersList: List<String>? = savedInstanceState.getStringArrayList(NUMBERS)
        loadSavedNumbers(numbersList)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            putInt(MOVES, moves)
            putLong(PAUSE_TIME, pauseTime)
            putBoolean(IS_STARTED, isStarted)
            putBoolean(MUSIC_ICON_STATE, soundButtonStatus)
        }
        val numbers = java.util.ArrayList<String>()
        for (i in 0 until binding.groupItems.childCount) {
            numbers.add((binding.groupItems.getChildAt(i) as Button).text.toString())
        }
        outState.putStringArrayList(NUMBERS, numbers)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        putDataToSharedPref()
    }

    private fun loadSavedNumbers(numbers: List<String>?) {
        for (i in numbers!!.indices) {
            if (numbers[i] == "") {
                emptyCoordinate = Coordinate(i % 4, i / 4)
                buttons[i / 4][i % 4].visibility = View.INVISIBLE
            }
            buttons[i / 4][i % 4].text = numbers[i]
        }
    }

    private fun loadButtons() {
        val count = binding.groupItems.childCount
        val size = sqrt(count.toDouble())
        buttons = Array(4) {
            Array(4) {
                Button(this)
            }
        }

        for (i in 0 until binding.groupItems.childCount) {
            val view = binding.groupItems.getChildAt(i)
            val button = view as Button
            button.setOnClickListener {
                onButtonClick(it)
            }
            val y = (i / size).toInt()
            val x = (i % size).toInt()
            button.tag = Coordinate(x, y)
            buttons[y][x] = button
        }
    }

    private fun initNumbers() {
        numbers = ArrayList()
        for (i in 1..15) {
            numbers.add(i)
        }
    }

    private fun loadNumbersToButtons() {
        shuffle()
        for (i in buttons.indices) {
            for (j in buttons.indices) {
                val index = i * 4 + j
                if (index < 15) {
                    buttons[i][j].text = numbers[index].toString()
                }
            }
        }
        buttons[3][3].visibility = View.INVISIBLE
        buttons[3][3].text = ""
        emptyCoordinate = Coordinate(3, 3)
        moves = 0
        isStarted = true
    }

    private fun onButtonClick(view: View) {
        val button = view as Button
        val c = button.tag as Coordinate
        val eX = emptyCoordinate.x
        val eY = emptyCoordinate.y
        val dX = abs(c.x - eX)
        val dY = abs(c.y - eY)
        if (dX + dY == 1) {
            moves++
            buttons[eY][eX].text = button.text
            button.visibility = View.INVISIBLE
            button.text = ""
            buttons[eY][eX].visibility = View.VISIBLE
            emptyCoordinate = c
            if (isWin()) {
                Toast.makeText(this, "You are winner", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isWin(): Boolean {
        if (!(emptyCoordinate.x == 3 && emptyCoordinate.y == 3)) return false
        for (i in 0..14) {
            val s = buttons[i / 4][i % 4].text.toString()
            if (s != (i + 1).toString()) return false
        }
        return true
    }

    private fun shuffle() {
        numbers.remove(Integer.valueOf(0))
        numbers.shuffle()
        if (!isSolvable(numbers)) shuffle()
    }

    private fun isSolvable(puzzle: ArrayList<Int>): Boolean {
        numbers.add(0)
        var parity = 0
        val gridWidth = sqrt(puzzle.size.toDouble()).toInt()
        var row = 0 // the current row we are on
        var blankRow = 0 // the row with the blank tile
        for (i in puzzle.indices) {
            if (i % gridWidth == 0) { // advance to next row
                row++
            }
            if (puzzle[i] == 0) { // the blank tile
                blankRow = row // save the row on which encountered
                continue
            }
            for (j in i + 1 until puzzle.size) {
                if (puzzle[i] > puzzle[j] && puzzle[j] != 0) {
                    parity++
                }
            }
        }
        return if (gridWidth % 2 == 0) { // even grid
            if (blankRow % 2 == 0) { // blank on odd row; counting from bottom
                parity % 2 == 0
            } else { // blank on even row; counting from bottom
                parity % 2 != 0
            }
        } else { // odd grid
            parity % 2 == 0
        }
    }

    private fun putDataToSharedPref() {
        preferences.isPlaying = true
        preferences.moves = moves
        preferences.pauseTime = pauseTime

        val builder = StringBuilder()
        for (i in 0 until binding.groupItems.childCount - 1) {
            builder.append((binding.groupItems.getChildAt(i) as Button).text.toString()).append("#")
        }
        builder.append((binding.groupItems.getChildAt(15) as Button).text.toString())
        preferences.numbers = builder.toString()
    }

    companion object {

        const val NUMBERS = "numbers"
        const val IS_STARTED = "is_started"
        const val MOVES = "moves"
        const val PAUSE_TIME = "pause_time"
        const val MUSIC_ICON_STATE = "music_icon_state"
    }
}