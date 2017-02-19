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

  def sensorMapping: ServiceCall[GlobalSensorId, SensorMapping]

  override final def descriptor: Descriptor = {
    import Service._
    named("mapping").withCalls(
      pathCall("/api/sensorMapping", sensorMapping)
    ).withAutoAcl(true)
  }

}

case class GlobalSensorId(nodeId: Long, sensorId: String)
case class SensorMapping(objectId: UUID, domain: String, metricName: String)
object GlobalSensorId {
  implicit val jsonFormat: Format[GlobalSensorId] = Json.format[GlobalSensorId]
}
object SensorMapping {
  implicit val jsonFormat: Format[SensorMapping] = Json.format[SensorMapping]
}
