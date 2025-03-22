package com.redstone.beacon.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ：clok
 * @project Beacon
 * @date ：2025/2/12 下午7:00
 * 此注解表示该接口是一个插件提供者接口，用于在应用程序中实现特定的功能。
 * 将此注解应用于各个提供者接口，以便进行更好的分类和处理。
 */

@Target(ElementType.TYPE) // 该注解可以应用于类或接口
@Retention(RetentionPolicy.RUNTIME) // 注解将在运行时保留
public @interface Provider {
    String description(); // 描述信息
}
