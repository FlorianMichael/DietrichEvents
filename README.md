# DietrichEvents
A fast and feature rich but still easy to use Event library for Java

## Contact
If you encounter any issues, please report them on the
[issue tracker](https://github.com/FlorianMichael/DietrichEvents/issues).  
If you just want to talk or need help with DietrichEvents feel free to join my
[Discord](https://discord.gg/BwWhCHUKDf).

## How to add this to your project
### Gradle/Maven
To use DietrichEvents with Gradle/Maven you can use this [Maven server](https://maven.lenni0451.net/#/releases/de/florianmichael/DietrichEvents) or [Jitpack](https://jitpack.io/#FlorianMichael/DietrichEvents).  
You can also find instructions how to implement it into your build script there.

### Jar File
If you just want the latest jar file you can download it from the GitHub [Actions](https://github.com/FlorianMichael/DietrichEvents/actions) or use the [Release](https://github.com/FlorianMichael/DietrichEvents/releases).

## Example usage
### Create instance
You can use either **DietrichEvents.createThreadSafe()** or **DietrichEvents.createDefault()** to create an instance of the EventSystem, 
if you want to specify the mapping function yourself, there is also a normal **DietrichEvents.create()**. 
For a thread safe DietrichEvents there is already a global instance that you can call with **DietrichEvents.global()**.

### There are a few functions in DietrichEvents to implement parts of the event system yourself:

This function allows you to override the comparator used for sorting the priorities: <br>
**setPriorityOrder(Comparator<Subscription<?>> priorityOrder);**

This function allows you to override the error handling: <br>
**setErrorHandler(Consumer<Throwable> errorHandler);**

This function allows you to replace the sorting algorithm used for sorting the priorities: <br>
**setSortCallback(BiConsumer<List<Subscription<?>>, Comparator<Subscription<?>>> sortCallback);**

### Usage with FastUtil
To get an DietrichEvents that uses FastUtil, you can do this: <br>
**DietrichEvents.create(new ConcurrentHashMap<>(), Object2ObjectArrayMap::new);**

### Create an Event
```java
public interface ExampleListener extends Listener {
    
    void onPreExample();
    void onExample(final EventStateType eventStateType);
    
    class ExampleEvent extends AbstractEvent<ExampleListener> {
        private final EventExecutor<ExampleListener> eventExecutor;
        
        public ExampleEvent(final EventStateType eventStateType) {
            this.eventExecutor = listener -> {
                listener.onExample(eventStateType);
                if (eventStateType == EventStateType.PRE) {
                    listener.onPreExample();
                }
            };
        }
        
        @Override
        public EventExecutor<ExampleListener> getEventExecutor() {
            return this.eventExecutor;
        }

        @Override
        public Class<ExampleListener> getListenerType() {
            return ExampleListener.class;
        }
    }
}
```

### Register listener
```java
public class ExampleListenerUsage implements ExampleListener {

    // You can also use subscribeClass and subscribeClassInternal to subscribe all listeners from a specific class / object
    public void registerListeners() {
        DietrichEvents.global().subscribe(ExampleListener.class, this);
    }

    public void unregisterListeners() {
        DietrichEvents.global().unsubscribe(ExampleListener.class, this);
    }
    
    @Override
    public void onPreExample() {
    }

    @Override
    public void onExample(EventStateType eventStateType) {
    }
}
```

### Calling an Event
```java
// You can use either the post or the postInternal function, where postInternal has no error handling.
DietrichEvents.global().post(new ExampleListener.ExampleEvent(EventStateType.PRE));
```

## JMH Benchmark
The Benchmark shows the average time it takes to call an event 100.000 times.

| Benchmark                             | Mode | Cnt | Score      | Error      | Units |
|---------------------------------------|------|-----|------------|------------|-------|
| BenchmarkCaller.callBenchmarkListener | avgt | 4   | 716681.647 | 145474,182 | ns/op |

For a better comparison you can look [here](https://github.com/FlorianMichael/DietrichEvents2)