package com.example.Ui.CaptainBill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ryte.R

class CaptainBilling : Fragment() {

    companion object {
        fun newInstance() = CaptainBilling()
    }

    private lateinit var viewModel: CaptainBillingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.captain_billing_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CaptainBillingViewModel::class.java)
        // TODO: Use the ViewModel
    }

}