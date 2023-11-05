package kr.ac.wku.albeapp.photos


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.ac.wku.albeapp.R

class PhotoAdapter(var context: Context, var photoList: ArrayList<Photo>) :
    RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView!!) {
        var imageIv: ImageView = itemView.findViewById(R.id.image_iv)

        fun bind(photo: Photo) {
            Glide.with(context).load(photo.imageUrl).into(imageIv)
            imageIv.setOnClickListener {
                if (onItemClickListener != null)
                    onItemClickListener?.onItemClick(photo)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(photo: Photo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return photoList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var photo = photoList[position]
        holder.bind(photo)
    }
}