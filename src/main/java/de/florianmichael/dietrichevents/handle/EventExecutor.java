package de.florianmichael.dietrichevents.handle;

public interface EventExecutor<L> {

    void execute(L listener);
}
