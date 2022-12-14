package com.catelt.mome.di

import android.content.Context
import com.catelt.mome.BuildConfig
import com.catelt.mome.R
import com.catelt.mome.data.remote.api.ApiParams
import com.catelt.mome.data.remote.api.DateJsonAdapter
import com.catelt.mome.data.remote.api.TMDB_API_KEY
import com.catelt.mome.data.remote.api.movie.TmdbMoviesApi
import com.catelt.mome.data.remote.api.movie.TmdbMoviesApiHelper
import com.catelt.mome.data.remote.api.movie.TmdbMoviesApiHelperImpl
import com.catelt.mome.data.remote.api.ophim.OphimApi
import com.catelt.mome.data.remote.api.ophim.OphimApiHelper
import com.catelt.mome.data.remote.api.ophim.OphimApiHelperImpl
import com.catelt.mome.data.remote.api.others.TmdbOthersApi
import com.catelt.mome.data.remote.api.others.TmdbOthersApiHelper
import com.catelt.mome.data.remote.api.others.TmdbOthersApiHelperImpl
import com.catelt.mome.data.remote.api.tvshow.TmdbTvShowsApi
import com.catelt.mome.data.remote.api.tvshow.TmdbTvShowsApiHelper
import com.catelt.mome.data.remote.api.tvshow.TmdbTvShowsApiHelperImpl
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import javax.inject.Singleton
import kotlin.time.toJavaDuration

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context) =
        Cache(context.cacheDir, ApiParams.cacheSize)

    @Provides
    @Singleton
    fun provideAuthenticationInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val requestUrl = request.url
            val url = requestUrl.newBuilder()
                .addQueryParameter("api_key", TMDB_API_KEY)
                .build()

            val modifiedRequest = request.newBuilder()
                .url(url)
                .build()
            chain.proceed(modifiedRequest)
        }
    }

    @Singleton
    @Provides
    fun provideChuckerInterceptor(@ApplicationContext context: Context): ChuckerInterceptor {
        val collector = ChuckerCollector(
            context = context,
            showNotification = true,
            retentionPeriod = RetentionManager.Period.ONE_HOUR
        )

        return ChuckerInterceptor.Builder(context)
            .collector(collector)
            .maxContentLength(250_000L)
            .redactHeaders("Auth-Token", "Bearer")
            .alwaysReadResponseBody(true)
            .build()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        cache: Cache,
        authenticationInterceptor: Interceptor,
        chuckerInterceptor: ChuckerInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                addInterceptor(loggingInterceptor)
            }
        }
        .addInterceptor(chuckerInterceptor)
        .addInterceptor(authenticationInterceptor)
        .cache(cache)
        .connectTimeout(ApiParams.Timeouts.connect.toJavaDuration())
        .writeTimeout(ApiParams.Timeouts.write.toJavaDuration())
        .readTimeout(ApiParams.Timeouts.read.toJavaDuration())
        .build()

    @Singleton
    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(Date::class.java, DateJsonAdapter().nullSafe())
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(ApiParams.secureBaseUrl.toHttpUrl())
            .client(client)
            .build()

    @Singleton
    @Provides
    fun provideTmdbMoviesApi(retrofit: Retrofit): TmdbMoviesApi =
        retrofit.create(TmdbMoviesApi::class.java)

    @Singleton
    @Provides
    fun provideTmdbMoviesApiHelper(apiHelper: TmdbMoviesApiHelperImpl): TmdbMoviesApiHelper =
        apiHelper

    @Singleton
    @Provides
    fun provideTmdbTvShowsApi(retrofit: Retrofit): TmdbTvShowsApi =
        retrofit.create(TmdbTvShowsApi::class.java)

    @Singleton
    @Provides
    fun provideTmdbTvShowsApiHelper(apiHelper: TmdbTvShowsApiHelperImpl): TmdbTvShowsApiHelper =
        apiHelper

    @Singleton
    @Provides
    fun provideTmdbOthersApi(retrofit: Retrofit): TmdbOthersApi =
        retrofit.create(TmdbOthersApi::class.java)

    @Singleton
    @Provides
    fun provideTmdbOthersApiHelper(apiHelper: TmdbOthersApiHelperImpl): TmdbOthersApiHelper =
        apiHelper

    @Singleton
    @Provides
    fun provideOphimApi(
        client: OkHttpClient,
        moshi: Moshi,
        retrofit: Retrofit
    ): OphimApi =
        Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(ApiParams.ophimUrl.toHttpUrl())
            .client(client)
            .build()
            .create(OphimApi::class.java)

    @Singleton
    @Provides
    fun provideOphimApiHelper(apiHelper: OphimApiHelperImpl): OphimApiHelper =
        apiHelper

    @Singleton
    @Provides
    fun provideFirebaseFireStore(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Singleton
    @Provides
    fun provideGoogleSignInClient(@ApplicationContext context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.id_token_google))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }
}