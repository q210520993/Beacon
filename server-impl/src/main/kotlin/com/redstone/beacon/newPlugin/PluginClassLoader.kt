package com.redstone.beacon.newPlugin

import java.net.URL
import java.net.URLClassLoader


class PluginClassLoader(pluginJars: Array<URL>, parent: ClassLoader) :
    URLClassLoader(pluginJars, parent)
{

}