package com.catelt.mome.core

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.catelt.mome.R
import com.google.android.material.bottomnavigation.BottomNavigationView


typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<VBinding : ViewBinding>(
    private val inflate: Inflate<VBinding>
) : Fragment() {
    protected lateinit var binding: VBinding

    open var isFullScreen: Boolean = false

    open var isHideBottom: Boolean = false

    open val viewModel: BaseViewModel? get() = null

    open fun setUpViews() {}

    open fun setUpViewModel() {}

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
        hideBottomNavigation()
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
                val rectangle = Rect()
                it.window.decorView.getWindowVisibleDisplayFrame(rectangle)
                val statusBarHeight: Int = rectangle.top
                binding.root.setPadding(0, statusBarHeight, 0, 0)
                changeColorStatusBar(R.color.black)
            }
        }
    }

    private fun changeColorStatusBar(color: Int) {
        val window = requireActivity().window
        window.statusBarColor = ContextCompat.getColor(requireContext(), color)
    }

    private fun hideBottomNavigation() {
        activity?.let {
            it.findViewById<BottomNavigationView>(R.id.bottomNavigation)
                ?.let { bottomNavigationView ->
                    bottomNavigationView.isVisible = !isHideBottom
                }
        }
    }

    fun setVisionView(isVision: Boolean): Int {
        return if (isVision) View.VISIBLE else View.GONE
    }

    open fun showKeyboard(editText: EditText) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    open fun closeKeyboard(editText: EditText) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.hideSoftInputFromWindow(editText.windowToken, 0)
    }
}
