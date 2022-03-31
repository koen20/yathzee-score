package nl.koenhabets.yahtzeescore.view

import android.widget.EditText

data class ScoreViewItem(
    val editText: EditText,
    val id: String,
    val defaultValue: Int?,
    val maxScore: Int = 30
)
