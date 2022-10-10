package com.rank.me.ui.component.register

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.rank.me.databinding.ImageItemBinding

class ImageAdapter(
    private val context: Context,
    private val imageController: ImageController,
    private var imagePaths: List<Uri> = emptyList()
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ImageItemBinding.inflate(LayoutInflater.from(context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imagePath = imagePaths[position]

        holder.imageView.load(imagePath)

        holder.imageView.setOnClickListener { imageController.setImgMain(imagePath) }
    }

    fun changePath(imagePaths: ArrayList<Uri>) {
        this.imagePaths = imagePaths
        imageController.setImgMain(imagePaths[0])
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = imagePaths.size

    class ViewHolder(binding: ImageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView = binding.imgItem
    }
}
