package com.github.libretube.test.ui.sheets

import android.content.Intent
import android.os.Bundle
import com.github.libretube.test.constants.IntentData
import com.github.libretube.test.helpers.IntentHelper
import com.github.libretube.test.obj.BottomSheetItem

class IntentChooserSheet : BaseBottomSheet() {
    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = arguments?.getString(IntentData.url)!!
        
        val packages = IntentHelper.getResolveInfo(requireContext(), url)
        val items = packages.map { info ->
            BottomSheetItem(
                title = info.loadLabel(requireContext().packageManager).toString(),
                iconDrawable = info.loadIcon(requireContext().packageManager),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW)
                        .setData(android.net.Uri.parse(url))
                        .setClassName(info.activityInfo.packageName, info.activityInfo.name)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    requireContext().startActivity(intent)
                    dismiss()
                }
            )
        }
        setItems(items, null)
    }
}

