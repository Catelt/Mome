package com.catelt.mome.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment : Fragment() {

    open fun setUpViews() {}

    open fun observeData() {}

    open fun setUpAdapter() {}

    open fun setUpArgument(bundle: Bundle) {}


    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}