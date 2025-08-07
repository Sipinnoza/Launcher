package com.znliang.launcher.main

import android.app.ActivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.znliang.launcher.LauncherApplication
import com.znliang.launcher.appdata.AppLoader
import com.znliang.launcher.R
import com.znliang.launcher.search.SearchAppListAdapter
import com.znliang.launcher.tags.adapter.AppAdapter
import com.znliang.launcher.tags.listener.ITagClickListener
import com.znliang.launcher.tags.model.AppInfo
import com.znliang.launcher.tags.tag.TagCloudView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        val app = application as LauncherApplication
        MainViewModelFactory(AppLoader(this, packageManager, app.appInfoDao))
    }

    private lateinit var rootView: ViewGroup
    private lateinit var tagContainer: ViewGroup
    private lateinit var tagCloudView: TagCloudView
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: EditText

    // Scroll property
    private var isZoomOut = true
    private var touchStartY = 0f
    private val swipeThreshold = 30f

    // temp data
    private val clickAppList = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setDarkSystemBars()
        initViews()

        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                // 更新UI
                if (state.appList.size != tagCloudView.getAdapter()?.count) {
                    val adapter = AppAdapter(state.appList, object : ITagClickListener {
                        override fun onItemClick(app: AppInfo) {
                            viewModel.processIntent(MainIntent.LaunchApp(app))
                            clickAppList.add(app)
                        }
                        override fun onItemLongPress(app: AppInfo) {
                            viewModel.processIntent(MainIntent.LongPress(app, this@MainActivity, tagContainer))
                        }
                    })
                    tagCloudView.setAdapter(adapter)
                } else {
                    (tagCloudView.getAdapter() as? AppAdapter)?.updateView(clickAppList.toList())
                    clickAppList.clear()
                }
                if (state.isSearchVisible) {
                    searchView.visibility = View.VISIBLE
                    searchView.post {
                        searchView.requestFocus()
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)
                    }
                } else {
                    searchView.setText("")
                    searchView.visibility = View.GONE
                    zoomOut(tagCloudView)
                }
                recyclerView.visibility = if (state.isResultVisible) View.VISIBLE else View.GONE
                (recyclerView.adapter as? SearchAppListAdapter)?.updateData(state.searchResults)
            }
        }
        viewModel.processIntent(MainIntent.LoadApps)
    }

    override fun onResume() {
        super.onResume()
        viewModel.processIntent(MainIntent.LoadApps)
    }

    private fun initViews() {
        rootView = findViewById(R.id.root_container)
        tagContainer = findViewById(R.id.tag_container)
        tagCloudView = findViewById(R.id.tagCloudView)
        recyclerView = findViewById(R.id.search_result)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SearchAppListAdapter(emptyList()) { app ->
            viewModel.processIntent(MainIntent.LaunchApp(app))
            zoomOut(tagCloudView)
        }

        searchView = findViewById(R.id.searchView)
        searchView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.processIntent(MainIntent.SearchQueryChanged(s?.toString().orEmpty()))
            }
        })
        initRootView()
    }

    private fun initRootView() {
        rootView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchStartY = event.y
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaY = event.y - touchStartY

                    if (abs(deltaY) < swipeThreshold) {
                        v.performClick()
                        return@setOnTouchListener false
                    }

                    if (deltaY < -swipeThreshold) {
                        zoomIn()
                    } else if (deltaY > swipeThreshold) {
                        zoomOut(v)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setDarkSystemBars() {
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.black)
        window.decorView.systemUiVisibility = 0
        val color = ContextCompat.getColor(this, R.color.black)
        setTaskDescription(ActivityManager.TaskDescription(getString(R.string.app_name), null, color))
        // hook back
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                zoomOut(tagCloudView)
            }
        })
    }

    private fun zoomIn() {
        if (!isZoomOut) return
        // 向上滑动：缩小并显示键盘
        tagContainer.animate()
            .scaleX(0.5f)
            .scaleY(0.5f)
            .setDuration(300)
            .withEndAction {
                viewModel.processIntent(MainIntent.PageStateChanged(true))
            }
            .start()
        isZoomOut = false
    }

    private fun zoomOut(v: View) {
        if (isZoomOut) return
        viewModel.processIntent(MainIntent.PageStateChanged(false))
        val imm = v.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
        tagContainer.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .start()
        isZoomOut = true
    }
}
