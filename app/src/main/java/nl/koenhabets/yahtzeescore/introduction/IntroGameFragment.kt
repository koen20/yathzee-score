package nl.koenhabets.yahtzeescore.introduction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import nl.koenhabets.yahtzeescore.R
import nl.koenhabets.yahtzeescore.data.Game

class IntroGameFragment : Fragment() {
    private lateinit var radioGroup: RadioGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_intro_game, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        radioGroup = view.findViewById(R.id.radioGroupGame)
    }

    fun getGame(): Game {
        var game: Game = Game.Yahtzee
        when (radioGroup.checkedRadioButtonId) {
            R.id.radioYahtzee -> {
                game = Game.Yahtzee
            }
            R.id.radioYahtzeeBonus -> {
                game = Game.YahtzeeBonus
            }
            R.id.radioYatzy -> {
                game = Game.Yatzy
            }
        }
        return game
    }

    companion object {
        fun newInstance(): IntroGameFragment {
            return IntroGameFragment()
        }
    }
}