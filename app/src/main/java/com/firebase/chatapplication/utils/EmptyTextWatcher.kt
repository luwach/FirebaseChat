package com.firebase.chatapplication.utils

import android.text.Editable
import android.text.TextWatcher

abstract class SimpleTextWatcher: TextWatcher {

    override fun afterTextChanged(s: Editable) {
        // do nothing
    }

    override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
        // do nothing
    }

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
        // do nothing
    }
}