package com.example.tienda_app.util

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class QrScannerHelper(private val context: Context) {

    /**
     * Launches the Google Play Services Code Scanner UI.
     * Requires no runtime camera permissions inside the app.
     */
    fun startScan(
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit,
        onCancelled: () -> Unit
    ) {
        val options = GmsBarcodeScannerOptions.Builder()
            .build()

        val scanner = GmsBarcodeScanning.getClient(context, options)
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val rawValue = barcode.rawValue
                if (rawValue != null) {
                    onSuccess(rawValue)
                } else {
                    onFailure(Exception("No se pudo obtener el contenido del código QR."))
                }
            }
            .addOnFailureListener { e ->
                if (e is ApiException && e.statusCode == CommonStatusCodes.CANCELED) {
                    onCancelled()
                } else {
                    onFailure(e)
                }
            }
    }
}
