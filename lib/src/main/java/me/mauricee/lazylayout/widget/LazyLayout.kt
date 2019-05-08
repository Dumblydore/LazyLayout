package me.mauricee.lazylayout.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.lazy_layout_error.view.*
import me.mauricee.lazylayout.lib.R

/**
 * A layout view that simplifies loading, error, and success states of a view
 */
class LazyLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr) {

    @State
    var state = LOADING
        set(value) {
            if (field != value) {
                field = value
                setState(value)
            }
        }

    var displayRetryButton: Boolean
        get() = errorRetry?.isVisible == true
        set(value) {
            lazy_error_retry?.isVisible = value
        }
    var errorText: CharSequence?
        get() = errorTextView?.text
        set(value) {
            errorTextView?.text = value
        }

    var loadingView: View? = null
        set(value) {
            field?.also(::removeView)
            if (value !is SwipeRefreshLayout)
                value?.also(::addView)
            field = value
        }

    var errorView: View? = null
        set(value) {
            field?.also(::removeView)
            value?.also(::addView)
            field = value
        }

    var retryListener: RetryListener? = null
    var stateUpdateListener: StateUpdateListener? = null

    private val errorTextView: TextView?
        get() = errorView?.findViewById(R.id.lazy_error_text)
    private val errorRetry: Button?
        get() = errorView?.findViewById(R.id.lazy_error_retry)
    private lateinit var successView: View

    init {
        attrs?.run { context.obtainStyledAttributes(this, R.styleable.LazyLayout) }?.also { a ->
            @LayoutRes val loadingLayout =
                a.getResourceId(
                    R.styleable.LazyLayout_loadingLayout,
                    R.layout.lazy_layout_loading
                )
            @LayoutRes val errorLayout = a.getResourceId(
                R.styleable.LazyLayout_errorLayout,
                R.layout.lazy_layout_error
            )
            LayoutInflater.from(context).also {
                loadingView = it.inflate(loadingLayout, this, false)
                errorView = it.inflate(errorLayout, this, false)
            }
            displayRetryButton = a.getBoolean(R.styleable.LazyLayout_displayRetry, false)
            state = a.getInt(
                R.styleable.LazyLayout_state,
                LOADING
            )
            a
        }?.recycle()

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount != 3) {
            throw RuntimeException("This layout requires one child!")
        }

        successView = getChildAt(2)
        loadingView?.visibility = View.GONE
        errorView?.visibility = View.GONE

        successView?.visibility = View.GONE

        lazy_error_retry?.setOnClickListener {
            state = LOADING
            retryListener?.onRetry()
        }
        updateViewState()
    }

    fun setErrorView(@LayoutRes viewId: Int) {
        errorView = LayoutInflater.from(context).inflate(viewId, this, false)
    }

    fun setState(@State state: Int, animate: Boolean) = setState(state, animate, true)

    @Synchronized
    private fun setState(@State state: Int, animate: Boolean = true, triggerNotify: Boolean = true) {
        postDelayed({
            when {
                loadingView is SwipeRefreshLayout -> updateSwipeRefreshViewState()
                animate -> animateViewState()
                else -> updateViewState()
            }

            if (triggerNotify) {
                stateUpdateListener?.onStateUpdated(state)
            }
        }, DEFAULT_ANIMATION_DURATION)
    }


    fun setupWithSwipeRefreshLayout(swipeRefreshLayout: SwipeRefreshLayout) {
        loadingView = swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener { state = LOADING }
    }

    private fun animateViewState() {

        val newActiveView: View = (
                when (state) {
                    ERROR -> errorView
                    LOADING -> loadingView
                    SUCCESS -> successView
                    else -> throw RuntimeException("Invalid LazyLayout.State")
                }) as View
        children().firstOrNull(View::isVisible)?.let {
            it.alpha = 1f
            getInactiveAnimation(it, getActiveAnimation(newActiveView))
        }?.start()
    }

    private fun updateViewState() {
        val newActiveView: View = (
                when (state) {
                    ERROR -> errorView
                    LOADING -> loadingView
                    SUCCESS -> successView
                    else -> throw RuntimeException("Invalid LazyLayout.State")
                }) as View

        children().filter { i -> newActiveView != i }.forEach { i -> i.visibility = View.GONE }
        newActiveView.visibility = View.VISIBLE
        newActiveView.bringToFront()
    }

    private fun updateSwipeRefreshViewState() {
        val loadingView = this.loadingView as? SwipeRefreshLayout
        loadingView?.isRefreshing = state == LOADING
        if (state == SUCCESS) {
            getInactiveAnimation(errorView!!, getActiveAnimation(successView!!)).start()
        } else if (state == ERROR) {
            getInactiveAnimation(successView!!, getActiveAnimation(errorView!!)).start()
        }
    }

    private fun getActiveAnimation(view: View): ViewPropertyAnimator {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        view.bringToFront()
        return view.animate().alpha(1f)
            .setStartDelay(DEFAULT_ANIMATION_DURATION)
            .setDuration(DEFAULT_ANIMATION_DURATION)
    }

    private fun getInactiveAnimation(
        view: View,
        activeAnimation: ViewPropertyAnimator
    ): ViewPropertyAnimator {
        return view.animate().alpha(0f)
            .setStartDelay(DEFAULT_ANIMATION_DURATION)
            .setDuration(DEFAULT_ANIMATION_DURATION)
            .withEndAction {
                view.visibility = View.GONE
                activeAnimation.start()
            }
    }

    private fun children(): List<View> {
        return IntRange(0, childCount - 1).map(this::getChildAt)
    }

    @IntDef(
        ERROR,
        LOADING,
        SUCCESS
    )
    @Retention
    private annotation class State

    interface StateUpdateListener {
        fun onStateUpdated(@State state: Int)
    }

    interface RetryListener {
        fun onRetry()
    }

    companion object {
        private const val DEFAULT_ANIMATION_DURATION: Long = 250
        const val ERROR = -1
        const val LOADING = 0
        const val SUCCESS = 1
    }
}