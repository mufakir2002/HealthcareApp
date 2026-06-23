package com.healthcare.portal.ui.booking

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.healthcare.portal.databinding.FragmentConfirmationBinding

class ConfirmationFragment : Fragment() {

    private var _binding: FragmentConfirmationBinding? = null
    private val binding get() = _binding!!
    private val args: ConfirmationFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvAptNumber.text = args.appointmentNumber

        binding.btnTrackStatus.setOnClickListener {
            findNavController().navigate(R.id.action_confirmation_to_track)
        }
        binding.btnBackHome.setOnClickListener {
            findNavController().navigate(R.id.action_confirmation_to_home)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
