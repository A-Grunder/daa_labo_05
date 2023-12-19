package ch.heigvd.daa.groupe_a5

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import android.view.Menu
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.cancelChildren
import java.net.URL
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: GalleryAdapter
    private lateinit var recyclerView: RecyclerView
    private val images = mutableListOf<Item>()
    private val scope = lifecycleScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recycler_view)
        adapter = GalleryAdapter(images, scope)
        recyclerView.adapter = adapter

        populateImages()

        val workManager = WorkManager.getInstance(this)
        val cleanCacheRequest = PeriodicWorkRequestBuilder<CleanCacheWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        ).addTag("cleanCacheTag").build()

        workManager.enqueueUniquePeriodicWork(
            "cleanCache",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanCacheRequest
        )
    }

    private fun populateImages() {
        for (i in 0..10_000) {
            images.add(Item(i, URL("https://daa.iict.ch/images/${i}.jpg")))
        }
    }

    fun onCacheCleared() {
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // add listeners to the menu items
        menu?.findItem(R.id.reload)?.setOnMenuItemClickListener {
            CleanCacheWorker.cleanCache(this)
            onCacheCleared()
            true
        }

        return true
    }

    override fun onDestroy() {
        // end all coroutines
        scope.coroutineContext.cancelChildren()
        super.onDestroy()
    }
}