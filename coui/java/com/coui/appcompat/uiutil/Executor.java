package com.coui.appcompat.uiutil;

public interface Executor<T extends Rule<? extends Domain>, U extends Domain> {
    Executor<T, U> execute(T rule, U domain);
}
