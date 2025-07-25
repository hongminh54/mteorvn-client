/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorvnclient.systems.accounts;

import meteordevelopment.meteorvnclient.systems.System;
import meteordevelopment.meteorvnclient.systems.Systems;
import meteordevelopment.meteorvnclient.systems.accounts.types.CrackedAccount;
import meteordevelopment.meteorvnclient.systems.accounts.types.MicrosoftAccount;
import meteordevelopment.meteorvnclient.systems.accounts.types.TheAlteningAccount;
import meteordevelopment.meteorvnclient.utils.misc.NbtException;
import meteordevelopment.meteorvnclient.utils.misc.NbtUtils;
import meteordevelopment.meteorvnclient.utils.network.MeteorExecutor;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Accounts extends System<Accounts> implements Iterable<Account<?>> {
    private List<Account<?>> accounts = new ArrayList<>();

    public Accounts() {
        super("accounts");
    }

    public static Accounts get() {
        return Systems.get(Accounts.class);
    }

    public void add(Account<?> account) {
        accounts.add(account);
        save();
    }

    public boolean exists(Account<?> account) {
        return accounts.contains(account);
    }

    public void remove(Account<?> account) {
        if (accounts.remove(account)) {
            save();
        }
    }

    public int size() {
        return accounts.size();
    }

    @Override
    public @NotNull Iterator<Account<?>> iterator() {
        return accounts.iterator();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.put("accounts", NbtUtils.listToTag(accounts));

        return tag;
    }

    @Override
    public Accounts fromTag(NbtCompound tag) {
        MeteorExecutor.execute(() -> accounts = NbtUtils.listFromTag(tag.getList("accounts", 10), tag1 -> {
            NbtCompound t = (NbtCompound) tag1;
            if (!t.contains("type")) return null;

            AccountType type = AccountType.valueOf(t.getString("type"));

            try {
                return switch (type) {
                    case Cracked ->     new CrackedAccount(null).fromTag(t);
                    case Microsoft ->   new MicrosoftAccount(null).fromTag(t);
                    case TheAltening -> new TheAlteningAccount(null).fromTag(t);
                };
            } catch (NbtException e) {
                return null;
            }
        }));

        return this;
    }
}
