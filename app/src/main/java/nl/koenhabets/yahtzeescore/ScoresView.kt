package nl.koenhabets.yahtzeescore

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout

class ScoresView : ConstraintLayout {

    constructor(ctx: Context): super(ctx) {
    }

    constructor(ctx: Context, attributeSet: AttributeSet) : super(ctx, attributeSet) {
        inflate(context, R.layout.yahtzee_scoresheet, this)
        setBackgroundColor(Color.TRANSPARENT)
    }

    constructor(ctx: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(ctx, attributeSet, defStyleAttr) {
    }
}