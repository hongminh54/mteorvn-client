/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.mixininterface;

import net.minecraft.text.Text;

public interface IChatHud {
    void meteor$add(Text message, int id);
}
