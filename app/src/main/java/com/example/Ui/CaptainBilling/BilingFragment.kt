package com.example.Ui.CaptainBilling

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ryte.R
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.biling_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class BilingFragment : Fragment(R.layout.biling_fragment) {
    private val theViewModel:BilingViewMode by viewModels ()
    private val arguments: BilingFragmentArgs by navArgs()
    lateinit var theProgress :ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        theViewModel.getMyCurrentRideIdForCap(arguments.RideId)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        theProgress= ProgressDialog(requireContext()).apply {
            setMessage("Fetching Data ..")
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            show()
        }
            theViewModel.muCurrentRideIdForCaptain.observe(viewLifecycleOwner, { theRide ->
                theRide?.let {
                    lifecycleScope.launch(Dispatchers.IO) {
                        theViewModel.getTheCstOldCredit(theRide.Customer!!)
                        theViewModel.getTheCapCredit()

                    }
                    theBillingDetails.text = "Distance is\nWaiting is\nVat Is\nPrice is "
                    theAmountToBeCollected.text = "${theRide.Price}"
                    rideDetails.text = " : ${
                        theRide.Distance!!
                    } \n : ${
                        theRide.WaitingTime!!
                    } \n : ${theRide.vat!!}\n : ${
                        theRide.Price!!
                    }..."
                    cashMethod.callOnClick()
                    theProgress.dismiss()
                    theViewModel.capId = theRide.Captain
                    theViewModel.cstId = theRide.Customer
                    theViewModel.initializeForCash(theRide.Price!!.toDouble())
                    cashMethod.setOnClickListener {
                        theProgress.apply {
                            setMessage("Updating..")
                            setCancelable(false)
                            setCanceledOnTouchOutside(false)
                            show()
                        }
                        theViewModel.initializeForCash(theRide.Price!!.toDouble())
                        lifecycleScope.launch(Dispatchers.IO) {

                            withContext(Dispatchers.Main) {
                                theBillingDetails.text = "Distance is\nWaiting is\nVat Is\nPrice is"
                                theAmountToBeCollected.text = "${theRide.Price}"
                                rideDetails.text = " : ${
                                    theRide.Distance!!
                                } \n : ${
                                    theRide.WaitingTime!!
                                } \n : ${
                                    theRide.vat!!
                                }\n : ${theRide.Price!!}... "
                                theProgress.dismiss()
                            }


                        }
                    }
                    creditThenCash.setOnClickListener {


                        theProgress.apply {
                            setMessage("Updating..")
                            setCancelable(false)
                            setCanceledOnTouchOutside(false)
                            show()
                        }

                        theViewModel.doTheMathForCashThenCreditFirst(theRide)
                        theAmountToBeCollected.text = "${theViewModel.cashToBeCollected!!}"
                        theProgress.dismiss()
                        recievedAmount.isVisible = true
                    }
                    recievedAmount.setOnClickListener {

                        if (enterTheAmount.text.isEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                "Please Enter The Required Amount",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            theViewModel.theCollectedAmount =
                                enterTheAmount.text.toString().toDouble()
                            theViewModel.theCollectedAmount?.let { theCollectedAmount ->


                                when (radioGroupofPaymentOptions.checkedRadioButtonId) {
                                    R.id.cashMethod -> {
                                        if (theCollectedAmount < theRide.Price!!.toDouble()) {
                                            theProgress.dismiss()
                                            Snackbar.make(
                                                requireView(),
                                                "Please Enter a higher or equal amount to the Ride Price  ",
                                                Snackbar.LENGTH_LONG
                                            ).show()
                                        } else {
                                            recievedAmount.isClickable = false
                                            lifecycleScope.launch(Dispatchers.Default) {
                                                theViewModel.cashPayment(theRide).join()
                                                theViewModel.dotheCompletionProcess(theRide).join()
                                                withContext(Dispatchers.Main) {
                                                    theProgress.dismiss()
                                          /*          findNavController().navigate(
                                                        BilingFragmentDirections.actionBilingFragmentToProfileFragment2(
                                                            "Cap"
                                                        )
                                                    )*/

                                                }
                                            }
                                        }
                                    }
                                    R.id.creditThenCash -> {
                                        if (theCollectedAmount < theViewModel.cashToBeCollected!!) {
                                            theProgress.dismiss()
                                            Toast.makeText(requireContext(), "Please Enter a higher or equal amount to the Ride Price  ", Toast.LENGTH_SHORT).show()
                                        } else {
                                            recievedAmount.isClickable = false
                                            lifecycleScope.launch(Dispatchers.Default) {
                                                theViewModel.creditThenCashPayment(theRide)
                                                theViewModel.dotheCompletionProcess(theRide).join()
                                                withContext(Dispatchers.Main) {
                                                    theProgress.dismiss()
                                                    AlertDialog.Builder(requireContext()).apply {
                                                        setMessage("Please  Make Sure  That The Customer approve the payment ")
                                                        setTitle("Completion Dialogue  ")
                                                        setPositiveButton(" Confirm ") { _, _ ->


                                                        }
                                                        setNegativeButton("later ") { _, _ ->


                                                        }
                                                    }.show()

                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                    }
                                }


                            }

                        }
                    }

                }
            })

theViewModel.updated.observe(viewLifecycleOwner,{
    it?.let {
        if (it)
            findNavController().navigate(BilingFragmentDirections.actionBilingFragmentToProfileFragment2("Cap")    )
    }
})

        }

    }







