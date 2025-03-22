package com.redstone.beacon.provider

//此接口用来标记一个provider类
interface Provider {

    // 获得到该Provider在ProvierHnadler对象下的属性名称
    fun getFieldName(): String

}