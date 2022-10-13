package com.catelt.mome.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import com.catelt.mome.MainActivity
import com.catelt.mome.R
import com.catelt.mome.data.model.account.UserManager
import com.catelt.mome.data.paging.ConfigDataSource
import com.catelt.mome.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    @Inject
    lateinit var configDataSource: ConfigDataSource

    @Inject
    lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.findFragmentById(R.id.nav_auth_fragment) as NavHostFragment? ?: return

        if (userManager.isLogged()){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
}