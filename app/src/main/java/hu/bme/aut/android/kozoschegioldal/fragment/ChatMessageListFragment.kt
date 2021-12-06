package hu.bme.aut.android.kozoschegioldal.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.kozoschegioldal.adapter.ChatMessageListAdapter
import hu.bme.aut.android.kozoschegioldal.databinding.FragmentChatMessageListBinding
import hu.bme.aut.android.kozoschegioldal.model.Group
import hu.bme.aut.android.kozoschegioldal.model.Message
import hu.bme.aut.android.kozoschegioldal.model.User
import hu.bme.aut.android.kozoschegioldal.viewmodel.ChatViewModel
import java.util.*

class ChatMessageListFragment : Fragment() {
    private var _binding: FragmentChatMessageListBinding? = null
    private val binding get() = _binding!!

    private val chatViewModel: ChatViewModel by activityViewModels()
    private val messageList = mutableListOf<Message>()
    private lateinit var adapter: ChatMessageListAdapter
    private lateinit var user: User
    private lateinit var group: Group
    private var isScrolling = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatMessageListBinding.inflate(inflater, container, false)

        user = ChatMessageListFragmentArgs.fromBundle(requireArguments()).user
        group = ChatMessageListFragmentArgs.fromBundle(requireArguments()).group
        adapter = ChatMessageListAdapter(messageList, user)



        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        binding.rvChatDetail.layoutManager = layoutManager
        binding.rvChatDetail.adapter = adapter

        binding.rvChatDetail.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            binding.rvChatDetail.scrollToPosition(0)
        }

        binding.rvChatDetail.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_TOUCH_SCROLL)
                    isScrolling = true
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lm = binding.rvChatDetail.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = lm.findFirstVisibleItemPosition()
                val visibleCount = lm.childCount
                val totalCount = lm.itemCount

                if (isScrolling && (firstVisibleItemPosition + visibleCount == totalCount)) {
                    isScrolling = false
                    getProducts()
                }
            }
        })
        getProducts()

        binding.btnSendMessage.setOnClickListener {
            if (binding.editText.text.toString().isNotEmpty()) {
                chatViewModel.sendMessage(Message(binding.editText.text.toString(), Date(), user.uid), group)
                binding.editText.text.clear()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        //messageList.clear()
        chatViewModel.resetChatList()
    }

    fun getProducts() {
        val ld = chatViewModel.getChatListLiveData(group.id)
        if (ld != null) {
            ld.observe(viewLifecycleOwner, { operation ->
                when (operation.type) {
                    0 -> {
                        messageList.add(operation.message)
                    }
                    1 -> {
                    }
                    2 -> {
                        messageList.remove(operation.message)
                    }
                }
                messageList.sortWith { lhs, rhs ->
                    if (lhs.sentAt < rhs.sentAt) 1 else -1
                }
                adapter.notifyDataSetChanged()
                if (messageList.size == 10 || (binding.rvChatDetail.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() == 0)
                    binding.rvChatDetail.smoothScrollToPosition(0)
                //Log.d("Size", messageList.size.toString())
            })
        }
    }
}