package com.catelt.mome.utils

import android.net.Uri
import com.catelt.mome.BuildConfig
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase

object DynamicLink {
    private const val BASE_URI_PREFIX = "https://mome.page.link"
    fun createDynamicLinkProduct(mediaId: Int,isMovie: Boolean,isUpcoming: String = "", handlerLink: (String) -> Unit) {
        Firebase.dynamicLinks.shortLinkAsync {
            link = Uri.parse("$BASE_LINK_MEDIA$isMovie/$isUpcoming/$mediaId")
            domainUriPrefix = BASE_URI_PREFIX
            androidParameters(BuildConfig.APPLICATION_ID) {
                minimumVersion = 26
            }
        }.addOnSuccessListener {
            handlerLink(it.shortLink.toString())
        }.addOnFailureListener {}
    }
}