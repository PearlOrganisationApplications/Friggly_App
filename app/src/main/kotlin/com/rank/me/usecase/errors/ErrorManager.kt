package com.rank.me.usecase.errors

import com.rank.me.data.error.Error
import com.rank.me.data.error.mapper.ErrorMapper
import javax.inject.Inject

/**
 * Created by Saurabh, 27th sept 2022
 */

class ErrorManager @Inject constructor(private val errorMapper: ErrorMapper) : ErrorUseCase {
    override fun getError(errorCode: Int): Error {
        return Error(code = errorCode, description = errorMapper.errorsMap.getValue(errorCode))
    }
}
