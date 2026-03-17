package pl.wsei.pam

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class Lab01Activity : AppCompatActivity() {

    private lateinit var mLayout: LinearLayout
    private lateinit var mTitle: TextView
    private lateinit var mProgress: ProgressBar
    private val mBoxes: MutableList<CheckBox> = mutableListOf()
    private val mButtons: MutableList<Button> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab01)

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
        mProgress = ProgressBar(this)
        mLayout.addView(mProgress)

        // Checkboxy
        for (i in 1..6) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL

            val checkBox = CheckBox(this)
            checkBox.text = "Zadanie $i"
            checkBox.isEnabled = false

            mBoxes.add(checkBox)
            row.addView(checkBox)

            mLayout.addView(row)
        }
    }
}