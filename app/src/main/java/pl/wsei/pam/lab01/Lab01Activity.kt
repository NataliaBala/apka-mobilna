package pl.wsei.pam.lab01

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import pl.wsei.pam.R

class Lab01Activity : AppCompatActivity() {

    private lateinit var mLayout: LinearLayout
    private lateinit var mTitle: TextView
    private lateinit var mProgress: ProgressBar
    private val mBoxes: MutableList<CheckBox> = mutableListOf()
    private val mButtons: MutableList<Button> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab01)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mLayout = findViewById(R.id.main)

        // Tytuł
        mTitle = TextView(this)
        mTitle.text = "Laboratorium 1"
        mTitle.textSize = 24f
        mTitle.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(20, 20, 20, 20)
        mTitle.layoutParams = params

        mLayout.addView(mTitle)

        // ProgressBar
        mProgress = ProgressBar(
            this,
            null,
            androidx.appcompat.R.attr.progressBarStyle,
            androidx.appcompat.R.style.Widget_AppCompat_ProgressBar_Horizontal
        )
        mLayout.addView(mProgress)

        // Checkboxy i Przyciski (Lab 2)
        for (i in 1..6) {
            val row = LinearLayout(this)
            row.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            row.orientation = LinearLayout.HORIZONTAL

            val checkBox = CheckBox(this)
            checkBox.text = "Zadanie $i"
            checkBox.isEnabled = false
            mBoxes.add(checkBox)
            row.addView(checkBox)

            val testButton = Button(this).also {
                it.text = "Testuj"
                it.setOnClickListener {
                    runTest(i, checkBox)
                }
            }
            mButtons.add(testButton)
            row.addView(testButton)

            mLayout.addView(row)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun runTest(taskNumber: Int, checkBox: CheckBox) {
        val isCorrect = when (taskNumber) {
            1 -> task11()
            2 -> task12()
            3 -> task13()
            4 -> task14()
            5 -> task15()
            6 -> task16()
            else -> false
        }

        if (isCorrect) {
            checkBox.isChecked = true
            mProgress.progress += 100 / 6
            Toast.makeText(this, "Zadanie $taskNumber zaliczone", Toast.LENGTH_SHORT).show()
        }
    }

    // Przykładowe implementacje zadań (użytkownik powinien je uzupełnić)
    private fun task11(): Boolean = true
    private fun task12(): Boolean = true
    private fun task13(): Boolean = true
    private fun task14(): Boolean = true
    private fun task15(): Boolean = true
    private fun task16(): Boolean = true
}
