package com.github.libretube.test.ui.sheets

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.libretube.test.R
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.github.libretube.test.obj.BottomSheetItem
import com.github.libretube.test.ui.theme.LibreTubeTheme
import kotlinx.coroutines.launch
import com.github.libretube.test.ui.extensions.onSystemInsets


open class BaseBottomSheet : ExpandedBottomSheet(0) {

    private var title: String? = null
    private var preselectedItem: String? = null
    private lateinit var items: List<BottomSheetItem>
    private lateinit var listener: (index: Int) -> Unit

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LibreTubeTheme {
                    GenericBottomSheetScreen(
                        title = title,
                        items = items,
                        onItemClick = { index ->
                            listener(index)
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // No-op - content is in ComposeView
    }

    fun setItems(items: List<BottomSheetItem>, listener: (suspend (index: Int) -> Unit)?) = apply {
        this.items = items
        this.listener = { index ->
            lifecycleScope.launch {
                val item = items[index]
                dialog?.hide()
                item.onClick()
                listener?.invoke(index)
                runCatching {
                    dismiss()
                }
            }
        }
    }

    fun setTitle(title: String?) {
        this.title = title
    }

    fun setSimpleItems(
        titles: List<String>,
        preselectedItem: String? = null,
        listener: (suspend (index: Int) -> Unit)?
    ) = apply {
        setItems(titles.map { BottomSheetItem(it) }, listener)
        this.preselectedItem = preselectedItem
    }

    companion object {
        // Removed legacy constants
    }
}

