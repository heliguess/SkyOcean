package me.owdding.skyocean.features.misc

import me.owdding.ktmodules.Module
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.rendering.RenderUtils.renderCylinder
import net.minecraft.util.ARGB
import net.minecraft.world.entity.decoration.ArmorStand
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyNonGuest
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.entity.EntityEquipmentUpdateEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.extentions.getHelmet
import tech.thatgravyboat.skyblockapi.utils.extentions.getTexture
import tech.thatgravyboat.skyblockapi.utils.text.Text


@Module
object MinionCrystalOverlay {

    private val ORB_PROFILES: Map<String, Pair<Float, Int>> = mapOf(
         /* Farm */"ewogICJ0aW1lc3RhbXAiIDogMTcxOTQyMjU2NTE0MywKICAicHJvZmlsZUlkIiA6ICI1ODc5MjNlNDkxMzM0ZDMzYWE4ZjQ3ZWJkZTljOTc3MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJFbGV2ZW5mb3VyMTAiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2MwMWNmOWI2NWQ0NTE4ZjU4MTU4MmEwYmE3MjA5NzNjNDg3ZjFkZTIxMDgwZmI2OTdjMWE1YzE1NDM5YzM3MyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9" to (8.5f to ARGB.color(100, 255, 246, 156)),
        /* Mithril */"eyJ0aW1lc3RhbXAiOjE1NTczMzMxMjcyNDQsInByb2ZpbGVJZCI6IjkxZjA0ZmU5MGYzNjQzYjU4ZjIwZTMzNzVmODZkMzllIiwicHJvZmlsZU5hbWUiOiJTdG9ybVN0b3JteSIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTVlOGI4OTk3ODQ4MTJlZTNlNjUwNjEzN2ZiOGRiNjkyOTY1NGVhOGEzNThmMzlmZDdkZWJjNzk4MzljYjMxZCJ9fX0=" to (40.5f to ARGB.color(100, 107, 177, 165)),
        /* Wood */"ewogICJ0aW1lc3RhbXAiIDogMTU5MjA4Mzk1NTQyNSwKICAicHJvZmlsZUlkIiA6ICI2MTI4MTA4MjU5M2Q0OGQ2OWIzMmI3YjlkMzIxMGUxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJuaWNyb25pYzcyMTk2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQ4Njg4Y2NmNWNjYjRlMTVjODkxNWFiMWM5MTk4YzNkNTdkOWM0NjcxZjhjMGEwY2MwYzlmNWFlN2IyNzM5YjQiCiAgICB9CiAgfQp9" to (12.5f to ARGB.color(100, 105, 84, 51)),
        )

    private val detectedOrbs = mutableMapOf<ArmorStand, Pair<Float, Int>>()
    private val lockedY = mutableMapOf<ArmorStand, Double>()

    @Subscription
    @OnlyNonGuest
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    fun onEntityEquipmentUpdateEvent(event: EntityEquipmentUpdateEvent) {
        val armorStand = event.entity as? ArmorStand ?: return
        val helmet = armorStand.getHelmet()

        if (helmet.isEmpty) return;

        val texture = helmet.getTexture() ?: return
        val standInfo = ORB_PROFILES[texture] ?: return

        if (detectedOrbs.containsKey(armorStand)) return

        detectedOrbs[armorStand] = standInfo
        lockedY[armorStand] = armorStand.position().y

        Text.of("Spotted orb (r=${standInfo.first})").sendWithPrefix()
    }

    @Subscription
    @OnlyNonGuest
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    fun onRenderWorldEvent(event: RenderWorldEvent) {
        if (detectedOrbs.isEmpty()) return

        val iterator = detectedOrbs.iterator()

        while (iterator.hasNext()){
            val (armorStand, standInfo) = iterator.next()

            if (!armorStand.isAlive){
                iterator.remove()
                lockedY.remove(armorStand)
                continue
            }

            val (radius, color) = standInfo

            val pos = armorStand.position()
            val y = lockedY[armorStand] ?: pos.y
            event.renderCylinder(
                pos.x.toFloat(), y.toFloat(), pos.z.toFloat(),
                radius,
                2.0F,
                color,
            )
        }
    }

    @OnlyOnSkyBlock
    @Subscription(ServerChangeEvent::class)
    fun onServerChange(){
        detectedOrbs.clear()
    }

}
