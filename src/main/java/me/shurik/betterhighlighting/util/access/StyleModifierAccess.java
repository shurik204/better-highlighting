package me.shurik.betterhighlighting.util.access;

import net.minecraft.network.chat.Style;

public interface StyleModifierAccess {
    Style highlight$updateModifiers(boolean bold, boolean italic, boolean underlined, boolean strikethrough);
}