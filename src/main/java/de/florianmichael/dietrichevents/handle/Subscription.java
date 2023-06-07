/*
 * This file is part of DietrichEvents - https://github.com/FlorianMichael/DietrichEvents
 * Copyright (C) 2023 FlorianMichael/EnZaXD and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
