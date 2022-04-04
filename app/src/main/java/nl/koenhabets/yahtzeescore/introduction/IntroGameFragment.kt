package nl.koenhabets.yahtzeescore.introduction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import nl.koenhabets.yahtzeescore.R

class IntroGameFragment : Fragment() {
    //private lateinit var checkBox: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_intro_game, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //checkBox = view.findViewById(R.id.checkBoxStartMultiplayer2)
    }


    companion object {
        fun newInstance(): IntroGameFragment {
            return IntroGameFragment()
        }
    }
}