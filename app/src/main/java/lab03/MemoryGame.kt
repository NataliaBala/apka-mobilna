package lab03

import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import pl.wsei.pam.R
import java.util.*

data class Tile(val button: ImageButton, val tileResource: Int, val deckResource: Int) {
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

    fun setMatches(value: Int) {
        matches = value
    }

    fun getMatches(): Int = matches

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
}

data class MemoryGameEvent(
    val tiles: List<Tile>,
    val state: GameStates
)

class MemoryBoardView(
    private val gridLayout: GridLayout,
    private val cols: Int,
    private val rows: Int,
    savedIcons: IntArray? = null,
    savedRevealed: BooleanArray? = null
) {
    private val tiles: MutableMap<String, Tile> = mutableMapOf()
    private val icons: List<Int> = listOf(
        R.drawable.baseline_rocket_launch_24,
        R.drawable.baseline_audiotrack_24,
        R.drawable.baseline_rocket_24,
        android.R.drawable.ic_menu_gallery,
        android.R.drawable.ic_menu_camera,
        android.R.drawable.ic_menu_slideshow,
        android.R.drawable.ic_menu_manage,
        android.R.drawable.ic_menu_compass,
        android.R.drawable.ic_menu_directions,
        android.R.drawable.ic_menu_mapmode
    )
    private val deckResource: Int = R.drawable.deck
    private var onGameChangeStateListener: (MemoryGameEvent) -> Unit = { }
    private val matchedPair: Stack<Tile> = Stack<Tile>()
    private val logic: MemoryGameLogic = MemoryGameLogic(cols * rows / 2)

    var isLocked: Boolean = false

    init {
        val boardIcons: MutableList<Int> = mutableListOf()
        val totalTiles = cols * rows

        if (savedIcons != null && savedIcons.size == totalTiles) {
            boardIcons.addAll(savedIcons.toList())
        } else {
            val neededPairs = totalTiles / 2
            val iconsToUse = mutableListOf<Int>()
            while (iconsToUse.size < neededPairs) {
                iconsToUse.addAll(icons.shuffled())
            }
            val finalPairs = iconsToUse.take(neededPairs)
            boardIcons.addAll(finalPairs)
            boardIcons.addAll(finalPairs)
            boardIcons.shuffle()
        }

        gridLayout.removeAllViews()
        gridLayout.columnCount = cols
        gridLayout.rowCount = rows

        var iconIndex = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val btn = ImageButton(gridLayout.context).also {
                    it.tag = "${row}x${col}"
                    // POPRAWKA: Użycie standardowego GridLayout.LayoutParams
                    val layoutParams = GridLayout.LayoutParams()
                    layoutParams.width = 0
                    layoutParams.height = 0
                    layoutParams.setGravity(Gravity.CENTER)
                    layoutParams.columnSpec = GridLayout.spec(col, 1, 1f)
                    layoutParams.rowSpec = GridLayout.spec(row, 1, 1f)
                    it.layoutParams = layoutParams
                    gridLayout.addView(it)
                }

                val iconRes = if (iconIndex < boardIcons.size) boardIcons[iconIndex] else icons[0]
                addTile(btn, iconRes)

                if (savedRevealed != null && iconIndex < savedRevealed.size && savedRevealed[iconIndex]) {
                    tiles[btn.tag.toString()]?.revealed = true
                }
                iconIndex++
            }
        }

        if (savedRevealed != null) {
            val revealedCount = savedRevealed.count { it }
            logic.setMatches(revealedCount / 2)
        }
    }

    fun getStateIcons(): IntArray {
        val state = mutableListOf<Int>()
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                tiles["${row}x${col}"]?.let { state.add(it.tileResource) }
            }
        }
        return state.toIntArray()
    }

    fun getStateRevealed(): BooleanArray {
        val state = mutableListOf<Boolean>()
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                tiles["${row}x${col}"]?.let { state.add(it.revealed) }
            }
        }
        return state.toBooleanArray()
    }

    private fun onClickTile(v: View) {
        if (isLocked) return

        val tile = tiles[v.tag] ?: return
        if (tile.revealed) return

        matchedPair.push(tile)
        val matchResult = logic.process {
            tile.tileResource
        }

        val eventTiles = mutableListOf<Tile>()
        eventTiles.addAll(matchedPair)

        onGameChangeStateListener(MemoryGameEvent(eventTiles, matchResult))

        if (matchResult != GameStates.Matching) {
            matchedPair.clear()
        }
    }

    fun setOnGameChangeListener(listener: (event: MemoryGameEvent) -> Unit) {
        onGameChangeStateListener = listener
    }

    private fun addTile(button: ImageButton, resourceImage: Int) {
        button.setOnClickListener(::onClickTile)
        val tile = Tile(button, resourceImage, deckResource)
        tiles[button.tag.toString()] = tile
    }
}