/*
 * Copyright (c) 2026. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.color.MaterialColors.getColor
import org.depparse.NightMode.isNightMode
import org.grammarscope.common.R
import com.google.android.material.R as MaterialR

class SampleBottomSheet(private val select: (query: String) -> Unit) : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(MaterialR.id.design_bottom_sheet)
            bottomSheet?.background = getColor(requireContext(), MaterialR.attr.colorSurface, Color.TRANSPARENT).toDrawable()
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_bottomsheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragment = SampleFragment().apply {
            itemLayoutId = R.layout.item_sample
            selectListener = { query ->
                dismiss()
                select(query)
            }
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.list_container, fragment)
            .commit()

        // Fix icon contrast (prevents white icons on white/light bar).
        val isLightMode = !isNightMode(requireContext())
        val dialog = dialog as? BottomSheetDialog ?: return
        val window = dialog.window ?: return
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightNavigationBars = isLightMode
            isAppearanceLightStatusBars = isLightMode
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        val bottomSheet = dialog?.findViewById<View>(MaterialR.id.design_bottom_sheet)
        val isLandscape = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        bottomSheet?.let {
            if (isLandscape) {
                val behavior = BottomSheetBehavior.from(it)

                // Force the sheet to be expanded immediately
                behavior.state = BottomSheetBehavior.STATE_EXPANDED

                // Optional: Ensure it can still be dragged/dismissed
                behavior.skipCollapsed = true
            }
        }
    }
}
