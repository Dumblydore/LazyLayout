package me.mauricee.lazyLayout.demo

import android.os.Bundle
import android.os.Handler
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import me.mauricee.lazylayout.widget.LazyLayout

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    private var stateChangedRunnable: Runnable? = null
        set(value) {
            handler.removeCallbacks(field)
            field = value
        }

    private val handler by lazy { Handler(mainLooper) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lazy_loading_duration.text = getString(R.string.duration, lazy_loading_seekbar.progress)
        lazy_loading_seekbar.setOnSeekBarChangeListener(this)

        lazy_to_success.setOnClickListener { setState(LazyLayout.SUCCESS) }
        lazy_to_error.setOnClickListener { setState(LazyLayout.ERROR) }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        lazy_loading_duration.text = getString(R.string.duration, progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

    private fun setState(state: Int) {
        stateChangedRunnable = Runnable { lazy_container.state = state }
        lazy_container.state = LazyLayout.LOADING
        handler.postDelayed(stateChangedRunnable, lazy_loading_seekbar.progress.toLong())
    }

    override fun onDestroy() {
        handler.removeCallbacks(stateChangedRunnable)
        super.onDestroy()
    }
}
