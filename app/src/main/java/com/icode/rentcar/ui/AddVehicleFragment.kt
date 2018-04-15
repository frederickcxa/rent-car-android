package com.icode.rentcar.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.icode.rentcar.R
import com.icode.rentcar.models.Vehicle
import kotlinx.android.synthetic.main.fragment_add_vehicle.*
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.google.android.gms.tasks.OnSuccessListener
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import android.graphics.Bitmap
import com.google.firebase.firestore.SetOptions
import com.icode.rentcar.R.id.imageView
import com.icode.rentcar.R.id.imageView
import java.io.ByteArrayOutputStream
import com.google.firebase.storage.StorageReference


const val TAG = "AddVehicleFragment"

class AddVehicleFragment : Fragment() {
    var db = FirebaseFirestore.getInstance()
    var storage = FirebaseStorage.getInstance().reference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_add_vehicle, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val years = (1990..2018).toList()
        val colors = listOf(
                "Black",
                "Yellow",
                "Blue",
                "Red",
                "Gray",
                "White",
                "Orange"
        ).sorted()

        val makes = listOf(
                "Toyota",
                "Honda",
                "Mazda",
                "Nissan",
                "Chevrolet"
        ).sorted()

        yearSpinner.adapter = getSpinnerAdapter(years)
        colorSpinner.adapter = getSpinnerAdapter(colors)
        makeSpinner.adapter = getSpinnerAdapter(makes)

        addPhotoButton.setOnClickListener { pickPhoto() }
        saveVehicleButton.setOnClickListener {
            if (photosContainer.childCount > 0) {
                saveVehicleInfo(getVehicle())
            } else {
                Toast.makeText(context, "Debe agregar las fotos del vehiculo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            0 -> {
                if (resultCode == RESULT_OK) {
                    addImageToLayout(data?.data)
                }
            }
        }
    }

    private fun uploadImages(vehicleId: String) {
        val images = (0..photosContainer.childCount).map {
            photosContainer.getChildAt(it)
        }.filter { it is ImageView }

        images.map { image ->
            image.isDrawingCacheEnabled = true
            image.buildDrawingCache()

            val byteArrayOutputStream = ByteArrayOutputStream().apply {
                val bitmap = image.drawingCache
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, this)
            }

            byteArrayOutputStream.toByteArray()
        }.forEachIndexed { index, data ->
            val ref = storage.child("vehicles/$vehicleId/photos_$index.jpg")

            ref.putBytes(data).addOnSuccessListener { it.downloadUrl?.let { updateVehiclePhotos(vehicleId, it) } }
        }
    }

    private fun updateVehiclePhotos(vehicleId: String, imageUrl: Uri) {
        db.collection("photos")
                .document(vehicleId)
                .set(mapOf(imageUrl.toString() to "true"), SetOptions.merge())
    }

    private fun getVehicle(): Vehicle {
        return Vehicle(
                makeSpinner.selectedItem.toString(),
                colorSpinner.selectedItem.toString(),
                yearSpinner.selectedItem as Int,
                1
        )
    }

    private fun saveVehicleInfo(vehicle: Vehicle) {
        db.collection("vehicles")
                .add(vehicle)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")

                    uploadImages(documentReference.id)
                }.addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
    }

    private fun addImageToLayout(uri: Uri?) {
        uri?.let {
            val layoutParams = LinearLayout.LayoutParams(
                    500,
                    500
            )

            val imageView = ImageView(context).apply {
                setImageURI(uri)
                setLayoutParams(layoutParams)
            }

            photosContainer.addView(imageView)
        }
    }

    private fun pickPhoto() {
        val galleryUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Intent(Intent.ACTION_PICK, galleryUri).also {
            startActivityForResult(it, 0)
        }
    }

    private fun <T> getSpinnerAdapter(items: List<T>) = ArrayAdapter<T>(context, android.R.layout.simple_spinner_item, items).apply {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }
}
