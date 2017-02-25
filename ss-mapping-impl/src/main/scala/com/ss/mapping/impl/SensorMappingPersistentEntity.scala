package com.ss.mapping.impl

import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.ss.mapping.api.SensorMapping
import play.api.libs.json.{Format, JsPath, Json, OFormat}

/**
  * Created by ytaras on 24.02.17.
  */
class SensorMappingPersistentEntity extends PersistentEntity {
  override type Command = SensorMappingCommand
  override type Event = SensorMappingEvent
  override type State = SensorMappingState

  override def initialState: SensorMappingState = SensorMappingState.unregistered

  override def behavior: Behavior = {
    case state if state.isDefined => registeredActions.orElse(commonActions)
    case state if !state.isDefined => unregisteredActions.orElse(commonActions)
  }

  private val unregisteredActions: Actions =
    Actions().onReadOnlyCommand[LoadMapping.type, SensorMapping] {
      case (LoadMapping, ctx, SensorMappingState.unregistered) =>
        // FIXME - Is it ok to use transport layer exceptions on persistence level?
        ctx.commandFailed(NotFound(entityId))
    }.onReadOnlyCommand[UnregisterMapping.type, Done] {
      case (UnregisterMapping, ctx, SensorMappingState.unregistered) =>
        ctx.commandFailed(NotFound(entityId))
    }

  private val registeredActions: Actions =
    Actions().onReadOnlyCommand[LoadMapping.type, SensorMapping] {
      case (LoadMapping, ctx, SensorMappingState.registered(mapping)) => ctx.reply(mapping)
    }.onCommand[UnregisterMapping.type, Done] {
      case (UnregisterMapping, ctx, _) => ctx.thenPersist(MappingUnregistered) { _ => ctx.reply(Done)}
    }.onEvent {
      case (MappingUnregistered, _) => SensorMappingState.unregistered
    }

  private val commonActions: Actions =
    Actions().onCommand[RegisterMapping, Done] {
      case (x: RegisterMapping, ctx, _) =>
        ctx.thenPersist(MappingRegistered(x)) { _ => ctx.reply(Done) }
    }.onEvent {
      case (MappingRegistered(mapping), _) => SensorMappingState.registered(mapping)
    }

}

sealed trait SensorMappingCommand
case class RegisterMapping(mapping: SensorMapping) extends SensorMappingCommand with ReplyType[Done]
case object UnregisterMapping extends SensorMappingCommand with ReplyType[Done] {
  implicit val format: Format[UnregisterMapping.type] = JsonSerializer.emptySingletonFormat(this)
}
case object LoadMapping extends SensorMappingCommand with ReplyType[SensorMapping] {
  implicit val format: Format[LoadMapping.type] = JsonSerializer.emptySingletonFormat(this)
}
object RegisterMapping {
  implicit val format: Format[RegisterMapping] = Json.format[RegisterMapping]
}
sealed trait SensorMappingEvent extends AggregateEvent[SensorMappingEvent] {
  override def aggregateTag: AggregateEventShards[SensorMappingEvent] = SensorMappingEvent.Tag
}
object SensorMappingEvent {
  val numShards = 20
  val Tag: AggregateEventShards[SensorMappingEvent] = AggregateEventTag.sharded[SensorMappingEvent](numShards)

}
case class MappingRegistered(mapping: SensorMapping) extends SensorMappingEvent
case object MappingUnregistered extends SensorMappingEvent {
  implicit val format: Format[MappingUnregistered.type] = JsonSerializer.emptySingletonFormat(this)
}

object MappingRegistered {
  def apply(x: RegisterMapping): MappingRegistered = MappingRegistered(x.mapping)
  implicit val format: Format[MappingRegistered] = Json.format[MappingRegistered]
}
sealed abstract class SensorMappingState(val isDefined: Boolean)
object SensorMappingState {
  object unregistered extends SensorMappingState(false)
  case class registered(mapping: SensorMapping) extends SensorMappingState(true)
}
