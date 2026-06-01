package com.laszlo.tienda_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.laszlo.tienda_app.model.ProductAnalysis

class AnalysisResultsFragment : Fragment() {

    private var results: ArrayList<ProductAnalysis> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val serializableList = it.getSerializable("results") as? ArrayList<ProductAnalysis>
            if (serializableList != null) {
                results = serializableList
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analysis_results, container, false)
        
        val rvResults = view.findViewById<RecyclerView>(R.id.rvResults)
        rvResults.layoutManager = LinearLayoutManager(context)
        rvResults.adapter = AnalysisResultAdapter(results) { selectedProduct ->
            val detailFragment = ProductDetailFragment.newInstance(selectedProduct)
            val containerId = (requireView().parent as ViewGroup).id
            parentFragmentManager.beginTransaction()
                .replace(containerId, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        val tvItemsFound = view.findViewById<TextView>(R.id.tvItemsFound)
        tvItemsFound.text = "${String.format("%02d", results.size)} Elementos"

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(results: ArrayList<ProductAnalysis>) =
            AnalysisResultsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("results", results)
                }
            }
    }
}
