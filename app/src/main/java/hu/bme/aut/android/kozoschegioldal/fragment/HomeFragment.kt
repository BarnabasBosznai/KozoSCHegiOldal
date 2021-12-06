package hu.bme.aut.android.kozoschegioldal.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import hu.bme.aut.android.kozoschegioldal.adapter.CustomFirestorePagingAdapter
import hu.bme.aut.android.kozoschegioldal.databinding.FragmentHomeBinding
import hu.bme.aut.android.kozoschegioldal.model.User
import hu.bme.aut.android.kozoschegioldal.viewmodel.PostViewModel
import hu.bme.aut.android.kozoschegioldal.viewmodel.PostViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var user: User
    private val postViewModel: PostViewModel by activityViewModels { PostViewModelFactory(user) }
    private lateinit var finalAdapter: CustomFirestorePagingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        finalAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        finalAdapter.stopListening()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        user = HomeFragmentArgs.fromBundle(requireArguments()).user

        finalAdapter = CustomFirestorePagingAdapter(this, postViewModel.getPostsAdapterOptions(this))

        binding.srlPosts.setOnRefreshListener {
            finalAdapter.refresh()
        }

        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvPosts.layoutManager = layoutManager
        binding.rvPosts.adapter = finalAdapter

        lifecycleScope.launch {
            finalAdapter.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh) {
                    is LoadState.Loading -> {
                        // The initial Load has begun
                        Log.d("LoadState", "Initial loading")
                    }
                }

                when (loadStates.append) {
                    is LoadState.Loading -> {
                        // The adapter has started to load an additional page
                        Log.d("LoadState", "Append loading")
                    }
                    is LoadState.NotLoading -> {
                        if (loadStates.append.endOfPaginationReached) {
                            // The adapter has finished loading all of the data set
                            Log.d("LoadState", "End of pagination")
                        }
                        if (loadStates.refresh is LoadState.NotLoading) {
                            // The previous load (either initial or additional) completed
                            Log.d("LoadState", "Completed?")
                            binding.srlPosts.isRefreshing = false
                            binding.rvPosts.smoothScrollToPosition(0)
                        }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}