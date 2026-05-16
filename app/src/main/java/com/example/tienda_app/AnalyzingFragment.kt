package com.example.tienda_app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.tienda_app.api.ApiController
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class AnalyzingFragment : Fragment() {

    private var fileToUpload: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val filePath = it.getString("filePath")
            if (filePath != null) {
                fileToUpload = File(filePath)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analyzing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fileToUpload?.let {
            uploadFile(it)
        }
    }

    private fun uploadFile(file: File) {
        lifecycleScope.launch {
            try {
                val requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file)
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                
                val response = ApiController.api.identifyProduct(body)
                
                if (response.data.isNotEmpty()) {
                    val fragment = AnalysisResultsFragment.newInstance(ArrayList(response.data))
                    val containerId = (requireView().parent as ViewGroup).id
                    parentFragmentManager.beginTransaction()
                        .replace(containerId, fragment)
                        .commit()
                } else {
                    Toast.makeText(requireContext(), "No se encontraron resultados", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            } catch (e: Exception) {
                Log.e("AnalyzingFragment", "Error uploading file", e)
                Toast.makeText(requireContext(), "Error de red al analizar imagen", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(filePath: String) =
            AnalyzingFragment().apply {
                arguments = Bundle().apply {
                    putString("filePath", filePath)
                }
            }
    }
}
