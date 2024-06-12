package com.app.fortunapaymonitor.utils.helpers

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText

class CpfTextWatcher(private val editText: TextInputEditText) : TextWatcher {

    private var isUpdating = false
    private val mask = "###.###.###-##"
    private var oldText = ""

    override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
        val str = unmask(charSequence.toString())
        var formattedText = ""

        if (isUpdating) {
            oldText = str
            isUpdating = false
            return
        }

        var index = 0
        for (m in mask.toCharArray()) {
            if (m != '#' && str.length > oldText.length) {
                formattedText += m
                continue
            }
            try {
                formattedText += str[index]
            } catch (e: Exception) {
                break
            }
            index++
        }

        isUpdating = true
        editText.setText(formattedText)
        editText.setSelection(formattedText.length)
    }

    override fun afterTextChanged(editable: Editable?) {}

    private fun unmask(s: String): String {
        return s.replace("[^\\d]".toRegex(), "")
    }
}
