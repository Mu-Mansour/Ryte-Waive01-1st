package com.example.Ui.Profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.example.ryte.R
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.profile_fragment.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.profile_fragment) {

private val theUserType:ProfileFragmentArgs by navArgs()
 private val theViewModel :ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            theViewModel.getTheUserData(theUserType.UserType)
            theViewModel.getTheTotalRides()
        }

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHomeUI()
        signOutFromProfile.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragment2ToSplash())
        }



    }

    @SuppressLint("SetTextI18n")
    private fun setUpHomeUI()
    {
        if (theUserType.UserType=="Cst")
        {
            theViewModel.cstDetails.observe(viewLifecycleOwner, {
                it?.let {
                    userImage.load(it.Image) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                        scale(Scale.FILL)


                    }
                    detailsofUser.text = " Phone: ${it.Phone} \n" +
                            " Credit: ${it.Credit} \n" +
                            " Status:${it.Status}    "

                    goToHomeFragment.setOnClickListener { _ ->
                        if (it.Credit.toString().toInt() >= 5) {
                            findNavController().navigate(ProfileFragmentDirections.actionProfileFragment2ToHomeFragment("Cst", it.Image))

                        } else {
                            Toast.makeText(requireContext(), "Low Credit..please Recharge", Toast.LENGTH_SHORT).show()
                        }
                    }

                }

            })
        }
        else
        {
            theViewModel.captainDetails.observe(viewLifecycleOwner,{
                it?.let {
                    userImage.load(it.image){
                        crossfade(true)
                        transformations(CircleCropTransformation())
                        scale(Scale.FILL)


                    }
                    detailsofUser.text=" Name: ${it.name} \n" +
                            " OnCity: ${it.OnCity} \n" +
                            " Zone:${it.zone }    "

                    goToHomeFragment.setOnClickListener { _->
                        findNavController().navigate(ProfileFragmentDirections.actionProfileFragment2ToHomeFragment("Cap",it.image))
                    }


                }

            })
        }
    }


}