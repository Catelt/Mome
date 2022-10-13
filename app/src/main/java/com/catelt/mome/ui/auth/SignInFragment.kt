package com.catelt.mome.ui.auth

import android.content.Intent
import androidx.navigation.fragment.findNavController
import com.catelt.mome.MainActivity
import com.catelt.mome.R
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.databinding.FragmentSignInBinding

class SignInFragment : BaseFragment<FragmentSignInBinding>(
    FragmentSignInBinding::inflate
) {
    override var isHideBottom = true

    override fun setUpViews() {
        binding.apply {
            editTextEmail.setupView(getString(R.string.email))

            editTextPassword.setupView(getString(R.string.password), true)

            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            txtSignUp.setOnClickListener {
                findNavController().apply {
                    if (previousBackStackEntry?.destination?.id == R.id.signUpFragment){
                        navigateUp()
                    }
                    else{
                        navigate(R.id.signUpFragment)
                    }
                }
            }

            btnSignIn.setOnClickListener {
                startActivity(Intent(requireActivity(),MainActivity::class.java))
                activity?.finish()
            }

        }
    }
}