package com.laszlo.tienda_app.util

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import java.util.regex.Pattern

object MarkdownParser {
    fun setMarkdownText(textView: TextView, text: String) {
        val ssb = SpannableStringBuilder()
        
        // Match either **bold** or [text](url)
        val pattern = Pattern.compile("(\\*\\*(.*?)\\*\\*)|(\\[(.*?)\\]\\((.*?)\\))")
        val matcher = pattern.matcher(text)
        
        var lastIndex = 0
        while (matcher.find()) {
            // Append plain text before the match
            ssb.append(text.substring(lastIndex, matcher.start()))
            
            if (matcher.group(1) != null) {
                // Bold match: group(2) contains the inner text
                val boldText = matcher.group(2) ?: ""
                val start = ssb.length
                ssb.append(boldText)
                ssb.setSpan(StyleSpan(Typeface.BOLD), start, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else if (matcher.group(3) != null) {
                // Link match: group(4) contains link text, group(5) contains URL
                val linkText = matcher.group(4) ?: ""
                val url = matcher.group(5) ?: ""
                val start = ssb.length
                ssb.append(linkText)
                
                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            widget.context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = true
                        ds.color = Color.parseColor("#0F766E") // Nice readable teal link color
                    }
                }
                ssb.setSpan(clickableSpan, start, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            
            lastIndex = matcher.end()
        }
        
        if (lastIndex < text.length) {
            ssb.append(text.substring(lastIndex))
        }
        
        textView.text = ssb
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
}
