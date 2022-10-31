package com.catelt.mome.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.catelt.mome.MainActivity
import com.catelt.mome.data.model.account.UserManager
import com.catelt.mome.databinding.ActivitySplashBinding
import com.catelt.mome.ui.auth.AuthActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var userManager: UserManager

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { true }
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (userManager.isLogged()){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else{
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }
}