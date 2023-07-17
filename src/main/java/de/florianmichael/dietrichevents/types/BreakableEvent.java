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
package de.florianmichael.dietrichevents.types;

import de.florianmichael.dietrichevents.AbstractEvent;
import de.florianmichael.dietrichevents.handle.Listener;

/**
 * This class represents a breakable event. It is used to call the event on the listener.
 *
 * @param <L> The listener type
 */
public abstract class BreakableEvent<L extends Listener> extends AbstractEvent<L> {

    private boolean abort;

    /**
     * Recommended method to cancel the event
     */
    public void stopHandling() {
        this.abort = true;
    }

    @Override
    public boolean isAbort() {
        return this.abort;
    }

    public void setAbort(boolean abort) {
        this.abort = abort;
    }
}
