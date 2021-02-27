package com.example.Ui.Splash

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.ryte.R
import com.google.firebase.auth.FirebaseAuth
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.splash_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Splash : Fragment(R.layout.splash_fragment) {
private val theViewModel:SplashViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            theViewModel.checkUser()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            YoYo.with(Techniques.Bounce).duration(2000).playOn(imageView)
            delay(2000)
            checkForPermissionAndNavigate()
        }


    }


    private fun   checkForPermissionAndNavigate()
    {
        TedPermission.with(requireContext()).setPermissionListener(object : PermissionListener {
            override fun onPermissionGranted() {
                theViewModel.theUser.observe(viewLifecycleOwner,{
                    it?.let {
                        if (it)
                        {
                            theViewModel.theUserType.observe(viewLifecycleOwner,{user->
                                user?.let {
                                    if (user=="Admin")
                                    {
                                        FirebaseAuth.getInstance().signOut()
                                        findNavController().navigate(SplashDirections.actionSplashToLoginForAllHome())
                                    }
                                    else
                                    {
                                        findNavController().navigate(SplashDirections.actionSplashToProfileFragment2(user))
                                    }

                                }
                            })
                        }
                        else
                        {
                            findNavController().navigate(SplashDirections.actionSplashToLoginForAllHome())
                        }
                    }
                })
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(requireContext(), "Please grant permissions manually", Toast.LENGTH_SHORT).show()
                    delay(3000)
                    requireActivity().finish()
                }
            }
        }).setPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION).check()

    }

}