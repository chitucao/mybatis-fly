/**
 *    Copyright 2009-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.binding;

import org.apache.ibatis.binding.MapperProxy.MapperMethodInvoker;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lasse Voss
 * 用于创建Mapper接口的代理类，JDK动态代理实现
 * 问题1：为什么直接代理一个接口呢？
 * 问题2：Mapper接口没有实现类如何进行代理？
 *    我们的目的是根据— 个可以执行的方法，直接找到Mapper.xml 中的StatementID, 方便调用。
 *    如果根据接口类型＋方法的名称找到StatementID 这个逻辑在Handler类(MapperProxy) 中就可以完成，其实也就没有实现类的什么事了。
 *
 */
public class MapperProxyFactory<T> {

  private final Class<T> mapperInterface;
  private final Map<Method, MapperMethodInvoker> methodCache = new ConcurrentHashMap<>();

  public MapperProxyFactory(Class<T> mapperInterface) {
    this.mapperInterface = mapperInterface;
  }

  public Class<T> getMapperInterface() {
    return mapperInterface;
  }

  public Map<Method, MapperMethodInvoker> getMethodCache() {
    return methodCache;
  }

  /**
   * JDK动态代理的核心对象，：被代理类（实现类）、接口、实现了lnvocationHandler的触发管理类，用来生成代理对象。
   * @param mapperProxy
   * @return
   */
  // 通过JDK动态代理模式创建和返回代理对象
  @SuppressWarnings("unchecked")
  protected T newInstance(MapperProxy<T> mapperProxy) {
    /**
     * 动态代理
     * 参数1 classLoader  用哪个类加载器去加载代理对象
     * 参数2 interfaces 动态代理类需要实现的接口
     * 参数3 invocationHander实现类  动态代理方法在执行时，会调用里面的invoke方法去执行
     **/
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
  }

  public T newInstance(SqlSession sqlSession) {
    final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy);
  }

}
