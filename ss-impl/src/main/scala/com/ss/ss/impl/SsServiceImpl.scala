package com.ss.ss.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.ss.ss.api.SsService

/**
  * Implementation of the SsService.
  */
class SsServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends SsService {

  override def hello(id: String) = ServiceCall { _ =>
    // Look up the ss entity for the given ID.
    val ref = persistentEntityRegistry.refFor[SsEntity](id)

    // Ask the entity the Hello command.
    ref.ask(Hello(id, None))
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    // Look up the ss entity for the given ID.
    val ref = persistentEntityRegistry.refFor[SsEntity](id)

    // Tell the entity to use the greeting message specified.
    ref.ask(UseGreetingMessage(request.message))
  }
}
