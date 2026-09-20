package gd.app.hiboard.ui

import android.content.Context
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import gd.app.hiboard.R
import com.coui.appcompat.R as CouiR

internal fun couiContext(context: Context): Context {
    val probed = TypedValue()
    return if (context.theme.resolveAttribute(CouiR.attr.couiColorCardBackground, probed, true)) {
        context
    } else {
        ContextThemeWrapper(context, R.style.Theme_Hiboard)
    }
}

@ColorInt
internal fun Context.couiColor(@AttrRes attr: Int): Int {
    val value = TypedValue()
    if (!theme.resolveAttribute(attr, value, true)) return 0
    return if (value.resourceId != 0) getColor(value.resourceId) else value.data
}

tailrec fun Context.findActivity(): android.app.Activity? = when (this) {
    is android.app.Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}
