package gd.app.hiboard.ui

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.coui.appcompat.cardview.COUICardView
import com.coui.appcompat.R as CouiR
import gd.app.hiboard.R
import gd.app.hiboard.engine.RECENT_APP_LIMIT
import gd.app.hiboard.model.CardEngineId
import gd.app.hiboard.model.CardInstance
import gd.app.hiboard.model.ShortcutApp

class CardBinder(
    private val onOpenNotes: () -> Unit,
    private val onCreateNote: () -> Unit,
    private val onOpenApp: (ShortcutApp) -> Unit,
    private val onRemove: (String) -> Unit,
    private val onAdd: (String) -> Unit,
) {
    fun create(parent: ViewGroup, card: CardInstance, state: HiboardUiState, recommend: Boolean): View {
        val inflater = LayoutInflater.from(parent.context)
        val root = inflater.inflate(R.layout.item_board_card, parent, false)
        val body = root.findViewById<LinearLayout>(R.id.cardBody)
        val badge = root.findViewById<TextView>(R.id.cardBadge)
        when (card.engine) {
            CardEngineId.Advice -> bindAdvice(inflater, body, state)
            CardEngineId.Weather -> bindWeather(inflater, body, state)
            CardEngineId.Notes -> bindNotes(inflater, root, body, state)
            CardEngineId.InfoFlow -> bindInfoFlow(inflater, body, state)
            CardEngineId.RecentApps -> bindRecentApps(inflater, root, body, state)
        }
        if (state.editMode && card.canEdit) {
            badge.visibility = View.VISIBLE
            badge.text = if (recommend) "+" else "×"
            badge.setOnClickListener {
                if (recommend) onAdd(card.catalogId) else onRemove(card.catalogId)
            }
        } else {
            badge.visibility = View.GONE
        }
        return root
    }

    private fun bindAdvice(inflater: LayoutInflater, body: LinearLayout, state: HiboardUiState) {
        val view = inflater.inflate(R.layout.card_advice, body, true)
        view.findViewById<TextView>(R.id.adviceGreeting).text = state.content.adviceGreeting
        val items = view.findViewById<LinearLayout>(R.id.adviceItems)
        items.removeAllViews()
        state.content.adviceItems.forEach { item ->
            val title = TextView(body.context).apply {
                text = item.title
                setTextColor(body.context.couiColor(CouiR.attr.couiColorLabelPrimary))
                textSize = 14f
            }
            val subtitle = TextView(body.context).apply {
                text = item.subtitle
                setTextColor(body.context.couiColor(CouiR.attr.couiColorLabelSecondary))
                textSize = 12f
            }
            items.addView(title)
            items.addView(subtitle)
        }
    }

    private fun bindWeather(inflater: LayoutInflater, body: LinearLayout, state: HiboardUiState) {
        val view = inflater.inflate(R.layout.card_weather, body, true)
        view.findViewById<TextView>(R.id.weatherTemp).text = "${state.content.weatherTempC}°"
        view.findViewById<TextView>(R.id.weatherSummary).text =
            state.content.weatherSummary.ifBlank { "Local sample" }
    }

    private fun bindNotes(
        inflater: LayoutInflater,
        root: View,
        body: LinearLayout,
        state: HiboardUiState,
    ) {
        (root as? COUICardView)?.setCardBackgroundColor(body.context.getColor(R.color.hiboard_notes_card))
        val view = inflater.inflate(R.layout.card_notes, body, true)
        view.findViewById<TextView>(R.id.notesTitle).text = state.content.notesPreview
        view.findViewById<TextView>(R.id.notesSnippet).text = state.content.notesSnippet
        view.findViewById<TextView>(R.id.notesWhen).text = state.content.notesWhen
        view.findViewById<View>(R.id.notesAdd).setOnClickListener { onCreateNote() }
        view.findViewById<View>(R.id.notesRoot).setOnClickListener { onOpenNotes() }
        root.setOnClickListener { onOpenNotes() }
    }

    private fun bindInfoFlow(inflater: LayoutInflater, body: LinearLayout, state: HiboardUiState) {
        val view = inflater.inflate(R.layout.card_infoflow, body, true)
        val list = view.findViewById<LinearLayout>(R.id.infoFlowItems)
        list.removeAllViews()
        state.content.infoFlow.forEach { item ->
            val row = inflater.inflate(R.layout.item_infoflow, list, false)
            row.findViewById<TextView>(R.id.infoTitle).text = item.title
            row.findViewById<TextView>(R.id.infoSource).text = item.source
            list.addView(row)
        }
    }

    private fun bindRecentApps(
        inflater: LayoutInflater,
        root: View,
        body: LinearLayout,
        state: HiboardUiState,
    ) {
        val pad = (8 * body.resources.displayMetrics.density).toInt()
        val fill = body.context.getColor(R.color.hiboard_chrome_fill)
        (root as? COUICardView)?.apply {
            setCardBackgroundColor(fill)
            setContentPadding(pad, pad, pad, pad)
        }
        val view = inflater.inflate(R.layout.card_recent_apps, body, true)
        val row = view.findViewById<LinearLayout>(R.id.recentRow)
        row.removeAllViews()
        val pm = body.context.packageManager
        val labelColor = body.context.getColor(R.color.hiboard_chrome)
        state.content.recentApps.take(RECENT_APP_LIMIT).forEach { app ->
            val item = inflater.inflate(R.layout.item_recent_app, row, false)
            item.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            val iconView = item.findViewById<ImageView>(R.id.recentIcon)
            iconView.setImageDrawable(recentIcon(pm, app))
            item.findViewById<TextView>(R.id.recentLabel).apply {
                text = app.label
                setTextColor(labelColor)
            }
            item.setOnClickListener { onOpenApp(app) }
            row.addView(item)
        }
    }
}

private fun recentIcon(pm: PackageManager, app: ShortcutApp) = try {
    if (app.activityName != null) {
        pm.getActivityIcon(ComponentName(app.packageName, app.activityName))
    } else {
        pm.getApplicationIcon(app.packageName)
    }
} catch (_: PackageManager.NameNotFoundException) {
    null
}

fun launchIntent(view: View, intent: Intent?) {
    if (intent != null) {
        view.context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
