package com.catelt.mome.ui.profile

import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
    override val viewModel: ProfileViewModel by viewModels()

    override fun setUpViews() {
        binding.apply {
            txtLogout.setOnClickListener {
                viewModel.logOut()
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