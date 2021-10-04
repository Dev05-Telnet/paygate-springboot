package com.kiebot.paygate.utils;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {

    public static <S, D> D convert(S source, Class<D> toClass) {
        if (source == null || toClass == null) return null;
        if (toClass.isAssignableFrom(source.getClass())) {
            //noinspection unchecked
            return (D) source;
        }
        try {
            Constructor<D> constructor = toClass.getConstructor();
            D destination = constructor.newInstance();
            copyFields(source, destination);
            return destination;
        } catch (
            NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e
        ) {
            return null;
        }
    }

    public static <S, D> void copyFields(S source, D destination) {
        Class<?> sourceClass = source.getClass();
        Set<String> getterMethods = Arrays
            .stream(sourceClass.getMethods())
            .map(Method::getName)
            .collect(Collectors.toSet());

        for (Method setterMethod : destination.getClass().getMethods()) {
            String setterName = setterMethod.getName();
            if (!Modifier.isPublic(setterMethod.getModifiers()) || !setterName.startsWith("set")) continue;
            String getterName = "get" + setterName.substring(3);
            if (getterMethods.contains(getterName)) {
                try {
                    Method getterMethod = sourceClass.getMethod(getterName);
                    Class<?> getterType = getterMethod.getReturnType();
                    Class<?> setterType = setterMethod.getParameterTypes()[0];
                    if (setterType.isAssignableFrom(List.class)) {
                        if (!getterType.isAssignableFrom(List.class)) continue;
                        setterMethod.invoke(
                            destination,
                            convertCollection(source, getterMethod, setterMethod, new ArrayList<>())
                        );
                    } else if (setterType.isAssignableFrom(Set.class)) {
                        if (!getterType.isAssignableFrom(Set.class)) continue;
                        setterMethod.invoke(
                            destination,
                            convertCollection(source, getterMethod, setterMethod, new HashSet<>())
                        );
                    } else copy(source, destination, setterMethod, getterMethod);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <S, T extends Collection> T convertCollection(
        S source,
        Method getterMethod,
        Method setterMethod,
        T destinationCollection
    ) throws IllegalAccessException, InvocationTargetException {
        Collection sourceCollection = (Collection) getterMethod.invoke(source);
        if (sourceCollection == null) return null;
        Class<?> setterCollectionItemType = (Class<?>) (
            (ParameterizedType) setterMethod.getGenericParameterTypes()[0]
        ).getActualTypeArguments()[0];
        sourceCollection.forEach(it -> destinationCollection.add(convert(it, setterCollectionItemType)));
        return destinationCollection;
    }

    private static <S, D> void copy(S source, D destination, Method setterMethod, Method getterMethod)
        throws IllegalAccessException, InvocationTargetException {
        Class<?> getterType = getterMethod.getReturnType();
        Class<?> setterType = setterMethod.getParameterTypes()[0];

        if (setterType.isAssignableFrom(getterType)) {
            setterMethod.invoke(destination, getterMethod.invoke(source));
        } else {
            setterMethod.invoke(destination, convert(getterMethod.invoke(source), setterType));
        }
    }

    @SafeVarargs
    public static <T> List<T> listOf(T... args) {
        return new ArrayList<T>(args.length) {
            {
                for (T item : args) if (item != null) add(item);
            }
        };
    }

    @SafeVarargs
    public static <T> HashSet<T> setOf(T... args) {
        return new HashSet<T>(args.length) {
            {
                for (T item : args) if (item != null) add(item);
            }
        };
    }

    @SafeVarargs
    public static <T> Map<String, T> mapOf(Pair<String, T>... keyValuePairs) {
        return new HashMap<String, T>() {
            {
                for (Pair<String, T> pair : keyValuePairs) if (pair != null && pair.first != null) put(
                    pair.first,
                    pair.second
                );
            }
        };
    }
}
