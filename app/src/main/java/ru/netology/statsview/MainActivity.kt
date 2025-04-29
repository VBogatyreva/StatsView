package ru.netology.statsview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val view = findViewById<StatsView>(R.id.statsView)

        view.postDelayed({
            view.data = listOf(
                500F, 500F, 500F, 500F
            )
        }, 300)
    }
}