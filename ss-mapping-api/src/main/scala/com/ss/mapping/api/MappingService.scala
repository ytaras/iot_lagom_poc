package com.ss.mapping.api

import java.util.UUID

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json.{Format, Json}

import scala.concurrent.Future

/**
  * Created by ytaras on 18.02.17.
  */
trait MappingService extends Service {

  def sensorMapping(nodeId: Long, sensorId: Int): ServiceCall[NotUsed, SensorMapping]
  def registerMapping(nodeId: Long, sensorId: Int): ServiceCall[SensorMapping, NotUsed]

  override final def descriptor: Descriptor = {
    import Service._
    named("mapping").withCalls(
      pathCall("/api/sensorMapping/nodes/:node/sensors/:sensor", sensorMapping _),
      pathCall("/api/sensorMapping/nodes/:node/sensors/:sensor", registerMapping _)
    ).withAutoAcl(true)
  }

}

case class GlobalSensorId(nodeId: Long, sensorId: Int)
case class SensorMapping(objectId: UUID, domain: String, metricName: String)
object GlobalSensorId {
  implicit val jsonFormat: Format[GlobalSensorId] = Json.format[GlobalSensorId]
}
object SensorMapping {
  implicit val jsonFormat: Format[SensorMapping] = Json.format[SensorMapping]
}
