package nl.koenhabets.yahtzeescore

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import org.json.JSONObject

class ScoresView(context: Context, attributeSet: AttributeSet) : ConstraintLayout(
    context,
    attributeSet
), TextWatcher {
    lateinit var listener: ScoreListener
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
    private val editTextBonus: EditText
    private val tvTotalLeft: TextView
    private val tvTotalRight: TextView
    private val tvYahtzeeBonus: TextView

    private var totalLeft = 0
    private var totalRight = 0

    init {
        inflate(context, R.layout.yahtzee_scoresheet, this)
        setBackgroundColor(Color.TRANSPARENT)

        editText1 = findViewById(R.id.editText)
        editText2 = findViewById(R.id.editText3)
        editText3 = findViewById(R.id.editText4)
        editText4 = findViewById(R.id.editText5)
        editText5 = findViewById(R.id.editText6)
        editText6 = findViewById(R.id.editText7)

        editText21 = findViewById(R.id.editText9)
        editText22 = findViewById(R.id.editText10)
        editText23 = findViewById(R.id.editText8)
        editText24 = findViewById(R.id.editText11)
        editText25 = findViewById(R.id.editText12)
        editText26 = findViewById(R.id.editText13)
        editText27 = findViewById(R.id.editText14)
        editText28 = findViewById(R.id.editText16)
        editTextBonus = findViewById(R.id.editTextBonus)

        editText1.addTextChangedListener(this)
        editText2.addTextChangedListener(this)
        editText3.addTextChangedListener(this)
        editText4.addTextChangedListener(this)
        editText5.addTextChangedListener(this)
        editText6.addTextChangedListener(this)
        editText21.addTextChangedListener(this)
        editText22.addTextChangedListener(this)
        editText23.addTextChangedListener(this)
        editText24.addTextChangedListener(this)
        editText25.addTextChangedListener(this)
        editText26.addTextChangedListener(this)
        editText27.addTextChangedListener(this)
        editText28.addTextChangedListener(this)

        editText23.setOnClickListener {
            setDefaultValue(editText23, 25)
        }
        editText24.setOnClickListener {
            setDefaultValue(editText24, 30)
        }
        editText25.setOnClickListener {
            setDefaultValue(editText25, 40)
        }
        editText26.setOnClickListener {
            setDefaultValue(editText26, 50)
        }

        tvTotalLeft = findViewById(R.id.textViewTotalLeft)
        tvTotalRight = findViewById(R.id.textViewTotalRight)
        tvYahtzeeBonus = findViewById(R.id.textView7)
    }

    interface ScoreListener {
        fun onScoreJson(scores: JSONObject)
        fun onScore(score: Int)
    }

    fun setScoreListener(listener: ScoreListener) {
        this.listener = listener
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
        calculateTotal()
        listener.onScore(totalLeft + totalRight)
        listener.onScoreJson(createJsonScores())
    }

    override fun afterTextChanged(p0: Editable?) {

    }

    private fun setDefaultValue(editTextD: EditText, value: Int) {
        var number = -1
        try {
            number = editTextD.text.toString().toInt()
        } catch (ignored: NumberFormatException) {
        }
        if (number == value) {
            editTextD.setText(0.toString())
        } else if (number == 0) {
            editTextD.setText("")
        } else {
            editTextD.setText(value.toString())
        }
        val sharedPref: SharedPreferences =
            context.getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE)
        if (!sharedPref.getBoolean("fieldHintShown", false)) {
            Snackbar.make(
                this,
                resources.getString(R.string.press_again),
                Snackbar.LENGTH_LONG
            ).show()
            sharedPref.edit().putBoolean("fieldHintShown", true).apply()
        }
    }

    private fun calculateTotal() {
        totalLeft =
            getTextInt(editText1) + getTextInt(editText2) + getTextInt(editText3) + getTextInt(
                editText4
            ) + getTextInt(editText5) + getTextInt(editText6)
        totalRight = (getTextInt(editText21) + getTextInt(editText22) + getTextInt(editText23)
                + getTextInt(editText24) + getTextInt(editText25) + getTextInt(editText26) + getTextInt(
            editText27
        ) + getTextInt(editText28))
        if (totalLeft >= 63) {
            editTextBonus.setText(35.toString())
            totalLeft += 35
        } else {
            editTextBonus.setText(resources.getString(R.string.bonus_value, 63 - totalLeft))
        }
        tvTotalLeft.text = resources.getString(R.string.left, totalLeft)
        tvTotalRight.text = resources.getString(R.string.right, totalRight)

        var color = Color.BLACK
        // change editText color to white if there is a black theme
        val nightModeFlags = this.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            color = Color.WHITE
        }
        if (getTextInt(editText1) > 5) {
            editText1.setTextColor(Color.RED)
        } else {
            editText1.setTextColor(color)
        }
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
        if (getTextInt(editText21) > 30) {
            editText21.setTextColor(Color.RED)
        } else {
            editText21.setTextColor(color)
        }
        if (getTextInt(editText22) > 30) {
            editText22.setTextColor(Color.RED)
        } else {
            editText22.setTextColor(color)
        }
        if (getTextInt(editText27) > 30) {
            editText27.setTextColor(Color.RED)
        } else {
            editText27.setTextColor(color)
        }
    }

    private fun getTextInt(editText: EditText): Int {
        var d = 0
        try {
            d = editText.text.toString().toInt()
        } catch (ignored: Exception) {
        }
        return d
    }

    fun setYahtzeeBonusVisibility(visible: Boolean) {
        if (visible) {
            tvYahtzeeBonus.visibility = VISIBLE
            editText28.visibility = VISIBLE
        } else {
            tvYahtzeeBonus.visibility = GONE
            editText28.visibility = GONE
        }
    }

    fun createJsonScores(): JSONObject {
        val jsonObject = JSONObject()
        Log.i("score", "saving")
        try {
            jsonObject.put("1", editText1.text.toString())
            jsonObject.put("2", editText2.text.toString())
            jsonObject.put("3", editText3.text.toString())
            jsonObject.put("4", editText4.text.toString())
            jsonObject.put("5", editText5.text.toString())
            jsonObject.put("6", editText6.text.toString())
            jsonObject.put("21", editText21.text.toString())
            jsonObject.put("22", editText22.text.toString())
            jsonObject.put("23", editText23.text.toString())
            jsonObject.put("24", editText24.text.toString())
            jsonObject.put("25", editText25.text.toString())
            jsonObject.put("26", editText26.text.toString())
            jsonObject.put("27", editText27.text.toString())
            jsonObject.put("28", editText28.text.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject
    }

    fun setScores(jsonObject: JSONObject) {
        Log.i("score", "read$jsonObject")
        try {
            editText1.setText(jsonObject.getString("1"))
            editText2.setText(jsonObject.getString("2"))
            editText3.setText(jsonObject.getString("3"))
            editText4.setText(jsonObject.getString("4"))
            editText5.setText(jsonObject.getString("5"))
            editText6.setText(jsonObject.getString("6"))
            editText21.setText(jsonObject.getString("21"))
            editText22.setText(jsonObject.getString("22"))
            editText23.setText(jsonObject.getString("23"))
            editText24.setText(jsonObject.getString("24"))
            editText25.setText(jsonObject.getString("25"))
            editText26.setText(jsonObject.getString("26"))
            editText27.setText(jsonObject.getString("27"))
            editText28.setText(jsonObject.getString("28"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun clearScores() {
        Log.i("clear", "tada")
        editText1.setText("")
        editText2.setText("")
        editText3.setText("")
        editText4.setText("")
        editText5.setText("")
        editText6.setText("")
        editText21.setText("")
        editText22.setText("")
        editText23.setText("")
        editText24.setText("")
        editText25.setText("")
        editText26.setText("")
        editText27.setText("")
        editText28.setText("")
    }
}