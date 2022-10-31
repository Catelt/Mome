package com.catelt.mome

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.catelt.mome.data.paging.ConfigDataSource
import com.catelt.mome.databinding.ActivityMainBinding
import com.catelt.mome.utils.BASE_LINK_MEDIA
import com.catelt.mome.utils.BUNDLE_DETAIL_UPCOMING
import com.catelt.mome.utils.BUNDLE_ID_MEDIA
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var host: NavHostFragment

    @Inject
    lateinit var configDataSource: ConfigDataSource

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        host = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return

        getDynamicLinkFromFirebase()

        val navController = host.navController
        setupBottomNavMenu(navController)
    }

    private fun getDynamicLinkFromFirebase() {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener() { pendingDynamicLinkData: PendingDynamicLinkData? ->
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }

                if (deepLink.toString().contains(BASE_LINK_MEDIA)) {
                    val info = deepLink.toString().replace(BASE_LINK_MEDIA, "").split("/")
                    println(info)
                    val isMovie = info[0].toBoolean()
                    val isUpcoming = info[1]
                    val mediaId = info[2].toInt()

                    if (isMovie){
                        if (isUpcoming.isNotBlank()){
                            host.findNavController().navigate(
                                R.id.detailMovieFragment,
                                bundleOf(
                                    BUNDLE_ID_MEDIA to mediaId,
                                    BUNDLE_DETAIL_UPCOMING to isUpcoming
                                )
                            )
                        }
                        else{
                            host.findNavController().navigate(
                                R.id.detailMovieFragment,
                                bundleOf(
                                    BUNDLE_ID_MEDIA to mediaId,
                                )
                            )
                        }
                    }
                    else{
                        host.findNavController().navigate(
                            R.id.detailTvShowFragment,
                            bundleOf(
                                BUNDLE_ID_MEDIA to mediaId,
                            )
                        )
                    }
                }
            }
    }

    private fun setupBottomNavMenu(navController: NavController) {
        binding.bottomNavigation.apply {
            setupWithNavController(navController)

            setOnItemSelectedListener { item ->
                NavigationUI.onNavDestinationSelected(item, navController)
                navController.popBackStack(item.itemId, inclusive = false)
                true
            }
        }
    }
}