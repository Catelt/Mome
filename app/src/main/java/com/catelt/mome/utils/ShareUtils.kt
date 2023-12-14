package com.catelt.mome.utils

import android.app.Activity
import android.content.Intent
import com.catelt.mome.R
import com.catelt.mome.data.model.Presentable
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

object ShareUtils {
    fun share(presentable: Presentable, isMovie: Boolean, isUpcoming: String = "") = callbackFlow<String>{
        DynamicLink.createDynamicLinkProduct(presentable.id,isMovie,isUpcoming){
            trySend(it)
        }
        awaitClose {
            channel.close()
        }
    }

    fun shareUrl(activity: Activity,link: String,nameMedia: String){
        try {
//            val shareIntent = Intent(Intent.ACTION_SEND)
//            shareIntent.type = "text/plain"
//            val shareMessage = link.trimIndent()
//            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
//            activity.startActivity(
//                Intent.createChooser(
//                    shareIntent,
//                    getApplicationContext().getString(R.string.title_share,nameMedia)
//                )
//            )
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
}