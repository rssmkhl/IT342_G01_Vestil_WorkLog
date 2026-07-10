package cit.edu.vestil.worklog.ui.common

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import cit.edu.vestil.worklog.R

object RowRenderer {
    fun addRow(
        container: LinearLayout,
        title: String,
        subtitle: String,
        meta: String,
        actionText: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        val row = LayoutInflater.from(container.context)
            .inflate(R.layout.view_info_row, container, false)

        row.findViewById<TextView>(R.id.tvRowTitle).text = title
        row.findViewById<TextView>(R.id.tvRowSubtitle).text = subtitle
        row.findViewById<TextView>(R.id.tvRowMeta).text = meta

        val actionButton = row.findViewById<Button>(R.id.btnRowAction)
        if (actionText != null && onAction != null) {
            actionButton.visibility = View.VISIBLE
            actionButton.text = actionText
            actionButton.setOnClickListener { onAction() }
        } else {
            actionButton.visibility = View.GONE
        }

        container.addView(row)
    }
}
