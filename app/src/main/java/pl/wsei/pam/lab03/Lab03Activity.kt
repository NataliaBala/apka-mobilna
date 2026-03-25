package pl.wsei.pam.lab03

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.animation.DecelerateInterpolator
import android.widget.GridLayout
import android.widget.ImageButton
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

        // POPRAWKA: Obsługa odczytu stanu gry
        if (savedInstanceState != null) {
            val savedIcons = savedInstanceState.getIntArray("icons")
            val savedRevealed = savedInstanceState.getBooleanArray("revealed")

            if (savedIcons != null && savedRevealed != null) {
                // To wywołanie wymaga dodatkowego konstruktora w MemoryBoardView.kt
                mBoardModel = MemoryBoardView(mBoard, columns, rows, savedIcons, savedRevealed)
            } else {
                mBoardModel = MemoryBoardView(mBoard, columns, rows)
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
                        if (isSound) completionPlayer?.start()
                        e.tiles.forEach { tile ->
                            tile.revealed = true
                            animatePairedButton(tile.button) {
                                // Akcja po animacji
                            }
                        }
                    }
                    GameStates.NoMatch -> {
                        if (isSound) negativePlayer?.start()
                        e.tiles.forEach { tile ->
                            tile.revealed = true
                            animateNoMatchButton(tile.button) {
                                runOnUiThread {
                                    tile.revealed = false
                                }
                            }
                        }
                    }
                    GameStates.Finished -> {
                        if (isSound) completionPlayer?.start()
                        e.tiles.forEach { it.revealed = true }
                        Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            completionPlayer = MediaPlayer.create(applicationContext, R.raw.completion)
            negativePlayer = MediaPlayer.create(applicationContext, R.raw.negative_guitar)
        } catch (e: Exception) {
            // Pliki dźwiękowe muszą znajdować się w res/raw/
        }
    }

    override fun onPause() {
        super.onPause()
        completionPlayer?.release()
        negativePlayer?.release()
        completionPlayer = null
        negativePlayer = null
    }

    private fun animatePairedButton(button: ImageButton, action: Runnable) {
        val set = AnimatorSet()
        val random = Random()
        button.pivotX = random.nextFloat() * 200f
        button.pivotY = random.nextFloat() * 200f

        val rotation = ObjectAnimator.ofFloat(button, "rotation", 1080f)
        val scalingX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 4f)
        val scalingY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 4f)
        val fade = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f)

        set.duration = 2000
        set.interpolator = DecelerateInterpolator()
        set.playTogether(rotation, scalingX, scalingY, fade)
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                button.scaleX = 1f
                button.scaleY = 1f
                button.alpha = 0.0f
                action.run()
            }
        })
        set.start()
    }

    private fun animateNoMatchButton(button: ImageButton, action: Runnable) {
        val set = AnimatorSet()
        val rotate1 = ObjectAnimator.ofFloat(button, "rotation", 0f, 20f)
        val rotate2 = ObjectAnimator.ofFloat(button, "rotation", 20f, -20f)
        val rotate3 = ObjectAnimator.ofFloat(button, "rotation", -20f, 0f)

        set.duration = 100
        set.playSequentially(rotate1, rotate2, rotate3)
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                action.run()
            }
        })
        set.start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.board_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.board_activity_sound -> {
                isSound = !isSound
                item.setIcon(if (isSound) R.drawable.baseline_volume_up_24 else R.drawable.baseline_volume_off_24)
                Toast.makeText(this, "Sound ${if (isSound) "on" else "off"}", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // POPRAWKA: Te metody muszą istnieć w MemoryBoardView.kt
        outState.putIntArray("icons", mBoardModel.getStateIcons())
        outState.putBooleanArray("revealed", mBoardModel.getStateRevealed())
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}