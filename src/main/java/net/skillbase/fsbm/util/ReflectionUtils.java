package net.skillbase.fsbm.util;

import java.lang.reflect.*;

public class ReflectionUtils
{
    public static int getPrivateIntFieldFromObject(final Object object, final String forgeFieldName, final String vanillaFieldName) throws NoSuchFieldException, SecurityException, NumberFormatException, IllegalArgumentException, IllegalAccessException {
        Field targetField = null;
        try {
            targetField = object.getClass().getDeclaredField(forgeFieldName);
        }
        catch (NoSuchFieldException e) {
            targetField = object.getClass().getDeclaredField(vanillaFieldName);
        }
        targetField.setAccessible(true);
        return Integer.parseInt(targetField.get(object).toString());
    }
    
    public static double getPrivateDoubleFieldFromObject(final Object object, final String forgeFieldName, final String vanillaFieldName) throws NoSuchFieldException, SecurityException, NumberFormatException, IllegalArgumentException, IllegalAccessException {
        Field targetField = null;
        try {
            targetField = object.getClass().getDeclaredField(forgeFieldName);
        }
        catch (NoSuchFieldException e) {
            targetField = object.getClass().getDeclaredField(vanillaFieldName);
        }
        if (targetField != null) {
            targetField.setAccessible(true);
            return Double.valueOf(targetField.get(object).toString());
        }
        return 0.0;
    }
    
    public static void setPrivateIntFieldOfObject(final Object object, final String forgeFieldName, final String vanillaFieldName, final int value) throws NoSuchFieldException, SecurityException, NumberFormatException, IllegalArgumentException, IllegalAccessException {
        Field targetField = null;
        try {
            targetField = object.getClass().getDeclaredField(forgeFieldName);
        }
        catch (NoSuchFieldException e) {
            targetField = object.getClass().getDeclaredField(vanillaFieldName);
        }
        if (targetField != null) {
            targetField.setAccessible(true);
            targetField.set(object, value);
        }
    }
}
