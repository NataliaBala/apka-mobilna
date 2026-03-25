package pl.wsei.pam.lab03

import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import pl.wsei.pam.R
import java.util.*

data class Tile(val button: ImageButton, var tileResource: Int, val deckResource: Int) {
    init {
        button.setImageResource(deckResource)
    }
    private var _revealed: Boolean = false
    var revealed: Boolean
        get() = _revealed
        set(value) {
            _revealed = value
            if (_revealed) {
                button.setImageResource(tileResource)
            } else {
                button.setImageResource(deckResource)
            }
        }
    fun removeOnClickListener() {
        button.setOnClickListener(null)
    }
}

enum class GameStates {
    Matching, Match, NoMatch, Finished
}

class MemoryGameLogic(private val maxMatches: Int) {
    private var valueFunctions: MutableList<() -> Int> = mutableListOf()
    private var matches: Int = 0

    fun process(value: () -> Int): GameStates {
        if (valueFunctions.size < 1) {
            valueFunctions.add(value)
            return GameStates.Matching
        }
        valueFunctions.add(value)
        val result = valueFunctions[0]() == valueFunctions[1]()
        matches += if (result) 1 else 0
        valueFunctions.clear()
        return when (result) {
            true -> if (matches == maxMatches) GameStates.Finished else GameStates.Match
            false -> GameStates.NoMatch
        }
    }

    fun getMatches(): Int = matches
    fun setMatches(m: Int) {
        matches = m
    }
}

data class MemoryGameEvent(
    val tiles: List<Tile>,
    val state: GameStates
)

class MemoryBoardView(
    private val gridLayout: GridLayout,
    private val cols: Int,
    private val rows: Int
) {
    private val tiles: MutableMap<String, Tile> = mutableMapOf()
    private val icons: List<Int> = listOf(
        R.drawable.baseline_rocket_24,
        R.drawable.baseline_rocket_launch_24,
        R.drawable.baseline_audiotrack_24,
        android.R.drawable.ic_menu_gallery,
        android.R.drawable.ic_menu_camera,
        android.R.drawable.ic_menu_compass,
        android.R.drawable.ic_menu_directions,
        android.R.drawable.ic_menu_mapmode
    )
    private val deckResource: Int = R.drawable.deck
    private var onGameChangeStateListener: (MemoryGameEvent) -> Unit = { }
    private val matchedPair: Stack<Tile> = Stack()
    private val logic: MemoryGameLogic = MemoryGameLogic(cols * rows / 2)

    // Zmienna blokująca kliknięcia (ważna dla Lab03Activity)
    var isLocked: Boolean = false

    // Konstruktor dodatkowy do przywracania stanu gry
    constructor(
        gridLayout: GridLayout,
        cols: Int,
        rows: Int,
        savedIcons: IntArray,
        savedRevealed: BooleanArray
    ) : this(gridLayout, cols, rows) {
        setupBoard(savedIcons, savedRevealed)
    }

    init {
        // Ten blok uruchomi się TYLKO przy wywołaniu głównego konstruktora (nowa gra)
        // Jeśli używamy dodatkowego konstruktora, plansza zostanie wygenerowana tam
        if (tiles.isEmpty()) {
            generateNewBoard()
        }
    }

    private fun generateNewBoard() {
        val totalTiles = cols * rows
        val pool = mutableListOf<Int>()
        val neededPairs = totalTiles / 2
        while (pool.size < neededPairs) pool.addAll(icons.shuffled())

        val selectedIcons = pool.take(neededPairs)
        val shuffledIcons = (selectedIcons + selectedIcons).toMutableList()
        shuffledIcons.shuffle()

        setupBoard(shuffledIcons.toIntArray(), BooleanArray(totalTiles) { false })
    }

    private fun setupBoard(icons: IntArray, revealedStates: BooleanArray) {
        gridLayout.removeAllViews()
        tiles.clear()
        gridLayout.columnCount = cols
        gridLayout.rowCount = rows

        var matchesFound = 0

        for (i in icons.indices) {
            val row = i / cols
            val col = i % cols

            val btn = ImageButton(gridLayout.context).also {
                it.tag = "${row}x${col}"
                val params = GridLayout.LayoutParams()
                params.width = 0
                params.height = 0
                params.setGravity(Gravity.CENTER)
                params.columnSpec = GridLayout.spec(col, 1, 1f)
                params.rowSpec = GridLayout.spec(row, 1, 1f)
                it.layoutParams = params
                it.setOnClickListener(::onClickTile)
                gridLayout.addView(it)
            }

            val tile = Tile(btn, icons[i], deckResource)
            tiles[btn.tag.toString()] = tile

            // Przywracamy stan odkrycia
            tile.revealed = revealedStates[i]
            if (tile.revealed) {
                matchesFound++
            }
        }
        // Aktualizujemy logikę (dzielimy przez 2, bo matches liczy pary)
        logic.setMatches(matchesFound / 2)
    }

    private fun onClickTile(v: View) {
        if (isLocked) return
        val tile = tiles[v.tag]
        if (tile?.revealed == true) return

        matchedPair.push(tile)
        val matchResult = logic.process { tile?.tileResource ?: -1 }
        onGameChangeStateListener(MemoryGameEvent(matchedPair.toList().filterNotNull(), matchResult))

        if (matchResult != GameStates.Matching) {
            matchedPair.clear()
        }
    }

    fun setOnGameChangeListener(listener: (event: MemoryGameEvent) -> Unit) {
        onGameChangeStateListener = listener
    }

    // Metody zapisu stanu
    fun getStateIcons(): IntArray {
        val result = IntArray(rows * cols)
        for (i in 0 until (rows * cols)) {
            val r = i / cols
            val c = i % cols
            result[i] = tiles["${r}x${c}"]?.tileResource ?: 0
        }
        return result
    }

    fun getStateRevealed(): BooleanArray {
        val result = BooleanArray(rows * cols)
        for (i in 0 until (rows * cols)) {
            val r = i / cols
            val c = i % cols
            result[i] = tiles["${r}x${c}"]?.revealed ?: false
        }
        return result
    }
}