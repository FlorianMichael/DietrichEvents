package de.florianmichael.dietrichevents.legacy;

import de.florianmichael.dietrichevents.AbstractEvent;
import de.florianmichael.dietrichevents.EventDispatcher;
import de.florianmichael.dietrichevents.handle.EventExecutor;

public class SimpleEvent extends AbstractEvent<ListenerBypass> {
    private final EventExecutor<ListenerBypass> eventExecutor = listener -> listener.execute(this);

    public static <T extends SimpleEvent> void subscribe(Class<T> eventType, final ListenerBypass<T> listener) {
        EventDispatcher.g().subscribe(ListenerBypass.class, event -> {
            if (event.getClass().isAssignableFrom(eventType)) {
                listener.execute((T) event);
            }
        });
    }

    @Override
    public EventExecutor<ListenerBypass> getEventExecutor() {
        return eventExecutor;
    }

    @Override
    public Class<ListenerBypass> getListenerType() {
        return ListenerBypass.class;
    }
}
