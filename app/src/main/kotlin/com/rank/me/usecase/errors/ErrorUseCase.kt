package com.rank.me.usecase.errors

import com.rank.me.data.error.Error

interface ErrorUseCase {
    fun getError(errorCode: Int): Error
}
