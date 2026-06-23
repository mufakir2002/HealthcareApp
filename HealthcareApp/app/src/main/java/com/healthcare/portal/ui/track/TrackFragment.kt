package com.healthcare.portal.ui.track

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.healthcare.portal.databinding.FragmentTrackBinding
import com.healthcare.portal.utils.*

class TrackFragment : Fragment() {

    private var _binding: FragmentTrackBinding? = null
    private val binding get() = _binding!!
    private val vm: TrackViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSearch.setOnClickListener {
            val aptNum = binding.etAptNumber.text.toString().trim()
            val mobile = binding.etMobile.text.toString().trim()
            if (aptNum.isEmpty() && mobile.isEmpty()) {
                binding.root.snackError("Enter appointment number or mobile")
                return@setOnClickListener
            }
            vm.track(aptNum, mobile)
        }

        vm.loading.observe(viewLifecycleOwner) { loading ->
            binding.btnSearch.isEnabled = !loading
            binding.btnSearch.text = if (loading) "Searching..." else "Track Appointment"
            binding.progressBar.apply { if (loading) show() else hide() }
        }

        vm.result.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            binding.resultCard.show()

            binding.tvAptNumber.text  = result.appointmentNumber
            binding.tvStatusLabel.text = result.statusLabel
            binding.tvPatient.text    = result.patientName
            binding.tvDept.text       = result.department
            binding.tvDoctor.text     = result.doctor ?: "Any Available Doctor"
            binding.tvDate.text       = result.appointmentDate
            binding.tvTime.text       = result.appointmentTime ?: "To be confirmed"
            binding.tvPayment.text    = result.paymentStatus
            binding.tvNotes.apply {
                if (!result.internalNotes.isNullOrBlank()) {
                    show()
                    text = result.internalNotes
                } else hide()
            }

            // Status colour
            val color = statusColor(result.status)
            binding.tvStatusLabel.setBackgroundColor(color)
            binding.statusBar.setProgressCompat(statusProgress(result.status), true)
            binding.statusBar.setIndicatorColor(color)
        }

        vm.error.observe(viewLifecycleOwner) { err ->
            err?.let {
                binding.resultCard.hide()
                binding.root.snackError(it)
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
