package com.example.util

class ZodString(private val value: String) {
    private val errors = mutableListOf<String>()

    fun min(length: Int, message: String): ZodString {
        if (value.length < length) {
            errors.add(message)
        }
        return this
    }

    fun max(length: Int, message: String): ZodString {
        if (value.length > length) {
            errors.add(message)
        }
        return this
    }

    fun regex(pattern: String, message: String): ZodString {
        if (!value.matches(Regex(pattern))) {
            errors.add(message)
        }
        return this
    }

    fun safeParse(): ZodResult {
        return if (errors.isEmpty()) {
            ZodResult.Success(value)
        } else {
            ZodResult.Error(errors.first())
        }
    }
}

sealed class ZodResult {
    data class Success(val data: String) : ZodResult()
    data class Error(val message: String) : ZodResult()
}

object Zod {
    fun string(value: String): ZodString = ZodString(value)
}
