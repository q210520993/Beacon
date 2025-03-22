package com.redstone.beacon.annotations;

import com.redstone.beacon.provider.Provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author ：clok
 * @project Beacon
 * @date ：2025/2/12 下午7:01
 * 此注解表示该类是一个接口的实现，提供了具体的功能。
 * 将此注解应用于插件中的实现类，以便进行有效的管理和识别。
 */
@Target(ElementType.TYPE) // 该注解可以应用于类或接口
@Retention(RetentionPolicy.RUNTIME) // 注解将在运行时保留
public @interface Implementation {
    Class<? extends Provider> provider(); // 提供者接口的类
    String description(); // 描述信息
}
