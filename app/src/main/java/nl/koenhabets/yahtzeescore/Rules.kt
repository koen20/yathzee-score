package nl.koenhabets.yahtzeescore

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import nl.koenhabets.yahtzeescore.data.Game
import java.util.*

class Rules {
    companion object {
        fun getRules(game: Game): String? {
            if (game == Game.Yahtzee || game == Game.YahtzeeBonus) {
                return when (Locale.getDefault().language) {
                    "nl" -> ("https://nl.wikipedia.org/wiki/Yahtzee#Spelverloop")
                    "fr" -> ("https://fr.wikipedia.org/wiki/Yahtzee#R%C3%A8gles")
                    "de" -> ("https://de.wikipedia.org/wiki/Kniffel#Spielregeln")
                    "pl" -> ("https://pl.wikipedia.org/wiki/Ko%C5%9Bci_(gra)#Klasyczne_zasady_gry_(Yahtzee)")
                    "it" -> ("https://it.wikipedia.org/wiki/Yahtzee")
                    else -> ("https://en.wikipedia.org/wiki/Yahtzee#Rules")
                }
            } else if (game == Game.Yatzy) {
                return when (Locale.getDefault().language) {
                    "no" -> ("https://no.wikipedia.org/wiki/Yatzy#Kombinasjonstyper_og_poengberegning")
                    "da" -> ("https://da.wikipedia.org/wiki/Yatzy#Regler")
                    else -> ("https://en.wikipedia.org/wiki/Yatzy#Scoring")
                }
            }
            return null
        }

        fun openUrl(url: String, context: Context) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(browserIntent)
            } catch (exception: ActivityNotFoundException) {
                val toast = Toast.makeText(context, R.string.browser_fail, Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }
}