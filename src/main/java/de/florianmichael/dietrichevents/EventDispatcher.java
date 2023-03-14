package de.florianmichael.dietrichevents;

import de.florianmichael.dietrichevents.handle.Listener;
import de.florianmichael.dietrichevents.handle.Subscription;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.IntSupplier;

public class EventDispatcher {

    private static final Function<Class<?>, Map<Object, Subscription<?>>> MAPPING_FUNCTION = key -> new ConcurrentHashMap<>();
    private static final Comparator<Subscription<?>> PRIORITY_ORDER = Comparator.comparingInt(subscription -> conjugatePriority(subscription.getPrioritySupplier().getAsInt()));

    private final Map<Class<?>, Map<Object, Subscription<?>>> subscriptions = new HashMap<>();

    public <L extends Listener> void subscribe(Class<L> listenerType, L listener) {
        subscribe(listenerType, new Subscription<L>(listener));
    }

    public <L extends Listener> void subscribe(Class<L> listenerType, L listener, int priority) {
        subscribe(listenerType, new Subscription<L>(listener, priority));
    }

    public <L extends Listener> void subscribe(Class<L> listenerType, L listener, IntSupplier priority) {
        subscribe(listenerType, new Subscription<L>(listener, priority));
    }

    public <L extends Listener> void subscribe(Class<L> listenerType, Subscription<L> subscription) {
        this.subscriptions.computeIfAbsent(listenerType, MAPPING_FUNCTION).put(subscription.getListenerType(), subscription);
    }

    public <L extends Listener> void unsubscribe(Class<L> listenerType, L listener) {
        try {
            this.subscriptions.get(listenerType).remove(listener);
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("unchecked")
    public <L extends Listener, E extends AbstractEvent<L>> E post(E event) {
        try {
            if (event.isAbort()) return event;

            final Map<Object, Subscription<?>> subscriptions = this.subscriptions.get(event.getListenerType());
            if (subscriptions == null || subscriptions.isEmpty()) return event;

            final List<Subscription<?>> subscriptionList = new ArrayList<>(subscriptions.values());
            subscriptionList.sort(PRIORITY_ORDER);

            for (Subscription<?> subscription : subscriptionList) {
                event.getEventExecutor().execute((L) subscription.getListenerType());

                if (event.isAbort()) return event;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }

    private static int conjugatePriority(int value) {
        if (value == Integer.MIN_VALUE) return Integer.MAX_VALUE;
        if (value == Integer.MAX_VALUE) return Integer.MIN_VALUE;

        return -value;
    }
}
