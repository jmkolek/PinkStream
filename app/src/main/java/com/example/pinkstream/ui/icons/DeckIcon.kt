package com.example.pinkstream.ui.icons

import androidx.annotation.DrawableRes
import com.example.pinkstream.R

/**
 * Icons a tile can use (the icon picker offers these). [offDrawable] is shown when a
 * toggle is off or the mic is muted, for the icons that have one.
 * Interface icons (chevrons, pencil, sliders…) are used straight from R.drawable.
 */
enum class DeckIcon(@get:DrawableRes val drawable: Int, @get:DrawableRes val offDrawable: Int = drawable) {
    CLOCK(R.drawable.ic_deck_clock),
    GAMEPAD(R.drawable.ic_deck_gamepad),
    CHAT(R.drawable.ic_deck_chat),
    COFFEE(R.drawable.ic_deck_coffee),
    MOON(R.drawable.ic_deck_moon),
    EYE_OFF(R.drawable.ic_deck_eye_off),
    MIC(R.drawable.ic_deck_mic, R.drawable.ic_deck_mic_off),
    VOLUME(R.drawable.ic_deck_volume, R.drawable.ic_deck_volume_off),
    MUSIC(R.drawable.ic_deck_music),
    SPARKLES(R.drawable.ic_deck_sparkles),
    SMILE(R.drawable.ic_deck_smile),
    HEART(R.drawable.ic_deck_heart),
    RESET(R.drawable.ic_deck_reset),
    STAR(R.drawable.ic_deck_star),
    CAMERA(R.drawable.ic_deck_camera),
    SCISSORS(R.drawable.ic_deck_scissors),
    FLAG(R.drawable.ic_deck_flag),
    PLAY(R.drawable.ic_deck_play),
    MONITOR(R.drawable.ic_deck_monitor),
    BOLT(R.drawable.ic_deck_bolt),
}
