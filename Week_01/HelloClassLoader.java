/**
 * Description: 作业二： load Hello.xlass
 * Author:   Larry
 * Date:     2020/10/21 21:26
 */
package week01;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;


public class HelloClassLoader extends ClassLoader{


    public static void main(String[] args) {
        try {
            // 通过 对象.findClass 获取类Hello
            Class hello_class=new HelloClassLoader().findClass("Hello");
            // 反射获取 hello 方法
            Method hello_methond=hello_class.getMethod("hello");
            // 通过反射执行hello方法   输出 Hello, classLoader!
            hello_methond.invoke(hello_class.newInstance());

        }catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 重写 findClass方法 实现自定义类加载器
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            // 读取Hello.xclass文件到Byte数组  注意文件路径
            byte[] helloClassBytes = getContent("./src/week01/Hello.xlass");
            // 将读取到的字节（x=255-x）
            for (int i = 0; i < Objects.requireNonNull(helloClassBytes).length; i++) {
                helloClassBytes[i] = (byte) (255 - helloClassBytes[i]);
            }
            // 加载自定义Hello类
            return defineClass(name, helloClassBytes, 0, helloClassBytes.length);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.findClass(name);
    }

    // 读取文件到字节数组
    public static byte[] getContent(String filePath) throws IOException {
        File file = new File(filePath);
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            return null;
        }
        FileInputStream fi = new FileInputStream(file);
        byte[] buffer = new byte[(int) fileSize];
        int offset = 0;
        int numRead = 0;
        while (offset < buffer.length
                && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += numRead;
        }
        if (offset != buffer.length) {
            throw new IOException("Could not completely read file "
                    + file.getName());
        }
        fi.close();
        return buffer;
    }


}
