package lab03

import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pl.wsei.pam.R
import java.util.*

class Lab03Activity : AppCompatActivity() {
    private lateinit var mBoard: GridLayout
    private lateinit var mBoardModel: MemoryBoardView

    private var completionPlayer: MediaPlayer? = null
    private var negativePlayer: MediaPlayer? = null
    private var isSound: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab03)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mBoard = findViewById(R.id.lab03_grid)
        val rows = intent.getIntExtra("rows", 3)
        val columns = intent.getIntExtra("columns", 3)

        // Przywracanie stanu (w tym stanu przycisku dźwięku)
        if (savedInstanceState != null) {
            isSound = savedInstanceState.getBoolean("is_sound", true)
            val savedIcons = savedInstanceState.getIntArray("icons")
            val savedRevealed = savedInstanceState.getBooleanArray("revealed")

            mBoardModel = if (savedIcons != null && savedRevealed != null) {
                MemoryBoardView(mBoard, columns, rows, savedIcons, savedRevealed)
            } else {
                MemoryBoardView(mBoard, columns, rows)
            }
        } else {
            mBoardModel = MemoryBoardView(mBoard, columns, rows)
        }

        mBoardModel.setOnGameChangeListener { e ->
            runOnUiThread {
                when (e.state) {
                    GameStates.Matching -> {
                        e.tiles.forEach { it.revealed = true }
                    }
                    GameStates.Match -> {
                        if (isSound) {
                            completionPlayer?.seekTo(0)
                            completionPlayer?.start()
                        }
                        e.tiles.forEach { it.revealed = true }
                    }
                    GameStates.NoMatch -> {
                        if (isSound) {
                            negativePlayer?.seekTo(0)
                            negativePlayer?.start()
                        }

                        mBoardModel.isLocked = true
                        e.tiles.forEach { it.revealed = true }

                        Timer().schedule(object : TimerTask() {
                            override fun run() {
                                runOnUiThread {
                                    e.tiles.forEach { it.revealed = false }
                                    mBoardModel.isLocked = false
                                }
                            }
                        }, 1000)
                    }
                    GameStates.Finished -> {
                        if (isSound) {
                            completionPlayer?.seekTo(0)
                            completionPlayer?.start()
                        }
                        e.tiles.forEach { it.revealed = true }
                        Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Używamy applicationContext dla lepszej stabilności MediaPlayer
        if (completionPlayer == null) {
            completionPlayer = MediaPlayer.create(applicationContext, R.raw.completion)
        }
        if (negativePlayer == null) {
            negativePlayer = MediaPlayer.create(applicationContext, R.raw.negative_guitar)
        }

        // Jeśli MediaPlayer.create zwróci null, oznacza to problem z plikiem lub emulatorem
        if (completionPlayer == null || negativePlayer == null) {
            Toast.makeText(this, "Błąd inicjalizacji dźwięku!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        completionPlayer?.release()
        negativePlayer?.release()
        completionPlayer = null
        negativePlayer = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.board_activity_menu, menu)
        // Ustawienie poprawnej ikony przy starcie na podstawie isSound
        val soundItem = menu.findItem(R.id.board_activity_sound)
        soundItem?.setIcon(if (isSound) R.drawable.baseline_volume_up_24 else R.drawable.baseline_volume_off_24)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.board_activity_sound -> {
                isSound = !isSound
                item.setIcon(if (isSound) R.drawable.baseline_volume_up_24 else R.drawable.baseline_volume_off_24)
                Toast.makeText(this, "Sound ${if (isSound) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("is_sound", isSound)
        outState.putIntArray("icons", mBoardModel.getStateIcons())
        outState.putBooleanArray("revealed", mBoardModel.getStateRevealed())
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}