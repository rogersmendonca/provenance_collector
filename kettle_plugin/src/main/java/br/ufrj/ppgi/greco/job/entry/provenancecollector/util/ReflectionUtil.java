package br.ufrj.ppgi.greco.job.entry.provenancecollector.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since out-2013
 * 
 */
public class ReflectionUtil
{
    public static Object genericInvokeMethod(Object obj, String methodName,
            int paramCount, Object... params)
    {
        Method method;
        Object requiredObj = null;
        Object[] parameters = new Object[paramCount];
        Class<?>[] classArray = new Class<?>[paramCount];
        for (int i = 0; i < paramCount; i++)
        {
            parameters[i] = params[i];
            classArray[i] = params[i].getClass();
        }
        try
        {
            method = getMethod(obj.getClass(), methodName, classArray);
            method.setAccessible(true);
            requiredObj = method.invoke(obj, params);
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
            requiredObj = null;
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            requiredObj = null;
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            requiredObj = null;
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
            requiredObj = null;
        }

        return requiredObj;
    }

    public static Method getMethod(Class<?> clazz, String methodName,
            Class<?>[] paramArray) throws NoSuchMethodException
    {
        Method method = null;
        do
        {
            try
            {
                method = clazz.getDeclaredMethod(methodName, paramArray);
                break;
            }
            catch (NoSuchMethodException e)
            {
                clazz = clazz.getSuperclass();
            }
            catch (SecurityException e)
            {
                clazz = clazz.getSuperclass();
            }

        }
        while (clazz != null);

        if (method != null)
        {
            return method;
        }
        else
        {
            throw new NoSuchMethodException();
        }
    }
}
