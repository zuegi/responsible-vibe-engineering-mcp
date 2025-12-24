package ch.zuegi.rvmcp.shared

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun <R : Any> R.rvmcpLogger(): Lazy<Logger> = lazy { LoggerFactory.getLogger(this.javaClass) }
