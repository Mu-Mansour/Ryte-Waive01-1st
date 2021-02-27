package com.example.Ui.Login

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ryte.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.login_for_all_home_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class LoginForAllHome : Fragment(R.layout.login_for_all_home_fragment) {

    private val theViewModel:LoginForAllHomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adminLogin.setOnClickListener {
            AlertDialog.Builder(requireContext()).setMessage("This is An Admin Login ...\nPlease Confirm that You are an Admin").setTitle("Admin Login").setPositiveButton(" Confirm", DialogInterface.OnClickListener { _, _ ->
                radioGroupForLogin.isVisible=false
                inputField1.hint="Email"
                inputField2.hint="Password"
                inputField1.isVisible=true
                inputField2.isVisible=true
                inputField2.transformationMethod = PasswordTransformationMethod.getInstance()
                theViewModel.theTypeOfUser="Admin"
                doTheJop.setOnClickListener {
                    if (inputField1.text.isNotEmpty() && inputField2.text.isNotEmpty())
                    {
                        theViewModel.adminEmail=inputField1.text.toString()
                        theViewModel.adminPass=inputField2.text.toString()
                        lifecycleScope.launch(Dispatchers.IO) {
                            theViewModel.loginForAdmin()
                            withContext(Dispatchers.Main){
                                inputField1.isVisible=false
                                inputField2.isVisible=false
                                progressBar.isVisible=true
                            }
                        }
                    }
                    else
                    {
                        Toast.makeText(requireContext(), "Empty Fields", Toast.LENGTH_SHORT).show()
                    }
                }
            }).setNegativeButton("Cancel", DialogInterface.OnClickListener { _, _ ->
                Toast.makeText(requireContext(), "Canceled ..", Toast.LENGTH_SHORT).show()
            }).show()
        }




        CaptainLogin.setOnClickListener {

            inputField1.hint="Email"
            inputField2.hint="Password"
            inputField1.isVisible=true
            inputField1.inputType=InputType.TYPE_CLASS_TEXT
            inputField2.isVisible=true
            inputField2.transformationMethod = PasswordTransformationMethod.getInstance()
            doTheJop.setOnClickListener {
                if (inputField1.text.isNotEmpty() && inputField2.text.isNotEmpty())
                {
                    theViewModel.capEmail=inputField1.text.toString()
                    theViewModel.capPass=inputField2.text.toString()
                    lifecycleScope.launch(Dispatchers.IO) {
                        theViewModel.loginForCap()
                        withContext(Dispatchers.Main){
                            inputField1.isVisible=false
                            inputField2.isVisible=false
                            progressBar.isVisible=true
                        }
                    }
                }
                else
                {
                    Toast.makeText(requireContext(), "Empty Fields", Toast.LENGTH_SHORT).show()
                }
            }
            theViewModel.theTypeOfUser="Cap"
        }
        cstLogin.setOnClickListener {
            inputField1.hint="Phone No"
            doTheJop.text="Verify"
            inputField1.inputType=InputType.TYPE_CLASS_NUMBER
            this.inputField2.isVisible=false
            doTheJop.setOnClickListener {
                if (inputField1.text.isNotEmpty())
                {
                   theViewModel.thePhoneNumber= inputField1.text.toString()
                    logInForCst()
                    inputField1.text.clear()
                    inputField1.isVisible=false
                    progressBar.isVisible=true
                    inputField1.transformationMethod = PasswordTransformationMethod.getInstance()
                }
                else
                {
                    Toast.makeText(requireContext(), "Empty Field", Toast.LENGTH_SHORT).show()
                }

            }
            theViewModel.theTypeOfUser="Cst"
        }

        radioGroupForLogin.checkedRadioButtonId.let {
            if (it == R.id.CaptainLogin)
            {
                CaptainLogin.callOnClick()
            }
          if (it == R.id.cstLogin)
            {
                cstLogin.callOnClick()
            }
        }

        theViewModel.loginSuccess.observe(viewLifecycleOwner, {
            it?.let {
                when {
                    it && theViewModel.theTypeOfUser == "Cap" -> {
                    findNavController().navigate(LoginForAllHomeDirections.actionLoginForAllHomeToProfileFragment2(theViewModel.theTypeOfUser!!))
                    }
                    it && theViewModel.theTypeOfUser == "Cst" -> {
                        theViewModel.checkIfCstExit()
                        theViewModel.cstExist.observe(viewLifecycleOwner,{exists->
                            exists?.let {
                                if (exists)
                                {
                                    findNavController().navigate(LoginForAllHomeDirections.actionLoginForAllHomeToProfileFragment2(theViewModel.theTypeOfUser!!))
                                }

                            }
                        })

                    }
                    it && theViewModel.theTypeOfUser == "Admin" -> {
                    theViewModel.checkIfAdmin()
                        theViewModel.isAdmin.observe(viewLifecycleOwner, { isadmin ->
                            isadmin?.let {
                                if (isadmin) {
                                    findNavController().navigate(LoginForAllHomeDirections.actionLoginForAllHomeToAdminHome2())
                                } else
                                {
                                    FirebaseAuth.getInstance().signOut()
                                    Toast.makeText(requireContext(), " Login", Toast.LENGTH_SHORT).show()
                                    inputField1.text.clear()
                                    inputField2.text.clear()
                                    inputField1.isVisible=true
                                    inputField2.isVisible=true
                                    progressBar.isVisible=false
                                }
                            }
                        })
                    }

                    else -> {
                        theViewModel.erorr.observe(viewLifecycleOwner,{error->
                            error?.let {theMessageFromit->
                                Toast.makeText(requireContext(), theMessageFromit, Toast.LENGTH_SHORT).show()
                                if (theViewModel.theTypeOfUser=="Cap")
                                {
                                    inputField1.isVisible=true
                                    inputField2.isVisible=true
                                    progressBar.isVisible=false
                                }
                                else if (theViewModel.theTypeOfUser=="Cst")
                                {
                                    inputField1.isVisible=true
                                    progressBar.isVisible=false
                                }

                            }
                        })

                    }
                }

            }
        })
        theViewModel.codeSentAlreay.observe(viewLifecycleOwner,{
            it?.let {
                inputField1.hint="Code Sent"
                doTheJop.text="Log In"
                progressBar.isVisible=false
                inputField1.isVisible=true

                doTheJop.setOnClickListener {
                    if (inputField1.text.isNotEmpty())
                    {
                        theViewModel.theCodeRecieved=inputField1.text.toString()
                        theViewModel.verifyTheNewCustomer()


                    }

                }

            }
        })
    }


    fun logInForCst(){


        theViewModel.thePhoneNumber?.let {
            val theCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    Toast.makeText(activity, "${p0.message}", Toast.LENGTH_SHORT).show()

                }

                override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(p0, p1)
                    theViewModel. theCodeSent = p0
                    theViewModel. codeSentAlreay.value=true

                }

            }

            val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                    .setPhoneNumber("+2${it.trim()}")       // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit // Activity (for callback binding)
                    .setActivity(requireActivity())
                    .setCallbacks(theCallBack)          // OnVerificationStateChangedCallbacks
                    .build()

            return@let  PhoneAuthProvider.verifyPhoneNumber(options)

        }
    }


}