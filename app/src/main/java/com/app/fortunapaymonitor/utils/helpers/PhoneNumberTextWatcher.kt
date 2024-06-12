package com.app.fortunapaymonitor.utils.helpers

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText

class PhoneNumberTextWatcher(private val editText: TextInputEditText) : TextWatcher {
    private var isFormatting: Boolean = false

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // This can still be used to detect if the change was an addition or deletion
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (isFormatting) return

        isFormatting = true

        // Calculate where the cursor should be
        val currentPos = editText.selectionStart
        val digitsBeforeCursor =
            editText.text?.substring(0, currentPos)?.count { it.isDigit() } ?: 0

        val digits = s.filter { it.isDigit() }
        val formatted = formatPhoneNumber(digits.toString())

        editText.setText(formatted)
        setCursorPosition(digitsBeforeCursor)

        isFormatting = false
    }

    override fun afterTextChanged(s: Editable) {
        // Most logic can be done in onTextChanged to reflect changes immediately
    }

    private fun formatPhoneNumber(digits: String): String {
        // Handling based on the number of digits present
        return buildString {
            when {
                digits.length >= 2 -> {
                    append(
                        "(${
                            digits.substring(
                                0,
                                2
                            )
                        }) "
                    ) // Encloses the first two digits in parentheses and adds a space
                    if (digits.length > 2) {
                        append("${digits[2]} ") // Adds the third digit and a space
                        if (digits.length > 3) {
                            append(
                                digits.substring(
                                    3,
                                    digits.length.coerceAtMost(7)
                                )
                            ) // Appends the next four digits
                            if (digits.length > 7) {
                                append("-${digits.substring(7)}") // Adds a dash and then the remaining digits
                            }
                        }
                    }
                }

                digits.length == 1 -> {
                    append("(${digits})") // Partial formatting for a single digit
                }
            }
        }
    }


    private fun setCursorPosition(digitsBeforeCursor: Int) {
        // Calculate cursor position based only on digit count before cursor
        val cursorPos = editText.text?.let { text ->
            var count = 0
            var position = 0
            while (count < digitsBeforeCursor && position < text.length) {
                if (text[position].isDigit()) count++
                position++
            }
            position
        } ?: 0

        editText.setSelection(cursorPos.coerceAtMost(editText.text?.length ?: 0))
    }


}