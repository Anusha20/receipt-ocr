package org.korosoft.javaocr.core.api;

/**
 * Simple Runnable with an argument
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public interface SimpleCallback<T> {
    /**
     * Callback method
     *
     * @param t an argument
     */
    void call(T t);
}
