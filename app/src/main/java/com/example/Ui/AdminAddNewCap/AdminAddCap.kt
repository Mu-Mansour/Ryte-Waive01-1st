package com.example.Ui.AdminAddNewCap

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.example.ryte.R
import com.google.firebase.auth.FirebaseAuth
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.admin_add_cap_fragment.*

@AndroidEntryPoint
class AdminAddCap : Fragment(R.layout.admin_add_cap_fragment) {
    val theViewModel:AdminAddCapViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        val addingCaptain =  ProgressDialog(requireContext()).apply {
            setMessage("Please Wait")
            setCancelable(false)
            setCanceledOnTouchOutside(false)

        }

        theCaptainPublicImage.setOnClickListener {
            CropImage.activity().start(requireContext(), this)
        }.also {
            theCaptainPublicImage.load(R.drawable.pilot){
                scale(Scale.FILL)
                transformations(CircleCropTransformation())
            }
        }
        CompleteAddingCaptain.setOnClickListener {
            if (CapnationalId.text.isEmpty()
                    ||Capzone.text.isEmpty()||
                    Capcategory.text.isEmpty()||
                    Capmodel.text.isEmpty()||
                    CapPhone.text.isEmpty()||
                    Caplicence.text.isEmpty()||
                    CapName.text.isEmpty()||
                    Capemail.text.isEmpty()||
                    theViewModel.imageURI==null
                    )
            {
                Toast.makeText(requireContext(), "Empty Field", Toast.LENGTH_SHORT).show()
            }
            else
            {
                addingCaptain.show()
                theViewModel.nationalId=CapnationalId.text.toString()
                theViewModel.zone=Capzone.text.toString()
                theViewModel.category=Capcategory.text.toString()
                theViewModel.model=Capmodel.text.toString()
                theViewModel.phone=CapPhone.text.toString()
                theViewModel.licence=Caplicence.text.toString()
                theViewModel.name=CapName.text.toString()
                theViewModel.email=Capemail.text.toString()
                theViewModel.addNewCaptain()
            }
        }


theViewModel.allTasksAreDone.observe(viewLifecycleOwner, {
    if (it) {
        addingCaptain.dismiss()
         findNavController().navigate(AdminAddCapDirections.actionAdminAddCapToAdminHome2())
    }
})

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode== CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE &&resultCode== Activity.RESULT_OK)
        {

            val result = CropImage.getActivityResult(data)
            theCaptainPublicImage.load(result.uri){
                scale(Scale.FILL)
                transformations(CircleCropTransformation())
                placeholder(R.drawable.pilot)
            }
            theViewModel.imageURI=result.uri
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseAuth.getInstance().signOut()
    }

}