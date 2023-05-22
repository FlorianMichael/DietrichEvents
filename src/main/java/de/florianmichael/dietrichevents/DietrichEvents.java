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
import java.util.function.*;
import java.util.stream.Collectors;

public class DietrichEvents {
    private final static DietrichEvents GLOBAL = createThreadSafe();

    public static DietrichEvents global() {
        return GLOBAL;
    }

    private final Map<Class<?>, Map<Object, Subscription<?>>> subscriptions;
    private final Supplier<Map<Object, Subscription<?>>> mappingFunction;

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

    public DietrichEvents(final Map<Class<?>, Map<Object, Subscription<?>>> subscriptions, final Supplier<Map<Object, Subscription<?>>> mappingFunction) {
        this.subscriptions = subscriptions;
        this.mappingFunction = mappingFunction;
    }

    public static DietrichEvents createThreadSafe() {
        return create(new ConcurrentHashMap<>(), ConcurrentHashMap::new);
    }

    public static DietrichEvents createDefault() {
        return create(new ConcurrentHashMap<>(), HashMap::new);
    }

    public static DietrichEvents create(final Map<Class<?>, Map<Object, Subscription<?>>> subscriptions, final Supplier< Map<Object, Subscription<?>>> mappingFunction) {
        return new DietrichEvents(subscriptions, mappingFunction);
    }

    public <L extends Listener> void subscribe(Class<L> listenerType, L listener) {
        subscribeInternal(listenerType, new Subscription<>(listener));
    }

    public <L extends Listener> void subscribe(Class<L> listenerType, L listener, int priority) {
        subscribeInternal(listenerType, new Subscription<>(listener, priority));
    }

    public <L extends Listener> void subscribe(Class<L> listenerType, L listener, IntSupplier priority) {
        subscribeInternal(listenerType, new Subscription<>(listener, priority));
    }

    public <L extends Listener> void subscribeInternal(Class<L> listenerType, Subscription<L> subscription) {
        final Map<Object, Subscription<?>> subscriptionMap = this.subscriptions.computeIfAbsent(listenerType, value -> this.mappingFunction.get());
        subscriptionMap.put(subscription.getListenerType(), subscription);

        this.subscriptions.put(listenerType, subscriptionMap.entrySet().stream().sorted(Map.Entry.comparingByValue(this.priorityOrder)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, ConcurrentHashMap::new)));
    }

    public void subscribeClass(final Listener listener) {
        subscribeClassInternal(new Subscription<>(listener));
    }

    public void subscribeClass(final Listener listener, final int priority) {
        subscribeClassInternal(new Subscription<>(listener, priority));
    }

    public void subscribeClass(final Listener listener, final IntSupplier priority) {
        subscribeClassInternal(new Subscription<>(listener, priority));
    }

    public void subscribeClassUnsafe(final Object listener) {
        if (!Listener.class.isAssignableFrom(listener.getClass())) return;

        subscribeClassInternal(new Subscription<>((Listener) listener));
    }

    public void subscribeClassUnsafe(final Object listener, final int priority) {
        if (!Listener.class.isAssignableFrom(listener.getClass())) return;

        subscribeClassInternal(new Subscription<>((Listener) listener, priority));
    }

    public void subscribeClassUnsafe(final Object listener, final IntSupplier priority) {
        if (!Listener.class.isAssignableFrom(listener.getClass())) return;

        subscribeClassInternal(new Subscription<>((Listener) listener, priority));
    }

    @SuppressWarnings("unchecked")
    public <L extends Listener> void subscribeClassInternal(final Subscription<L> subscription) {
        for (Class<?> classInterface : subscription.getListenerType().getClass().getInterfaces()) {
            if (Listener.class.isAssignableFrom(classInterface)) {
                this.subscribeInternal((Class<L>) classInterface, subscription);
            }
        }
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

    public void unsubscribeClass(final Object listener) {
        if (!Listener.class.isAssignableFrom(listener.getClass())) return;

        unsubscribeClass(listener);
    }

    @SuppressWarnings("unchecked")
    public <L extends Listener> void unsubscribeClass(final L listener) {
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

    public <L extends Listener, E extends AbstractEvent<L>> E post(final E event) {
        return this.post(event, false);
    }

    public <L extends Listener, E extends AbstractEvent<L>> E post(final E event, final boolean forceSortPriorities) {
        try {
            return postInternal(event, forceSortPriorities);
        } catch (Throwable e) {
            this.errorHandler.accept(e);
            return event;
        }
    }

    public <L extends Listener, E extends AbstractEvent<L>> E postInternal(final E event) {
        return this.postInternal(event, false);
    }

    @SuppressWarnings("unchecked")
    public <L extends Listener, E extends AbstractEvent<L>> E postInternal(final E event, final boolean forceSortPriorities) {
        if (event.isAbort()) return event;

        final Map<Object, Subscription<?>> subscriptions = this.subscriptions.get(event.getListenerType());
        if (subscriptions == null || subscriptions.isEmpty()) return event;

        final List<Subscription<?>> subscriptionList = new ArrayList<>(subscriptions.values());
        if (forceSortPriorities) {
            sortCallback.accept(subscriptionList, this.priorityOrder);
        }

        for (Subscription<?> subscription : subscriptionList) {
            event.getEventExecutor().execute((L) subscription.getListenerType());

            if (event.isAbort()) return event;
        }
        return event;
    }
}
