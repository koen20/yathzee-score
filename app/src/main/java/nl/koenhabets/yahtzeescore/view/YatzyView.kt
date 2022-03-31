package nl.koenhabets.yahtzeescore.view

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import android.widget.TextView
import nl.koenhabets.yahtzeescore.R

class YatzyView(context: Context, attributeSet: AttributeSet?) : ScoreView(
    context,
    attributeSet
), TextWatcher {
    private var editText1: EditText
    private var editText2: EditText
    private var editText3: EditText
    private var editText4: EditText
    private var editText5: EditText
    private var editText6: EditText
    private var editText21: EditText
    private var editText22: EditText
    private var editText23: EditText
    private var editText24: EditText
    private var editText25: EditText
    private var editText26: EditText
    private var editText27: EditText
    private var editText28: EditText
    private val editText29: EditText
    private val editTextBonus: EditText
    private val tvTotalLeft: TextView
    private val tvTotalRight: TextView
    private val tvBonus: TextView

    private var totalLeft = 0
    private var totalRight = 0

    init {
        inflate(context, R.layout.yatzy_scoresheet, this)
        setBackgroundColor(Color.TRANSPARENT)

        editText1 = findViewById(R.id.editText)
        editText2 = findViewById(R.id.editText3)
        editText3 = findViewById(R.id.editText4)
        editText4 = findViewById(R.id.editText5)
        editText5 = findViewById(R.id.editText6)
        editText6 = findViewById(R.id.editText7)

        editText21 = findViewById(R.id.editText21) // one pair
        editText22 = findViewById(R.id.editText22) // two pairs
        editText23 = findViewById(R.id.editText23) // 3 of a kind
        editText24 = findViewById(R.id.editText24) // 4 of a kind
        editText25 = findViewById(R.id.editText25) // Full house
        editText26 = findViewById(R.id.editText26) // Small Straight
        editText27 = findViewById(R.id.editText27) // Large Straight
        editText28 = findViewById(R.id.editText28) // Chance
        editText29 = findViewById(R.id.editText29) // Yatzy
        editTextBonus = findViewById(R.id.editTextBonus)

        editTextList.add(ScoreViewItem(editText1, "1", null, 5))
        editTextList.add(ScoreViewItem(editText2, "2", null))
        editTextList.add(ScoreViewItem(editText3, "3", null))
        editTextList.add(ScoreViewItem(editText4, "4", null))
        editTextList.add(ScoreViewItem(editText5, "5", null))
        editTextList.add(ScoreViewItem(editText6, "6", null))
        editTextList.add(ScoreViewItem(editText21, "21", null))
        editTextList.add(ScoreViewItem(editText22, "22", null))
        editTextList.add(ScoreViewItem(editText23, "23", null))
        editTextList.add(ScoreViewItem(editText24, "24", null))
        editTextList.add(ScoreViewItem(editText25, "25", null))
        editTextList.add(ScoreViewItem(editText26, "26", 15))
        editTextList.add(ScoreViewItem(editText27, "27", 20))
        editTextList.add(ScoreViewItem(editText28, "28", null))
        editTextList.add(ScoreViewItem(editText29, "29", 50))

        tvTotalLeft = findViewById(R.id.textViewTotalLeft)
        tvTotalRight = findViewById(R.id.textViewTotalRight)
        tvBonus = findViewById(R.id.textViewBonus)

        addListeners()
        calculateTotal()
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        calculateTotal()
        if (listener !== null) {
            listener!!.onScore(totalLeft + totalRight)
            listener!!.onScoreJson(createJsonScores())
        }
    }

    override fun afterTextChanged(p0: Editable?) {

    }

    private fun calculateTotal() {
        totalLeft =
            getTextInt(editText1) + getTextInt(editText2) + getTextInt(editText3) + getTextInt(
                editText4
            ) + getTextInt(editText5) + getTextInt(editText6)
        totalRight = (getTextInt(editText21) + getTextInt(editText22) + getTextInt(editText23)
                + getTextInt(editText24) + getTextInt(editText25) + getTextInt(editText26) + getTextInt(
            editText27
        ) + getTextInt(editText28) + getTextInt(editText29))
        if (totalLeft >= 63) {
            editTextBonus.setText(50.toString())
            totalLeft += 50
        } else {
            editTextBonus.setText(resources.getString(R.string.bonus_value, 63 - totalLeft))
        }
        tvTotalLeft.text = resources.getString(R.string.left, totalLeft)
        tvTotalRight.text = resources.getString(R.string.right, totalRight)

        validateScores()
    }

    override fun validateScores() {
        super.validateScores()

        val color = getColor()
        if (getTextInt(editText2) > 10 || getTextInt(editText2) % 2 != 0) {
            editText2.setTextColor(Color.RED)
        } else {
            editText2.setTextColor(color)
        }
        if (getTextInt(editText3) > 15 || getTextInt(editText3) % 3 != 0) {
            editText3.setTextColor(Color.RED)
        } else {
            editText3.setTextColor(color)
        }
        if (getTextInt(editText4) > 20 || getTextInt(editText4) % 4 != 0) {
            editText4.setTextColor(Color.RED)
        } else {
            editText4.setTextColor(color)
        }
        if (getTextInt(editText5) > 25 || getTextInt(editText5) % 5 != 0) {
            editText5.setTextColor(Color.RED)
        } else {
            editText5.setTextColor(color)
        }
        if (getTextInt(editText6) > 30 || getTextInt(editText6) % 6 != 0) {
            editText6.setTextColor(Color.RED)
        } else {
            editText6.setTextColor(color)
        }
    }

    override fun setTotalVisibility(visible: Boolean) {
        if (visible) {
            tvTotalLeft.visibility = VISIBLE
            tvTotalRight.visibility = VISIBLE
            editTextBonus.visibility = VISIBLE
            tvBonus.visibility = VISIBLE
        } else {
            tvTotalLeft.visibility = GONE
            tvTotalRight.visibility = GONE
            editTextBonus.visibility = GONE
            tvBonus.visibility = GONE
        }
    }
}