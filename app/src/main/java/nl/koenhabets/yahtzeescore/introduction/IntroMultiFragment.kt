package nl.koenhabets.yahtzeescore.introduction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import nl.koenhabets.yahtzeescore.R

class IntroMultiFragment : Fragment() {
    private lateinit var checkBox: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_intro_multi, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkBox = view.findViewById(R.id.checkBoxStartMultiplayer)
    }

    fun multiplayerEnabled(): Boolean {
        return checkBox.isChecked
    }

    companion object {
        fun newInstance(): IntroMultiFragment {
            return IntroMultiFragment()
        }
    }
}