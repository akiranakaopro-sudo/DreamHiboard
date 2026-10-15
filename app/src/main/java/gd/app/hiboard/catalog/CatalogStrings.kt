package gd.app.hiboard.catalog

import android.content.Context
import android.content.res.Resources
import gd.app.hiboard.model.CardCatalogEntry

fun CardCatalogEntry.name(resources: Resources): String = resources.getString(nameRes)

fun CardCatalogEntry.description(resources: Resources): String = resources.getString(descriptionRes)

fun CardCatalogEntry.groupTitle(resources: Resources): String = resources.getString(groupTitleRes)

fun CardCatalogEntry.listCategory(resources: Resources): String =
    if (storeCategoryRes != 0) resources.getString(storeCategoryRes) else name(resources)

fun CardCatalogEntry.name(context: Context): String = name(context.resources)

fun CardCatalogEntry.description(context: Context): String = description(context.resources)

fun CardCatalogEntry.groupTitle(context: Context): String = groupTitle(context.resources)

fun CardCatalogEntry.listCategory(context: Context): String = listCategory(context.resources)
