package com.example.pinkstream.ui.icons

import androidx.annotation.DrawableRes
import com.example.pinkstream.R

/**
 * The line icons a page can show in the bottom dock, in the order the page sheet
 * offers them. Saved in the layout by name: never rename an entry, or saved pages
 * stop loading (reordering is fine).
 */
enum class PageIcon(@get:DrawableRes val drawable: Int, val label: String) {
    GRID(R.drawable.ic_deck_grid, "Grid"),
    SMILE(R.drawable.ic_deck_smile, "Smile"),
    SPARKLES(R.drawable.ic_deck_sparkles, "Sparkles"),
    STAR(R.drawable.ic_deck_star, "Star"),
    HEART(R.drawable.ic_deck_heart, "Heart"),
    HOME(R.drawable.ic_deck_home, "Home"),
    GAMEPAD(R.drawable.ic_deck_gamepad, "Controller"),
    CHAT(R.drawable.ic_deck_chat, "Chat"),
    USERS(R.drawable.ic_deck_users, "People"),
    MIC(R.drawable.ic_deck_mic, "Microphone"),
    HEADPHONES(R.drawable.ic_deck_headphones, "Headphones"),
    VOLUME(R.drawable.ic_deck_volume, "Speaker"),
    MUSIC(R.drawable.ic_deck_music, "Music"),
    MONITOR(R.drawable.ic_deck_monitor, "Screen"),
    CAMERA(R.drawable.ic_deck_camera, "Camera"),
    PLAY(R.drawable.ic_deck_play, "Play"),
    SCISSORS(R.drawable.ic_deck_scissors, "Scissors"),
    FLAG(R.drawable.ic_deck_flag, "Flag"),
    GIFT(R.drawable.ic_deck_gift, "Gift"),
    TROPHY(R.drawable.ic_deck_trophy, "Trophy"),
    CROWN(R.drawable.ic_deck_crown, "Crown"),
    BELL(R.drawable.ic_deck_bell, "Bell"),
    BOLT(R.drawable.ic_deck_bolt, "Bolt"),
    PALETTE(R.drawable.ic_deck_palette, "Palette"),
    CLOCK(R.drawable.ic_deck_clock, "Clock"),
    COFFEE(R.drawable.ic_deck_coffee, "Coffee"),
    MOON(R.drawable.ic_deck_moon, "Moon"),
    SUN(R.drawable.ic_deck_sun, "Sun"),
    EYE_OFF(R.drawable.ic_deck_eye_off, "Hidden"),
    RESET(R.drawable.ic_deck_reset, "Reset"),
}
