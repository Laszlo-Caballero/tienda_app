package com.example.tienda_app

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ScanFragment : Fragment() {

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            val file = saveBitmapToFile(it)
            uploadFile(file)
        }
    }

    private val pickGalleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val file = getFileFromUri(it)
            if (file != null) {
                uploadFile(file)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<View>(R.id.btnTakePhoto).setOnClickListener {
            takePhotoLauncher.launch(null)
        }
        
        view.findViewById<View>(R.id.btnUploadGallery).setOnClickListener {
            pickGalleryLauncher.launch("image/*")
        }

        view.findViewById<View>(R.id.btnScanQr).setOnClickListener {
            scanQrCode()
        }
    }

    private fun scanQrCode() {
        val helper = com.example.tienda_app.util.QrScannerHelper(requireContext())
        helper.startScan(
            onSuccess = { qrContent ->
                handleQrContent(qrContent)
            },
            onFailure = { e ->
                Toast.makeText(requireContext(), "Error al escanear QR: ${e.message}", Toast.LENGTH_SHORT).show()
            },
            onCancelled = {
                Toast.makeText(requireContext(), "Escaneo cancelado", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun handleQrContent(content: String) {
        val trimmed = content.trim()
        try {
            // Option 1: QR contains full Product JSON
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                val product = com.google.gson.Gson().fromJson(trimmed, com.example.tienda_app.model.ProductAnalysis::class.java)
                if (product != null && !product.nombre.isNullOrEmpty()) {
                    navigateToProductDetails(product)
                    return
                }
            }
        } catch (e: Exception) {
            // Fallback to text matching
        }

        // Option 2: QR contains a Product ID or Name - Search in repository
        val matchedProduct = com.example.tienda_app.api.ProductRepository.searchLocal(trimmed).firstOrNull()
        if (matchedProduct != null) {
            navigateToProductDetails(matchedProduct)
            view?.let {
                com.example.tienda_app.util.AccessibilityHelper.announce(it, "Código QR de ${matchedProduct.nombre} identificado.")
            }
        } else {
            Toast.makeText(requireContext(), "Contenido QR: $trimmed (No se encontró producto correspondiente)", Toast.LENGTH_LONG).show()
            view?.let {
                com.example.tienda_app.util.AccessibilityHelper.announce(it, "QR escaneado: $trimmed. Producto no encontrado.")
            }
        }
    }

    private fun navigateToProductDetails(product: com.example.tienda_app.model.ProductAnalysis) {
        val fragment = ProductDetailFragment.newInstance(product)
        val containerId = (requireView().parent as ViewGroup).id
        parentFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val file = File(requireContext().cacheDir, "temp_image.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return file
    }

    private fun getFileFromUri(uri: Uri): File? {
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        if (inputStream != null) {
            val file = File(requireContext().cacheDir, "temp_upload.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            return file
        }
        return null
    }

    private fun uploadFile(file: File) {
        val fragment = AnalyzingFragment.newInstance(file.absolutePath)
        val containerId = (requireView().parent as ViewGroup).id
        parentFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        @JvmStatic
        fun newInstance() = ScanFragment()
    }
}