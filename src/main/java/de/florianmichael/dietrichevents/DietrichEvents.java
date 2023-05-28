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

import de.florianmichael.dietrichevents.handle.Caller;
import de.florianmichael.dietrichevents.handle.Listener;
import de.florianmichael.dietrichevents.handle.Subscription;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.*;

public class DietrichEvents {
    private final static DietrichEvents GLOBAL = createThreadSafe();

    public static DietrichEvents global() {
        return GLOBAL;
    }

    private final Map<Class<?>, List<Caller>> subscriptions;
    private final Supplier<List<Caller>> mappingFunction;

    private Comparator<Caller> priorityOrder = Comparator.comparingInt(caller -> {
        final int priority = caller.getSubscription().getPrioritySupplier().getAsInt();
        if (priority == Integer.MIN_VALUE) return Integer.MAX_VALUE;
        if (priority == Integer.MAX_VALUE) return Integer.MIN_VALUE;
        return -priority;
    });

    public void setPriorityOrder(Comparator<Caller> priorityOrder) {
        this.priorityOrder = priorityOrder;
    }

    private Consumer<Throwable> errorHandler = Throwable::printStackTrace;

    public void setErrorHandler(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
    }

    private BiConsumer<List<Caller>, Comparator<Caller>> sortCallback = List::sort;

    public void setSortCallback(BiConsumer<List<Caller>, Comparator<Caller>> sortCallback) {
        this.sortCallback = sortCallback;
    }

    public DietrichEvents(final Map<Class<?>, List<Caller>> subscriptions, final Supplier<List<Caller>> mappingFunction) {
        this.subscriptions = subscriptions;
        this.mappingFunction = mappingFunction;
    }

    public static DietrichEvents createThreadSafe() {
        return create(new ConcurrentHashMap<>(), CopyOnWriteArrayList::new);
    }

    public static DietrichEvents createDefault() {
        return create(new ConcurrentHashMap<>(), ArrayList::new);
    }

    public static DietrichEvents create(final Map<Class<?>, List<Caller>> subscriptions, final Supplier<List<Caller>> mappingFunction) {
        return new DietrichEvents(subscriptions, mappingFunction);
    }

    public <L extends Listener> L subscribe(Class<L> listenerType, L listener) {
        return subscribeInternal(listenerType, new Subscription<>(listener));
    }

    public <L extends Listener> L subscribe(Class<L> listenerType, L listener, int priority) {
        return subscribeInternal(listenerType, new Subscription<>(listener, priority));
    }

    public <L extends Listener> L subscribe(Class<L> listenerType, L listener, IntSupplier priority) {
        return subscribeInternal(listenerType, new Subscription<>(listener, priority));
    }

    public <L extends Listener> L subscribeInternal(Class<L> listenerType, Subscription<L> subscription) {
        this.subscriptions.computeIfAbsent(listenerType, c -> this.mappingFunction.get()).add(new Caller(subscription.getListenerType(), subscription));
        final List<Caller> sortedCallers = this.subscriptions.get(listenerType);
        this.sortCallback.accept(sortedCallers, this.priorityOrder);
        this.subscriptions.put(listenerType, sortedCallers);

        return subscription.getListenerType();
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

    @SuppressWarnings("unchecked")
    public <L extends Listener> void unsubscribeClass(final L listener) {
        try {
            for (Map.Entry<Class<?>, List<Caller>> entry : this.subscriptions.entrySet()) {
                for (Caller caller : entry.getValue()) {
                    final Subscription<?> subscription = caller.getSubscription();

                    if (subscription.getListenerType() == listener) {
                        this.unsubscribeInternal((Class<L>) entry.getKey(), (L) subscription.getListenerType());
                    }
                }
            }
        } catch (Exception e) {
            this.errorHandler.accept(e);
        }
    }


    public void unsubscribeClassUnsafe(final Object listener) {
        if (!Listener.class.isAssignableFrom(listener.getClass())) return;

        unsubscribeClass((Listener) listener);
    }

    public <L extends Listener> void unsubscribeListenerType(final Class<L> listenerType) {
        this.subscriptions.remove(listenerType);
    }

    public <L extends Listener> void unsubscribeInternal(Class<L> listenerType, L listener) {
        try {
            this.subscriptions.get(listenerType).remove(listener);
            if (this.subscriptions.get(listenerType).isEmpty()) {
                this.subscriptions.remove(listenerType);
            }
        } catch (Exception e) {
            this.errorHandler.accept(e);
        }
    }

    public <L extends Listener> boolean hasSubscribers(final Class<L> listenerType) {
        return this.subscriptions.containsKey(listenerType);
    }

    public <L extends Listener> boolean hasListeners(final Class<L> listenerType, final L listener) {
        if (!hasSubscribers(listenerType)) return false;

        return this.subscriptions.get(listenerType).stream().anyMatch(caller -> caller.getListener() == listener);
    }

    public <L extends Listener, E extends AbstractEvent<L>> E postPush(final E event) {
        final List<Caller> sortedCallers = this.subscriptions.get(event.getListenerType());
        this.sortCallback.accept(sortedCallers, this.priorityOrder);
        this.subscriptions.put(event.getListenerType(), sortedCallers);

        return post(event);
    }

    public <L extends Listener, E extends AbstractEvent<L>> E post(final E event) {
        try {
            return postInternal(event);
        } catch (Throwable e) {
            this.errorHandler.accept(e);
            return event;
        }
    }

    public <L extends Listener, E extends AbstractEvent<L>> E postInternalPush(final E event) {
        final List<Caller> sortedCallers = this.subscriptions.get(event.getListenerType());
        this.sortCallback.accept(sortedCallers, this.priorityOrder);
        this.subscriptions.put(event.getListenerType(), sortedCallers);

        return postInternal(event);
    }

    @SuppressWarnings("unchecked")
    public <L extends Listener, E extends AbstractEvent<L>> E postInternal(final E event) {
        for (Caller caller : this.subscriptions.get(event.getListenerType())) {
            event.getEventExecutor().execute((L) caller.getSubscription().getListenerType());

            if (event.isAbort()) return event;
        }
        return event;
    }
}
