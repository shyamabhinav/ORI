package com.example.ori20

import java.util.UUID

object MessageIdGenerator {

    fun generateId(): String {
        return UUID.randomUUID().toString()
    }
}