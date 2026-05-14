package com.elmakers.mine.bukkit.plugins.liminal.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeightedList<T extends WeightedElement> {
    protected final List<T> elements = new ArrayList<>();
    protected double totalWeight = 0;

    public void add(T element) {
        elements.add(element);
        totalWeight += element.getWeight();
    }

    public T get(final Random random) {
        double weight = Math.random() * totalWeight;
        for (T element : elements) {
            weight -= element.getWeight();
            if (weight <= 0) {
                return element;
            }
        }
        // Should never happen
        return elements.get(elements.size() - 1);
    }

    public boolean isEmpty() {
        return totalWeight == 0;
    }
}
