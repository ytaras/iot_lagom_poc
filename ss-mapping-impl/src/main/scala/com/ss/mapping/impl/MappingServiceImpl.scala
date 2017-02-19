package com.ss.mapping.impl

import java.util.UUID

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.ss.mapping.api.{GlobalSensorId, MappingService, SensorMapping}

import scala.concurrent.Future

/**
  * Created by ytaras on 18.02.17.
  */
class MappingServiceImpl extends MappingService {
  override def sensorMapping: ServiceCall[GlobalSensorId, SensorMapping] = ServiceCall { sid =>
    ???
  }
}
