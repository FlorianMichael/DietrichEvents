/*
 * This file is part of DietrichEvents - https://github.com/FlorianMichael/DietrichEvents
 * Copyright (C) 2023 FlorianMichael/EnZaXD and contributors
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
package de.florianmichael.dietrichevents;

import de.florianmichael.dietrichevents.handle.Listener;
import de.florianmichael.dietrichevents.handle.Subscription;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.IntSupplier;

public class EventDispatcher {
    private final static EventDispatcher GLOBAL = createThreadSafe();

    private final Map<Class<?>, Map<Object, Subscription<?>>> subscriptions = new HashMap<>();

    private final Function<Class<?>, Map<Object, Subscription<?>>> mappingFunction;
    private final Comparator<Subscription<?>> priorityOrder;

    protected EventDispatcher(Function<Class<?>, Map<Object, Subscription<?>>> mappingFunction, Comparator<Subscription<?>> priorityOrder) {
        this.mappingFunction = mappingFunction;
        this.priorityOrder = priorityOrder;
    }

    public static EventDispatcher g() { // "g" for global -> faster access to class
        return GLOBAL;
    }

    public static EventDispatcher createThreadSafe() {
        return create(key -> new ConcurrentHashMap<>());
    }

    public static EventDispatcher createDefault() {
        return create(key -> new HashMap<>());
    }

    public static EventDispatcher create(final Function<Class<?>, Map<Object, Subscription<?>>> mappingFunction) {
        return create(mappingFunction, Comparator.comparingInt(subscription -> conjugatePriority(subscription.getPrioritySupplier().getAsInt())));
    }

    public static EventDispatcher create(final Function<Class<?>, Map<Object, Subscription<?>>> mappingFunction, final Comparator<Subscription<?>> priorityOrder) {
        return new EventDispatcher(mappingFunction, priorityOrder);
    }

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
        this.subscriptions.computeIfAbsent(listenerType, this.mappingFunction).put(subscription.getListenerType(), subscription);
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
            subscriptionList.sort(this.priorityOrder);

            for (Subscription<?> subscription : subscriptionList) {
                event.getEventExecutor().execute((L) subscription.getListenerType());

                if (event.isAbort()) return event;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }

    public static int conjugatePriority(int value) {
        if (value == Integer.MIN_VALUE) return Integer.MAX_VALUE;
        if (value == Integer.MAX_VALUE) return Integer.MIN_VALUE;

        return -value;
    }
}
