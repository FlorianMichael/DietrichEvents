# DietrichEvents
A fast and feature rich but still easy to use Event library for Java

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
    implementation "com.github.FlorianMichael:DietrichEvents:1.1.0"
}
```

## Example usage
### Create instance
You can use either **EventDispatcher.createThreadSafe()** or **EventDispatcher.createDefault()** to create an instance of the EventSystem, <br>
if you want to specify the priority order and the mapping function yourself, there is also a normal **EventDispatcher.create()**. <br>
For a thread safe EventDispatcher there is already a global instance that you can call with **EventDispatcher.g()**. <br>

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
        EventDispatcher.g().subscribe(ExampleListener.class, this);
    }

    public void unregisterListeners() {
        EventDispatcher.g().unsubscribe(ExampleListener.class, this);
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
EventDispatcher.g().post(new ExampleListener.ExampleEvent(EventStateType.PRE));
```
