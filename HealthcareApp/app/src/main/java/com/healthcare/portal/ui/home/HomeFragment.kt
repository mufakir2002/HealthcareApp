package com.healthcare.portal.ui.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.healthcare.portal.R
import com.healthcare.portal.data.model.Department
import com.healthcare.portal.databinding.FragmentHomeBinding
import com.healthcare.portal.databinding.ItemDepartmentBinding
import com.healthcare.portal.utils.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val vm: HomeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DepartmentAdapter { dept ->
            val action = HomeFragmentDirections.actionHomeToDoctors(dept.id, dept.name)
            findNavController().navigate(action)
        }

        binding.rvDepartments.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            this.adapter = adapter
        }

        binding.btnTrack.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_track)
        }

        binding.btnBookNow.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_selectDept)
        }

        binding.swipeRefresh.setOnRefreshListener { vm.loadDepartments() }

        vm.departments.observe(viewLifecycleOwner) { depts ->
            adapter.submitList(depts)
            binding.swipeRefresh.isRefreshing = false
        }

        vm.loading.observe(viewLifecycleOwner) { loading ->
            binding.shimmerLayout.apply { if (loading) { show(); startShimmer() }
                                         else { stopShimmer(); hide() } }
            binding.rvDepartments.apply { if (loading) hide() else show() }
        }

        vm.error.observe(viewLifecycleOwner) { err ->
            err?.let { binding.root.snackError(it) }
        }

        vm.loadDepartments()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ── Department Adapter ────────────────────────────────────────
class DepartmentAdapter(
    private val onClick: (Department) -> Unit
) : ListAdapter<Department, DepartmentAdapter.VH>(DIFF) {

    inner class VH(val b: ItemDepartmentBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(dept: Department) {
            b.tvName.text = dept.name
            b.tvDesc.text = dept.description ?: ""

            if (!dept.imageUrl.isNullOrBlank()) {
                b.ivImage.show()
                b.ivIcon.hide()
                Glide.with(b.ivImage)
                    .load(dept.imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.bg_dept_placeholder)
                    .into(b.ivImage)
            } else {
                b.ivImage.hide()
                b.ivIcon.show()
                // Map icon class to drawable resource
                b.ivIcon.setImageResource(iconForClass(dept.iconClass))
            }
            b.root.setOnClickListener { onClick(dept) }
        }

        private fun iconForClass(cls: String?): Int = when {
            cls == null -> R.drawable.ic_medical
            "heart"  in cls -> R.drawable.ic_heart
            "tooth"  in cls || "teeth" in cls -> R.drawable.ic_dental
            "eye"    in cls -> R.drawable.ic_eye
            "brain"  in cls -> R.drawable.ic_brain
            "child"  in cls || "baby" in cls -> R.drawable.ic_child
            "bone"   in cls -> R.drawable.ic_bone
            "lungs"  in cls || "chest" in cls -> R.drawable.ic_lungs
            else -> R.drawable.ic_medical
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemDepartmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Department>() {
            override fun areItemsTheSame(a: Department, b: Department) = a.id == b.id
            override fun areContentsTheSame(a: Department, b: Department) = a == b
        }
    }
}
