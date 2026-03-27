/*
 * Copyright (c) 2026. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.history

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.color.MaterialColors.getColor
import org.depparse.NightMode.isNightMode
import org.grammarscope.common.R
import com.google.android.material.R as MaterialR

class HistoryBottomSheet(private val select: (query: String) -> Unit) : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(MaterialR.id.design_bottom_sheet)
            bottomSheet?.background = getColor(requireContext(), MaterialR.attr.colorSecondary, Color.TRANSPARENT).toDrawable()
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // We reuse the existing HistoryFragment by adding it to this BottomSheet
        return inflater.inflate(R.layout.fragment_history_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragment = HistoryFragment().apply {
            selectListener = { query ->
                dismiss()
                select(query)
            }
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.history_container, fragment)
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
}
