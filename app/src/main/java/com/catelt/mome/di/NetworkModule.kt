package com.catelt.mome.di

import android.content.Context
import com.catelt.mome.BuildConfig
import com.catelt.mome.data.remote.api.ApiParams
import com.catelt.mome.data.remote.api.DateJsonAdapter
import com.catelt.mome.data.remote.api.TMDB_API_KEY
import com.catelt.mome.data.remote.api.movie.TmdbMoviesApi
import com.catelt.mome.data.remote.api.movie.TmdbMoviesApiHelper
import com.catelt.mome.data.remote.api.movie.TmdbMoviesApiHelperImpl
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
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import javax.inject.Singleton

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
}