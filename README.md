# DietrichEvents
Basic Java event system

## How to add this to your plugin
Just copy this part to your *build.gradle*:
```groovy
repositories {
    maven {
        name = "Jitpack"
        url = "https://jitpack.io"
    }
}

dependencies {
    implementation "com.github.FlorianMichael:DietrichEvents:1.0.0"
}
```

## Example usage
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

    public final static EventDispatcher EVENT_DISPATCHER = new EventDispatcher();
    
    public void registerListeners() {
        EVENT_DISPATCHER.subscribe(ExampleListener.class, this);
    }

    public void unregisterListeners() {
        EVENT_DISPATCHER.unsubscribe(ExampleListener.class, this);
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
ExampleListenerUsage.EVENT_DISPATCHER.post(new ExampleListener.ExampleEvent(EventStateType.PRE));
```

#### You should create only one instance of the EventDispatcher, which should be stored in your main class. But of course you can create more than one
