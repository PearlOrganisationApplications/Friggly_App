package com.rank.me.ui.base

import androidx.lifecycle.ViewModel
import com.rank.me.usecase.errors.ErrorManager
import javax.inject.Inject


/**
 * Created by Saurabh, 27th sept 2022
 */


abstract class BaseViewModel : ViewModel() {
    /**Inject Singleton ErrorManager
     * Use this errorManager to get the Errors
     */
    @Inject
    lateinit var errorManager: ErrorManager
}
