package com.firebase.chatapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.chatapplication.R
import com.firebase.chatapplication.openLink
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_force_update.view.*

class ForceUpdateDialogFragment : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_force_update, null).apply {
            btnUpdate.setOnClickListener {
                context.openLink("https://appdistribution.firebase.dev/app_distro/projects/5d87c82b893cfa5dab18335c")
            }
        }
    }
}