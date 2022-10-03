package com.catelt.mome.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.catelt.mome.R


typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<VBinding : ViewBinding>(
    private val inflate: Inflate<VBinding>
) : Fragment() {
    protected lateinit var binding: VBinding

    open var isFullScreen: Boolean = false

    open val viewModel: BaseViewModel? get() = null

    open fun setUpViews() {}

    open fun setUpViewModel() {}

    open fun observeData() {}

    open fun setUpAdapter() {}

    open fun setUpArgument(bundle: Bundle) {}


    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            setUpArgument(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflate(inflater, container, false)
        setUpHomeScreen()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpAdapter()
        setUpViewModel()
        setUpViews()
    }

    private fun setUpHomeScreen() {
        activity?.let {
            if (isFullScreen) {
                WindowCompat.setDecorFitsSystemWindows(it.window, false)
                changeColorStatusBar(R.color.transparent)
            } else {
                changeColorStatusBar(R.color.black)
            }
        }

    }

    private fun changeColorStatusBar(color: Int) {
        val window = requireActivity().window
        window.statusBarColor = ContextCompat.getColor(requireContext(), color)
    }
}
