package com.boydti.fawe.object;

public abstract class RunnableVal5<T, U, V, W, X> implements Runnable {
    public T value1;
    public U value2;
    public V value3;
    public W value4;
    public X value5;

    public RunnableVal5() {
    }

    public RunnableVal5(T value1, U value2, V value3, W value4, X value5) {
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
        this.value4 = value4;
        this.value5 = value5;
    }

    @Override
    public void run() {
        this.run(this.value1, this.value2, this.value3, this.value4, this.value5);
    }

    public abstract void run(T value1, U value2, V value3, W value4, X value5);
}
