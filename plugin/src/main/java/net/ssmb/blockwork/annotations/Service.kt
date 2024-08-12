package net.ssmb.blockwork.annotations

@Target(AnnotationTarget.CLASS) annotation class Service(val loadOrder: Int = 1)
