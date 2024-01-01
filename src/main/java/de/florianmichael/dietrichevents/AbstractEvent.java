/*
 * This file is part of DietrichEvents - https://github.com/FlorianMichael/DietrichEvents
 * Copyright (C) 2023-2024 FlorianMichael/EnZaXD <florian.michael07@gmail.com> and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.florianmichael.dietrichevents;

import de.florianmichael.dietrichevents.handle.Listener;

/**
 * This class represents a basic event. It is used to call the event on the listener.
 *
 * @param <L> The listener type
 */
public abstract class AbstractEvent<L extends Listener> {

    /**
     * @return true if the event should be aborted
     */
    public boolean isAbort() {
        return false;
    }

    /**
     * @param listener The listener to call the event on, should be implemented by the user
     */
    public abstract void call(L listener);

    /**
     * @return The listener type
     */
    public abstract Class<L> getListenerType();
}
