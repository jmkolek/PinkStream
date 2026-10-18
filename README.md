# Stream deck: key art and screen mockups

Everything here is ready to drop into the Android Studio project. Unzip into the project root, next to `settings.gradle.kts`; the `app/` folder merges into yours.

## What's inside

| Path | What it is |
|---|---|
| `app/src/main/res/drawable-mdpi … drawable-xxxhdpi/` | Every key as lossless WebP with transparency: 45 keys × on and off, plus `key_empty_slot`. Android picks the right density automatically. |
| `design/keys/master/` | The same keys as 512 × 512 PNG masters, for editing or for the store listing. Not used by the app. |
| `design/keys/keys.json` | The key catalog: id, name, group, drawable names and a suggested Streamer.bot sub-action. |
| `design/keys/key-sheet.png` | Every key, off and on, on one sheet. |
| `docs/mockups/*.png` | The five screens at 3× (1170 × 2532 px). Drawn at 390 × 844 dp. |
| `docs/mockups/html/*.html` | The same screens as HTML, for exact sizes, spacing and colours. They load key art from `design/keys/master/`. |
| `docs/snippets/DeckKeyArt.kt` | A Kotlin enum mapping each key to its on and off drawables. Change the package lines and move it into the app. |

## Screens

| File | Screen |
|---|---|
| `01-main` | The deck: 3 × 5 keys, status chips under the header, page tabs in a dock at the bottom |
| `02-edit-layout` | Edit layout mode on the Model page: remove badges, empty slots, a key being dragged, add page in the dock |
| `03-edit-key` | Edit key: preview of off and on art, name, key art picker by group, Streamer.bot action, switches |
| `04-choose-action` | The Streamer.bot action picker (bottom sheet) |
| `05-settings` | Connection and deck settings |

These replace the earlier deck design (pastel tiles with labels, the mic "hero" card and serif page tabs). Where the earlier `docs/DESIGN.md` and these mockups disagree, follow these mockups. Colours, fonts and the Choose action and Settings screens are unchanged.

In the PNGs the sans text was rendered with Inter as a stand-in for Plus Jakarta Sans. Layout, sizes, colours and the key art are exact.

## How the keys work

- **No text on keys.** The art is the whole key. Give every key a content description from its name ("Gaming, on").
- **Two states per key.**
  - **On (active):** deep rose key with a soft glow. Use it when the thing is active right now: the live scene, a mic or source that's on, a running timer.
  - **Off (idle):** the soft pink key. The microphone, desktop audio and music keys show a slash when off, because off means muted.
  - Keys without a real state (Applause, Create clip and similar) use the off art and can flash the on art for about 300 ms when pressed.
- **No status dots.** The rose colour is the only "active" signal on keys and tabs.
- **Size.** The art is drawn for keys about 112 dp wide; on a 3-column grid with 20 dp screen padding and 12 dp gaps each key is about 109 dp. The images include a 2 % transparent margin and their own rounded corners (radius ≈ 22 % of the size), so don't clip or add a background behind them.
- **Shadows.** The soft drop shadow under each key is not in the image. Add it in Compose:
  - off: `0 3dp` hard shadow `#E6B3C6` plus a soft `0 8dp 14dp` shadow `#B23A68` at 14 %
  - on: `0 3dp` hard shadow `#A93C68` plus a soft `0 8dp 20dp` shadow `#CF4C80` at 42 %
  - A simple approximation is `Modifier.shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = …, spotColor = …)`.

### In Compose

```kotlin
Image(
    painter = painterResource(DeckKeyArt.GAMING.art(isOn = sceneIsLive)),
    contentDescription = if (sceneIsLive) "Gaming, on air" else "Gaming",
    modifier = Modifier.size(109.dp)
)
```

The deck's button model can store a `DeckKeyArt` name instead of the earlier `DeckIcon` and `TileColor` pair.

## Key catalog

| id | Name | Group | Suggested Streamer.bot sub-action |
|---|---|---|---|
| `starting_soon` | Starting soon | Scenes | OBS Studio › Set Active Scene |
| `gaming` | Gaming | Scenes | OBS Studio › Set Active Scene |
| `just_chatting` | Just chatting | Scenes | OBS Studio › Set Active Scene |
| `brb` | BRB | Scenes | OBS Studio › Set Active Scene |
| `ending` | Ending | Scenes | OBS Studio › Set Active Scene |
| `privacy` | Privacy | Scenes | OBS Studio › Set Active Scene or Sources › Set Source Visibility State |
| `mic` | Microphone | Sound | OBS Studio › Sources › Set Source Mute State (Toggle) |
| `desktop_audio` | Desktop audio | Sound | OBS Studio › Sources › Set Source Mute State (Toggle) |
| `music` | Music | Sound | OBS Studio › Sources › Set Source Mute State (Toggle) |
| `applause` | Applause | Sound | Core › Sounds › Play Sound |
| `happy` | Happy | Model | VTube Studio › Trigger Hotkey |
| `blush` | Blush | Model | VTube Studio › Trigger Hotkey |
| `surprised` | Surprised | Model | VTube Studio › Trigger Hotkey |
| `sad` | Sad | Model | VTube Studio › Trigger Hotkey |
| `pout` | Pout | Model | VTube Studio › Trigger Hotkey |
| `reset_model` | Reset model | Model | VTube Studio › Trigger Hotkey (remove all expressions) |
| `create_clip` | Create clip | Extras | Twitch › Channel › Create Clip |
| `stream_marker` | Stream marker | Extras | Twitch › Channel › Create Stream Marker |
| `ad_break` | Ad break | Extras | Twitch › Ads › Run Commercial |
| `camera` | Camera | Extras | OBS Studio › Take Screenshot |
| `highlight` | Highlight | Extras | Twitch › Channel › Create Stream Marker |
| `go_live` | Go live | Going live and community | OBS Studio › Streaming (start) |
| `end_stream` | End stream | Going live and community | OBS Studio › Streaming (stop) |
| `raid` | Raid | Going live and community | Twitch › Raid › Start Raid |
| `shoutout` | Shoutout | Going live and community | Twitch › Moderation › Send Shoutout |
| `gift_subs` | Gift subs | Going live and community | Your own alert or chat message |
| `cheer` | Cheer | Going live and community | Your own alert or chat message |
| `poll` | Poll | Chat tools, comfort and gear | Twitch › Polls › Create Poll |
| `prediction` | Prediction | Chat tools, comfort and gear | Twitch › Predictions › Create Prediction |
| `slow_mode` | Slow mode | Chat tools, comfort and gear | Twitch › Moderation › Chat Modes |
| `hydrate` | Hydrate | Chat tools, comfort and gear | Core › Sounds › Play Sound or a chat message |
| `ring_light` | Ring light | Chat tools, comfort and gear | Core › Network › Fetch URL (smart light) or a program |
| `headset` | Headset | Chat tools, comfort and gear | Your own audio routing action |
| `webcam` | Webcam | Production | OBS Studio › Sources › Set Source Visibility State |
| `screen_share` | Screen share | Production | OBS Studio › Sources › Set Source Visibility State |
| `record` | Record | Production | OBS Studio › Recording |
| `instant_replay` | Instant replay | Production | OBS Studio › Replay › Set Replay Buffer State |
| `timer` | Timer | Production | Core › Timers › Set Timer State |
| `hype_train` | Hype train | Production | Your own alert action |
| `giveaway` | Giveaway | Fun and moderation | Your own giveaway action |
| `random_pick` | Random pick | Fun and moderation | Twitch › User › Add Random Users |
| `ban_hammer` | Ban hammer | Fun and moderation | Twitch › Moderation › Ban User |
| `lurk` | Lurk | Fun and moderation | A chat message or scene |
| `snack_break` | Snack break | Fun and moderation | OBS Studio › Set Active Scene |
| `lights` | Lights | Fun and moderation | Core › Network › Fetch URL (smart light) |

Drawables are `key_<id>_on` and `key_<id>_off`, plus `key_empty_slot`.
