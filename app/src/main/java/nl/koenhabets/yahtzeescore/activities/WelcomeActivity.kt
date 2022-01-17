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
import nl.koenhabets.yahtzeescore.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        changeStatusBarColor()
        supportActionBar?.hide()

        binding.checkBoxStartMultiplayer.setOnClickListener {
            if (binding.checkBoxStartMultiplayer.isChecked) {
                binding.tVM1.visibility = View.VISIBLE
                binding.tVM2.visibility = View.VISIBLE
            } else {
                binding.tVM1.visibility = View.INVISIBLE
                binding.tVM2.visibility = View.INVISIBLE
            }
        }

        val sharedPref = getSharedPreferences("nl.koenhabets.yahtzeescore", Context.MODE_PRIVATE);
        Log.i("multiplayer", sharedPref.getBoolean("multiplayer", false).toString())
        binding.checkBoxStartMultiplayer.setOnCheckedChangeListener { button, b ->
            if (!b) {
                binding.buttonOpenApp.visibility = View.VISIBLE
                binding.buttonNext.visibility = View.INVISIBLE
            }
        }

        binding.buttonNext.setOnClickListener {
            binding.buttonOpenApp.visibility = View.VISIBLE
            binding.buttonNext.visibility = View.INVISIBLE
            binding.tVM1.visibility = View.INVISIBLE
            binding.tVM2.visibility = View.INVISIBLE
            binding.checkBoxStartMultiplayer.visibility = View.INVISIBLE
            binding.editTextStartName.visibility = View.VISIBLE
            binding.textViewStartName.visibility = View.VISIBLE
        }

        binding.buttonOpenApp.setOnClickListener {
            val edit = sharedPref.edit()
            if (binding.checkBoxStartMultiplayer.isChecked) {
                if (binding.editTextStartName.text.toString().trim() != "") {
                    edit.putBoolean("multiplayer", binding.checkBoxStartMultiplayer.isChecked)
                    edit.putBoolean("multiplayerAsked", binding.checkBoxStartMultiplayer.isChecked)
                    edit.putBoolean("welcomeShown", true)
                    edit.putString("name", binding.editTextStartName.text.toString())
                    edit.commit()
                    val myIntent = Intent(this, MainActivity::class.java)
                    this.startActivity(myIntent)
                    finish()
                } else {
                    binding.editTextStartName.error = getString(R.string.username_required_error)
                }
            } else {
                edit.putBoolean("multiplayer", binding.checkBoxStartMultiplayer.isChecked)
                edit.putBoolean("multiplayerAsked", binding.checkBoxStartMultiplayer.isChecked)
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