package org.example;

import java.lang.reflect.Field;

public class Main {

    private static void setField(Object object, String fieldName, Object value) throws Exception {
        Class<?> clazz = object.getClass();
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("================ Reflection Test ================");
        String text = "Hello world!";
        char[] chinaNO1Chars = new char[]{'C', 'h', 'i', 'n', 'a', ' ', 'N', 'O', '.', '1', '.'};
        byte[] chinaNO1Bytes = new byte[]{'C', 'h', 'i', 'n', 'a', ' ', 'N', 'O', '.', '1', '.'};

        System.out.println("(before) text = " + text);
        try {
            setField(text, "value", chinaNO1Chars);
        } catch (IllegalArgumentException ignore) {
            setField(text, "value", chinaNO1Bytes);
        }
        System.out.println(" (after) text = " + text);
        System.out.println("================ Reflection Test ================");
    }

}