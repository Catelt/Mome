package com.catelt.mome.ui.auth

import android.content.Intent
import androidx.navigation.fragment.findNavController
import com.catelt.mome.MainActivity
import com.catelt.mome.R
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.databinding.FragmentSignUpBinding

class SignUpFragment : BaseFragment<FragmentSignUpBinding>(
    FragmentSignUpBinding::inflate
) {
    override var isHideBottom = true

    override fun setUpViews() {
        binding.apply {
            editTextEmail.setupView(getString(R.string.email))

            editTextPassword.setupView(getString(R.string.password), true)

            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            txtSignIn.setOnClickListener {
                findNavController().apply {
                    if (previousBackStackEntry?.destination?.id == R.id.signInFragment){
                        navigateUp()
                    }
                    else{
                        navigate(R.id.signInFragment)
                    }
                }
            }

            btnSignUp.setOnClickListener {
                startActivity(Intent(requireActivity(), MainActivity::class.java))
                activity?.finish()
            }
        }
    }
}