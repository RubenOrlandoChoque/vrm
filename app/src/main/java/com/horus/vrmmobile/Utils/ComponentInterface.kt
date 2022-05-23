package com.horus.vrmmobile.Utils

/**
 * Created by mparraga on 28/8/2018.
 */
open interface ComponentInterface {
    fun setOnChangeValues(l: ComponentInterface.OnChangeValues)

    interface OnChangeValues {
        fun onChangeValues(questionSectionId: String)
    }
}