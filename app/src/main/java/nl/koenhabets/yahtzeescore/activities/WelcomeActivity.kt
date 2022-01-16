package nl.koenhabets.yahtzeescore.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import nl.koenhabets.yahtzeescore.R
import android.view.WindowManager

import android.os.Build
import android.util.Log
import android.view.View
import android.view.Window
import kotlinx.android.synthetic.main.activity_welcome.*


class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        changeStatusBarColor()
        supportActionBar?.hide()

        checkBoxStartMultiplayer.setOnClickListener {
            if (checkBoxStartMultiplayer.isChecked) {
                tVM1.visibility = View.VISIBLE
                tVM2.visibility = View.VISIBLE
            } else {
                tVM1.visibility = View.INVISIBLE
                tVM2.visibility = View.INVISIBLE
            }
        }

        val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        Log.i("multiplayer", sharedPref.getBoolean("multiplayer", false).toString())
        checkBoxStartMultiplayer.setOnCheckedChangeListener { button, b ->
            if (!b) {
                buttonOpenApp.visibility = View.VISIBLE
                buttonNext.visibility = View.INVISIBLE
            }
        }

        buttonNext.setOnClickListener {
            buttonOpenApp.visibility = View.VISIBLE
            buttonNext.visibility = View.INVISIBLE
            tVM1.visibility = View.INVISIBLE
            tVM2.visibility = View.INVISIBLE
            checkBoxStartMultiplayer.visibility = View.INVISIBLE
            editTextStartName.visibility = View.VISIBLE
            textViewStartName.visibility = View.VISIBLE
        }

        buttonOpenApp.setOnClickListener {
            val edit = sharedPref.edit()
            if (checkBoxStartMultiplayer.isChecked) {
                if (editTextStartName.text.toString().trim() != "") {
                    edit.putBoolean("multiplayer", checkBoxStartMultiplayer.isChecked)
                    edit.putBoolean("multiplayerAsked", checkBoxStartMultiplayer.isChecked)
                    edit.putBoolean("welcomeShown", true)
                    edit.putString("name", editTextStartName.text.toString())
                    edit.commit()
                    val myIntent = Intent(this, MainActivity::class.java)
                    this.startActivity(myIntent)
                    finish()
                } else {
                    editTextStartName.error = getString(R.string.username_required_error)
                }
            } else {
                edit.putBoolean("multiplayer", checkBoxStartMultiplayer.isChecked)
                edit.putBoolean("multiplayerAsked", checkBoxStartMultiplayer.isChecked)
                edit.putBoolean("welcomeShown", true)
                edit.commit()
                val myIntent = Intent(this, MainActivity::class.java)
                this.startActivity(myIntent)
                finish()
            }
        }
    }

    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }
}