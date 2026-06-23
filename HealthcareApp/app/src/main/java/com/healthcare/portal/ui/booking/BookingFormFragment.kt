package com.healthcare.portal.ui.booking

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.healthcare.portal.R
import com.healthcare.portal.databinding.FragmentBookingFormBinding
import com.healthcare.portal.utils.*
import java.util.*

class BookingFormFragment : Fragment() {

    private var _binding: FragmentBookingFormBinding? = null
    private val binding get() = _binding!!
    private val vm: BookingViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookingFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show selected doctor summary
        val doc = vm.selectedDoctor
        if (doc != null) {
            binding.cardDoctor.show()
            binding.tvDocName.text    = "Dr. ${doc.fullName}"
            binding.tvDocSpec.text    = doc.specialty ?: vm.selectedDeptName
            binding.tvDocFee.text     = "$${String.format("%.2f", doc.fee)} / visit"
            binding.tvDocAvail.text   = if (doc.isAvailable) "Available" else "Unavailable"
        } else {
            binding.cardDoctor.show()
            binding.tvDocName.text  = "Any Available Doctor"
            binding.tvDocSpec.text  = vm.selectedDeptName
            binding.tvDocFee.text   = ""
            binding.tvDocAvail.text = ""
        }

        // Time slots
        val times = listOf(
            "08:00 AM","09:00 AM","10:00 AM","11:00 AM",
            "12:00 PM","01:00 PM","02:00 PM","03:00 PM",
            "04:00 PM","05:00 PM"
        )
        binding.spinnerTime.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, times
        )

        // Date picker
        binding.etDate.isFocusable = false
        binding.etDate.setOnClickListener { showDatePicker() }
        binding.tilDate.setEndIconOnClickListener { showDatePicker() }

        // Restore values
        binding.etName.setText(vm.fullName)
        binding.etMobile.setText(vm.mobile)
        if (vm.apptDate.isNotBlank()) binding.etDate.setText(vm.apptDate)
        if (vm.apptTime.isNotBlank()) {
            val idx = times.indexOf(vm.apptTime)
            if (idx >= 0) binding.spinnerTime.setSelection(idx)
        }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnNext.setOnClickListener {
            val name   = binding.etName.text.toString().trim()
            val mobile = binding.etMobile.text.toString().trim()
            val date   = binding.etDate.text.toString().trim()

            if (name.isEmpty())   { binding.tilName.error = "Full name required"; return@setOnClickListener }
            if (mobile.isEmpty()) { binding.tilMobile.error = "Mobile number required"; return@setOnClickListener }
            if (date.isEmpty())   { binding.tilDate.error = "Please select a date"; return@setOnClickListener }

            binding.tilName.error   = null
            binding.tilMobile.error = null
            binding.tilDate.error   = null

            vm.fullName  = name
            vm.mobile    = mobile
            vm.apptDate  = date
            vm.apptTime  = times[binding.spinnerTime.selectedItemPosition]

            findNavController().navigate(R.id.action_bookingForm_to_payment)
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, 1) }
        DatePickerDialog(
            requireContext(),
            { _, y, m, d -> binding.etDate.setText("$y-${(m+1).toString().padStart(2,'0')}-${d.toString().padStart(2,'0')}") },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).also { it.datePicker.minDate = cal.timeInMillis }.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
