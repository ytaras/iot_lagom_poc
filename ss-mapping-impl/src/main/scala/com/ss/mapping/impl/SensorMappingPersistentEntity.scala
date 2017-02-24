package com.ss.mapping.impl

import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.ss.mapping.api.SensorMapping

/**
  * Created by ytaras on 24.02.17.
  */
class SensorMappingPersistentEntity extends PersistentEntity {
  override type Command = SensorMappingCommand
  override type Event = SensorMappingEvent
  override type State = SampleMappingState

  override def initialState: SampleMappingState = SampleMappingState.unregistered

  override def behavior: Behavior = {
    case state if state.isDefined => registeredActions.orElse(commonActions)
    case state if !state.isDefined => unregisteredActions.orElse(commonActions)
  }

  private val unregisteredActions: Actions =
    Actions().onReadOnlyCommand[LoadMapping.type, SensorMapping] {
      case (LoadMapping, ctx, SampleMappingState.unregistered) =>
        // FIXME - Is it ok to use transport layer exceptions on persistence level?
        ctx.commandFailed(NotFound(entityId))
    }

  private val registeredActions: Actions =
    Actions().onReadOnlyCommand[LoadMapping.type, SensorMapping] {
      case (LoadMapping, ctx, SampleMappingState.registered(mapping)) => ctx.reply(mapping)
    }

  private val commonActions: Actions =
    Actions().onCommand[RegisterMapping, Done] {
      case (x: RegisterMapping, ctx, _) =>
        ctx.thenPersist(MappingRegistered(x)) { _ => ctx.reply(Done) }
    }.onEvent {
      case (MappingRegistered(mapping), _) => SampleMappingState.registered(mapping)
    }

}

sealed trait SensorMappingCommand
case class RegisterMapping(mapping: SensorMapping) extends SensorMappingCommand with ReplyType[Done]
case object LoadMapping extends SensorMappingCommand with ReplyType[SensorMapping]
sealed trait SensorMappingEvent
case class MappingRegistered(mapping: SensorMapping) extends SensorMappingEvent
object MappingRegistered {
  def apply(x: RegisterMapping): MappingRegistered = MappingRegistered(x.mapping)
}
sealed abstract class SampleMappingState(val isDefined: Boolean)
object SampleMappingState {
  object unregistered extends SampleMappingState(false)
  case class registered(mapping: SensorMapping) extends SampleMappingState(true)
}
