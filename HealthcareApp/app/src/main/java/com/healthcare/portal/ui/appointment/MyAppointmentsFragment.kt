package com.healthcare.portal.ui.appointment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.*
import com.healthcare.portal.data.model.Appointment
import com.healthcare.portal.databinding.FragmentMyAppointmentsBinding
import com.healthcare.portal.databinding.ItemAppointmentBinding
import com.healthcare.portal.utils.*

class MyAppointmentsFragment : Fragment() {

    private var _binding: FragmentMyAppointmentsBinding? = null
    private val binding get() = _binding!!
    private val vm: MyAppointmentsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AppointmentAdapter()
        binding.rvAppointments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        vm.appointments.observe(viewLifecycleOwner) { apts ->
            adapter.submitList(apts)
            binding.tvEmpty.apply { if (apts.isEmpty()) show() else hide() }
        }

        vm.loading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.apply { if (loading) show() else hide() }
        }

        binding.swipeRefresh.setOnRefreshListener {
            vm.load()
            binding.swipeRefresh.isRefreshing = false
        }

        vm.load()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

class AppointmentAdapter : ListAdapter<Appointment, AppointmentAdapter.VH>(DIFF) {

    inner class VH(val b: ItemAppointmentBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(apt: Appointment) {
            b.tvAptNumber.text  = apt.appointmentNumber
            b.tvPatient.text    = apt.patientName ?: ""
            b.tvDept.text       = apt.departmentName ?: ""
            b.tvDate.text       = apt.appointmentDate
            b.tvStatus.text     = apt.statusLabel ?: statusLabel(apt.status)
            b.tvStatus.setBackgroundColor(statusColor(apt.status))

            val pct = statusProgress(apt.status)
            b.progressStatus.setProgressCompat(pct, true)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Appointment>() {
            override fun areItemsTheSame(a: Appointment, b: Appointment) = a.id == b.id
            override fun areContentsTheSame(a: Appointment, b: Appointment) = a == b
        }
    }
}
