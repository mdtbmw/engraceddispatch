package com.example.util

object FormatUtils {
    /**
     * Formats the tracking number input string dynamically.
     * - If it starts with "RDR" (rider ID), formats it as RDR-XX.
     * - Otherwise, formats it as XXXX-XXXX-XXXX (groups of 4 with hyphens).
     */
    fun formatTrackingId(input: String): String {
        val upper = input.uppercase()
        if (upper.startsWith("RDR")) {
            val clean = upper.replace("-", "").filter { it.isLetterOrDigit() }
            return if (clean.length > 3) {
                "RDR-" + clean.substring(3).take(2)
            } else {
                clean
            }
        } else {
            val clean = upper.replace("-", "").filter { it.isLetterOrDigit() }.take(12)
            return buildString {
                for (i in clean.indices) {
                    if (i > 0 && i % 4 == 0) {
                        append('-')
                    }
                    append(clean[i])
                }
            }
        }
    }

    /**
     * Checks if the phone number prefix begins correctly.
     * Valid prefixes are:
     * - Local: starts with '0', next digit must be '1', '7', '8', '9'
     * - Int'l (no plus): starts with '2', next digit must be '3', third must be '4'
     * - Int'l (plus): starts with '+', next digit can be '1', '4' (e.g. +44), or '2' (must be '234')
     */
    fun isPhoneBeginningWell(input: String): Boolean {
        if (input.isEmpty()) return true
        
        val first = input[0]
        if (first == '0') {
            if (input.length > 1) {
                val second = input[1]
                return second in listOf('1', '7', '8', '9')
            }
            return true
        }
        if (first == '2') {
            if (input.length > 1 && input[1] != '3') return false
            if (input.length > 2 && input[2] != '4') return false
            return true
        }
        if (first == '+') {
            if (input.length > 1) {
                val second = input[1]
                if (second == '2') {
                    if (input.length > 2 && input[2] != '3') return false
                    if (input.length > 3 && input[3] != '4') return false
                } else {
                    // Allow US (+1), UK (+44), etc.
                    return second == '1' || second == '4'
                }
            }
            return true
        }
        // Any other starting digit is invalid for our logistics brand
        return false
    }

    /**
     * Filters, formats, and strictly limits the length of a phone number as the user types.
     * Automatically inserts demarcation hyphens based on the prefix and stops further input.
     */
    fun formatAndLimitPhone(input: String): String {
        if (input.isEmpty()) return ""

        // Keep '+' if it is at the very beginning, and digits
        val clean = buildString {
            input.forEachIndexed { index, char ->
                if (char == '+' && index == 0) {
                    append(char)
                } else if (char.isDigit()) {
                    append(char)
                }
            }
        }

        if (clean.isEmpty()) return ""

        if (clean.startsWith("0")) {
            // Local Nigerian number format (e.g. 08031234567) - max 11 digits
            val digits = clean.take(11)
            return buildString {
                append(digits.take(4))
                if (digits.length > 4) {
                    append("-")
                    append(digits.substring(4, minOf(7, digits.length)))
                }
                if (digits.length > 7) {
                    append("-")
                    append(digits.substring(7))
                }
            }
        } else if (clean.startsWith("234")) {
            // International Nigerian format without plus (e.g. 2348031234567) - max 13 digits
            val digits = clean.take(13)
            return buildString {
                append(digits.take(3))
                if (digits.length > 3) {
                    append("-")
                    append(digits.substring(3, minOf(6, digits.length)))
                }
                if (digits.length > 6) {
                    append("-")
                    append(digits.substring(6, minOf(9, digits.length)))
                }
                if (digits.length > 9) {
                    append("-")
                    append(digits.substring(9))
                }
            }
        } else if (clean.startsWith("+234")) {
            // International Nigerian format with plus (e.g. +2348031234567) - max '+' + 13 digits = 14 chars
            val digits = clean.take(14) // includes '+'
            return buildString {
                append(digits.take(4)) // "+234"
                if (digits.length > 4) {
                    append("-")
                    append(digits.substring(4, minOf(7, digits.length)))
                }
                if (digits.length > 7) {
                    append("-")
                    append(digits.substring(7, minOf(10, digits.length)))
                }
                if (digits.length > 10) {
                    append("-")
                    append(digits.substring(10))
                }
            }
        } else if (clean.startsWith("+1")) {
            // US format with plus (e.g. +18005550199) - max '+' + 11 digits = 12 chars
            val digits = clean.take(12)
            return buildString {
                append(digits.take(2)) // "+1"
                if (digits.length > 2) {
                    append("-")
                    append(digits.substring(2, minOf(5, digits.length)))
                }
                if (digits.length > 5) {
                    append("-")
                    append(digits.substring(5, minOf(8, digits.length)))
                }
                if (digits.length > 8) {
                    append("-")
                    append(digits.substring(8))
                }
            }
        } else if (clean.startsWith("+44")) {
            // UK format with plus (e.g. +447911123456) - max '+' + 12 digits = 13 chars
            val digits = clean.take(13)
            return buildString {
                append(digits.take(3)) // "+44"
                if (digits.length > 3) {
                    append("-")
                    append(digits.substring(3, minOf(7, digits.length)))
                }
                if (digits.length > 7) {
                    append("-")
                    append(digits.substring(7))
                }
            }
        } else {
            // Default global format - max 15 digits
            val digits = clean.take(15)
            return digits
        }
    }
}

class PhoneVisualTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val originalText = text.text
        val formatted = formatPhone(originalText)
        
        val offsetMapping = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val originalTextLen = originalText.length
                val actualOffset = offset.coerceIn(0, originalTextLen)
                if (actualOffset <= 0) return 0

                var originalCharCount = 0
                for (i in 0 until formatted.length) {
                    if (formatted[i] != '-') {
                        originalCharCount++
                    }
                    if (originalCharCount == actualOffset) {
                        return i + 1
                    }
                }
                return formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                val formattedLen = formatted.length
                val actualOffset = offset.coerceIn(0, formattedLen)
                if (actualOffset <= 0) return 0

                var originalCharCount = 0
                for (i in 0 until actualOffset) {
                    if (formatted[i] != '-') {
                        originalCharCount++
                    }
                }
                return originalCharCount
            }
        }
        
        return androidx.compose.ui.text.input.TransformedText(androidx.compose.ui.text.AnnotatedString(formatted), offsetMapping)
    }

    private fun formatPhone(clean: String): String {
        if (clean.isEmpty()) return ""
        
        if (clean.startsWith("0")) {
            val digits = clean.take(11)
            return buildString {
                append(digits.take(4))
                if (digits.length > 4) {
                    append("-")
                    append(digits.substring(4, minOf(7, digits.length)))
                }
                if (digits.length > 7) {
                    append("-")
                    append(digits.substring(7))
                }
            }
        } else if (clean.startsWith("234")) {
            val digits = clean.take(13)
            return buildString {
                append(digits.take(3))
                if (digits.length > 3) {
                    append("-")
                    append(digits.substring(3, minOf(6, digits.length)))
                }
                if (digits.length > 6) {
                    append("-")
                    append(digits.substring(6, minOf(9, digits.length)))
                }
                if (digits.length > 9) {
                    append("-")
                    append(digits.substring(9))
                }
            }
        } else if (clean.startsWith("+234")) {
            val digits = clean.take(14)
            return buildString {
                append(digits.take(4))
                if (digits.length > 4) {
                    append("-")
                    append(digits.substring(4, minOf(7, digits.length)))
                }
                if (digits.length > 7) {
                    append("-")
                    append(digits.substring(7, minOf(10, digits.length)))
                }
                if (digits.length > 10) {
                    append("-")
                    append(digits.substring(10))
                }
            }
        } else if (clean.startsWith("+1")) {
            val digits = clean.take(12)
            return buildString {
                append(digits.take(2))
                if (digits.length > 2) {
                    append("-")
                    append(digits.substring(2, minOf(5, digits.length)))
                }
                if (digits.length > 5) {
                    append("-")
                    append(digits.substring(5, minOf(8, digits.length)))
                }
                if (digits.length > 8) {
                    append("-")
                    append(digits.substring(8))
                }
            }
        } else if (clean.startsWith("+44")) {
            val digits = clean.take(13)
            return buildString {
                append(digits.take(3))
                if (digits.length > 3) {
                    append("-")
                    append(digits.substring(3, minOf(7, digits.length)))
                }
                if (digits.length > 7) {
                    append("-")
                    append(digits.substring(7))
                }
            }
        } else {
            return clean.take(15)
        }
    }
}


