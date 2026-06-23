package com.healthcare.portal.ui.booking

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.healthcare.portal.R
import com.healthcare.portal.data.model.PaymentMethod
import com.healthcare.portal.databinding.FragmentPaymentBinding
import com.healthcare.portal.utils.*

class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!
    private val vm: BookingViewModel by activityViewModels()

    private var selectedPm: PaymentMethod? = null
    private var bookedAppointmentId = 0
    private var bookedAppointmentNumber = ""
    private var isEvcPm = false
    private var evcPaid = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        // First book the appointment, then show payment UI
        if (bookedAppointmentId == 0) {
            binding.progressBar.show()
            binding.scrollContent.hide()
            vm.submitBooking()
        }

        vm.loading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.apply { if (loading) show() else hide() }
        }

        vm.booking.observe(viewLifecycleOwner) { apt ->
            apt ?: return@observe
            binding.progressBar.hide()
            binding.scrollContent.show()

            bookedAppointmentId     = apt.id
            bookedAppointmentNumber = apt.appointmentNumber

            binding.tvAptNumber.text = apt.appointmentNumber
            binding.tvPatientName.text = apt.patientName ?: vm.fullName
            binding.tvDept.text        = apt.departmentName ?: vm.selectedDeptName
            binding.tvDate.text        = apt.appointmentDate

            val fee = vm.selectedDoctor?.fee ?: 0.0
            binding.tvFeeAmount.text = "$${String.format("%.2f", fee)}"
        }

        vm.error.observe(viewLifecycleOwner) { err ->
            err?.let { binding.root.snackError(it) }
        }

        vm.paymentMethods.observe(viewLifecycleOwner) { methods ->
            setupPaymentMethods(methods)
        }
        vm.loadPaymentMethods()

        // EVC status
        vm.evcStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is EvcStatus.Sending -> {
                    binding.evcStatusCard.show()
                    binding.tvEvcStatus.text = "⏳ Sending payment request..."
                    binding.tvEvcStatus.setTextColor(0xFF3498DB.toInt())
                    binding.btnEvcPay.isEnabled = false
                }
                is EvcStatus.WaitingForPin -> {
                    binding.evcStatusCard.show()
                    binding.tvEvcStatus.text = "📱 ${status.message}"
                    binding.tvEvcStatus.setTextColor(0xFFF39C12.toInt())
                    binding.btnEvcPay.isEnabled = false
                }
                is EvcStatus.Approved -> {
                    evcPaid = true
                    binding.evcStatusCard.show()
                    binding.tvEvcStatus.text = "✓ ${status.message}"
                    binding.tvEvcStatus.setTextColor(0xFF00B894.toInt())
                    binding.btnConfirm.isEnabled = true
                    binding.btnConfirm.text = "Appointment Confirmed!"
                    // Auto-navigate to confirmation
                    val action = PaymentFragmentDirections.actionPaymentToConfirmation(
                        bookedAppointmentId, bookedAppointmentNumber
                    )
                    findNavController().navigate(action)
                }
                is EvcStatus.Failed -> {
                    binding.evcStatusCard.show()
                    binding.tvEvcStatus.text = "✗ ${status.message}"
                    binding.tvEvcStatus.setTextColor(0xFFE74C3C.toInt())
                    binding.btnEvcPay.isEnabled = true
                    binding.btnEvcPay.text = "Retry Payment Request"
                }
            }
        }

        // EVC pay button
        binding.btnEvcPay.setOnClickListener {
            val phone = binding.etEvcPhone.text.toString().trim()
            if (phone.length < 9) {
                binding.tilEvcPhone.error = "Enter a valid EVC phone number"
                return@setOnClickListener
            }
            binding.tilEvcPhone.error = null
            val fee = vm.selectedDoctor?.fee ?: 0.0
            selectedPm?.let { pm ->
                vm.initiateEvcPayment(phone, fee, bookedAppointmentId, bookedAppointmentNumber, pm.id)
            }
        }

        // Manual confirm
        binding.btnConfirm.setOnClickListener {
            val ref    = binding.etTxRef.text.toString().trim()
            val sender = binding.etSender.text.toString().trim()
            if (!isEvcPm) {
                if (ref.isEmpty())    { binding.tilTxRef.error = "Transaction reference required"; return@setOnClickListener }
                if (sender.isEmpty()) { binding.tilSender.error = "Sender number required"; return@setOnClickListener }
            }
            val action = PaymentFragmentDirections.actionPaymentToConfirmation(
                bookedAppointmentId, bookedAppointmentNumber
            )
            findNavController().navigate(action)
        }
    }

    private fun setupPaymentMethods(methods: List<PaymentMethod>) {
        binding.paymentMethodsGroup.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        methods.forEach { pm ->
            val isEvc = pm.name.lowercase().run { contains("evc") || contains("hormuud") || contains("waafi") }
            val chip  = inflater.inflate(R.layout.item_payment_chip, binding.paymentMethodsGroup, false)
            chip.tag = pm.id

            chip.findViewById<android.widget.TextView>(R.id.tvPmName).text = pm.name
            chip.setOnClickListener { selectPaymentMethod(pm, isEvc, methods) }
            binding.paymentMethodsGroup.addView(chip)
        }
    }

    private fun selectPaymentMethod(pm: PaymentMethod, isEvc: Boolean, all: List<PaymentMethod>) {
        selectedPm = pm
        isEvcPm    = isEvc

        // Highlight selected chip
        for (i in 0 until binding.paymentMethodsGroup.childCount) {
            val chip = binding.paymentMethodsGroup.getChildAt(i)
            val selected = chip.tag == pm.id
            chip.setBackgroundResource(
                if (selected) R.drawable.bg_pm_selected else R.drawable.bg_pm_default
            )
        }

        if (isEvc) {
            binding.evcSection.show()
            binding.manualSection.hide()
            binding.tvPmInstructions.hide()
            // Pre-fill phone from mobile field
            if (binding.etEvcPhone.text.isNullOrBlank()) {
                binding.etEvcPhone.setText(vm.mobile)
            }
        } else {
            binding.evcSection.hide()
            binding.manualSection.show()
            val instr = pm.instructions
            if (!instr.isNullOrBlank()) {
                binding.tvPmInstructions.show()
                binding.tvPmInstructions.text = buildString {
                    if (!pm.accountNumber.isNullOrBlank()) append("Account: ${pm.accountNumber}\n")
                    append(instr)
                }
            } else {
                binding.tvPmInstructions.hide()
            }
        }
    }

    override fun onDestroyView() {
        vm.cancelPolling()
        super.onDestroyView()
        _binding = null
    }
}
