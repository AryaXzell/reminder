package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconUtils {

    val availableIcons: List<Pair<String, ImageVector>> = listOf(
        "list" to Icons.Filled.List,
        "bookmark" to Icons.Filled.Bookmark,
        "pin" to Icons.Filled.PushPin,
        "gift" to Icons.Filled.CardGiftcard,
        "cake" to Icons.Filled.Cake,
        "school" to Icons.Filled.School,
        "book" to Icons.Filled.Book,
        "work" to Icons.Filled.Work,
        "cart" to Icons.Filled.ShoppingCart,
        "car" to Icons.Filled.DirectionsCar,
        "flight" to Icons.Filled.Flight,
        "restaurant" to Icons.Filled.Restaurant,
        "health" to Icons.Filled.LocalHospital,
        "fitness" to Icons.Filled.FitnessCenter,
        "pet" to Icons.Filled.Pets,
        "music" to Icons.Filled.MusicNote,
        "home" to Icons.Filled.Home,
        "heart" to Icons.Filled.Favorite,
        "star" to Icons.Filled.Star,
        "tag" to Icons.Filled.LocalOffer,
        "phone" to Icons.Filled.Phone,
        "email" to Icons.Filled.Email,
        "camera" to Icons.Filled.PhotoCamera,
        "code" to Icons.Filled.Code,
        "palette" to Icons.Filled.Palette,
        "lightbulb" to Icons.Filled.Lightbulb,
        "wb_sunny" to Icons.Filled.WbSunny,
        "nights_stay" to Icons.Filled.NightsStay,
        "cloud" to Icons.Filled.Cloud,
        "coffee" to Icons.Filled.LocalCafe,
        "wallet" to Icons.Filled.AccountBalanceWallet,
        "movie" to Icons.Filled.Movie,
        "flag" to Icons.Filled.Flag
    )

    fun getIconByName(name: String): ImageVector {
        return availableIcons.find { it.first.equals(name, ignoreCase = true) }?.second ?: Icons.Filled.List
    }
}
