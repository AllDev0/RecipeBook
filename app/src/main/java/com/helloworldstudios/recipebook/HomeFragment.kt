package com.helloworldstudios.recipebook

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.helloworldstudios.recipebook.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private var foodList = mutableListOf<Food>()
    private lateinit var adapter: FoodAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = FoodAdapter(requireContext(), foodList)
        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerView.adapter = adapter
        getFoods()
        binding.fab.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToRecipeFragment(-1)
            Navigation.findNavController(it).navigate(action)
        }
    }

    private fun getFoods(){
        try {
            activity?.let {
                val database = it.openOrCreateDatabase("Foods", Context.MODE_PRIVATE, null)
                val cursor = database.rawQuery("SELECT * FROM foods", null)
                val idIx = cursor.getColumnIndex("id")
                val imageIx = cursor.getColumnIndex("image")
                val foodNameIx = cursor.getColumnIndex("foodname")
                val foodIngredientsIx = cursor.getColumnIndex("foodingredients")

                foodList.clear()

                while (cursor.moveToNext()){
                    foodList.add(Food(cursor.getInt(idIx), cursor.getBlob(imageIx), cursor.getString(foodNameIx), cursor.getString(foodIngredientsIx)))
                }
                adapter.notifyDataSetChanged()
                cursor.close()

            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }
}