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
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView


class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        changeStatusBarColor()
        supportActionBar?.hide()
        val buttonOpen = findViewById<Button>(R.id.buttonOpenApp)
        val buttonNext = findViewById<Button>(R.id.buttonNext)
        val checkBoxMultiplayer = findViewById<CheckBox>(R.id.checkBoxMultiplayer)
        val tVMultiplayer1 = findViewById<TextView>(R.id.tVM1)
        val tVMultiplayer2 = findViewById<TextView>(R.id.tVM2)
        val tVName = findViewById<TextView>(R.id.textViewName)
        val editTextName = findViewById<EditText>(R.id.editTextName)

        checkBoxMultiplayer.setOnClickListener {
            if (checkBoxMultiplayer.isChecked) {
                tVMultiplayer1.visibility = View.VISIBLE
                tVMultiplayer2.visibility = View.VISIBLE
            } else {
                tVMultiplayer1.visibility = View.INVISIBLE
                tVMultiplayer2.visibility = View.INVISIBLE
            }
        }

        val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        Log.i("multiplayer", sharedPref.getBoolean("multiplayer", false).toString())
        checkBoxMultiplayer.setOnCheckedChangeListener { button, b ->
            if (!b) {
                buttonOpen.visibility = View.VISIBLE
                buttonNext.visibility = View.INVISIBLE
            }
        }

        buttonNext.setOnClickListener {
            buttonOpen.visibility = View.VISIBLE
            buttonNext.visibility = View.INVISIBLE
            tVMultiplayer1.visibility = View.INVISIBLE
            tVMultiplayer2.visibility = View.INVISIBLE
            checkBoxMultiplayer.visibility = View.INVISIBLE
            editTextName.visibility = View.VISIBLE
            tVName.visibility = View.VISIBLE
        }

        buttonOpen.setOnClickListener {
            val edit = sharedPref.edit()
            if (checkBoxMultiplayer.isChecked) {
                if (editTextName.text.toString().trim() != "") {
                    edit.putBoolean("multiplayer", checkBoxMultiplayer.isChecked)
                    edit.putBoolean("multiplayerAsked", checkBoxMultiplayer.isChecked)
                    edit.putBoolean("welcomeShown", true)
                    edit.putString("name", editTextName.text.toString())
                    edit.commit()
                    val myIntent = Intent(this, MainActivity::class.java)
                    this.startActivity(myIntent)
                    finish()
                } else {
                    editTextName.error = getString(R.string.username_required_error)
                }
            } else {
                edit.putBoolean("multiplayer", checkBoxMultiplayer.isChecked)
                edit.putBoolean("multiplayerAsked", checkBoxMultiplayer.isChecked)
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