package pl.wsei.pam.lab03

import android.os.Bundle
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pl.wsei.pam.R
import java.util.*

class Lab03Activity : AppCompatActivity() {
    private lateinit var mBoard: GridLayout
    private lateinit var mBoardModel: MemoryBoardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab03)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mBoard = findViewById(R.id.lab03_grid)
        val rows = intent.getIntExtra("rows", 3)
        val columns = intent.getIntExtra("columns", 3)

        if (savedInstanceState != null) {
            val savedIcons = savedInstanceState.getIntArray("icons")
            val savedRevealed = savedInstanceState.getBooleanArray("revealed")
            mBoardModel = MemoryBoardView(mBoard, columns, rows, savedIcons, savedRevealed)
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
                        e.tiles.forEach { it.revealed = true }
                    }
                    GameStates.NoMatch -> {
                        e.tiles.forEach { it.revealed = true }
                        Timer().schedule(object : TimerTask() {
                            override fun run() {
                                runOnUiThread {
                                    e.tiles.forEach { it.revealed = false }
                                }
                            }
                        }, 1000)
                    }
                    GameStates.Finished -> {
                        e.tiles.forEach { it.revealed = true }
                        Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray("icons", mBoardModel.getStateIcons())
        outState.putBooleanArray("revealed", mBoardModel.getStateRevealed())
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
