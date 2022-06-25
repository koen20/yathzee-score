package nl.koenhabets.yahtzeescore.view

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import nl.koenhabets.yahtzeescore.R
import nl.koenhabets.yahtzeescore.data.Game
import org.json.JSONException
import org.json.JSONObject

open class ScoreView(context: Context, attributeSet: AttributeSet?) : ConstraintLayout(
    context,
    attributeSet
), TextWatcher {
    var editDisabled = false
    var listener: ScoreListener? = null
    val editTextList: MutableList<ScoreViewItem> = ArrayList()

    interface ScoreListener {
        fun onScoreJson(scores: JSONObject)
        fun onScore(score: Int)
    }

    fun setScoreListener(listener: ScoreListener) {
        this.listener = listener
    }

    fun addListeners() {
        editTextList.forEach {
            it.editText.addTextChangedListener(this)
            if (it.defaultValue !== null) {
                it.editText.setOnClickListener { _ ->
                    setDefaultValue(it.editText, it.defaultValue)
                }
            }
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun afterTextChanged(p0: Editable?) {
    }

    fun setDefaultValue(editTextD: EditText, value: Int) {
        if (!editDisabled) {
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
    }

    fun getTextInt(editText: EditText): Int {
        var d = 0
        try {
            d = editText.text.toString().toInt()
        } catch (ignored: Exception) {
        }
        return d
    }

    fun getColor(): Int {
        var color = Color.BLACK
        // change editText color to white if there is a black theme
        val nightModeFlags = this.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            color = Color.WHITE
        }

        return color
    }

    fun disableEdit() {
        editDisabled = true
        editTextList.forEach {
            it.editText.inputType = InputType.TYPE_NULL
        }
    }

    fun clearScores() {
        editTextList.forEach {
            it.editText.setText("")
        }
    }

    fun setScores(jsonObject: JSONObject) {
        Log.i("score", "read$jsonObject")
        try {
            editTextList.forEach {
                it.editText.setText(jsonObject.getString(it.id))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun createJsonScores(): JSONObject {
        val jsonObject = JSONObject()
        Log.i("score", "saving")
        try {
            editTextList.forEach {
                jsonObject.put(it.id, it.editText.text.toString())
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject
    }

    open fun validateScores() {
        val color = getColor()
        editTextList.forEach {
            // scores with a default value do not need to be validated
            if (it.defaultValue == null) {
                if (getTextInt(it.editText) > it.maxScore) {
                    it.editText.setTextColor(Color.RED)
                } else {
                    it.editText.setTextColor(color)
                }
            }
        }
    }

    open fun setSpecialFieldVisibility(visible: Boolean) {

    }

    open fun setTotalVisibility(visible: Boolean) {

    }

    companion object {
        fun getView(game: Game, context: Context): ScoreView {
            val scoreView: ScoreView
            when (game) {
                Game.Yahtzee -> {
                    scoreView = YahtzeeView(context, null)
                    scoreView.setSpecialFieldVisibility(false)
                }
                Game.YahtzeeBonus -> {
                    scoreView = YahtzeeView(context, null)
                    scoreView.setSpecialFieldVisibility(true)
                }
                Game.Yatzy -> {
                    scoreView = YatzyView(context, null)
                }
            }

            scoreView.id = View.generateViewId()
            scoreView.layoutParams = ViewGroup.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            return scoreView
        }
    }
}