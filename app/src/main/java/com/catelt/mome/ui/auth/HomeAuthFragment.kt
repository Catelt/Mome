package com.catelt.mome.ui.auth

import androidx.navigation.fragment.findNavController
import com.catelt.mome.R
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.databinding.FragmentHomeAuthBinding

class HomeAuthFragment : BaseFragment<FragmentHomeAuthBinding>(
    FragmentHomeAuthBinding::inflate
) {
    override var isHideBottom = true

    override fun setUpViews() {
        binding.apply {
            btnSignInWithPassword.setOnClickListener {
                findNavController().navigate(R.id.signInFragment)
            }

            txtSignUp.setOnClickListener {
                findNavController().navigate(R.id.signUpFragment)
            }
        }
    }
}