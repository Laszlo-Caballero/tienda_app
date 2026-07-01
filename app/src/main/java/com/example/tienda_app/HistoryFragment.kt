package com.laszlo.tienda_app
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.laszlo.tienda_app.api.ApiController
import com.laszlo.tienda_app.components.HistoryAdapter
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistoryFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.container_user)
        adapter = HistoryAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        loadData()
    }

    private fun loadData(){
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val history = ApiController.api.getHistory()
                val emptyView = view?.findViewById<View>(R.id.layout_empty_state)
                val recyclerView = view?.findViewById<View>(R.id.container_user)
                if (history.isEmpty()) {
                    emptyView?.visibility = View.VISIBLE
                    recyclerView?.visibility = View.GONE
                } else {
                    emptyView?.visibility = View.GONE
                    recyclerView?.visibility = View.VISIBLE
                    adapter.updateData(history)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                view?.findViewById<View>(R.id.layout_empty_state)?.visibility = View.VISIBLE
                view?.findViewById<View>(R.id.container_user)?.visibility = View.GONE
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment history.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HistoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
