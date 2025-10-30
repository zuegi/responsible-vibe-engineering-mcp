package ch.zuegi.rvmcp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RvmcpApplication

fun main(args: Array<String>) {
    runApplication<RvmcpApplication>(*args)
}
