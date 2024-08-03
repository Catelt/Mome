package com.catelt.mome.ui.profile

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.catelt.mome.R
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.databinding.FragmentProfileBinding
import com.catelt.mome.ui.auth.AuthActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>(
    FragmentProfileBinding::inflate
) {
    override var isHideBottom = true
    override var isTransitionInflater = true
    override val viewModel: ProfileViewModel by viewModels()

    override fun setUpViews() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            if(viewModel.getName().isNotBlank() && viewModel.getName() != "null"){
                txtName.text = viewModel.getName()
            }

            txtLogout.setOnClickListener {
                viewModel.logOut()
            }

            txtDeleteAccount.setOnClickListener {
                context?.let {
                    val builder = AlertDialog.Builder(it)
                    builder.setMessage(getString(R.string.delete_account_content))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.yes)) { _, _ ->
                            viewModel.deleteAccount()
                        }
                        .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                            dialog.dismiss()
                        }
                    val alert = builder.create()
                    alert.show()
                }

            }
        }
    }

    override fun setUpViewModel() {
        lifecycleScope.launch {
            viewModel.isLogged.collectLatest {
                activity?.apply {
                    if (!it) {
                        startActivity(Intent(activity, AuthActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}