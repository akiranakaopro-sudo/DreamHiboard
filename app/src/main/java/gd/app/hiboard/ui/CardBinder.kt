package gd.app.hiboard.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.coui.appcompat.R as CouiR
import gd.app.hiboard.R
import gd.app.hiboard.model.CardEngineId
import gd.app.hiboard.model.CardInstance
import gd.app.hiboard.model.ShortcutApp

class CardBinder(
    private val onOpenNotes: () -> Unit,
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
            CardEngineId.Shortcuts -> bindShortcuts(inflater, body, state.content.shortcuts)
            CardEngineId.Weather -> bindWeather(inflater, body, state)
            CardEngineId.Notes -> bindNotes(inflater, body, state)
            CardEngineId.Favorite -> bindShortcuts(inflater, body, state.content.favorites)
            CardEngineId.InfoFlow -> bindInfoFlow(inflater, body, state)
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

    private fun bindNotes(inflater: LayoutInflater, body: LinearLayout, state: HiboardUiState) {
        val view = inflater.inflate(R.layout.card_notes, body, true)
        view.findViewById<TextView>(R.id.notesPreview).text = state.content.notesPreview
        view.findViewById<View>(R.id.notesRoot).setOnClickListener { onOpenNotes() }
    }

    private fun bindShortcuts(
        inflater: LayoutInflater,
        body: LinearLayout,
        apps: List<ShortcutApp>,
    ) {
        val view = inflater.inflate(R.layout.card_shortcuts, body, true)
        val row = view.findViewById<LinearLayout>(R.id.shortcutRow)
        row.removeAllViews()
        apps.take(5).forEach { app ->
            val item = inflater.inflate(R.layout.item_shortcut, row, false)
            item.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            item.findViewById<TextView>(R.id.shortcutGlyph).text = app.label.take(1)
            item.findViewById<TextView>(R.id.shortcutLabel).text = app.label
            item.setOnClickListener { onOpenApp(app) }
            row.addView(item)
        }
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
}

fun launchIntent(view: View, intent: Intent?) {
    if (intent != null) {
        view.context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
