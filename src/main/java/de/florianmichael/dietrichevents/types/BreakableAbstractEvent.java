package de.florianmichael.dietrichevents.types;

import de.florianmichael.dietrichevents.AbstractEvent;
import de.florianmichael.dietrichevents.handle.Listener;

public abstract class BreakableAbstractEvent<L extends Listener> extends AbstractEvent<L> {

    private boolean abort;

    public void stopHandling() {
        this.abort = true;
    }

    @Override
    public boolean isAbort() {
        return this.abort;
    }
}
