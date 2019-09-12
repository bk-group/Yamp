package com.prush.justanotherplayer.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.prush.justanotherplayer.ui.main.MainActivity
import com.prush.justanotherplayer.R

class SplashActivity : AppCompatActivity(), SplashActivityView {


    override fun gotoMainActivity() {
        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 500)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val splashActivityPresenter = SplashActivityPresenter(this)
        splashActivityPresenter.gotoMainActivity()

    }
}