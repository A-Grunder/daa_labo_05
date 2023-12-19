package ch.heigvd.daa.groupe_a5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GalleryAdapter(private val images: List<Item>,
                     private val scope: CoroutineScope) :
    RecyclerView.Adapter<GalleryAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View,
                          private val scope: CoroutineScope) :
        RecyclerView.ViewHolder(view) {

        val imageView: ImageView = itemView.findViewById(R.id.gallery_image_view)
        val progressBar: ProgressBar = itemView.findViewById(R.id.gallery_progress_bar)

        private var currentJob: Job? = null

        fun cancelJob() {
            currentJob?.cancel()
        }

        fun loadImage(item: Item) {
            cancelJob()

            currentJob = scope.launch {
                progressBar.visibility = View.VISIBLE
                imageView.visibility = View.GONE
                val isCached = item.isImageCached(itemView.context)
                val  bytes = if (isCached) {
                    item.getCachedImage(itemView.context)
                } else {
                    item.downloadImage(itemView.context)
                }
                val bitmap = item.decodeImage(bytes)
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                    progressBar.visibility = View.GONE
                    imageView.visibility = View.VISIBLE
                }
            }
        }

        companion object {
            fun create(parent: ViewGroup,
                       inflater: LayoutInflater,
                       scope: CoroutineScope):
                    ImageViewHolder {

                val view: View = inflater.inflate(R.layout.gallery_item, parent, false)
                return ImageViewHolder(view, scope)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ImageViewHolder.create(parent, inflater, scope)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]
        holder.loadImage(image)

        holder.itemView.setOnClickListener {
            // feedback to the user that the image was clicked
            holder.imageView.alpha = 0.7f
            holder.imageView.animate().alpha(1f).duration = 300

        }
    }

    override fun onViewRecycled(holder: ImageViewHolder) {
        holder.cancelJob()
        super.onViewRecycled(holder)
    }

}