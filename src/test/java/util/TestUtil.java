package util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;

public class TestUtil {

    public static <T> HashMap<String, Method> getMethods(Class<T> tClass){
        final HashMap<String, Method> methodHashMap = new HashMap<>();
         Arrays.stream(tClass.getDeclaredMethods())
                .peek(method -> method.setAccessible(true))
                .forEach(method -> methodHashMap.put(method.getName(), method));

        return methodHashMap;
    }

    public static void setFinalStaticField(Class<?> clazz, String fieldName, Object value)
            throws ReflectiveOperationException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, value);
    }
}
