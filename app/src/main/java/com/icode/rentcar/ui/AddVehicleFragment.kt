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

import com.icode.rentcar.models.Vehicle
import kotlinx.android.synthetic.main.fragment_add_vehicle.*
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import android.graphics.Bitmap
import com.google.firebase.firestore.SetOptions
import com.icode.rentcar.*
import java.io.ByteArrayOutputStream

private const val TAG = "AddVehicleFragment"

class AddVehicleFragment : Fragment() {
  private val db = FirebaseFirestore.getInstance()
  private val storage = FirebaseStorage.getInstance().reference

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View =
      inflater.inflate(R.layout.fragment_add_vehicle, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    yearSpinner.adapter = getSpinnerAdapter(YEARS)
    colorSpinner.adapter = getSpinnerAdapter(COLORS)
    makeSpinner.adapter = getSpinnerAdapter(MAKES)

    addPhotoButton.setOnClickListener { pickPhoto() }
    saveVehicleButton.setOnClickListener {
      if (isFormValid()) {
        showProgressBar()
        saveVehicleInfo(getVehicle())
      } else {
        toast("Debe agregar fotos y la información del vehículo")
      }
    }
  }

  private fun showProgressBar(display: Boolean = true) {
    if (display) {
      formContainer.visibility = View.INVISIBLE
      progressBar.visibility = View.VISIBLE
    } else {
      formContainer.visibility = View.VISIBLE
      progressBar.visibility = View.INVISIBLE
    }
  }

  private fun isFormValid(): Boolean {
    return photosContainer.childCount > 0 && arrayOf(yearSpinner, colorSpinner, makeSpinner)
        .all { it.selectedItem.toString() != DEFAULT_OPTION }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    when (requestCode) {
      0 -> if (resultCode == RESULT_OK) addImageToLayout(data?.data)
    }
  }

  private fun uploadImages(vehicleId: String) {
    val images = (0..photosContainer.childCount).map {
      photosContainer.getChildAt(it)
    }.filter { it is ImageView }
    val imagesCount = images.size
    var uploadedImages = 0

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

      ref.putBytes(data).addOnSuccessListener {
        it.downloadUrl?.let {
          updateVehiclePhotos(vehicleId, it)
          if (index == 0) {
            updateVehicleField(vehicleId, "imageUrl", it.toString())
          }
        }

        uploadedImages++

        if (uploadedImages == imagesCount) clearForm()
      }
    }
  }

  private fun clearForm() {
    if (isAdded) {
      photosContainer.removeAllViews()
      arrayOf(colorSpinner, makeSpinner, yearSpinner).forEach { it.setSelection(0) }
      toast("El vehiculo fue guardado")
      showProgressBar(false)
    }
  }

  private fun updateVehiclePhotos(vehicleId: String, imageUrl: Uri) {
    db.collection("photos")
        .document(vehicleId)
        .set(mapOf(imageUrl.toString() to "true"), SetOptions.merge())
  }

  private fun getVehicle() = Vehicle(
      make = makeSpinner.selectedItem.toString(),
      color = colorSpinner.selectedItem.toString(),
      year = yearSpinner.selectedItem.toString(),
      dealerId = getUserId()
  )

  private fun saveVehicleInfo(vehicle: Vehicle) {
    db.collection("vehicles")
        .add(vehicle)
        .addOnSuccessListener { documentReference ->
          Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")

          with(documentReference.id) {
            updateVehicleField(this, "id", this)
            uploadImages(this)
          }
        }.addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
  }

  private fun addImageToLayout(uri: Uri?) {
    uri?.let {
      val layoutParams = LinearLayout.LayoutParams(500, 500)
      val imageView = ImageView(context).apply {
        setImageURI(uri)
        setLayoutParams(layoutParams)
      }

      photosContainer.addView(imageView)
    }
  }

  private fun pickPhoto() {
    val galleryUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    Intent(Intent.ACTION_PICK, galleryUri).also {
      startActivityForResult(it, 0)
    }
  }
}
