package com.lenis0012.bukkit.npc;

import net.minecraft.server.v1_7_R3.Packet;
import net.minecraft.server.v1_7_R3.PacketPlayOutAnimation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Class that contains all api functions from npc's in NPCFactory
 * 
 * @author lenis0012
 */
public class NPC {
	private NPCEntity entity;
	private int tick = 10;
	private NPCPath path;
	
	public NPC(NPCEntity entity) {
		this.entity = entity;
	}
	
	public Player getBukkitEntity() {
		return entity.getBukkitEntity();
	}
	
	public boolean isInvulnerable() {
		return entity.isInvulnerable();
	}
	
	public void setInvulnerable(boolean invulnerable) {
		entity.setInvulnerable(invulnerable);
	}
	
	public boolean pathfindTo(Location location) {
		return pathfindTo(location, 0.4);
	}
	
	public boolean pathfindTo(Location location, double speed) {
		return pathfindTo(location, speed, 30.0D);
	}
	
	public boolean pathfindTo(Location location, double speed, double range) {
		NPCPath path = NPCPath.find(entity, location, range, speed);
		return (this.path = path) != null;
	}
	
	protected void onTick() {
		if(target != null && path == null) {
			if(target.isDead()) {
				this.target = null;
			} if(entity.getBukkitEntity().getLocation().getWorld().equals(target.getWorld()) && entity.getBukkitEntity().getLocation().distanceSquared(target.getLocation()) <= 32 * 32) {
				lookAt(target.getLocation());
			}
		} if(tick-- <= 0) {
			this.tick = 10;
			if(path != null && !path.update()) {
				this.path = null;
			}
		}
	}
	
	/**
	 * Direction related methods
	 */
	private LivingEntity target;
	
	public void setTarget(LivingEntity target) {
		this.target = target;
		lookAt(target.getLocation());
	}
	
	public LivingEntity getTarget() {
		return target;
	}
	
	public void lookAt(Location location) {
		setYaw(getAngle(new Vector(entity.locX, 0, entity.locZ), location.toVector()));
	}
	
	public void setYaw(float yaw) {
		entity.yaw = yaw;
		entity.aP = yaw;
		entity.aO = yaw;
	}
	
	private final float getAngle(Vector point1, Vector point2) {
		double dx = point2.getX() - point1.getX();
		double dz = point2.getZ() - point1.getZ();
		float angle = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
		if(angle < 0) { angle += 360.0F; }
		return angle;
	}
	
	/**
	 * Packet related methods
	 */
	public void playAnimation(NPCAnimation animcation) {
		broadcastPacket(new PacketPlayOutAnimation(entity, animcation.getId()));
	}
	
	private final int RADIUS = Bukkit.getViewDistance() * 16;
	
	private final void broadcastPacket(Packet packet) {
		for(Player p : entity.getBukkitEntity().getWorld().getPlayers()) {
			if(entity.getBukkitEntity().getLocation().distanceSquared(p.getLocation()) <= RADIUS * RADIUS) {
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
			}
		}
	}
}