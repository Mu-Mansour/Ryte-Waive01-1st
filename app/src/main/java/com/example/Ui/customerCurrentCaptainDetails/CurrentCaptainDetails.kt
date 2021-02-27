package com.example.Ui.customerCurrentCaptainDetails

import android.app.Dialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.example.ryte.Logic.Captain
import com.example.ryte.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CurrentCaptainDetails(private val capId: Captain, private val uid:String): AppCompatDialogFragment() {


        lateinit var capImageFromDB1: ImageView
        lateinit var dialougeCapNameDB1: TextView
        lateinit var ddalougeCapLicenceDB1: TextView
        lateinit var dialougeCapcarModelDB1: TextView
        lateinit var dialougeCapPhoneDB1: TextView
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


            val viewByInterruptException = activity?.layoutInflater?.inflate(R.layout.cap_dialoge, null)
            capImageFromDB1=viewByInterruptException!!.findViewById(R.id.capImageFromDB)
            dialougeCapNameDB1= viewByInterruptException.findViewById(R.id.dialogueCapNameDB)
            ddalougeCapLicenceDB1= viewByInterruptException.findViewById(R.id.dialogueCapLicenceDB)
            dialougeCapcarModelDB1= viewByInterruptException.findViewById(R.id.dialogueCapcarModelDB)
            dialougeCapPhoneDB1= viewByInterruptException.findViewById(R.id.dialogueCapPhoneDB)

            capImageFromDB1.load(capId.image) {
                scale(Scale.FILL)
                transformations(CircleCropTransformation())
            }
            dialougeCapNameDB1.text = capId.name
            ddalougeCapLicenceDB1.text = capId.licence
            dialougeCapcarModelDB1.text = capId.model
            dialougeCapPhoneDB1.text = capId.phone

            val theBuilder = androidx.appcompat.app.AlertDialog.Builder(requireContext()).apply {
                setView(viewByInterruptException)
                setTitle("Captain Details")
                setMessage("Here is Your Captain Details")
                    .setPositiveButton("am in Ride ") { _, _ ->
                        FirebaseDatabase.getInstance().reference.child("Rides").child("Pending").child(uid+ FirebaseAuth.getInstance().currentUser!!.uid).child("Cstiswithme").setValue("true").addOnSuccessListener {
                            FirebaseDatabase.getInstance().reference.child("Customers").child(
                                FirebaseAuth.getInstance().currentUser!!.uid).child("Status").setValue("Riding")

                        }
                    }.setNegativeButton("Ok"){ _, _ ->

                    }


            }
            return theBuilder.create()
        }
    }
