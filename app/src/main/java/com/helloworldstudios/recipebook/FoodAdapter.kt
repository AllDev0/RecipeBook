package com.helloworldstudios.recipebook

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.helloworldstudios.recipebook.databinding.RecyclerRowBinding

class FoodAdapter(var mContext: Context, var foodList: MutableList<Food>) : RecyclerView.Adapter<FoodAdapter.RecyclerRowHolder>() {
    inner class RecyclerRowHolder(binding: RecyclerRowBinding): RecyclerView.ViewHolder(binding.root){
        var binding: RecyclerRowBinding
        init {
            this.binding = binding
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerRowHolder {
        val layoutInflater = LayoutInflater.from(mContext)
        val binding = RecyclerRowBinding.inflate(layoutInflater, parent, false)
        return RecyclerRowHolder(binding)
    }

    override fun getItemCount(): Int = foodList.size

    override fun onBindViewHolder(holder: RecyclerRowHolder, position: Int) {
        var food = foodList.get(position)
        val holderBinding = holder.binding
        val imageByteArray = foodList.get(position).image
        holderBinding.ivFood.setImageBitmap(BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size))
        holderBinding.tvFoodName.text = food.foodName

        holderBinding.cardViewFood.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToRecipeFragment(foodList.get(position).id)
            Navigation.findNavController(it).navigate(action)
        }

    }
}