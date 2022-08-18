package pt.ms.myshare

import android.os.Bundle
import android.view.View
import pt.ms.myshare.databinding.FragmentEditProfileBinding
import pt.ms.myshare.utils.BaseFragment
import pt.ms.myshare.utils.MoneyTextWatcher

class EditProfileFragment :
    BaseFragment<FragmentEditProfileBinding>(FragmentEditProfileBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.netSalary.addTextChangedListener(MoneyTextWatcher(binding.netSalary))
    }

    override fun toolbarTitle(): String = getString(R.string.edit_profile_toolbar_title)


}