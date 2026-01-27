package com.battleroyale.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

/**
 * TACZ 아이템 NBT 데이터 유틸리티
 * PersistentDataContainer를 사용하여 NBT 데이터 설정
 */
public class TaczItemUtil {
    
    private static final NamespacedKey GUN_ID_KEY = new NamespacedKey("tacz", "gun_id");
    private static final NamespacedKey GUN_AMMO_KEY = new NamespacedKey("tacz", "current_ammo");
    private static final NamespacedKey GUN_FIRE_MODE_KEY = new NamespacedKey("tacz", "fire_mode");
    private static final NamespacedKey GUN_BULLET_IN_BARREL_KEY = new NamespacedKey("tacz", "bullet_in_barrel");
    private static final NamespacedKey GUN_HEAT_KEY = new NamespacedKey("tacz", "heat_amount");
    
    private static final NamespacedKey ATTACHMENT_ID_KEY = new NamespacedKey("tacz", "attachment_id");
    private static final NamespacedKey AMMO_ID_KEY = new NamespacedKey("tacz", "ammo_id");
    
    /**
     * TACZ 총기 아이템 생성
     * 
     * @param gunId 총기 ID (예: "tacz:glock_17")
     * @param currentAmmo 현재 탄약 수
     * @param fireMode 발사 모드 ("SEMI", "AUTO", "BURST")
     * @return TACZ 총기 ItemStack
     */
    public static ItemStack createGun(String gunId, int currentAmmo, String fireMode) {
        return createGun(gunId, currentAmmo, fireMode, 0.0f);
    }
    
    /**
     * Heat가 있는 TACZ 총기 아이템 생성 (미니건용)
     * 
     * @param gunId 총기 ID
     * @param currentAmmo 현재 탄약 수
     * @param fireMode 발사 모드
     * @param heatAmount 열량
     * @return TACZ 총기 ItemStack
     */
    public static ItemStack createGun(String gunId, int currentAmmo, String fireMode, float heatAmount) {
        // TACZ 총기는 일반적으로 Material.IRON_HOE 또는 커스텀 아이템을 사용
        // 실제 TACZ 모드에서는 CustomModelData를 사용하여 모델을 구분
        ItemStack gun = new ItemStack(Material.IRON_HOE);
        ItemMeta meta = gun.getItemMeta();
        
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            
            // 총기 ID 설정
            container.set(GUN_ID_KEY, PersistentDataType.STRING, gunId);
            
            // 현재 탄약 수
            container.set(GUN_AMMO_KEY, PersistentDataType.INTEGER, currentAmmo);
            
            // 발사 모드
            container.set(GUN_FIRE_MODE_KEY, PersistentDataType.STRING, fireMode);
            
            // 총열에 탄환 있음
            container.set(GUN_BULLET_IN_BARREL_KEY, PersistentDataType.BYTE, (byte) 1);
            
            // Heat (미니건 등)
            if (heatAmount > 0) {
                container.set(GUN_HEAT_KEY, PersistentDataType.FLOAT, heatAmount);
            }
            
            // CustomModelData 설정 (TACZ는 이를 통해 모델 구분)
            int customModelData = getCustomModelDataForGun(gunId);
            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }
            
            // 아이템 이름 설정
            meta.setDisplayName("§f" + getGunDisplayName(gunId));
            
            gun.setItemMeta(meta);
        }
        
        return gun;
    }
    
    /**
     * TACZ 부착물 아이템 생성
     * 
     * @param attachmentId 부착물 ID (예: "tacz:sight_sro_dot")
     * @return TACZ 부착물 ItemStack
     */
    public static ItemStack createAttachment(String attachmentId) {
        ItemStack attachment = new ItemStack(Material.IRON_NUGGET);
        ItemMeta meta = attachment.getItemMeta();
        
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            
            // 부착물 ID 설정
            container.set(ATTACHMENT_ID_KEY, PersistentDataType.STRING, attachmentId);
            
            // CustomModelData 설정
            int customModelData = getCustomModelDataForAttachment(attachmentId);
            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }
            
            // 아이템 이름 설정
            meta.setDisplayName("§f" + getAttachmentDisplayName(attachmentId));
            
            attachment.setItemMeta(meta);
        }
        
        return attachment;
    }
    
    /**
     * TACZ 탄약 아이템 생성
     * 
     * @param ammoId 탄약 ID (예: "tacz:ammo_9mm")
     * @param amount 개수
     * @return TACZ 탄약 ItemStack
     */
    public static ItemStack createAmmo(String ammoId, int amount) {
        ItemStack ammo = new ItemStack(Material.ARROW, amount);
        ItemMeta meta = ammo.getItemMeta();
        
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            
            // 탄약 ID 설정
            container.set(AMMO_ID_KEY, PersistentDataType.STRING, ammoId);
            
            // CustomModelData 설정
            int customModelData = getCustomModelDataForAmmo(ammoId);
            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }
            
            // 아이템 이름 설정
            meta.setDisplayName("§f" + getAmmoDisplayName(ammoId));
            
            ammo.setItemMeta(meta);
        }
        
        return ammo;
    }
    
    /**
     * 총기 ID로부터 CustomModelData 가져오기
     * 실제 TACZ 모드의 CustomModelData 값으로 교체 필요
     */
    private static int getCustomModelDataForGun(String gunId) {
        // TODO: 실제 TACZ 모드의 CustomModelData 매핑
        // 예시: gunId.hashCode() % 10000 등으로 임시 처리
        return Math.abs(gunId.hashCode() % 10000) + 1000;
    }
    
    /**
     * 부착물 ID로부터 CustomModelData 가져오기
     */
    private static int getCustomModelDataForAttachment(String attachmentId) {
        return Math.abs(attachmentId.hashCode() % 10000) + 20000;
    }
    
    /**
     * 탄약 ID로부터 CustomModelData 가져오기
     */
    private static int getCustomModelDataForAmmo(String ammoId) {
        return Math.abs(ammoId.hashCode() % 10000) + 30000;
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
