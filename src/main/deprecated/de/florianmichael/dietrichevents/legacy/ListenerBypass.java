package de.florianmichael.dietrichevents.legacy;

import de.florianmichael.dietrichevents.handle.Listener;

public interface ListenerBypass<T extends SimpleEvent> extends Listener {

    void execute(final T event);
}
