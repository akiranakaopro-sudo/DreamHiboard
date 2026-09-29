package gd.app.hiboard.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
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
    private lateinit var sheet: View
    private val adapter = Adapter()
    private var closing = false
    private var entered = false
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
            onSave(selectedKey)
            dismiss()
        }
        findViewById<EditText>(R.id.pickerSearch).doAfterTextChanged { adapter.filter(it?.toString().orEmpty()) }
        bindDragToClose(findViewById(R.id.pickerTitleBar))

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
            if (!closing) super.dismiss()
            return
        }
        closing = true
        val height = sheet.height.toFloat()
        val remaining = if (height > 0f) 1f - sheet.translationY / height else 1f
        slideTo(height, (EXIT_MS * remaining).toLong().coerceAtLeast(MIN_EXIT_MS)) { super.dismiss() }
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

    /** Dragging the title bar moves the sheet; a downward fling or a long enough drag closes it. */
    @SuppressLint("ClickableViewAccessibility")
    private fun bindDragToClose(handle: View) {
        val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        val flingVelocity = FLING_DP_PER_S * context.resources.displayMetrics.density
        var downY = 0f
        var dragging = false
        var tracker: VelocityTracker? = null
        handle.setOnTouchListener { _, event ->
            if (closing) return@setOnTouchListener false
            // The handle moves with the sheet, so track screen coordinates.
            val screenEvent = MotionEvent.obtain(event).apply { setLocation(event.rawX, event.rawY) }
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downY = event.rawY
                    dragging = false
                    tracker?.recycle()
                    tracker = VelocityTracker.obtain().also { it.addMovement(screenEvent) }
                }
                MotionEvent.ACTION_MOVE -> {
                    tracker?.addMovement(screenEvent)
                    val dy = event.rawY - downY
                    if (!dragging && dy > touchSlop) {
                        dragging = true
                        sheet.animate().cancel()
                        downY += touchSlop
                    }
                    if (dragging) {
                        sheet.translationY = (event.rawY - downY).coerceAtLeast(0f)
                        syncScrim()
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    tracker?.addMovement(screenEvent)
                    tracker?.computeCurrentVelocity(1000)
                    val velocity = tracker?.yVelocity ?: 0f
                    tracker?.recycle()
                    tracker = null
                    if (dragging) {
                        val far = sheet.translationY > sheet.height * CLOSE_FRACTION
                        val flung = velocity > flingVelocity
                        val back = velocity < -flingVelocity
                        if (event.actionMasked == MotionEvent.ACTION_UP && !back && (flung || far)) {
                            dismiss()
                        } else {
                            slideTo(0f, SETTLE_MS)
                        }
                    }
                    dragging = false
                }
            }
            screenEvent.recycle()
            true
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
        val EASE = PathInterpolator(0.3f, 0f, 0.1f, 1f)
    }
}
