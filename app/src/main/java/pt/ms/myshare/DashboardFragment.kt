package pt.ms.myshare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pt.ms.myshare.databinding.FragmentDashboardBinding
import pt.ms.myshare.utils.BaseFragment
import pt.ms.myshare.utils.TimeUtils
import java.time.LocalDate

/**
 * The default destination in the navigation.
 */
class DashboardFragment : BaseFragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val today = LocalDate.now()

    private lateinit var rvCategoryGrid: RecyclerView
    private lateinit var categoryAdapter: CategoryGridAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvToday.text = TimeUtils.monthDayAndYear(today)
        rvCategoryGrid = binding.categoryGrid
        categoryAdapter = CategoryGridAdapter()
        rvCategoryGrid.adapter = categoryAdapter
        val adapter = rvCategoryGrid.adapter as CategoryGridAdapter
        adapter.submitList(
            arrayListOf(
                getString(R.string.stocks_label),
                getString(R.string.crypto_label),
                getString(R.string.savings_label)
            )
        )

        /*binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }*/
    }

    override fun toolbarTitle(): String = "Hello\nEdylson Frederico"

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}