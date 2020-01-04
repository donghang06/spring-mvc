package niohttp;

import java.lang.reflect.Method;

public class MethodInfo {
    private Method m;
    private String classname;

    public MethodInfo() {
    }

    public MethodInfo(Method m, String classname) {
        this.m = m;
        this.classname = classname;
    }

    public Method getM() {
        return m;
    }

    public void setM(Method m) {
        this.m = m;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    @Override
    public String toString() {
        return "MethodInfo{" +
                "m=" + m +
                ", classname='" + classname + '\'' +
                '}';
    }
}
