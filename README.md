# Java Free Reflection
An agent that makes the java reflection API to run perfectly on Java 15+.

When you call `setAccessible` on some private fields or methods, then JVM throws an exception `java.lang.reflect.InaccessibleObjectException: Unable to make...`
This agent can easily fix it.

# Compatibility
- OracleJDK 15~18 works
- OpenJDK 15~18 works

# What it did
The agent will hook `java.lang.reflect.AccessibleObject.checkCanSetAccessible(Class<?> caller, Class<?> 
declaringClass, boolean throwExceptionIfDenied)` method.

The key implementation is make this method return true, if the reflection caller is our class.

Before hook:
```java
public class AccessibleObject {
    private boolean checkCanSetAccessible(Class<?> caller, Class<?> declaringClass, boolean throwExceptionIfDenied) {
        if (caller == MethodHandle.class) {
            //......
        }
    }
}
```
After hook:
```java
public class AccessibleObject {
    private boolean checkCanSetAccessible(Class<?> caller, Class<?> declaringClass, boolean throwExceptionIfDenied) {
        if ("org.example.Main".equals(caller.getName())) {
            return true;
        } else if (caller.getName().startsWith("org.xxx.B")) {
            return true;
        } else if (caller.getName().startsWith("org.xxx.C")) {
            return true;
        } else if (caller.getName().startsWith("org.xxx.A")) {
            return true;
        } else if (caller == MethodHandle.class) {
            //......
        }
        //......
    }
}
```

So it makes reflection can work on inaccessible operations.

# How to use
1. Build
    ```shell
    mvn clean package
    ```
2. Found your package name or class name which your call reflection api
   ```java
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
            // see reflection-exmaple for more details
          }
    
    }
    ```
   in above code, package name is `org.example`, class name is `org.example.Main`
3. Add argument to your launch options

    Use without **free-reflection**:
    ```shell
    $ java -jar reflection-example.jar
    ================ Reflection Test ================
   (before) text = Hello world!
   Exception in thread "main" java.lang.reflect.InaccessibleObjectException: Unable to make field private final byte[] java.lang.String.value accessible: module java.base does not "opens java.lang" to unnamed module @a09ee92
   at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:354)
   at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:297)
   at java.base/java.lang.reflect.Field.checkCanSetAccessible(Field.java:178)
   at java.base/java.lang.reflect.Field.setAccessible(Field.java:172)
   at org.example.Main.setField(Main.java:10)
   at org.example.Main.main(Main.java:22)
    ```
   Use with **free-reflection**:(if you want `org.exmaple.Main` class can free use reflection 
   api):
    ```shell
   $ java -javaagent:free-reflection.jar=log=true;name=org.example.Main -jar reflection-example.jar
   [FreeReflection]  Agent is starting(log=true;name=org.example.Main)...
   [FreeReflection]  Call re-transformClasses(AccessibleObject)
   [FreeReflection]  Transforming...
   [FreeReflection]  Found checkCanSetAccessible
   [FreeReflection]  Injecting...
   [FreeReflection]  Whitelist name : org.example.Main
   [FreeReflection]  Transform successful
   ================ Reflection Test ================
   (before) text = Hello world!
   (after) text = China NO.1.
   ================ Reflection Test ================
    ```

# More Examples
- disable log output
   ```shell
   $ java -javaagent:free-reflection.jar=log=true -jar reflection-example.jar
   ```
- use for multi class names
   ```shell
   $ java -javaagent:free-reflection.jar=log=true;name=org.xxx.ClassA,org.xxx.ClassB,org.xxx.ClassC -jar 
  reflection-example.jar
   ```
- use for multi package prefix
   ```shell
   $ java -javaagent:free-reflection.jar=log=true;prefix=org.pkg.aaa,org.pkg.bbb,org.pkg.ccc -jar 
  reflection-example.jar
   ```
- full usage 
   ```shell
   $ java -javaagent:free-reflection.jar=log=true;name=org.xxx.ClassA,org.xxx.ClassB,org.xxx.ClassC;prefix=org.pkg.aaa,org.pkg.bbb,org.pkg.ccc -jar 
  reflection-example.jar
   ```

# License
```text
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
