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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;

public class DietrichEvents {
    private final static DietrichEvents GLOBAL = createThreadSafe();
    public static DietrichEvents global() {
        return GLOBAL;
    }

    private final Map<Class<?>, Map<Object, Subscription<?>>> subscriptions;
    private final Function<Class<?>, Map<Object, Subscription<?>>> mappingFunction;

    private Comparator<Subscription<?>> priorityOrder = Comparator.comparingInt(subscription -> {
        final int priority = subscription.getPrioritySupplier().getAsInt();
        if (priority == Integer.MIN_VALUE) return Integer.MAX_VALUE;
        if (priority == Integer.MAX_VALUE) return Integer.MIN_VALUE;
        return -priority;
    });

    public void setPriorityOrder(Comparator<Subscription<?>> priorityOrder) {
        this.priorityOrder = priorityOrder;
    }

    private Consumer<Throwable> errorHandler = Throwable::printStackTrace;

    public void setErrorHandler(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
    }

    private BiConsumer<List<Subscription<?>>, Comparator<Subscription<?>>> sortCallback = List::sort;

    public void setSortCallback(BiConsumer<List<Subscription<?>>, Comparator<Subscription<?>>> sortCallback) {
        this.sortCallback = sortCallback;
    }

    public DietrichEvents(final Map<Class<?>, Map<Object, Subscription<?>>> subscriptions, final Function<Class<?>, Map<Object, Subscription<?>>> mappingFunction) {
        this.subscriptions = subscriptions;
        this.mappingFunction = mappingFunction;
    }

    public static DietrichEvents createThreadSafe() {
        return create(new ConcurrentHashMap<>(), key -> new ConcurrentHashMap<>());
    }

    public static DietrichEvents createDefault() {
        return create(new HashMap<>(), key -> new HashMap<>());
    }

    public static DietrichEvents create(final Map<Class<?>, Map<Object, Subscription<?>>> subscriptions, final Function<Class<?>, Map<Object, Subscription<?>>> mappingFunction) {
        return new DietrichEvents(subscriptions, mappingFunction);
    }

    public<L extends Listener> void subscribeAll(final L listener) {
        for (Class<?> anInterface : listener.getClass().getInterfaces()) {
            if (Listener.class.isAssignableFrom(anInterface)) {
                this.subscribe((Class<L>) anInterface, listener);
            }
        }
    }

    public <L extends Listener> void unsubscribeAll(final L listener) {
        try {
            for (Map.Entry<Class<?>, Map<Object, Subscription<?>>> entry : this.subscriptions.entrySet()) {
                for (Subscription<?> subscription : entry.getValue().values()) {
                    if (subscription.getListenerType() == listener) {
                        this.unsubscribe((Class<L>) entry.getKey(), (L) subscription.getListenerType());
                    }
                }
            }
        } catch (Exception e) {
            this.errorHandler.accept(e);
        }
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
            if (this.subscriptions.get(listenerType).isEmpty()) {
                this.subscriptions.remove(listenerType);
            }
        } catch (Exception e) {
            this.errorHandler.accept(e);
        }
    }

    public <L extends Listener, E extends AbstractEvent<L>> E post(E event) {
        try {
            return postInternal(event);
        } catch (Throwable e) {
            this.errorHandler.accept(e);
            return event;
        }
    }

    @SuppressWarnings("unchecked")
    public <L extends Listener, E extends AbstractEvent<L>> E postInternal(E event) {
        if (event.isAbort()) return event;

        final Map<Object, Subscription<?>> subscriptions = this.subscriptions.get(event.getListenerType());
        if (subscriptions == null || subscriptions.isEmpty()) return event;

        final List<Subscription<?>> subscriptionList = new ArrayList<>(subscriptions.values());
        sortCallback.accept(subscriptionList, this.priorityOrder);

        for (Subscription<?> subscription : subscriptionList) {
            event.getEventExecutor().execute((L) subscription.getListenerType());

            if (event.isAbort()) return event;
        }
        return event;
    }
}
