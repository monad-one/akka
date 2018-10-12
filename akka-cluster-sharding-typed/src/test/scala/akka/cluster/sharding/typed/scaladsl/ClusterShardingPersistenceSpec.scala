/*
 * Copyright (C) 2017-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.cluster.sharding.typed.scaladsl

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.cluster.typed.Cluster
import akka.cluster.typed.Join
import akka.persistence.typed.scaladsl.Effect
import com.typesafe.config.ConfigFactory
import org.scalatest.WordSpecLike

object ClusterShardingPersistenceSpec {
  val config = ConfigFactory.parseString(
    """
      akka.actor.provider = cluster

      akka.remote.netty.tcp.port = 0
      akka.remote.artery.canonical.port = 0
      akka.remote.artery.canonical.hostname = 127.0.0.1

      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
    """)

  sealed trait Command
  final case class Add(s: String) extends Command
  final case class Get(replyTo: ActorRef[String]) extends Command
  final case object StopPlz extends Command

  val typeKey = EntityTypeKey[Command]("test")

  def persistentEntity(entityId: String): Behavior[Command] =
    PersistentEntity[Command, String, String](
      entityTypeKey = typeKey,
      entityId = entityId,
      emptyState = "",
      commandHandler = (state, cmd) ⇒ cmd match {
        case Add(s) ⇒ Effect.persist(s)
        case Get(replyTo) ⇒
          replyTo ! s"$entityId:$state"
          Effect.none
        case StopPlz ⇒ Effect.stop
      },
      eventHandler = (state, evt) ⇒ if (state.isEmpty) evt else state + "|" + evt)

}

class ClusterShardingPersistenceSpec extends ScalaTestWithActorTestKit(ClusterShardingPersistenceSpec.config) with WordSpecLike {
  import ClusterShardingPersistenceSpec._

  val sharding = ClusterSharding(system)

  "Typed cluster sharding with persistent actor" must {

    Cluster(system).manager ! Join(Cluster(system).selfMember.address)

    "start persistent actor" in {
      ClusterSharding(system).start(ShardedEntity(
        typeKey,
        ctx ⇒ persistentEntity(ctx.entityId),
        StopPlz
      ))

      val p = TestProbe[String]()

      val ref = ClusterSharding(system).entityRefFor(typeKey, "123")
      ref ! Add("a")
      ref ! Add("b")
      ref ! Add("c")
      ref ! Get(p.ref)
      p.expectMessage("123:a|b|c")
    }
  }
}
