package hu.bme.aut.android.kozoschegioldal.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import hu.bme.aut.android.kozoschegioldal.adapter.ChatListAdapter
import hu.bme.aut.android.kozoschegioldal.databinding.FragmentChatListBinding
import hu.bme.aut.android.kozoschegioldal.model.Group
import hu.bme.aut.android.kozoschegioldal.model.User
import hu.bme.aut.android.kozoschegioldal.viewmodel.ChatViewModel

class ChatListFragment : Fragment(), ChatListAdapter.ChatGroupSelectListener {
    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private val chatViewModel: ChatViewModel by activityViewModels()
    private val groupList = mutableListOf<Group>()
    private lateinit var adapter: ChatListAdapter
    private lateinit var user: User

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)

        user = ChatListFragmentArgs.fromBundle(requireArguments()).user

        adapter = ChatListAdapter(groupList, this)
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvGroupList.layoutManager = layoutManager
        binding.rvGroupList.adapter = adapter

        chatViewModel.getGroupsLiveData(user).observe(viewLifecycleOwner, { operation ->
            when (operation.type) {
                0 -> {
                    groupList.add(operation.message)
                }
                1 -> {
                    val idx = groupList.indexOfFirst { it.id == operation.message.id}
                    groupList.removeAt(idx)
                    groupList.add(operation.message)
                }
                2 -> {}
            }
            groupList.sortWith { lhs, rhs ->
                if (lhs.name < rhs.name) -1 else 1
            }
            adapter.notifyDataSetChanged()
        })

        binding.fabCreateGroup.setOnClickListener {
            findNavController().navigate(ChatListFragmentDirections.actionChatListFragmentToCreateGroupFragment(user))
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        groupList.clear()
    }

    override fun onSelect(group: Group) {
        findNavController().navigate(ChatListFragmentDirections.actionChatListFragmentToChatMessageListFragment(user, group))
    }
}