package gd.app.hiboard.ui

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.coui.appcompat.cardview.COUICardView
import gd.app.hiboard.R
import java.io.InputStream

data class ContactFace(
    val name: String,
    val avatarRes: Int = 0,
    val photoUri: String? = null,
    val lookupUri: String? = null,
)

fun bindContactsCard(
    inflater: LayoutInflater,
    card: COUICardView,
    body: LinearLayout,
    people: List<ContactFace>,
    message: String?,
    onPerson: ((ContactFace) -> Unit)?,
    onCard: (() -> Unit)?,
) {
    val density = body.resources.displayMetrics.density
    val padH = (8 * density).toInt()
    val padV = (6 * density).toInt()
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_contacts_card))
    card.setContentPadding(padH, padV, padH, padV)
    val view = inflater.inflate(R.layout.card_contacts, body, true)
    val row = view.findViewById<LinearLayout>(R.id.contactsRow)
    val messageView = view.findViewById<TextView>(R.id.contactsMessage)
    row.removeAllViews()
    if (people.isEmpty()) {
        row.visibility = View.GONE
        messageView.visibility = View.VISIBLE
        messageView.text = message.orEmpty()
        val open = View.OnClickListener { onCard?.invoke() }
        view.findViewById<View>(R.id.contactsRoot).setOnClickListener(open)
        card.setOnClickListener(open)
        return
    }
    messageView.visibility = View.GONE
    row.visibility = View.VISIBLE
    card.setOnClickListener(null)
    card.isClickable = false
    people.forEachIndexed { index, person ->
        val item = inflater.inflate(R.layout.item_contact, row, false)
        item.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        val photo = item.findViewById<FrameLayout>(R.id.contactPhoto)
        val avatar = item.findViewById<ImageView>(R.id.contactAvatar)
        val initial = item.findViewById<TextView>(R.id.contactInitial)
        photo.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(CONTACT_FALLBACK[index % CONTACT_FALLBACK.size])
        }
        photo.outlineProvider = ViewOutlineProvider.BACKGROUND
        photo.clipToOutline = true
        when {
            person.avatarRes != 0 -> {
                avatar.setImageResource(person.avatarRes)
                initial.visibility = View.GONE
            }
            else -> {
                val photo = contactBitmap(avatar, person.photoUri, person.lookupUri)
                if (photo != null) {
                    avatar.setImageBitmap(photo)
                    initial.visibility = View.GONE
                } else {
                    showInitial(avatar, initial, person)
                }
            }
        }
        item.findViewById<TextView>(R.id.contactName).text = person.name
        item.findViewById<ImageView>(R.id.contactAvatar).contentDescription = person.name
        if (onPerson != null) item.setOnClickListener { onPerson(person) }
        row.addView(item)
    }
}

private fun showInitial(avatar: ImageView, initial: TextView, person: ContactFace) {
    avatar.setImageDrawable(null)
    initial.visibility = View.VISIBLE
    initial.text = person.name.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
}

private fun contactBitmap(view: View, photoUri: String?, lookupUri: String?): Bitmap? {
    val resolver = view.context.contentResolver
    decodeUri(resolver, photoUri)?.let { return it }
    val contact = lookupUri?.takeIf { it.isNotBlank() }?.let(Uri::parse) ?: return null
    return try {
        ContactsContract.Contacts.openContactPhotoInputStream(resolver, contact, true)?.use(::decodePhoto)
    } catch (_: Exception) {
        null
    }
}

private fun decodeUri(resolver: ContentResolver, photoUri: String?): Bitmap? {
    if (photoUri.isNullOrBlank()) return null
    return try {
        resolver.openInputStream(Uri.parse(photoUri))?.use(::decodePhoto)
    } catch (_: Exception) {
        null
    }
}

/** Contact photo streams often cannot rewind, so decode the bytes instead of the stream. */
private fun decodePhoto(stream: InputStream): Bitmap? {
    val bytes = stream.readBytes()
    if (bytes.isEmpty()) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    var sample = 1
    while (bounds.outWidth / sample > 256 && bounds.outHeight / sample > 256) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
}

private val CONTACT_FALLBACK = intArrayOf(
    0xFF8BC34A.toInt(),
    0xFFF48FB1.toInt(),
    0xFF9575CD.toInt(),
    0xFFEF9A9A.toInt(),
)
