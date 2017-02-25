package com.ss.mapping.impl

import akka.Done
import akka.persistence.query.Offset
import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor.ReadSideHandler
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, EventStreamElement, ReadSideProcessor}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.collection.JavaConverters._

/**
  * Created by ytaras on 25.02.17.
  */
class NodeReadEventsProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext) extends ReadSideProcessor[SensorMappingEvent] {
  val builder = readSide
    .builder[SensorMappingEvent]("node_links_offset")
    .setGlobalPrepare(createTable)
    .setPrepare(_ => prepareUpdateSensor())
    .setEventHandler[MappingRegistered](processSensorMappingRegistered)

  private val updateSensorPromise = Promise[PreparedStatement]
  private def updateSensor: Future[PreparedStatement] = updateSensorPromise.future
  private def prepareUpdateSensor() = {
    val f =session.prepare(
      """update node_links set
        | sensor_to_metric = sensor_to_metric + :sensor_metric_update where node_id = :node_id""".stripMargin
    )
    updateSensorPromise.completeWith(f)
    f.map(_ => Done)
  }

  private def createTable(): Future[Done] = session.executeCreateTable(
    """
      |create table if not exists node_links (
      |	node_id bigint,
      |	sensor_to_object map<int, uuid>,
      |	sensor_to_metric map<int, text>,
      |	sensor_to_domain map<int, text>,
      |	primary key (node_id)
      |)
    """.stripMargin)

  private def processSensorMappingRegistered(registered: EventStreamElement[MappingRegistered]): Future[List[BoundStatement]] = updateSensor.map { us =>
    val bound = us.bind()
    val nodeId = registered.entityId.split(":").head.toLong
    val sensorId = registered.entityId.split(":")(1).toInt
    bound.setMap[Int, String]("sensor_metric_update", Map(sensorId -> registered.event.mapping.domain).asJava)
    bound.setLong("node_id", nodeId)
    List(bound)
  }


  override def aggregateTags: Set[AggregateEventTag[SensorMappingEvent]] =
    SensorMappingEvent.Tag.allTags

  override def buildHandler(): ReadSideHandler[SensorMappingEvent] = builder.build()
}

trait MyDatabase {
  def createTables: Future[Done]
  def loadOffset(tag: AggregateEventTag[SensorMappingEvent]): Future[Offset]
  def handleEvent(event: SensorMappingEvent, offset: Offset): Future[Done]
}
