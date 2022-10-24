package com.catelt.mome.data.paging.firebase

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.catelt.mome.data.model.account.Media
import com.catelt.mome.utils.SIZE_PAGE
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException


class FirebasePagingDataSource(
    private val query: Query,
) : PagingSource<QuerySnapshot, Media>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Media>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Media> {
        return try {

            val currentPage = params.key ?: query.limit(SIZE_PAGE.toLong()).get().await()
            val lastVisibleProduct: DocumentSnapshot
            val nextPage:  QuerySnapshot?

            if (currentPage.size() > 0){
                lastVisibleProduct = currentPage.documents[currentPage.size() - 1]
                nextPage = query.startAfter(lastVisibleProduct).get().await()
            }
            else{
                nextPage = null
            }

            LoadResult.Page(
                data = currentPage.toObjects(Media::class.java),
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        } catch (e: JsonDataException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            LoadResult.Error(e)
        }
    }
}