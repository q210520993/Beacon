package com.redstone.beacon

import com.redstone.beacon.annotations.Implementation
import com.redstone.beacon.provider.PlayerProvider
import com.redstone.beacon.provider.Provider
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField


object ProviderRegistrar {

    internal val logger = LoggerFactory.getLogger(ProviderRegistrar::class.java)

    lateinit var playerProvider: PlayerProvider

    fun registry(implementation: Provider) {
        val clazz = implementation::class.java
        if (clazz.isAnnotationPresent(Implementation::class.java)) {
            logger.error("错误的Implementation")
            return
        }
        val method: Method = clazz.getMethod("getFieldName")
        val fieldName = method.invoke(implementation) as String
        val clazz2 = ProviderRegistrar::class
        // 获取 name 属性
        val nameProperty = clazz2.memberProperties.find { it.name == fieldName }
        // 确保属性是可变的（var），且可以访问它
        if (nameProperty != null) {
            // 设置可访问性为 true
            nameProperty.isAccessible = true

            // 赋值
            nameProperty.javaField?.set(this, implementation)

            // 打印结果以确认属性已更改
            println("Updated name: ${clazz.name}") // 输出: Updated name: Bob
        }

    }

}