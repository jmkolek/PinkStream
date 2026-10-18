package com.example.pinkstream.ui.icons

import androidx.annotation.DrawableRes
import com.example.pinkstream.R

/**
 * Key art for the deck. Every key has an on (active) and an off (idle) image.
 * Files live in res/drawable-*dpi/ as key_<id>_on.webp and key_<id>_off.webp.
 * Saved in the layout by name: never rename an entry, or saved keys stop loading.
 */
enum class DeckKeyArt(val label: String, val group: String, @DrawableRes val on: Int, @DrawableRes val off: Int) {
    STARTING_SOON("Starting soon", "Scenes", R.drawable.key_starting_soon_on, R.drawable.key_starting_soon_off),
    GAMING("Gaming", "Scenes", R.drawable.key_gaming_on, R.drawable.key_gaming_off),
    JUST_CHATTING("Just chatting", "Scenes", R.drawable.key_just_chatting_on, R.drawable.key_just_chatting_off),
    BRB("BRB", "Scenes", R.drawable.key_brb_on, R.drawable.key_brb_off),
    ENDING("Ending", "Scenes", R.drawable.key_ending_on, R.drawable.key_ending_off),
    PRIVACY("Privacy", "Scenes", R.drawable.key_privacy_on, R.drawable.key_privacy_off),
    MIC("Microphone", "Sound", R.drawable.key_mic_on, R.drawable.key_mic_off),
    DESKTOP_AUDIO("Desktop audio", "Sound", R.drawable.key_desktop_audio_on, R.drawable.key_desktop_audio_off),
    MUSIC("Music", "Sound", R.drawable.key_music_on, R.drawable.key_music_off),
    APPLAUSE("Applause", "Sound", R.drawable.key_applause_on, R.drawable.key_applause_off),
    HAPPY("Happy", "Model", R.drawable.key_happy_on, R.drawable.key_happy_off),
    BLUSH("Blush", "Model", R.drawable.key_blush_on, R.drawable.key_blush_off),
    SURPRISED("Surprised", "Model", R.drawable.key_surprised_on, R.drawable.key_surprised_off),
    SAD("Sad", "Model", R.drawable.key_sad_on, R.drawable.key_sad_off),
    POUT("Pout", "Model", R.drawable.key_pout_on, R.drawable.key_pout_off),
    RESET_MODEL("Reset model", "Model", R.drawable.key_reset_model_on, R.drawable.key_reset_model_off),
    CREATE_CLIP("Create clip", "Extras", R.drawable.key_create_clip_on, R.drawable.key_create_clip_off),
    STREAM_MARKER("Stream marker", "Extras", R.drawable.key_stream_marker_on, R.drawable.key_stream_marker_off),
    AD_BREAK("Ad break", "Extras", R.drawable.key_ad_break_on, R.drawable.key_ad_break_off),
    CAMERA("Camera", "Extras", R.drawable.key_camera_on, R.drawable.key_camera_off),
    HIGHLIGHT("Highlight", "Extras", R.drawable.key_highlight_on, R.drawable.key_highlight_off),
    GO_LIVE("Go live", "Going live and community", R.drawable.key_go_live_on, R.drawable.key_go_live_off),
    END_STREAM("End stream", "Going live and community", R.drawable.key_end_stream_on, R.drawable.key_end_stream_off),
    RAID("Raid", "Going live and community", R.drawable.key_raid_on, R.drawable.key_raid_off),
    SHOUTOUT("Shoutout", "Going live and community", R.drawable.key_shoutout_on, R.drawable.key_shoutout_off),
    GIFT_SUBS("Gift subs", "Going live and community", R.drawable.key_gift_subs_on, R.drawable.key_gift_subs_off),
    CHEER("Cheer", "Going live and community", R.drawable.key_cheer_on, R.drawable.key_cheer_off),
    POLL("Poll", "Chat tools, comfort and gear", R.drawable.key_poll_on, R.drawable.key_poll_off),
    PREDICTION("Prediction", "Chat tools, comfort and gear", R.drawable.key_prediction_on, R.drawable.key_prediction_off),
    SLOW_MODE("Slow mode", "Chat tools, comfort and gear", R.drawable.key_slow_mode_on, R.drawable.key_slow_mode_off),
    HYDRATE("Hydrate", "Chat tools, comfort and gear", R.drawable.key_hydrate_on, R.drawable.key_hydrate_off),
    RING_LIGHT("Ring light", "Chat tools, comfort and gear", R.drawable.key_ring_light_on, R.drawable.key_ring_light_off),
    HEADSET("Headset", "Chat tools, comfort and gear", R.drawable.key_headset_on, R.drawable.key_headset_off),
    WEBCAM("Webcam", "Production", R.drawable.key_webcam_on, R.drawable.key_webcam_off),
    SCREEN_SHARE("Screen share", "Production", R.drawable.key_screen_share_on, R.drawable.key_screen_share_off),
    RECORD("Record", "Production", R.drawable.key_record_on, R.drawable.key_record_off),
    INSTANT_REPLAY("Instant replay", "Production", R.drawable.key_instant_replay_on, R.drawable.key_instant_replay_off),
    TIMER("Timer", "Production", R.drawable.key_timer_on, R.drawable.key_timer_off),
    HYPE_TRAIN("Hype train", "Production", R.drawable.key_hype_train_on, R.drawable.key_hype_train_off),
    GIVEAWAY("Giveaway", "Fun and moderation", R.drawable.key_giveaway_on, R.drawable.key_giveaway_off),
    RANDOM_PICK("Random pick", "Fun and moderation", R.drawable.key_random_pick_on, R.drawable.key_random_pick_off),
    BAN_HAMMER("Ban hammer", "Fun and moderation", R.drawable.key_ban_hammer_on, R.drawable.key_ban_hammer_off),
    LURK("Lurk", "Fun and moderation", R.drawable.key_lurk_on, R.drawable.key_lurk_off),
    SNACK_BREAK("Snack break", "Fun and moderation", R.drawable.key_snack_break_on, R.drawable.key_snack_break_off),
    LIGHTS("Lights", "Fun and moderation", R.drawable.key_lights_on, R.drawable.key_lights_off);

    @DrawableRes fun art(isOn: Boolean): Int = if (isOn) on else off

    companion object {
        /** Shown for empty slots while editing the layout. */
        @DrawableRes val EMPTY_SLOT: Int = R.drawable.key_empty_slot

        /** The "Choose key art" search: names containing [query], ignoring case, in catalog order. */
        fun search(query: String): List<DeckKeyArt> {
            val q = query.trim()
            return entries.filter { q.isEmpty() || it.label.contains(q, ignoreCase = true) }
        }
    }
}
