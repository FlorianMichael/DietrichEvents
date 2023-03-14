package de.florianmichael.dietrichevents.handle;

import java.util.function.IntSupplier;

public class Subscription<L> {

    private final L listenerType;
    private final IntSupplier prioritySupplier;

    public Subscription(L listenerType, IntSupplier prioritySupplier) {
        this.listenerType = listenerType;
        this.prioritySupplier = prioritySupplier;
    }

    public Subscription(L listenerType, int priority) {
        this(listenerType, () -> priority);
    }

    public Subscription(L listenerType) {
        this(listenerType, 0);
    }

    public L getListenerType() {
        return listenerType;
    }

    public IntSupplier getPrioritySupplier() {
        return prioritySupplier;
    }
}
