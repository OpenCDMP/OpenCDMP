package org.opencdmp.commons.fake;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FakeRequestAttributes implements RequestAttributes {
    private final Map<String, Object> requestAttributeMap = new HashMap<>();
    private final Map<String, Runnable> requestDestructionCallbacks = new LinkedHashMap<>(8);
    private volatile boolean requestActive = true;

    @Override
    public Object getAttribute(@NotNull String name, int scope) {
        if (scope == RequestAttributes.SCOPE_REQUEST) {
            if (!isRequestActive()) {
                throw new IllegalStateException("Cannot ask for request attribute - request is not active anymore!");
            }
            return this.requestAttributeMap.get(name);
        } else {
            throw new IllegalStateException("Only " + RequestAttributes.SCOPE_REQUEST + " allowed for " + FakeRequestAttributes.class.getSimpleName());
        }
    }

    @Override
    public void setAttribute(@NotNull String name, @NotNull Object value, int scope) {
        if (scope == RequestAttributes.SCOPE_REQUEST) {
            if (!isRequestActive()) {
                throw new IllegalStateException("Cannot set request attribute - request is not active anymore!");
            }
            this.requestAttributeMap.put(name, value);
        } else {
            throw new IllegalStateException("Only " + RequestAttributes.SCOPE_REQUEST + " allowed for " + FakeRequestAttributes.class.getSimpleName());
        }
    }

    @Override
    public void removeAttribute(@NotNull String name, int scope) {
        if (scope == RequestAttributes.SCOPE_REQUEST) {
            if (isRequestActive()) {
                removeRequestDestructionCallback(name);
                this.requestAttributeMap.remove(name);
            }
        } else {
            throw new IllegalStateException("Only " + RequestAttributes.SCOPE_REQUEST + " allowed for " + FakeRequestAttributes.class.getSimpleName());
        }
    }

    @Override
    public String @NotNull [] getAttributeNames(int scope) {
        if (scope == RequestAttributes.SCOPE_REQUEST) {
            if (!isRequestActive()) {
                throw new IllegalStateException("Cannot ask for request attributes - request is not active anymore!");
            }
            return this.requestAttributeMap.keySet().toArray(new String[0]);
        } else {
            throw new IllegalStateException("Only " + RequestAttributes.SCOPE_REQUEST + " allowed for " + FakeRequestAttributes.class.getSimpleName());
        }
        //return new String[0];
    }

    @Override
    public void registerDestructionCallback(@NotNull String name, @NotNull Runnable callback, int scope) {
        if (scope == SCOPE_REQUEST) {
            registerRequestDestructionCallback(name, callback);
        } else {
            throw new IllegalStateException("Only " + RequestAttributes.SCOPE_REQUEST + " allowed for " + FakeRequestAttributes.class.getSimpleName());
        }
    }

    protected final void registerRequestDestructionCallback(String name, Runnable callback) {
        Assert.notNull(name, "Name must not be null");
        Assert.notNull(callback, "Callback must not be null");
        synchronized (this.requestDestructionCallbacks) {
            this.requestDestructionCallbacks.put(name, callback);
        }
    }

    @Override
    public Object resolveReference(@NotNull String key) {
        // Not supported
        return null;
    }

    @Override
    public @NotNull String getSessionId() {
        return "";
    }

    @Override
    public @NotNull Object getSessionMutex() {
        return new Object();
    }

    public void requestCompleted() {
        executeRequestDestructionCallbacks();
        for (String name : getAttributeNames(RequestAttributes.SCOPE_REQUEST)) {
            this.removeAttribute(name, RequestAttributes.SCOPE_REQUEST);
        }
        this.requestActive = false;
    }

    private boolean isRequestActive() {
        return this.requestActive;
    }


    private void removeRequestDestructionCallback(String name) {
        Assert.notNull(name, "Name must not be null");
        synchronized (this.requestDestructionCallbacks) {
            this.requestDestructionCallbacks.remove(name);
        }
    }

    private void executeRequestDestructionCallbacks() {
        synchronized (this.requestDestructionCallbacks) {
            for (Runnable runnable : this.requestDestructionCallbacks.values()) {
                runnable.run();
            }
            this.requestDestructionCallbacks.clear();
        }
    }
}
