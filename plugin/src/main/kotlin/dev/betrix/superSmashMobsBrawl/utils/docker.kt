package dev.betrix.superSmashMobsBrawl.utils

import java.io.File

object DockerDetector {

    fun isRunningInDocker(): Boolean {
        return checkDockerEnvFile() ||
                checkCGroupFile() ||
                checkEnvironmentVariables()
    }

    private fun checkDockerEnvFile(): Boolean {
        return File("/.dockerenv").exists()
    }

    private fun checkCGroupFile(): Boolean {
        return try {
            File("/proc/1/cgroup").useLines { lines ->
                lines.any { it.contains("docker") }
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun checkEnvironmentVariables(): Boolean {
        return System.getenv("DOCKER_CONTAINER") != null
    }

    fun getContainerInfo(): String {
        return when {
            isRunningInDocker() -> "Running in Docker"
            else -> "Running natively"
        }
    }
}