package gd.app.hiboard.ui

import android.widget.TextView

/** Tickers call this every second; an unchanged setText still relayouts the whole board. */
internal fun TextView.setTextIfChanged(value: CharSequence) {
    if (text.toString() != value.toString()) text = value
}
