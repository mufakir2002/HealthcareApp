package com.healthcare.portal.ui.auth

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.healthcare.portal.databinding.FragmentProfileBinding
import com.healthcare.portal.utils.SessionManager
import com.healthcare.portal.utils.hide
import com.healthcare.portal.utils.show

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val session = SessionManager(requireContext())

        if (session.isLoggedIn) {
            binding.layoutLoggedIn.show()
            binding.layoutGuest.hide()
            binding.tvName.text  = session.fullName ?: "User"
            binding.tvEmail.text = session.email ?: ""
            binding.tvRole.text  = session.userType ?: "Patient"
            binding.btnLogout.setOnClickListener {
                session.logout()
                requireActivity().recreate()
            }
        } else {
            binding.layoutLoggedIn.hide()
            binding.layoutGuest.show()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
