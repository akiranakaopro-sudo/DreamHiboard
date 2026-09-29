package gd.app.hiboard.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.PathInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import gd.app.hiboard.R
import gd.app.hiboard.model.NoteFolder

/** Full-height ColorOS "Choose notebook" sheet; [onSave] gets the picked folder key. */
class NotebookPickerDialog(
    context: Context,
    private val notebooks: List<NoteFolder>,
    private var selectedKey: String,
    private val onSave: (String) -> Unit,
) : Dialog(context, R.style.Theme_Hiboard_NotebookPicker) {

    private lateinit var root: View
    private lateinit var sheet: DragSheetLayout
    private lateinit var list: RecyclerView
    private val adapter = Adapter()
    private var closing = false
    private var entered = false
    private var afterClose: (() -> Unit)? = null
    private val scrim = ColorDrawable(Color.TRANSPARENT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_notebook_picker)
        val window = window!!
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING,
        )
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = true
        }

        root = findViewById(R.id.pickerRoot)
        root.background = scrim
        sheet = findViewById(R.id.pickerSheet)
        val list = findViewById<RecyclerView>(R.id.pickerList)
        list.layoutManager = LinearLayoutManager(context)
        list.adapter = adapter
        root.setOnClickListener { dismiss() }
        findViewById<View>(R.id.pickerCancel).setOnClickListener { dismiss() }
        findViewById<View>(R.id.pickerSave).setOnClickListener {
            val picked = selectedKey
            // Rebinding the widget during the slide-out would drop frames, so apply once it is gone.
            afterClose = { onSave(picked) }
            dismiss()
        }
        findViewById<EditText>(R.id.pickerSearch).doAfterTextChanged { adapter.filter(it?.toString().orEmpty()) }
        this.list = list
        sheet.gesture = SheetGesture()

        // Stay off screen until the status bar gap is laid out, or the first frames flash the sheet in place.
        sheet.translationY = context.resources.displayMetrics.heightPixels.toFloat()
        val sheetGap = (SHEET_TOP_GAP_DP * context.resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            sheet.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = bars.top + sheetGap }
            list.updatePadding(bottom = maxOf(bars.bottom, ime.bottom))
            if (!entered) {
                entered = true
                sheet.doOnLayout { enter() }
            }
            insets
        }
        adapter.filter("")
    }

    private fun enter() {
        sheet.translationY = sheet.height.toFloat()
        slideTo(0f, ENTER_MS)
    }

    override fun dismiss() {
        if (closing || !::sheet.isInitialized || !sheet.isAttachedToWindow) {
            if (!closing) finish()
            return
        }
        closing = true
        val height = sheet.height.toFloat()
        val remaining = if (height > 0f) 1f - sheet.translationY / height else 1f
        slideTo(height, (EXIT_MS * remaining).toLong().coerceAtLeast(MIN_EXIT_MS)) { finish() }
    }

    private fun finish() {
        super.dismiss()
        afterClose?.invoke()
        afterClose = null
    }

    private fun slideTo(target: Float, duration: Long, end: (() -> Unit)? = null) {
        sheet.animate().translationY(target).setDuration(duration).setInterpolator(EASE)
            .setUpdateListener { syncScrim() }
            .withEndAction { end?.invoke() }
            .start()
    }

    /** Dims the board in proportion to how much of the sheet is on screen. */
    private fun syncScrim() {
        val height = sheet.height.takeIf { it > 0 } ?: return
        val shown = (1f - sheet.translationY / height).coerceIn(0f, 1f)
        scrim.color = ColorUtils.setAlphaComponent(Color.BLACK, (shown * SCRIM_ALPHA * 255).toInt())
    }

    /**
     * Pulling down anywhere moves the sheet (inside the list only once it is scrolled to the top) and
     * a fling or long enough drag closes it; pulling up past the end of the list stretches it and
     * springs back.
     */
    private inner class SheetGesture : DragSheetLayout.Gesture {
        private val density = context.resources.displayMetrics.density
        private val flingVelocity = FLING_DP_PER_S * density
        private val maxStretch = MAX_STRETCH_DP * density
        private val spring = SpringAnimation(list, DynamicAnimation.TRANSLATION_Y, 0f).apply {
            spring.stiffness = SPRING_STIFFNESS
            spring.dampingRatio = SPRING_DAMPING
        }
        private var closingDrag = false

        override fun claim(dy: Float, x: Float, y: Float): Boolean {
            if (closing) return false
            val inList = isInList(x, y)
            closingDrag = dy > 0
            val claimed = if (closingDrag) {
                !inList || !list.canScrollVertically(-1)
            } else {
                inList && !list.canScrollVertically(1)
            }
            if (claimed) {
                sheet.animate().cancel()
                spring.cancel()
            }
            return claimed
        }

        override fun move(dy: Float) {
            if (closingDrag) {
                sheet.translationY = dy.coerceAtLeast(0f)
                syncScrim()
            } else {
                val pull = (-dy).coerceAtLeast(0f)
                list.translationY = -maxStretch * (1f - 1f / (1f + pull * STRETCH_RESISTANCE / maxStretch))
            }
        }

        override fun release(velocityY: Float, canceled: Boolean) {
            if (!closingDrag) {
                spring.start()
                return
            }
            val far = sheet.translationY > sheet.height * CLOSE_FRACTION
            val flung = velocityY > flingVelocity
            val back = velocityY < -flingVelocity
            if (!canceled && !back && (flung || far)) dismiss() else slideTo(0f, SETTLE_MS)
        }

        private fun isInList(x: Float, y: Float): Boolean {
            val at = IntArray(2)
            list.getLocationOnScreen(at)
            return x >= at[0] && x < at[0] + list.width && y >= at[1] && y < at[1] + list.height
        }
    }

    private inner class Adapter : RecyclerView.Adapter<Adapter.Holder>() {
        private var shown: List<NoteFolder> = emptyList()

        fun filter(query: String) {
            val q = query.trim()
            shown = if (q.isEmpty()) notebooks else notebooks.filter { it.label.contains(q, ignoreCase = true) }
            @Suppress("NotifyDataSetChanged")
            notifyDataSetChanged()
        }

        override fun getItemCount() = shown.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_notebook, parent, false))

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val notebook = shown[position]
            holder.name.text = notebook.label
            holder.count.text = holder.itemView.resources.getQuantityString(
                R.plurals.notebook_note_count,
                notebook.count,
                notebook.count,
            )
            holder.radio.isSelected = notebook.key.equals(selectedKey, ignoreCase = true)
            holder.itemView.setOnClickListener {
                selectedKey = notebook.key
                @Suppress("NotifyDataSetChanged")
                notifyDataSetChanged()
            }
        }

        inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.notebookName)
            val count: TextView = view.findViewById(R.id.notebookCount)
            val radio: ImageView = view.findViewById(R.id.notebookRadio)
        }
    }

    private companion object {
        const val SHEET_TOP_GAP_DP = 8f
        const val SCRIM_ALPHA = 0.35f
        const val ENTER_MS = 350L
        const val EXIT_MS = 250L
        const val MIN_EXIT_MS = 120L
        const val SETTLE_MS = 250L
        const val FLING_DP_PER_S = 800f
        const val CLOSE_FRACTION = 0.3f
        const val MAX_STRETCH_DP = 160f
        const val STRETCH_RESISTANCE = 0.55f
        const val SPRING_STIFFNESS = 500f
        const val SPRING_DAMPING = 0.75f
        val EASE = PathInterpolator(0.3f, 0f, 0.1f, 1f)
    }
}
