package net.ssmb.blockwork

import java.lang.reflect.Constructor
import kotlin.math.max
import kotlin.reflect.KClass
import net.ssmb.blockwork.Blockwork.Companion.container
import net.ssmb.blockwork.annotations.Inject
import net.ssmb.blockwork.annotations.Service
import net.ssmb.blockwork.interfaces.OnTick
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.javaType

class Container {
    @PublishedApi internal val registeredExternalDependencies = hashMapOf<String, Any>()
    private val registeredServices = arrayListOf<Pair<Int, Class<*>>>()

    private val constructedServices = hashMapOf<String, Any>()
    private val tickableServices = arrayListOf<OnTick>()

    fun registerService(clazz: Class<*>) {
        if (registeredServices.any { it.second.name == clazz.name }) {
            return
        }

        val serviceData = clazz.getAnnotation(Service::class.java)

        var loadOrder = serviceData?.loadOrder ?: 1

        val constructor = clazz.constructors.first()

        constructor.parameters.forEach {
            val isParameterService = it.type.isAnnotationPresent(Service::class.java)

            if (!isParameterService) {
                val isParameterInjectable = it.isAnnotationPresent(Inject::class.java)

                if (isParameterInjectable) {
                    val injectData = it.getAnnotation(Inject::class.java)

                    if (!registeredExternalDependencies.contains(injectData.token)) {
                        throw Exception(
                            "Error occurred when adding service ${clazz.simpleName}, parameter \"${it.name}\" of type \"${it.type.name}\" is either not a service or not registered as a dependency during init"
                        )
                    }
                } else {
                    if (!registeredExternalDependencies.contains(it.type.name)) {
                        throw Exception(
                            "Error occurred when adding service ${clazz.simpleName}, parameter \"${it.name}\" of type \"${it.type.name}\" is either not a service or not registered as a dependency during init"
                        )
                    }
                }
            } else {
                registerService(it.type)

                val dependentServiceLoadOrder =
                    max(
                        registeredServices.find { (_, clazz) -> clazz.name == it.type.name }?.first
                            ?: 1,
                        1
                    )
                loadOrder += dependentServiceLoadOrder
            }
        }

        registeredServices.add(Pair(loadOrder, clazz))
    }

    fun register(clazz: Class<*>) {
        if (clazz.isAnnotationPresent(Service::class.java)) {
            throw Exception(
                "Attempted to register class \"${clazz.name}\" as an external dependency, but the class is annotated as a service"
            )
        }

        registeredExternalDependencies[clazz.name] = clazz
    }

    fun register(clazz: KClass<*>) {
        register(clazz.java)
    }

    fun register(any: Any) {
        registeredExternalDependencies[any::class.java.name] = any
    }

    fun register(token: String, value: Any) {
        registeredExternalDependencies[token] = value
    }

    fun instantiateServices() {
        registeredServices
            .sortedBy { it.first }
            .forEach { container.constructDependency(it.second) }
    }

    fun constructDependency(clazz: Class<*>): Any {
        val constructor = clazz.constructors.first()

        val parameters = getParametersForConstructor(constructor)

        val instance = constructor.newInstance(*parameters)

        if (clazz.isAnnotationPresent(Service::class.java)) {
            constructedServices[clazz.name] = instance

            Blockwork.modding.handleListenerAdded(instance)
            Blockwork.handleLifecycles(clazz, instance)
        }

        return instance
    }

    private fun getParametersForConstructor(constructor: Constructor<*>): Array<*> {
        val parameters = arrayListOf<Any>()

        constructor.parameters.forEach {
            val isService = it.type.isAnnotationPresent(Service::class.java)

            if (isService) {
                val serviceDep =
                    constructedServices[it.type.name]
                        ?: throw Exception(
                            "Error when getting parameters for constructor, type \"${it.type.name}\" is annotated as a service, but not constructed"
                        )

                parameters.add(serviceDep)
            } else {
                val isCustomInjection = it.isAnnotationPresent(Inject::class.java)

                if (isCustomInjection) {
                    val injectData = it.getAnnotation(Inject::class.java)

                    val injectedDep =
                        registeredExternalDependencies[injectData.token]
                            ?: throw Exception(
                                "Error when getting parameters for constructor, type \"${it.type.name}\" has @Inject annotation, but token, \"${injectData.token}\", was not registered"
                            )

                    parameters.add(injectedDep)
                } else {
                    val dep =
                        registeredExternalDependencies[it.type.name]
                            ?: throw Exception(
                                "Error when getting parameters for constructor, type \"${it.type.name}\", was not registered as an external dependency and is not annotated as a service"
                            )

                    parameters.add(dep)
                }
            }
        }

        if (parameters.size != constructor.parameters.size) {
            throw Exception(
                "Error when getting parameters for constructor, could not resolve every parameter"
            )
        }

        return parameters.toTypedArray()
    }

    // TODO make this function completely replace the getParametersForConstructor one eventually
    fun resolveParameters(parameters: List<KParameter>, customTypes: Map<String?, Any> = mapOf()): Map<KParameter, Any?> {
        val parametersMap = hashMapOf<KParameter, Any?>()

        parameters.forEach {
            val parameterTypeName = it.type.javaClass.name
            val isService = it.type.hasAnnotation<Service>()

            if (customTypes[parameterTypeName] != null) {
                parametersMap[it] = customTypes[parameterTypeName]
            } else if (isService) {
                val serviceDep =
                    constructedServices[it.type.javaClass.name]
                        ?: throw Exception(
                            "Error when getting parameters for constructor, type \"${parameterTypeName}\" is annotated as a service, but not constructed"
                        )
                parametersMap[it] = serviceDep
            } else {
                val isCustomInjection = it.hasAnnotation<Inject>()

                if (isCustomInjection) {
                    val injectData = it.findAnnotation<Inject>()

                    val injectedDep =
                        registeredExternalDependencies[injectData?.token]
                            ?: throw Exception(
                                "Error when getting parameters for constructor, type \"${parameterTypeName}\" has @Inject annotation, but token, \"${injectData?.token}\", was not registered"
                            )

                    parametersMap[it] = injectedDep
                } else {
                    val dep =
                        registeredExternalDependencies[parameterTypeName]
                            ?: throw Exception(
                                "Error when getting parameters for constructor, type \"${parameterTypeName}\", was not registered as an external dependency and is not annotated as a service"
                            )

                    parametersMap[it] = dep
                }
            }
        }

        if (parametersMap.size != parameters.size) {
            throw Exception(
                "Error when getting parameters for constructor, could not resolve every parameter"
            )
        }

        return parametersMap
    }
}
