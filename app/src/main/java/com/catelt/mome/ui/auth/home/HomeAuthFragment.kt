package com.catelt.mome.ui.auth.home

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.catelt.mome.MainActivity
import com.catelt.mome.R
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.core.OnSignInStartedListener
import com.catelt.mome.databinding.FragmentHomeAuthBinding
import com.catelt.mome.utils.REQUEST_SIGN_IN
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeAuthFragment : BaseFragment<FragmentHomeAuthBinding>(
    FragmentHomeAuthBinding::inflate
) {
    override var isHideBottom = true

    override val viewModel: AuthViewModel by viewModels()

    override fun setUpViews() {
        binding.apply {
            btnSignInWithPassword.setOnClickListener {
                findNavController().navigate(R.id.signInFragment)
            }

            txtSignUp.setOnClickListener {
                findNavController().navigate(R.id.signUpFragment)
            }

            btnGoogle.setOnClickListener {
                viewModel.signInWithGoogle(object :
                    OnSignInStartedListener {
                    override fun onSignInStarted(client: GoogleSignInClient?) {
                        startActivityForResult(client?.signInIntent, REQUEST_SIGN_IN)
                    }
                })
            }
        }
    }

    override fun setUpViewModel() {
        lifecycleScope.launch {
            viewModel.apply {
                toastMessage.observe(viewLifecycleOwner) {
                    if (it.isNotBlank()){
                        toast(it)
                        toastMessage.postValue("")
                    }
                }

                isLoading.observe(viewLifecycleOwner){
                    binding.layoutLoading.root.isVisible = it
                }

                isLogged.collectLatest {
                    activity?.apply {
                        if (it) {
                            startActivity(Intent(activity, MainActivity::class.java))
                            finish()
                        }
                    }
                }
            }

        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SIGN_IN && resultCode == Activity.RESULT_OK && data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)

                account.idToken?.let { viewModel.firebaseAuthWithGoogle(it) }

            } catch (e: ApiException) {
                Toast.makeText(this.context, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}