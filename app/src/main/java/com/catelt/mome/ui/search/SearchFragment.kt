package com.catelt.mome.ui.search


import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.data.model.MediaType
import com.catelt.mome.databinding.FragmentSearchBinding
import com.catelt.mome.ui.bottomsheet.MediaDetailsBottomSheet
import com.catelt.mome.ui.search.adapter.ResultSearchAdapter
import com.catelt.mome.ui.search.adapter.TopSearchAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>(FragmentSearchBinding::inflate) {
    override val viewModel: SearchViewModel by viewModels()
    override var isHideBottom: Boolean = true

    private lateinit var onQueryChanged: (query: String) -> Unit
    private lateinit var onQueryCleared: () -> Unit
    private val onMovieClicked = { movieId: Int, mediaType: MediaType ->
        MediaDetailsBottomSheet.newInstance(movieId,mediaType == MediaType.Movie)
            .show(requireActivity().supportFragmentManager, movieId.toString())
    }

    private val topSearchAdapter = TopSearchAdapter()
    private val resultSearchAdapter = ResultSearchAdapter()

    override fun setUpAdapter() {
        topSearchAdapter.onMovieClicked = onMovieClicked
        resultSearchAdapter.onMovieClicked = onMovieClicked

        resultSearchAdapter.apply {
            addLoadStateListener { loadState ->
                val isEmpty = resultSearchAdapter.itemCount < 1
                binding.apply {
                    if (loadState.source.refresh is LoadState.NotLoading){
                        layoutNoResult.visibility = setVisionView(isEmpty)
                        layoutWhenSearch.visibility = setVisionView(!isEmpty)
                    }
                }
            }
        }

    }

    override fun setUpViews() {
        binding.apply {
            recyclerListTopSearch.adapter = topSearchAdapter
            recyclerListResult.adapter = resultSearchAdapter

            layoutSearch.handleTextChange = {
                if (it.isNullOrBlank()) {
                    onQueryCleared()
                } else {
                    onQueryChanged(it.toString())
                }
            }

            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            context?.let {
                binding.layoutSearch.editText.apply {
                    requestFocus()
                    showKeyboard(this)
                }
            }
        }
    }

    override fun setUpViewModel() {
        onQueryChanged = viewModel::onQueryChange
        onQueryCleared = viewModel::onQueryClear

        viewModel.apply {
            lifecycleScope.launch {
                imageUrlParser.collectLatest { imageParser ->
                    topSearchAdapter.imageUrlParser = imageParser
                    resultSearchAdapter.imageUrlParser = imageParser
                    uiState.collectLatest {
                        binding.layoutNoResult.visibility = setVisionView(false)
                        if (it.queryLoading) {
                            binding.apply {
                                layoutLoading.visibility = setVisionView(true)
                                layoutWhenEmptySearch.visibility = setVisionView(false)
                                layoutWhenSearch.visibility = setVisionView(false)
                            }
                        } else {
                            binding.layoutLoading.visibility = setVisionView(false)
                            when (it.searchState) {
                                SearchState.EmptyQuery -> {
                                    binding.layoutWhenEmptySearch.visibility = setVisionView(true)
                                    binding.layoutWhenSearch.visibility = setVisionView(false)

                                }
                                SearchState.ValidQuery -> {
                                    binding.layoutWhenSearch.visibility = setVisionView(true)
                                    binding.layoutWhenEmptySearch.visibility = setVisionView(false)
                                }
                            }

                            when (it.resultState) {
                                is ResultState.Default -> {
                                    it.resultState.popular.flowOn(Dispatchers.IO).onEach { list ->
                                        topSearchAdapter.submitData(list)

                                    }.launchIn(lifecycleScope)
                                }
                                is ResultState.Search -> {
                                    it.resultState.result.flowOn(Dispatchers.IO).onEach { list ->
                                        resultSearchAdapter.submitData(list)
                                    }.launchIn(lifecycleScope)
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    override fun onPause() {
        super.onPause()
        context?.let {
            binding.layoutSearch.editText.apply {
                clearFocus()
                closeKeyboard(this)
            }
        }
    }
}