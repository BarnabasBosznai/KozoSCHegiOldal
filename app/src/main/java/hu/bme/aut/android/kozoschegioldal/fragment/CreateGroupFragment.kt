package hu.bme.aut.android.kozoschegioldal.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import hu.bme.aut.android.kozoschegioldal.adapter.SearchUserAdapter
import hu.bme.aut.android.kozoschegioldal.databinding.FragmentCreateGroupBinding
import hu.bme.aut.android.kozoschegioldal.model.User
import hu.bme.aut.android.kozoschegioldal.viewmodel.ChatViewModel

class CreateGroupFragment : Fragment(), SearchUserAdapter.OnItemClickedListener {
    private var _binding: FragmentCreateGroupBinding? = null
    private val binding get() = _binding!!
    private val chatViewModel: ChatViewModel by activityViewModels()


    private val addedUsers = mutableListOf<User>()
    private val adapter = SearchUserAdapter(addedUsers, this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateGroupBinding.inflate(inflater, container, false)

        val user = CreateGroupFragmentArgs.fromBundle(requireArguments()).user

        binding.rvSearchResult.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResult.adapter = adapter

        chatViewModel.getUsersTask().addOnSuccessListener { snapshot ->
            adapter.submitList(snapshot.toObjects(User::class.java).filter { it.uid != user.uid })
        }

        binding.btnCreateGroup.setOnClickListener {
            if (binding.etGroupName.text.toString().isNotEmpty() && addedUsers.size > 0) {
                val list = addedUsers.map { it.uid }.toMutableList()
                list.add(user.uid)
                chatViewModel.createGroup(user.displayName, binding.etGroupName.text.toString(), list)
                findNavController().popBackStack()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(user: User) {
        if (addedUsers.contains(user))
            addedUsers.remove(user)
        else
            addedUsers.add(user)
    }
}