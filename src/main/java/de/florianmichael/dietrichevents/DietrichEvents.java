/*
 * This file is part of DietrichEvents - https://github.com/FlorianMichael/DietrichEvents
 * Copyright (C) 2023 FlorianMichael/EnZaXD and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.florianmichael.dietrichevents;

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

    private final Map<Class<?>, List<Subscription<?>>> subscriptions;
    private final Supplier<List<Subscription<?>>> mappingFunction;

    /**
     * The default priority order comparator, that is used to sort the {@link de.florianmichael.dietrichevents.handle.Subscription} list.
     * Higher priority means that the {@link de.florianmichael.dietrichevents.handle.Subscription} is called earlier.
     */
    private Comparator<Subscription<?>> priorityOrder = Comparator.comparingInt(subscription -> {
        final int priority = subscription.getPrioritySupplier().getAsInt();
        if (priority == Integer.MIN_VALUE) return Integer.MAX_VALUE;
        if (priority == Integer.MAX_VALUE) return Integer.MIN_VALUE;
        return -priority;
    });

    /**
     * This priorityOrder is default used by the {@link de.florianmichael.dietrichevents.DietrichEvents#sortCallback} instance.
     *
     * @param priorityOrder A {@link java.util.Comparator} that is used to sort the {@link de.florianmichael.dietrichevents.handle.Subscription} list.
     */
    public void setPriorityOrder(Comparator<Subscription<?>> priorityOrder) {
        this.priorityOrder = priorityOrder;
    }

    private Consumer<Throwable> errorHandler = Throwable::printStackTrace;

    /**
     * API method to overwrite the default {@link de.florianmichael.dietrichevents.DietrichEvents#errorHandler} instance.
     *
     * @param errorHandler A callback that is called when an exception is thrown during the event call.
     */
    public void setErrorHandler(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
    }

    private BiConsumer<List<Subscription<?>>, Comparator<Subscription<?>>> sortCallback = List::sort;

    /**
     * API method to overwrite the default {@link de.florianmichael.dietrichevents.DietrichEvents#sortCallback} instance.
     *
     * @param sortCallback A callback that is called when the list of {@link de.florianmichael.dietrichevents.handle.Subscription} is sorted.
     */
    public void setSortCallback(BiConsumer<List<Subscription<?>>, Comparator<Subscription<?>>> sortCallback) {
        this.sortCallback = sortCallback;
    }

    public DietrichEvents(final Map<Class<?>, List<Subscription<?>>> subscriptions, final Supplier<List<Subscription<?>>> mappingFunction) {
        this.subscriptions = subscriptions;
        this.mappingFunction = mappingFunction;
    }

    /**
     * @return A thread-safe instance of {@link de.florianmichael.dietrichevents.DietrichEvents}.
     */
    public static DietrichEvents createThreadSafe() {
        return create(new ConcurrentHashMap<>(), CopyOnWriteArrayList::new);
    }

    /**
     * @return The default instance, which is not thread safe.
     */
    public static DietrichEvents createDefault() {
        return create(new ConcurrentHashMap<>(), ArrayList::new);
    }

    /**
     * Creates a new {@link de.florianmichael.dietrichevents.DietrichEvents} instance.
     *
     * @param subscriptions The subscriptions
     * @param mappingFunction The mapping function
     * @return The new instance
     */
    public static DietrichEvents create(final Map<Class<?>, List<Subscription<?>>> subscriptions, final Supplier<List<Subscription<?>>> mappingFunction) {
        return new DietrichEvents(subscriptions, mappingFunction);
    }

    /**
     * Calls the given event.
     *
     * @param listenerType The listener type
     * @param listener The listener
     * @return The listener
     */
    public <L extends Listener> L subscribe(Class<L> listenerType, L listener) {
        return subscribeInternal(listenerType, new Subscription<>(listener));
    }

    /**
     * Subscribes the given listener to the given listener type and calls the {@link de.florianmichael.dietrichevents.DietrichEvents#sortCallback}.
     *
     * @param listenerType The listener type
     * @param listener The listener
     * @param priority The priority
     * @return The listener
     */
    public <L extends Listener> L subscribe(Class<L> listenerType, L listener, int priority) {
        return subscribeInternal(listenerType, new Subscription<>(listener, priority));
    }

    /**
     * Subscribes the given listener to the given listener type and calls the {@link de.florianmichael.dietrichevents.DietrichEvents#sortCallback}.
     *
     * @param listenerType The listener type
     * @param listener The listener
     * @param priority The priority
     * @return The listener
     */
    public <L extends Listener> L subscribe(Class<L> listenerType, L listener, IntSupplier priority) {
        return subscribeInternal(listenerType, new Subscription<>(listener, priority));
    }

    /**
     * Subscribes the given listener to the given listener type and calls the {@link de.florianmichael.dietrichevents.DietrichEvents#sortCallback}.
     *
     * @param listenerType The listener type
     * @param subscription The subscription
     * @return The listener
     */
    public <L extends Listener> L subscribeInternal(Class<L> listenerType, Subscription<L> subscription) {
        this.subscriptions.computeIfAbsent(listenerType, c -> this.mappingFunction.get()).add(subscription);
        final List<Subscription<?>> sortedCallers = this.subscriptions.get(listenerType);
        this.sortCallback.accept(sortedCallers, this.priorityOrder);
        this.subscriptions.put(listenerType, sortedCallers);

        return subscription.getListenerType();
    }

    /**
     * Subscribes all events from the given listener class.
     *
     * @param listener The listener to subscribe
     */
    public void subscribeClass(final Listener listener) {
        subscribeClassInternal(new Subscription<>(listener));
    }

    /**
     * Subscribes all events from the given listener class.
     *
     * @param listener The listener to subscribe
     * @param priority The priority of the listener
     */
    public void subscribeClass(final Listener listener, final int priority) {
        subscribeClassInternal(new Subscription<>(listener, priority));
    }

    /**
     * Subscribes all events from the given listener class. <br>
     * Note: The IntSupplier will only be called every post() if you are using the postPush() methods, otherwise
     * it will be called every time a new listener is subscribed.
     *
     * @param listener The listener to subscribe
     * @param priority The priority of the listener
     */
    public void subscribeClass(final Listener listener, final IntSupplier priority) {
        subscribeClassInternal(new Subscription<>(listener, priority));
    }

    /**
     * Subscribes all listeners of the given type and automatically checks if the given listener object is
     * implementing any listener, if that's not the case, the method will abort without any Exception.
     *
     * @param listener The listener to subscribe
     */
    public void subscribeClassUnsafe(final Object listener) {
        if (!Listener.class.isAssignableFrom(listener.getClass())) return;

        subscribeClassInternal(new Subscription<>((Listener) listener));
    }

    /**
     * Subscribes all events from the given listener class.
     * @param listener The listener to subscribe
     * @param priority The priority of the listener
     */
    public void subscribeClassUnsafe(final Object listener, final int priority) {
        if (!Listener.class.isAssignableFrom(listener.getClass())) return;

        subscribeClassInternal(new Subscription<>((Listener) listener, priority));
    }

    /**
     * Subscribes all events from the given listener class. <br>
     * Note: The IntSupplier will only be called every post() if you are using the postPush() methods, otherwise
     * it will be called every time a new listener is subscribed.
     *
     * @param listener The listener to subscribe
     * @param priority The priority of the listener
     */
    public void subscribeClassUnsafe(final Object listener, final IntSupplier priority) {
        if (!Listener.class.isAssignableFrom(listener.getClass())) return;

        subscribeClassInternal(new Subscription<>((Listener) listener, priority));
    }

    /**
     * Subscribes all events from the given Subscription field, this method is not intended to be used by the user.
     *
     * @param subscription The subscription to subscribe
     */
    @SuppressWarnings("unchecked")
    public <L extends Listener> void subscribeClassInternal(final Subscription<L> subscription) {
        for (Class<?> classInterface : subscription.getListenerType().getClass().getInterfaces()) {
            if (Listener.class.isAssignableFrom(classInterface)) {
                this.subscribeInternal((Class<L>) classInterface, subscription);
            }
        }
    }

    /**
     * Unsubscribes all events from the given listener class.
     *
     * @param listener The listener to unsubscribe
     */
    @SuppressWarnings("unchecked")
    public <L extends Listener> void unsubscribeClass(final L listener) {
        try {
            for (Map.Entry<Class<?>, List<Subscription<?>>> entry : this.subscriptions.entrySet()) {
                for (Subscription<?> subscription : entry.getValue()) {
                    if (subscription.getListenerType() == listener) {
                        this.unsubscribe((Class<L>) entry.getKey(), (L) subscription.getListenerType());
                    }
                }
            }
        } catch (Exception e) {
            this.errorHandler.accept(e);
        }
    }

    /**
     * Unsubscribes all listeners of the given type and automatically checks if the given listener object is
     * implementing any listener, if that's not the case, the method will abort without any Exception.
     *
     * @param listener The listener to unsubscribe
     */
    public void unsubscribeClassUnsafe(final Object listener) {
        if (!Listener.class.isAssignableFrom(listener.getClass())) return;

        unsubscribeClass((Listener) listener);
    }

    /**
     * Unsubscribes all listeners of the given type
     *
     * @param listenerType The type of listener to unsubscribe
     */
    public <L extends Listener> void unsubscribeListenerType(final Class<L> listenerType) {
        this.subscriptions.remove(listenerType);
    }

    /**
     * Unsubscribed the given listener type from the listener
     *
     * @param listenerType The type of listener to unsubscribe
     * @param listener The listener to unsubscribe
     */
    public <L extends Listener> void unsubscribe(Class<L> listenerType, L listener) {
        try {
            this.subscriptions.get(listenerType).removeIf(subscription -> subscription.getListenerType() == listener);

            if (this.subscriptions.get(listenerType).isEmpty()) {
                this.subscriptions.remove(listenerType);
            }
        } catch (Exception e) {
            this.errorHandler.accept(e);
        }
    }

    /**
     * @param listenerType The type of listener to check
     * @return Whether the event has subscribers
     */
    public <L extends Listener> boolean hasSubscribers(final Class<L> listenerType) {
        return this.subscriptions.containsKey(listenerType);
    }

    /**
     * @param listenerType The type of listener to check
     * @param listener The listener to check
     * @return Whether the listener is subscribed to the event
     */
    public <L extends Listener> boolean hasListeners(final Class<L> listenerType, final L listener) {
        if (!hasSubscribers(listenerType)) return false;

        return this.subscriptions.get(listenerType).stream().anyMatch(subscription -> subscription.getListenerType() == listener);
    }

    /**
     * Calls all listeners of a given event, has error handling and reorders the priorities each time it is called.
     * @param event The event to post
     * @return The event
     */
    public <L extends Listener, E extends AbstractEvent<L>> E postPush(final E event) {
        final List<Subscription<?>> sortedCallers = this.subscriptions.get(event.getListenerType());
        this.sortCallback.accept(sortedCallers, this.priorityOrder);
        this.subscriptions.put(event.getListenerType(), sortedCallers);

        return post(event);
    }

    /**
     * Calls all listeners of a given event, has error handling and does not reorder the priorities each time it is called.
     * @param event The event to post
     * @return The event
     */
    public <L extends Listener, E extends AbstractEvent<L>> E post(final E event) {
        try {
            return postInternal(event);
        } catch (Throwable e) {
            this.errorHandler.accept(e);
            return event;
        }
    }

    /**
     * Calls all listeners of a given event, has no error handling and reorders the priorities each time it is called,
     * this method should be used if the priority is an IntSupplier and is Dynamic.
     *
     * @param event The event to post
     * @return The event
     */
    public <L extends Listener, E extends AbstractEvent<L>> E postInternalPush(final E event) {
        final List<Subscription<?>> sortedCallers = this.subscriptions.get(event.getListenerType());
        this.sortCallback.accept(sortedCallers, this.priorityOrder);
        this.subscriptions.put(event.getListenerType(), sortedCallers);

        return postInternal(event);
    }

    /**
     * Calls all listeners of a given event, but has no error handling and does not reorder the priorities.
     * This method is the fastest of all post methods and also the recommended one if the priorities are not dynamic.
     * As soon as an event is aborted, the call is cancelled and the event is returned.
     *
     * @param event The event to post
     * @return The event
     */
    @SuppressWarnings("unchecked")
    public <L extends Listener, E extends AbstractEvent<L>> E postInternal(final E event) {
        final List<Subscription<?>> subscriptionList = this.subscriptions.get(event.getListenerType());
        if (event.isAbort() || subscriptionList == null) return event;

        for (Subscription<?> subscription : subscriptionList) {
            event.call((L) subscription.getListenerType());
        }
        return event;
    }
}
