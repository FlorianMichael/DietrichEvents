package de.florianmichael.dietrichevents;

import de.florianmichael.dietrichevents.handle.EventExecutor;
import de.florianmichael.dietrichevents.handle.Listener;

public abstract class AbstractEvent<L extends Listener> {

    public boolean isAbort() {
        return false;
    }

    public abstract EventExecutor<L> getEventExecutor();
    public abstract Class<L> getListenerType();
}
