package com.kiebot.paygate.utils;

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public static HashMap<String, String> formToMap(String form) {
        HashMap<String, String> map = new HashMap<>();
        for (String pair : form.split("&")) {
            String[] split = pair.split("=");
            map.put(split[0], split[1]);
        }
        return map;
    }

    public static String md5(String text) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(text.getBytes());
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            StringBuilder hashtext = new StringBuilder(bigInt.toString(16));
            while (hashtext.length() < 32) {
                hashtext.insert(0, "0");
            }
            return hashtext.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static final String getInjectionScript(String host, Long userId) {
        return (
            "<script>\n" +
            "    function loadGateway(payRequestId, checksum, root) {\n" +
            "        const form = document.createElement(\"form\");\n" +
            "        form.setAttribute(\n" +
            "            \"action\",\n" +
            "            \"https://secure.paygate.co.za/payweb3/process.trans\"\n" +
            "        );\n" +
            "        form.setAttribute(\"method\", \"POST\");\n" +
            "        form.setAttribute(\"id\", \"paygate\");\n" +
            "        form.setAttribute(\"target\", \"pay\");\n" +
            "\n" +
            "        const requestInput = document.createElement(\"input\");\n" +
            "        const checksumInput = document.createElement(\"input\");\n" +
            "\n" +
            "        requestInput.setAttribute(\"type\", \"hidden\");\n" +
            "        requestInput.setAttribute(\"name\", \"PAY_REQUEST_ID\");\n" +
            "        requestInput.setAttribute(\"value\", payRequestId);\n" +
            "\n" +
            "        checksumInput.setAttribute(\"type\", \"hidden\");\n" +
            "        checksumInput.setAttribute(\"name\", \"CHECKSUM\");\n" +
            "        checksumInput.setAttribute(\"value\", checksum);\n" +
            "\n" +
            "        form.appendChild(requestInput);\n" +
            "        form.appendChild(checksumInput);\n" +
            "\n" +
            "        root.appendChild(form);\n" +
            "\n" +
            "        document.forms[\"paygate\"].submit();\n" +
            "        window.open(\"\", \"pay\");\n" +
            "    }\n" +
            "    const mutation = new MutationObserver(()=> {\n" +
            "        const root = document.getElementsByClassName(\"layout-main\")[0];\n" +
            "        console.log(root)\n" +
            "        if(root === null || root === undefined) return;\n" +
            "        mutation.disconnect();\n" +
            "        const backup = root.innerHTML;\n" +
            "        root.innerHTML = '<div class=\"loadingOverlay optimizedCheckout-overlay\"></div>';\n" +
            "        const eventMethod = window.addEventListener\n" +
            "        ? \"addEventListener\"\n" +
            "        : \"attachEvent\";\n" +
            "        const eventListener = window[eventMethod];\n" +
            "        const messageEvent = eventMethod == \"attachEvent\" ? \"onmessage\" : \"message\";\n" +
            "        eventListener(\n" +
            "        messageEvent,\n" +
            "        async function (e) {\n" +
            "            if(e.origin !== \"" +
            host +
            "\") return\n" +
            "            const key = e.message ? \"message\" : \"data\";\n" +
            "            const data = e[key];\n" +
            "            const end = await fetch(\"" +
            host +
            "/api/status/" +
            userId +
            "/{{checkout.order.id}}\", {method: \"POST\"});\n" +
            "            console.log(\"reload\");\n" +
            "            window.location.reload();\n" +
            "        },false);\n" +
            "\n" +
            "        fetch(\"" +
            host +
            "/api/process/" +
            userId +
            "/{{checkout.order.id}}\", {\n" +
            "        method: \"POST\",\n" +
            "        }).then((response) =>\n" +
            "        response.json().then((data) => {\n" +
            "            if (JSON.stringify(data) === \"{}\") root.innerHTML = backup;\n" +
            "            else {\n" +
            "            frame = document.createElement(\"iframe\");\n" +
            "            frame.setAttribute(\"width\", \"100%\");\n" +
            "            frame.setAttribute(\"height\", \"500px\");\n" +
            "            frame.setAttribute(\"name\", \"pay\");\n" +
            "            root.innerHTML = \"\";\n" +
            "            root.appendChild(frame);\n" +
            "            const checksum = data[\"CHECKSUM\"];\n" +
            "            const payRequestId = data[\"PAY_REQUEST_ID\"];\n" +
            "            loadGateway(payRequestId, checksum, root);\n" +
            "            }\n" +
            "        }));\n" +
            "    });\n" +
            "    const rootCheckoutDiv = document.getElementById(\"checkout-app\");\n" +
            "    mutation.observe(rootCheckoutDiv, { attributes: true, childList: true })\n" +
            " </script>"
        );
    }
}
