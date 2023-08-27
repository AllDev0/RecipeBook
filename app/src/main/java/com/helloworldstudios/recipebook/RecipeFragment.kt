package com.helloworldstudios.recipebook

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.helloworldstudios.recipebook.databinding.FragmentRecipeBinding
import java.io.ByteArrayOutputStream

class RecipeFragment : Fragment() {
    private lateinit var binding: FragmentRecipeBinding
    private var selectedImage: Uri? = null
    private var selectedBitmap: Bitmap? = null
    private lateinit var foodName: String
    private lateinit var foodIngredients: String
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivSelectImage.setOnClickListener {
            selectImage()
        }

        binding.btnSave.setOnClickListener {
            save()
        }

        arguments?.let {Bundle ->
            val id = RecipeFragmentArgs.fromBundle(Bundle).id
            if (id == -1){
                binding.btnSave.visibility = View.VISIBLE
                //binding.ivSelectImage.setImageBitmap(BitmapFactory.decodeResource(context?.resources, R.drawable.ic_gallery))
                binding.ivSelectImage.setImageResource(R.drawable.ic_gallery)
                binding.tietFoodName.setText("")
                binding.tietFoodIngredients.setText("")
            } else{
                binding.btnSave.visibility = View.INVISIBLE
                context?.let {context ->
                    try {
                        val database = context.openOrCreateDatabase("Foods", Context.MODE_PRIVATE, null)
                        val cursor = database.rawQuery("SELECT * FROM foods WHERE id = ?", arrayOf(id.toString()))

                        val imageIx = cursor.getColumnIndex("image")
                        val foodNameIx = cursor.getColumnIndex("foodname")
                        val foodIngredientsIx = cursor.getColumnIndex("foodingredients")

                        while (cursor.moveToNext()){
                            val imageByteArray = cursor.getBlob(imageIx)
                            binding.ivSelectImage.setImageBitmap(BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size))
                            binding.tietFoodName.setText(cursor.getString(foodNameIx))
                            binding.tietFoodIngredients.setText(cursor.getString(foodIngredientsIx))
                        }

                        cursor.close()

                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                }

            }
        }
    }

    private fun selectImage(){
        activity?.let {
            //Permission granted
            if (ContextCompat.checkSelfPermission(it.applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 2)
            } else{
                //Permission needed
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            }
        }

    }

    private fun save(){
        foodName = binding.tietFoodName.text.toString()
        foodIngredients = binding.tietFoodIngredients.text.toString()
        if (selectedBitmap != null){
            selectedBitmap = resizeBitmap(selectedBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            selectedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                context?.let {
                    val database = it.openOrCreateDatabase("Foods", Context.MODE_PRIVATE, null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS foods (id INTEGER PRIMARY KEY, image BLOB, foodname VARCHAR, foodingredients VARCHAR)")
                    val sql = "INSERT INTO foods (image, foodname, foodingredients) VALUES (?, ?, ?)"
                    val statement = database.compileStatement(sql)
                    statement.bindBlob(1, byteArray)
                    statement.bindString(2, foodName)
                    statement.bindString(3, foodIngredients)
                    statement.execute()
                }
            } catch (e: Exception){
                e.printStackTrace()
            }

            fragmentManager?.popBackStack()
            Toast.makeText(requireContext(), "Food saved successfully!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //Permisson granted
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 2)
        }

        //Permission denied
        else{

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //Image selected
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
            selectedImage = data.data

            try {
                context?.let {
                    if (selectedImage != null){
                        selectedBitmap = if (Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(it.contentResolver, selectedImage!!)
                            ImageDecoder.decodeBitmap(source)
                        } else{
                            MediaStore.Images.Media.getBitmap(it.contentResolver, selectedImage)
                        }
                        binding.ivSelectImage.setImageBitmap(selectedBitmap)
                    }
                }
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
        //Image not selected
        else if(requestCode == 2 && resultCode == Activity.RESULT_CANCELED){
            Toast.makeText(requireContext(), "You should select an image.", Toast.LENGTH_LONG).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun resizeBitmap(selectedBitmap: Bitmap, maxSize: Int): Bitmap{
        val bitmapRate = (selectedBitmap.width).toDouble() / (selectedBitmap.height).toDouble()
        return if (bitmapRate > 1){
            Bitmap.createScaledBitmap(selectedBitmap, maxSize, (maxSize / bitmapRate).toInt(), true)
        } else{
            Bitmap.createScaledBitmap(selectedBitmap, (maxSize*bitmapRate).toInt(), maxSize, true)
        }
    }
}