package com.catelt.mome.ui.mylist

import androidx.paging.PagingData
import com.catelt.mome.data.model.account.Media
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow


data class MyListUIState(
    val myList: Flow<PagingData<Media>>,
){
    companion object {
        val default: MyListUIState = MyListUIState(
            myList = emptyFlow(),
        )
    }
}