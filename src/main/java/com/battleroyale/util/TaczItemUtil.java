package com.battleroyale.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;

/**
 * TACZ 아이템 NBT 데이터 유틸리티
 * Arclight 서버 환경에서 Forge NBT API를 직접 사용
 * 
 * SUPPLY_DROP_CONFIG.md 형식:
 * - 총기:
 * {id:"tacz:modern_kinetic_gun",tag:{GunCurrentAmmoCount:17,GunFireMode:"SEMI",GunId:"tacz:glock_17",HasBulletInBarrel:1b}}
 * - 부착물: {id:"tacz:attachment",tag:{AttachmentId:"tacz:sight_sro_dot"}}
 * - 탄약: {id:"tacz:ammo",tag:{AmmoId:"tacz:9mm"}}
 */
public class TaczItemUtil {

    /**
     * TACZ 총기 아이템 생성
     * 
     * @param gunId       총기 ID (예: "tacz:glock_17")
     * @param currentAmmo 현재 탄약 수
     * @param fireMode    발사 모드 ("SEMI", "AUTO", "BURST")
     * @return TACZ 총기 ItemStack
     */
    public static org.bukkit.inventory.ItemStack createGun(String gunId, int currentAmmo, String fireMode) {
        return createGun(gunId, currentAmmo, fireMode, 0.0f);
    }

    /**
     * Heat가 있는 TACZ 총기 아이템 생성 (미니건용)
     * 
     * @param gunId       총기 ID
     * @param currentAmmo 현재 탄약 수
     * @param fireMode    발사 모드
     * @param heatAmount  열량
     * @return TACZ 총기 ItemStack
     */
    public static org.bukkit.inventory.ItemStack createGun(String gunId, int currentAmmo, String fireMode,
            float heatAmount) {
        try {
            // TACZ modern_kinetic_gun 아이템 생성
            ResourceLocation itemId = new ResourceLocation("tacz", "modern_kinetic_gun");
            net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.get(itemId);

            if (item == null || item == net.minecraft.world.item.Items.AIR) {
                org.bukkit.Bukkit.getLogger().warning("[BattleRoyale] TACZ 아이템을 찾을 수 없습니다: " + itemId);
                return new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR);
            }

            ItemStack nmsStack = new ItemStack(item, 1);
            CompoundTag tag = nmsStack.getOrCreateTag();

            // NBT 태그 설정
            tag.putInt("GunCurrentAmmoCount", currentAmmo);
            tag.putString("GunFireMode", fireMode);
            tag.putString("GunId", gunId);
            tag.putByte("HasBulletInBarrel", (byte) 1);

            // Heat (미니건 등)
            if (heatAmount > 0) {
                tag.putFloat("HeatAmount", heatAmount);
            }

            // NMS ItemStack을 Bukkit ItemStack으로 변환
            return CraftItemStack.asBukkitCopy(nmsStack);

        } catch (Exception e) {
            org.bukkit.Bukkit.getLogger().warning("[BattleRoyale] TACZ 총기 생성 실패: " + gunId);
            e.printStackTrace();
            return new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR);
        }
    }

    /**
     * TACZ 부착물 아이템 생성
     * 
     * @param attachmentId 부착물 ID (예: "tacz:sight_sro_dot")
     * @return TACZ 부착물 ItemStack
     */
    public static org.bukkit.inventory.ItemStack createAttachment(String attachmentId) {
        try {
            // TACZ attachment 아이템 생성
            ResourceLocation itemId = new ResourceLocation("tacz", "attachment");
            net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.get(itemId);

            if (item == null || item == net.minecraft.world.item.Items.AIR) {
                org.bukkit.Bukkit.getLogger().warning("[BattleRoyale] TACZ 아이템을 찾을 수 없습니다: " + itemId);
                return new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR);
            }

            ItemStack nmsStack = new ItemStack(item, 1);
            CompoundTag tag = nmsStack.getOrCreateTag();

            // NBT 태그 설정
            tag.putString("AttachmentId", attachmentId);

            // NMS ItemStack을 Bukkit ItemStack으로 변환
            return CraftItemStack.asBukkitCopy(nmsStack);

        } catch (Exception e) {
            org.bukkit.Bukkit.getLogger().warning("[BattleRoyale] TACZ 부착물 생성 실패: " + attachmentId);
            e.printStackTrace();
            return new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR);
        }
    }

    /**
     * TACZ 탄약 아이템 생성
     * 
     * @param ammoId 탄약 ID (예: "tacz:ammo_9mm")
     * @param amount 개수
     * @return TACZ 탄약 ItemStack
     */
    public static org.bukkit.inventory.ItemStack createAmmo(String ammoId, int amount) {
        try {
            // TACZ ammo 아이템 생성
            ResourceLocation itemId = new ResourceLocation("tacz", "ammo");
            net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.get(itemId);

            if (item == null || item == net.minecraft.world.item.Items.AIR) {
                org.bukkit.Bukkit.getLogger().warning("[BattleRoyale] TACZ 아이템을 찾을 수 없습니다: " + itemId);
                return new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR);
            }

            ItemStack nmsStack = new ItemStack(item, amount);
            CompoundTag tag = nmsStack.getOrCreateTag();

            // NBT 태그 설정
            tag.putString("AmmoId", ammoId);

            // NMS ItemStack을 Bukkit ItemStack으로 변환
            return CraftItemStack.asBukkitCopy(nmsStack);

        } catch (Exception e) {
            org.bukkit.Bukkit.getLogger().warning("[BattleRoyale] TACZ 탄약 생성 실패: " + ammoId);
            e.printStackTrace();
            return new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR);
        }
    }
}
