package br.com.dio.app.repositories.ui.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import br.com.dio.app.repositories.core.createDialog
import br.com.dio.app.repositories.core.createProgressDialog
import br.com.dio.app.repositories.databinding.FragmentHomeBinding
import br.com.dio.app.repositories.presentation.HomeViewModel
import br.com.dio.app.repositories.presentation.MainViewModel
import br.com.dio.app.repositories.ui.RepoListAdapter
import br.com.dio.app.repositories.ui.ui.adapters.HeaderAdapter
import org.koin.androidx.viewmodel.ext.android.sharedStateViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeFragment : Fragment() {

    private lateinit var adapter: RepoListAdapter
    private lateinit var headerAdapter: HeaderAdapter
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val dialog by lazy { context?.createProgressDialog() }
    private val viewModel by viewModel<HomeViewModel>()

    private val mainViewModel by sharedStateViewModel<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        headerAdapter = HeaderAdapter(viewModel::sortBy)
        adapter = RepoListAdapter(viewModel::save)
        binding.rvRepos.adapter = ConcatAdapter(headerAdapter, adapter)

        viewModel.repos.observe(viewLifecycleOwner) {
            when (it) {
                HomeViewModel.State.Loading -> {
                    dialog?.show()
                }
                is HomeViewModel.State.Error -> {
                    requireContext().createDialog {
                        setMessage(it.error.message)
                    }.show()
                    dialog?.dismiss()
                }
                is HomeViewModel.State.Success -> {
                    dialog?.dismiss()
                    adapter.submitList(it.list)
                }
            }
        }
        mainViewModel.query.observe(viewLifecycleOwner) {
            it?.let { query ->
                viewModel.getRepoList(query)
                // Keep query as a single event
                mainViewModel.clearSearch()
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}