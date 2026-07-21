package cit.edu.vestil.worklog.ui.common

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import cit.edu.vestil.worklog.R

object RowRenderer {
    data class RowAction(
        val text: String,
        val onClick: () -> Unit
    )

    fun addRow(
        container: LinearLayout,
        title: String,
        subtitle: String,
        meta: String,
        actions: List<RowAction> = emptyList()
    ) {
        val row = LayoutInflater.from(container.context)
            .inflate(R.layout.view_info_row, container, false)

        row.findViewById<TextView>(R.id.tvRowTitle).text = title
        row.findViewById<TextView>(R.id.tvRowSubtitle).text = subtitle
        row.findViewById<TextView>(R.id.tvRowMeta).text = meta

        val actionsContainer = row.findViewById<LinearLayout>(R.id.rowActionsContainer)
        actionsContainer.removeAllViews()
        if (actions.isEmpty()) {
            actionsContainer.visibility = View.GONE
        } else {
            actionsContainer.visibility = View.VISIBLE
            actions.forEach { action ->
                val actionButton = Button(container.context).apply {
                    text = action.text
                    isAllCaps = false
                    setTextColor(context.getColor(android.R.color.white))
                    setBackgroundResource(R.drawable.bg_primary_button)
                    setPadding(32, 20, 32, 20)
                    setOnClickListener { action.onClick() }
                }
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.marginEnd = 12
                actionsContainer.addView(actionButton, params)
            }
        }

        container.addView(row)
    }

    fun addRow(
        container: LinearLayout,
        title: String,
        subtitle: String,
        meta: String,
        actionText: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        val actions = if (actionText != null && onAction != null) {
            listOf(RowAction(actionText, onAction))
        } else {
            emptyList()
        }
        addRow(container, title, subtitle, meta, actions)
    }
}
