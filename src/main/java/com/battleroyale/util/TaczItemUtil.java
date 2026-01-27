package com.battleroyale.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

/**
 * TACZ 아이템 생성 유틸리티
 * 
 * Arclight 서버 환경에서 Reflection을 사용하여 NBT 데이터를 직접 설정합니다.
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
    public static ItemStack createGun(String gunId, int currentAmmo, String fireMode) {
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
    public static ItemStack createGun(String gunId, int currentAmmo, String fireMode, float heatAmount) {
        try {
            // NBT 문자열 생성
            StringBuilder nbtString = new StringBuilder();
            nbtString.append("{id:\"tacz:modern_kinetic_gun\",Count:1b,tag:{");
            nbtString.append("GunCurrentAmmoCount:").append(currentAmmo).append(",");
            nbtString.append("GunFireMode:\"").append(fireMode).append("\",");
            nbtString.append("GunId:\"").append(gunId).append("\",");
            nbtString.append("HasBulletInBarrel:1b");

            if (heatAmount > 0) {
                nbtString.append(",HeatAmount:").append(heatAmount).append("f");
            }

            nbtString.append("}}");

            // NBT 문자열로부터 ItemStack 생성
            ItemStack item = createItemFromNBT(nbtString.toString());

            if (item != null && item.getType() != Material.AIR) {
                Bukkit.getLogger().info("[BattleRoyale] TACZ 총기 생성 성공: " + gunId);
                return item;
            }

            Bukkit.getLogger().warning("[BattleRoyale] TACZ 총기 생성 실패, 플레이스홀더 반환: " + gunId);
            return createPlaceholderGun(gunId, currentAmmo, fireMode);

        } catch (Exception e) {
            Bukkit.getLogger().warning("[BattleRoyale] TACZ 총기 생성 중 오류: " + gunId);
            e.printStackTrace();
            return createPlaceholderGun(gunId, currentAmmo, fireMode);
        }
    }

    /**
     * TACZ 부착물 아이템 생성
     * 
     * @param attachmentId 부착물 ID (예: "tacz:sight_sro_dot")
     * @return TACZ 부착물 ItemStack
     */
    public static ItemStack createAttachment(String attachmentId) {
        try {
            // NBT 문자열 생성
            String nbtString = "{id:\"tacz:attachment\",Count:1b,tag:{AttachmentId:\"" + attachmentId + "\"}}";

            // NBT 문자열로부터 ItemStack 생성
            ItemStack item = createItemFromNBT(nbtString);

            if (item != null && item.getType() != Material.AIR) {
                return item;
            }

            return createPlaceholderAttachment(attachmentId);

        } catch (Exception e) {
            Bukkit.getLogger().warning("[BattleRoyale] TACZ 부착물 생성 중 오류: " + attachmentId);
            e.printStackTrace();
            return createPlaceholderAttachment(attachmentId);
        }
    }

    /**
     * TACZ 탄약 아이템 생성
     * 
     * @param ammoId 탄약 ID (예: "tacz:ammo_9mm")
     * @param amount 개수
     * @return TACZ 탄약 ItemStack
     */
    public static ItemStack createAmmo(String ammoId, int amount) {
        try {
            // NBT 문자열 생성
            String nbtString = "{id:\"tacz:ammo\",Count:" + amount + "b,tag:{AmmoId:\"" + ammoId + "\"}}";

            // NBT 문자열로부터 ItemStack 생성
            ItemStack item = createItemFromNBT(nbtString);

            if (item != null && item.getType() != Material.AIR) {
                return item;
            }

            return createPlaceholderAmmo(ammoId, amount);

        } catch (Exception e) {
            Bukkit.getLogger().warning("[BattleRoyale] TACZ 탄약 생성 중 오류: " + ammoId);
            e.printStackTrace();
            return createPlaceholderAmmo(ammoId, amount);
        }
    }

    /**
     * NBT 문자열로부터 ItemStack 생성 (Reflection 사용)
     */
    private static ItemStack createItemFromNBT(String nbtString) {
        try {
            // CraftItemStack 클래스 가져오기
            Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack");

            // MojangsonParser 클래스 가져오기 (NBT 문자열 파싱)
            Class<?> mojangsonParserClass = Class.forName("net.minecraft.nbt.MojangsonParser");
            Method parseMethod = mojangsonParserClass.getMethod("parse", String.class);

            // NBT 문자열을 CompoundTag로 파싱
            Object compoundTag = parseMethod.invoke(null, nbtString);

            // NMS ItemStack 클래스 가져오기
            Class<?> nmsItemStackClass = Class.forName("net.minecraft.world.item.ItemStack");

            // CompoundTag로부터 NMS ItemStack 생성
            Method ofMethod = nmsItemStackClass.getMethod("of", Class.forName("net.minecraft.nbt.CompoundTag"));
            Object nmsStack = ofMethod.invoke(null, compoundTag);

            // NMS ItemStack을 Bukkit ItemStack으로 변환
            Method asBukkitCopyMethod = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
            ItemStack bukkitStack = (ItemStack) asBukkitCopyMethod.invoke(null, nmsStack);

            return bukkitStack;

        } catch (Exception e) {
            Bukkit.getLogger().warning("[BattleRoyale] NBT 문자열로부터 아이템 생성 실패");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 플레이스홀더 총기 생성
     */
    private static ItemStack createPlaceholderGun(String gunId, int currentAmmo, String fireMode) {
        ItemStack gun = new ItemStack(Material.IRON_HOE);
        org.bukkit.inventory.meta.ItemMeta meta = gun.getItemMeta();

        if (meta != null) {
            String displayName = getGunDisplayName(gunId);
            meta.setDisplayName("§f" + displayName);
            meta.setLore(java.util.Arrays.asList(
                    "§7총기: §e" + gunId,
                    "§7탄약: §e" + currentAmmo,
                    "§7발사 모드: §e" + fireMode,
                    "§c[플레이스홀더 - TACZ 로드 필요]"));
            gun.setItemMeta(meta);
        }

        return gun;
    }

    /**
     * 플레이스홀더 부착물 생성
     */
    private static ItemStack createPlaceholderAttachment(String attachmentId) {
        ItemStack attachment = new ItemStack(Material.IRON_NUGGET);
        org.bukkit.inventory.meta.ItemMeta meta = attachment.getItemMeta();

        if (meta != null) {
            String displayName = getAttachmentDisplayName(attachmentId);
            meta.setDisplayName("§f" + displayName);
            meta.setLore(java.util.Arrays.asList(
                    "§7부착물: §e" + attachmentId,
                    "§c[플레이스홀더 - TACZ 로드 필요]"));
            attachment.setItemMeta(meta);
        }

        return attachment;
    }

    /**
     * 플레이스홀더 탄약 생성
     */
    private static ItemStack createPlaceholderAmmo(String ammoId, int amount) {
        ItemStack ammo = new ItemStack(Material.ARROW, amount);
        org.bukkit.inventory.meta.ItemMeta meta = ammo.getItemMeta();

        if (meta != null) {
            String displayName = getAmmoDisplayName(ammoId);
            meta.setDisplayName("§f" + displayName);
            meta.setLore(java.util.Arrays.asList(
                    "§7탄약: §e" + ammoId,
                    "§c[플레이스홀더 - TACZ 로드 필요]"));
            ammo.setItemMeta(meta);
        }

        return ammo;
    }

    /**
     * 총기 표시 이름 가져오기
     */
    private static String getGunDisplayName(String gunId) {
        String name = gunId.replace("tacz:", "").replace("_", " ");
        return capitalizeWords(name);
    }

    /**
     * 부착물 표시 이름 가져오기
     */
    private static String getAttachmentDisplayName(String attachmentId) {
        String name = attachmentId.replace("tacz:", "").replace("_", " ");
        return capitalizeWords(name);
    }

    /**
     * 탄약 표시 이름 가져오기
     */
    private static String getAmmoDisplayName(String ammoId) {
        String name = ammoId.replace("tacz:ammo_", "").replace("_", " ").toUpperCase();
        return name + " Ammo";
    }

    /**
     * 단어 첫 글자 대문자화
     */
    private static String capitalizeWords(String str) {
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return result.toString().trim();
    }
}
