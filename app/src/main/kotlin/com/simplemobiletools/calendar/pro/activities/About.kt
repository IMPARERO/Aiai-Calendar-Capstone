package com.simplemobiletools.calendar.pro.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.simplemobiletools.calendar.pro.R
import android.widget.TextView

class About : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about)

        val rickroll = findViewById<TextView>(R.id.rickroll)
        val ggggg = findViewById<TextView>(R.id.ggggg)
        val eeeee = findViewById<TextView>(R.id.eeeee)
        val gohome = findViewById<ImageButton>(R.id.gohome)
        val sssss = findViewById<TextView>(R.id.sssss)

        gohome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        rickroll.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/dQw4w9WgXcQ"))
            startActivity(intent)
        }

        ggggg.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/SimpleMobileTools/Simple-Calendar"))
            startActivity(intent)
        }

        eeeee.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:aiaidevelopers@duck.com")
            }
            startActivity(intent)
        }
        sssss.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:aiaidevelopers@duck.com")
            }
            startActivity(intent)
        }
    }
}
