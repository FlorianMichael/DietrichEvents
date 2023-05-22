# DietrichEvents
A fast and feature rich but still easy to use Event library for Java

## Contact
If you encounter any issues, please report them on the
[issue tracker](https://github.com/FlorianMichael/DietrichEvents/issues).  
If you just want to talk or need help with DietrichEvents feel free to join my
[Discord](https://discord.gg/BwWhCHUKDf).

## How to add this to your project
Just copy this part to your *build.gradle*:
```groovy
repositories {
    maven {
        name = "Jitpack"
        url = "https://jitpack.io"
    }
}

dependencies {
    implementation "com.github.FlorianMichael:DietrichEvents:1.3.2"
}
```

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

### v1.2.0 API
```java
public class ExampleListenerUsage implements ExampleListener {
    public void init() {
        DietrichEvents.global().subscribeClass(this);
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
