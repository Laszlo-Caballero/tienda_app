package com.laszlo.tienda_app.components

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.laszlo.tienda_app.Constants
import com.laszlo.tienda_app.R
import com.laszlo.tienda_app.model.Promotion
import com.laszlo.tienda_app.util.AccessibilityHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PromoRevealDialog : BottomSheetDialogFragment() {

    private var promotion: Promotion? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            promotion = it.getSerializable(ARG_PROMOTION) as? Promotion
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_promo_reveal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle = view.findViewById<TextView>(R.id.tvPromoTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvPromoDescription)
        val tvCode = view.findViewById<TextView>(R.id.tvPromoCode)
        val imgPromoIcon = view.findViewById<ImageView>(R.id.imgPromoIcon)
        val btnClaim = view.findViewById<Button>(R.id.btnClaim)
        val tvDismiss = view.findViewById<TextView>(R.id.tvDismiss)

        promotion?.let { promo ->
            tvTitle.text = promo.title
            tvDescription.text = promo.description ?: "¡Felicidades! Tienes un nuevo descuento listo para usar."
            tvCode.text = promo.discount_code ?: "PROMO_TEMP"

            // Load promo image / QR from server or surprise image
            val imageUrl = if (promo.qr_code_url.startsWith("http")) {
                promo.qr_code_url
            } else {
                Constants.API_BASE_URL.removeSuffix("/") + "/" + promo.qr_code_url.removePrefix("/")
            }

            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_scan_focus)
                .error(R.drawable.ic_scan_focus)
                .into(imgPromoIcon)

            // Accessiblity Announcement on view display
            val announceMsg = "¡Bono especial desbloqueado! ${promo.title}. Código: ${promo.discount_code}. ${promo.description}"
            Handler(Looper.getMainLooper()).postDelayed({
                AccessibilityHelper.announce(view, announceMsg)
            }, 500)
        }

        btnClaim.setOnClickListener {
            // Visual feedback on button press
            btnClaim.text = "¡Bono Reclamado! 🎉"
            btnClaim.isEnabled = false
            promotion?.let { promo ->
                val successAnnounce = "Bono ${promo.title} reclamado con éxito. El descuento se aplicará a tu cuenta."
                AccessibilityHelper.announce(view, successAnnounce)
                Toast.makeText(context, "¡Cupón ${promo.discount_code} activado!", Toast.LENGTH_SHORT).show()
            }
            
            // Auto close after brief delay for visual/auditory completion
            Handler(Looper.getMainLooper()).postDelayed({
                dismiss()
            }, 1500)
        }

        tvDismiss.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        private const val ARG_PROMOTION = "arg_promotion"

        @JvmStatic
        fun newInstance(promotion: Promotion) =
            PromoRevealDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PROMOTION, promotion)
                }
            }
    }
}
