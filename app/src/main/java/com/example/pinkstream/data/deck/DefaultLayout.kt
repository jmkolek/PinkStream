package com.example.pinkstream.data.deck

import com.example.pinkstream.ui.icons.DeckIcon
import com.example.pinkstream.ui.theme.TileColor
import java.util.UUID

/**
 * The layout a fresh install starts with (docs/PLAN.md → Default layout).
 * Actions are matched by name; their ids are filled in after the first GetActions.
 * Scene and input names are placeholders the user corrects in the editor.
 */
object DefaultLayout {

    fun create(): DeckLayout = DeckLayout(
        pages = listOf(
            page(
                "Main",
                section(
                    null, SectionStyle.HERO,
                    button("Microphone", DeckIcon.MIC, TileColor.ROSE, "Deck - Mic Toggle", stateInput = "Mic/Aux"),
                ),
                section(
                    "Scenes", SectionStyle.GRID,
                    button("Starting soon", DeckIcon.CLOCK, TileColor.ROSE, "Deck - Scene Starting", onAirScene = "Starting soon"),
                    button("Gaming", DeckIcon.GAMEPAD, TileColor.ROSE, "Deck - Scene Gaming", onAirScene = "Gaming"),
                    button("Just chatting", DeckIcon.CHAT, TileColor.ROSE, "Deck - Scene Just chatting", onAirScene = "Just chatting"),
                    button("BRB", DeckIcon.COFFEE, TileColor.ROSE, "Deck - Scene BRB", onAirScene = "BRB"),
                    button("Ending", DeckIcon.MOON, TileColor.ROSE, "Deck - Scene Ending", onAirScene = "Ending"),
                    button("Privacy", DeckIcon.EYE_OFF, TileColor.MIST, "Deck - Privacy"),
                ),
                section(
                    "Sound", SectionStyle.GRID,
                    button("Desktop", DeckIcon.VOLUME, TileColor.PEACH, "Deck - Desktop Toggle", stateInput = "Desktop Audio"),
                    button("Music", DeckIcon.MUSIC, TileColor.PEACH, "Deck - Music Toggle", stateInput = "Music"),
                    button("Applause", DeckIcon.SPARKLES, TileColor.PEACH, "Deck - Applause"),
                ),
                section(
                    "Model", SectionStyle.GRID,
                    button("Happy", DeckIcon.SMILE, TileColor.LILAC, "Deck - VTS Happy"),
                    button("Blush", DeckIcon.HEART, TileColor.LILAC, "Deck - VTS Blush"),
                    button("Reset model", DeckIcon.RESET, TileColor.MIST, "Deck - VTS Reset"),
                ),
            ),
            page("Model", section("Expressions", SectionStyle.GRID)),
            page("Extras", section("Twitch", SectionStyle.GRID)),
        ),
    )

    private fun newId() = UUID.randomUUID().toString()

    private fun page(name: String, vararg sections: DeckSection) =
        DeckPage(id = newId(), name = name, sections = sections.toList())

    private fun section(title: String?, style: SectionStyle, vararg buttons: DeckButton) =
        DeckSection(id = newId(), title = title, style = style, buttons = buttons.toList())

    private fun button(
        label: String,
        icon: DeckIcon,
        color: TileColor,
        actionName: String,
        onAirScene: String? = null,
        stateInput: String? = null,
    ) = DeckButton(
        id = newId(),
        label = label,
        icon = icon,
        color = color,
        actionName = actionName,
        onAirScene = onAirScene,
        stateInput = stateInput,
    )
}
