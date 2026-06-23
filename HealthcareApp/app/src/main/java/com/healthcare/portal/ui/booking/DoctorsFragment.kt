package com.healthcare.portal.ui.booking

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.healthcare.portal.R
import com.healthcare.portal.data.model.Doctor
import com.healthcare.portal.databinding.FragmentDoctorsBinding
import com.healthcare.portal.databinding.ItemDoctorBinding
import com.healthcare.portal.utils.*

class DoctorsFragment : Fragment() {

    private var _binding: FragmentDoctorsBinding? = null
    private val binding get() = _binding!!
    private val args: DoctorsFragmentArgs by navArgs()
    private val vm: BookingViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDoctorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDeptName.text = args.departmentName
        vm.setDepartment(args.departmentId, args.departmentName)

        val adapter = DoctorAdapter(
            onSelect = { doctor ->
                vm.setDoctor(doctor)
                findNavController().navigate(R.id.action_doctors_to_bookingForm)
            },
            onAnyDoctor = {
                vm.setDoctor(null)
                findNavController().navigate(R.id.action_doctors_to_bookingForm)
            }
        )

        binding.rvDoctors.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        vm.doctors.observe(viewLifecycleOwner) { docs ->
            adapter.submitList(docs)
        }

        vm.loading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.apply { if (loading) show() else hide() }
        }

        vm.error.observe(viewLifecycleOwner) { err ->
            err?.let { binding.root.snackError(it) }
        }

        vm.loadDoctors(args.departmentId)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

// ── Doctor Card Adapter ───────────────────────────────────────
class DoctorAdapter(
    private val onSelect: (Doctor) -> Unit,
    private val onAnyDoctor: () -> Unit
) : ListAdapter<Doctor, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val TYPE_ANY    = 0
        private const val TYPE_DOCTOR = 1

        val DIFF = object : DiffUtil.ItemCallback<Doctor>() {
            override fun areItemsTheSame(a: Doctor, b: Doctor) = a.id == b.id
            override fun areContentsTheSame(a: Doctor, b: Doctor) = a == b
        }
    }

    override fun getItemViewType(position: Int) =
        if (position == 0) TYPE_ANY else TYPE_DOCTOR

    override fun getItemCount() = super.getItemCount() + 1 // +1 for "Any Doctor"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ANY) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_any_doctor, parent, false)
            AnyDoctorVH(v)
        } else {
            val b = ItemDoctorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            DoctorVH(b)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AnyDoctorVH) {
            holder.itemView.setOnClickListener { onAnyDoctor() }
        } else if (holder is DoctorVH) {
            holder.bind(getItem(position - 1))
        }
    }

    inner class AnyDoctorVH(v: View) : RecyclerView.ViewHolder(v)

    inner class DoctorVH(val b: ItemDoctorBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(doc: Doctor) {
            b.tvName.text = "Dr. ${doc.fullName}"
            b.tvSpecialty.text  = doc.specialty ?: "General Practitioner"
            b.tvQual.text       = doc.qualification ?: ""
            b.tvFee.text        = if (doc.fee > 0) "$${String.format("%.2f", doc.fee)} / visit" else "Free"

            // Availability badge
            if (doc.isAvailable) {
                b.tvAvailability.text = "✓ Available"
                b.tvAvailability.setBackgroundColor(0xFF00B894.toInt())
                b.root.alpha = 1f
                b.root.setOnClickListener { onSelect(doc) }
            } else {
                b.tvAvailability.text = "✗ Unavailable"
                b.tvAvailability.setBackgroundColor(0xFFE74C3C.toInt())
                b.root.alpha = 0.6f
                b.root.setOnClickListener(null)
            }

            // Photo
            if (!doc.photoUrl.isNullOrBlank()) {
                Glide.with(b.ivPhoto)
                    .load(doc.photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_doctor_placeholder)
                    .into(b.ivPhoto)
            } else {
                b.ivPhoto.setImageResource(R.drawable.ic_doctor_placeholder)
            }
        }
    }
}
