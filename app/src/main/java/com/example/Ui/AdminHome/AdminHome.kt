package com.example.Ui.AdminHome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ryte.R
import kotlinx.android.synthetic.main.admin_home_fragment.*

class AdminHome : Fragment(R.layout.admin_home_fragment) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adminHomeAddCaps.setOnClickListener {
            findNavController().navigate(AdminHomeDirections.actionAdminHome2ToAdminAddCap())
        }
    }
}