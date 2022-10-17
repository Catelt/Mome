package com.catelt.mome.di

import android.content.Context
import com.catelt.mome.data.paging.ConfigDataSource
import com.catelt.mome.data.remote.api.movie.TmdbMoviesApiHelper
import com.catelt.mome.data.repository.config.ConfigRepository
import com.catelt.mome.data.repository.config.ConfigRepositoryImpl
import com.catelt.mome.data.repository.firebase.FirebaseRepository
import com.catelt.mome.data.repository.firebase.FirebaseRepositoryImpl
import com.catelt.mome.data.repository.movie.MovieRepository
import com.catelt.mome.data.repository.movie.MovieRepositoryImpl
import com.catelt.mome.data.repository.search.SearchRepository
import com.catelt.mome.data.repository.search.SearchRepositoryImpl
import com.example.CateltMovie.data.repository.tvshow.TvShowRepository
import com.example.CateltMovie.data.repository.tvshow.TvShowRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun provideConfigDataSource(
        @ApplicationContext context: Context,
        externalScope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
        apiMovieHelper: TmdbMoviesApiHelper
        ): ConfigDataSource = ConfigDataSource(
        context = context,
        externalScope = externalScope,
        defaultDispatcher = dispatcher,
        apiMovieHelper = apiMovieHelper,
    )

    @Module
    @InstallIn(SingletonComponent::class)
    interface RepositoryBinds {
        @Binds
        @Singleton
        fun provideConfigRepository(impl: ConfigRepositoryImpl): ConfigRepository

        @Binds
        @Singleton
        fun bindMovieRepository(impl: MovieRepositoryImpl): MovieRepository

        @Binds
        @Singleton
        fun bindTvShowRepository(impl: TvShowRepositoryImpl): TvShowRepository

        @Binds
        @Singleton
        fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

        @Binds
        @Singleton
        fun bindFirebaseRepository(impl: FirebaseRepositoryImpl): FirebaseRepository
    }
}