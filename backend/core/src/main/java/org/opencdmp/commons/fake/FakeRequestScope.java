package org.opencdmp.commons.fake;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.Closeable;

public class FakeRequestScope implements Closeable {
    private RequestAttributes initialRequestAttributes = null;
    private FakeRequestAttributes currentRequestAttributes = null;
    boolean isInUse = false;

    public FakeRequestScope() {
        this.reset();
    }

    public final void reset() {
        this.close();
        this.isInUse = true;

        this.initialRequestAttributes = RequestContextHolder.getRequestAttributes();
        this.currentRequestAttributes = new FakeRequestAttributes();
        RequestContextHolder.setRequestAttributes(this.currentRequestAttributes);
    }

    @Override
    public void close() {
        if (!this.isInUse)
            return;
        this.isInUse = false;

        if (initialRequestAttributes != null)
            RequestContextHolder.setRequestAttributes(initialRequestAttributes);
        else
            RequestContextHolder.resetRequestAttributes();

        if (currentRequestAttributes != null)
            currentRequestAttributes.requestCompleted();

        this.initialRequestAttributes = null;
        this.currentRequestAttributes = null;
    }

}
