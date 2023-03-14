package de.florianmichael.dietrichevents.types;

import de.florianmichael.dietrichevents.AbstractEvent;
import de.florianmichael.dietrichevents.handle.Listener;

public abstract class CancellableEvent<L extends Listener> extends AbstractEvent<L> {

    private boolean cancelled;

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

}