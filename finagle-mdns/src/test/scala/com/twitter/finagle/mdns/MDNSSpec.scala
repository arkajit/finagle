package com.twitter.finagle.mdns

import com.twitter.util.{Await, RandomSocket}
import java.net.InetSocketAddress
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually._
import org.scalatest.concurrent.Timeouts._
import org.scalatest.junit.JUnitRunner
import org.scalatest.time.SpanSugar._
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class MdnsSpec extends FunSuite with BeforeAndAfter {
  test("bind locally") {
    val addr = RandomSocket()
    val resolver = new MDNSResolver
    val announcer = new MDNSAnnouncer
    val name = "my-service"
    val target = "_finagle._tcp.local."

    val announcement = Await.result(announcer.announce(addr, name + "." + target))
    try {
      val group = resolver.resolve(target).get() map { _.asInstanceOf[InetSocketAddress].getPort }
      eventually(timeout(5 seconds)) { assert(group().contains(addr.getPort)) }
    } finally {
      Await.ready(announcement.close())
    }
  }
}
