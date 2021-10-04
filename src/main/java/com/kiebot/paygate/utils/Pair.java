package com.kiebot.paygate.utils;

public class Pair<F, S> {

    public F first;
    public S second;

    private Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <F, S> Pair<F, S> pair(F first, S second) {
        return new Pair<>(first, second);
    }

    @Override
    public String toString() {
        return "('" + first + "':'" + second + "')";
    }
}
