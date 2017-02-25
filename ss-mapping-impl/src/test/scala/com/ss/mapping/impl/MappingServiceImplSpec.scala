package com.ss.mapping.impl

import java.util.UUID

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.transport.{NotFound, TransportErrorCode, TransportException}
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.ss.mapping.api.{GlobalSensorId, MappingService, SensorMapping}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

import scala.concurrent.Future
import scala.util.Random

/**
  * Created by ytaras on 24.02.17.
  */
class MappingServiceImplSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new MappingApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[MappingService]

  override protected def afterAll() = server.stop()
  def withClient[T](f: MappingService => T): T = f(client)

  "Mapping service" should {
    "register mapping" in withClient { implicit client =>
      registerMapping(genSensorId, genMapping).map { _ should ===(NotUsed)}
    }
    "get registered mapping" in withClient { implicit client =>
      val sid = genSensorId
      val mapping = genMapping
      for {
        NotUsed <- registerMapping(sid, mapping)
        result <- getMapping(sid)
      } yield result should ===(mapping)
    }
    "return 404 for not registered mapping" in withClient { implicit client =>
      recoverToExceptionIf[TransportException] {
        getMapping(genSensorId)
      }.map(ex => ex.errorCode should ===(TransportErrorCode.NotFound))
    }
    "return 404 for not registered mapping if trying to delete" in withClient { implicit client =>
      recoverToExceptionIf[TransportException] {
        unregisterMapping(genSensorId)
      }.map(ex => ex.errorCode should ===(TransportErrorCode.NotFound))
    }
    "updates mapping" in withClient { implicit client =>
      val sid = genSensorId
      val mapping1 = genMapping
      val mapping2 = genMapping
      for {
        NotUsed <- registerMapping(sid, mapping1)
        NotUsed <- registerMapping(sid, mapping2)
        result <- getMapping(sid)
      } yield result should ===(mapping2)
    }
    "unregisters mapping" in withClient { implicit client =>
      val sid = genSensorId
      val mapping = genMapping
      for {
        NotUsed <- registerMapping(sid, mapping)
        NotUsed <- unregisterMapping(sid)
        ex <- recoverToExceptionIf[TransportException](getMapping(sid))
      } yield ex.errorCode should ===(TransportErrorCode.NotFound)
    }
  }

  def getMapping(globalSensorId: GlobalSensorId)(implicit client: MappingService): Future[SensorMapping] =
    client.sensorMapping(globalSensorId.nodeId, globalSensorId.sensorId).invoke()
  def registerMapping(globalSensorId: GlobalSensorId, sensorMapping: SensorMapping)(implicit client: MappingService): Future[NotUsed] =
    client.registerMapping(globalSensorId.nodeId, globalSensorId.sensorId).invoke(sensorMapping)
  def unregisterMapping(globalSensorId: GlobalSensorId)(implicit client: MappingService): Future[NotUsed] =
    client.unregisterMapping(globalSensorId.nodeId, globalSensorId.sensorId).invoke()
  def genMapping: SensorMapping = SensorMapping(UUID.randomUUID(), "doman", "metric")
  def genSensorId: GlobalSensorId = GlobalSensorId(Random.nextLong(), Random.nextInt())

}
