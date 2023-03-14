/*
 * This file is part of DietrichEvents - https://github.com/FlorianMichael/DietrichEvents
 * Copyright (C) 2023 FlorianMichael/MrLookAtMe (EnZaXD) and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.florianmichael.dietrichevents.handle;

import java.util.function.IntSupplier;

public class Subscription<L> {

    private final L listenerType;
    private final IntSupplier prioritySupplier;

    public Subscription(L listenerType, IntSupplier prioritySupplier) {
        this.listenerType = listenerType;
        this.prioritySupplier = prioritySupplier;
    }

    public Subscription(L listenerType, int priority) {
        this(listenerType, () -> priority);
    }

    public Subscription(L listenerType) {
        this(listenerType, 0);
    }

    public L getListenerType() {
        return listenerType;
    }

    public IntSupplier getPrioritySupplier() {
        return prioritySupplier;
    }
}