package niohttp;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {

    public static Map<String, Object> beanMap = new HashMap<>();//储存含有RestController注解的类的名字与对象
    public static Map<String, MethodInfo> methodMap = new HashMap<>();//储存含有MyRequestingMapping注解的路径和其下方法的名字

    public static void scanpkg(String pkgname) throws Exception {
        String path = pkgname.replace(".","/");
        URL url = Server.class.getClassLoader().getResource(path);//在根目录下查找资源
        File filedir = new File(URLDecoder.decode(url.getPath(),"utf-8"));//解码，获取文件夹根路径
        prase(filedir);
    }

    private static void prase(File filedir) throws Exception {
        if (!filedir.isDirectory()){
            return;
        }
        File[] files = filedir.listFiles( filepathname -> {
            if (filepathname.isDirectory()){//若是目录
                try {
                    prase(filepathname);//递归遍历
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
            return filepathname.getName().endsWith(".class");//只返回class文件
        });
        for (File file:files){
            String ap = file.getAbsolutePath();
//            System.out.println(ap);//D:\2019.12.31\target\classes\niohttp\Server.class
            String cn = ap.split("classes\\\\")[1].replace("\\",".").replace(".class","");//将类的绝对路径转化为类名
            Class<?> cls = Class.forName(cn);
            MyRestController m = cls.getAnnotation(MyRestController.class);
            if(m == null){
                continue;
            }
            init(cls); //处理有MyRestController注解的类
        }
    }

    private static void init(Class<?> cls) throws Exception{//MRM初始化
        beanMap.put(cls.getSimpleName(),cls.newInstance());
        Method[] methods = cls.getDeclaredMethods();
        for (Method m:methods){
            MyRequestMapping mrm = m.getDeclaredAnnotation(MyRequestMapping.class);
            if (mrm != null){
                methodMap.put(mrm.value(),new MethodInfo(m,cls.getSimpleName()));
            }
        }
    }

    public static String praseUrl(String url) throws Exception {
        if (!url.contains("?")){
            System.out.println("执行无参方法");
           return invokeo(url);//执行无参方法
        }else{
            System.out.println("执行有参方法");
          return invokex(url);//执行有参方法
        }

    }


    private static String invokeo(String url) throws Exception{
        MethodInfo mi = methodMap.get(url);
        if (mi ==null){
            return "404";
        }
        Method m = mi.getM();
        String classname = mi.getClassname();
        Object beanobject = beanMap.get(classname);
        m.setAccessible(true);
        return (String)m.invoke(beanobject,null);
    }

    private static String invokex(String url) throws Exception {
        String methodname = url.split("\\?")[0];

        if(methodname ==null){
            return "路径错误！";
        }


        String clsname = methodMap.get(methodname).getClassname();//获取类名
        Object beanobject = beanMap.get(clsname);//获取该方法的类的对象

        Map<String,String> map = new HashMap<>();//存放url中的name和value (id,1)
        String[] ps = url.replaceFirst(".*?\\?","").split("&");
        for (String s:ps){
            if(s.contains("=")){
                String name = s.split("=")[0];
                String value = s.split("=")[1];
                if (name==null||name.equals("")||value==null||value.equals("")){
                    continue;
                }
                System.out.println("------------------");
                System.out.println(name);
                System.out.println(value);
                map.put(name,value);
            }
        }

        Object[] array = new Object[map.size()];//存放执行方法的正确的参数

        int i =0;
        MethodInfo mi = methodMap.get(methodname);//获得该方法的信息(对象和类名)
        Method m = mi.getM();
        Parameter[] parameters = m.getParameters();//获得该方法的参数
        for (Parameter p:parameters){
         String typename = p.getType().getSimpleName();//获得该方法参数的类型名
         String name = p.getName();//获得该方法参数的名字
           if (map.get(name)!=null){
               Object o = checkType(typename,map.get(name));
              if(o!=null){
                  array[i++] = o;
              }

           }
        }

        return (String)m.invoke(beanobject,array);
    }


    private static Object checkType(String typename,String str){
        switch (typename){
            case "byte":return Byte.parseByte(str);
            case "short":return Short.parseShort(str);
            case "int": return Integer.parseInt(str);
            case "long":return Long.parseLong(str);
            case "double":return Double.parseDouble(str);
            case "float":return Float.parseFloat(str);
            default:return str;
        }
    }

}
