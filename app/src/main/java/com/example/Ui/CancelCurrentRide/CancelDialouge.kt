package com.example.Ui.CancelCurrentRide

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.ryte.Logic.PendingRideModule
import com.example.ryte.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class CancelDialouge(private val ride:PendingRideModule): AppCompatDialogFragment() {

    lateinit var dialougeMsg1: TextView
    lateinit var confimCancele: Button
    lateinit var cancelTheCancel: Button
    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


        val viewByInterruptException = activity?.layoutInflater?.inflate(R.layout.cancel_for_dialogue, null)



        dialougeMsg1=viewByInterruptException!!.findViewById(R.id.theMesseaGe)
        confimCancele=viewByInterruptException!!.findViewById(R.id.ConfirmCancelation)
        cancelTheCancel=viewByInterruptException!!.findViewById(R.id.canceTheCancel)

        if (ride.CancelableWithFees.toString().toBoolean())
        {
            dialougeMsg1.text = "We are sorry fur such a bad Experience ..This process will cost 5 Pounds "
        }
        else
        { dialougeMsg1.text = "We are sorry fur such a bad Experience .. This process will cost 0 Pounds "
        }
        confimCancele.setOnClickListener {
            confimCancele.isClickable=false
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(lifecycleScope.coroutineContext) {
                    cancelAndCreateRefrences(ride)
                    updateTheFees(ride)
                }

            }
            dialog!!.dismiss()
        }
        cancelTheCancel.setOnClickListener {
            dialog!!.dismiss()
        }

        val theBuilder = androidx.appcompat.app.AlertDialog.Builder(requireContext()).apply {
            setView(viewByInterruptException)

        }

        return theBuilder.create()
    }

    @SuppressLint("SimpleDateFormat")
    private fun cancelAndCreateRefrences(ride:PendingRideModule)
    {

        val theNewCompletedRideDetails= HashMap<String, Any>()
        theNewCompletedRideDetails["Status"]="Cancelled"
        theNewCompletedRideDetails["StartingTime"]=(ride.StartingTime!!).toString()
        theNewCompletedRideDetails["CaptainID"]=(ride.Captain!!).toString()
        theNewCompletedRideDetails["CustomerID"]=(ride.Customer!!).toString()
        theNewCompletedRideDetails["AmountDeducted"]=if (ride.CancelableWithFees!!.toBoolean()) "5" else "0"
        theNewCompletedRideDetails["Uid"]=(ride.Uid!!).toString()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss")
            theNewCompletedRideDetails["CancelledTime"]=  current.format(formatter)
        } else {
            var date = Date()
            val formatter = SimpleDateFormat("MMM dd yyyy HH:mma")
            theNewCompletedRideDetails["CancelledTime"]=formatter.format(date)
        }

        FirebaseDatabase.getInstance().reference.child("Rides").child("Cancelled").child(ride.Uid!!)
            .updateChildren(theNewCompletedRideDetails).addOnSuccessListener {
                FirebaseDatabase.getInstance().reference.child("MyRides").child(ride.Customer!!).child(ride.Uid!!).updateChildren(theNewCompletedRideDetails).addOnSuccessListener {
                    FirebaseDatabase.getInstance().reference.child("MyRides").child(ride.Captain!!).child(ride.Uid!!).updateChildren(theNewCompletedRideDetails).addOnSuccessListener {
                        FirebaseDatabase.getInstance().reference.child("Captins").child(ride.Captain!!).child("Status").setValue("OFF").addOnSuccessListener {
                            FirebaseDatabase.getInstance().reference.child("Customers").child(ride.Customer!!).child("Status").setValue("Ryte").addOnSuccessListener {
                                FirebaseDatabase.getInstance().reference.child("Rides").child("Pending").child(ride.Captain!!+ride.Customer).removeValue().addOnSuccessListener {
                                    FirebaseDatabase.getInstance().reference.child("Cap Busy With Rides").child(ride.Captain!!).removeValue().addOnSuccessListener {
                                        FirebaseDatabase.getInstance().reference.child("CustomersPending").child(ride.Customer!!).removeValue().addOnSuccessListener {
                                            FirebaseDatabase.getInstance().reference.child("CancelableRides").child(ride.Captain!!).removeValue().addOnSuccessListener {
                                                FirebaseDatabase.getInstance().reference.child("ConfirmedForCst").child(ride.Customer!!).removeValue()

                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
    }
    @SuppressLint("SetTextI18n")
    fun updateTheFees(ride: PendingRideModule)
    {
        if (ride.CancelableWithFees!!.toString().toBoolean())
        {

            FirebaseDatabase.getInstance().reference.child("Customers")
                .child(ride.Customer!!).child("Credit").addValueEventListener(object:
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists())
                        {

                            FirebaseDatabase.getInstance().reference.child("Customers")
                                .child(ride.Customer!!).child("Credit").removeEventListener(this)
                            FirebaseDatabase.getInstance().reference.child("Customers")
                                .child(ride.Customer!!).child("Credit").setValue(((snapshot.value.toString().toDouble())- 5.0).toString()).addOnSuccessListener {
                                    FirebaseDatabase.getInstance().reference.child("CaptainWallets")
                                        .child(ride.Captain!!).addValueEventListener(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists())
                                                {
                                                    FirebaseDatabase.getInstance().reference.child("CaptainWallets")
                                                        .child(ride.Captain!!).removeEventListener(this)
                                                    FirebaseDatabase.getInstance().reference.child("CaptainWallets")
                                                        .child(ride.Captain!!).setValue((((snapshot.value.toString().toDouble()) +5).toString()))
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                            }

                                        })
                                }


                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }

    }


}
