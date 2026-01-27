package com.battleroyale.supply;

import com.battleroyale.BattleRoyalePlugin;
import com.battleroyale.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * 보급품 루팅 생성기
 * SUPPLY_DROP_CONFIG.md의 확률과 규칙에 따라 아이템 생성
 */
public class SupplyLootGenerator {
    
    private final Random random = new Random();
    private final ConfigManager config;
    
    public SupplyLootGenerator(BattleRoyalePlugin plugin) {
        this.config = plugin.getConfigManager();
    }
    
    /**
     * 보급 상자에 루팅 생성
     */
    public void generateLoot(Inventory inventory) {
        inventory.clear();
        
        String selectedGunAmmo = null;
        
        // 1. 총기 (config에서 확률 읽기)
        if (random.nextDouble() < config.getSupplyGunChance()) {
            GunData gun = selectRandomGun();
            if (gun != null) {
                inventory.setItem(getRandomSlot(inventory), gun.itemStack);
                selectedGunAmmo = gun.ammoType;
            }
        }
        
        // 2. 부착물 (config에서 확률 및 최대값 읽기)
        int attachmentCount = 0;
        int maxAttachments = config.getSupplyAttachmentMax();
        double attachmentChance = config.getSupplyAttachmentChance();
        for (int i = 0; i < 27 && attachmentCount < maxAttachments; i++) {
            if (random.nextDouble() < attachmentChance) {
                ItemStack attachment = getRandomAttachment();
                if (attachment != null) {
                    inventory.setItem(getRandomSlot(inventory), attachment);
                    attachmentCount++;
                }
            }
        }
        
        // 3. 음식 (config에서 확률 및 최대값 읽기)
        int foodCount = 0;
        int maxFood = config.getSupplyFoodMax();
        double foodChance = config.getSupplyFoodChance();
        for (int i = 0; i < 27 && foodCount < maxFood; i++) {
            if (random.nextDouble() < foodChance) {
                ItemStack food = getRandomFood();
                if (food != null) {
                    inventory.setItem(getRandomSlot(inventory), food);
                    foodCount++;
                }
            }
        }
        
        // 4. 장비 (config에서 확률 및 최대값 읽기)
        int equipmentCount = 0;
        int maxEquipment = config.getSupplyEquipmentMax();
        double equipmentChance = config.getSupplyEquipmentChance();
        for (int i = 0; i < 27 && equipmentCount < maxEquipment; i++) {
            if (random.nextDouble() < equipmentChance) {
                ItemStack equipment = getRandomEquipment();
                if (equipment != null) {
                    inventory.setItem(getRandomSlot(inventory), equipment);
                    equipmentCount++;
                }
            }
        }
        
        // 5. 조약돌 (config에서 확률 및 개수 읽기)
        if (random.nextDouble() < config.getSupplyCobblestoneChance()) {
            int cobblestoneAmount = config.getSupplyCobblestoneAmount();
            inventory.setItem(getRandomSlot(inventory), new ItemStack(Material.COBBLESTONE, cobblestoneAmount));
        }
        
        // 6. 탄약 (config에서 확률 읽기)
        double ammoChance = config.getSupplyAmmoChance();
        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null && random.nextDouble() < ammoChance) {
                ItemStack ammo = getRandomAmmo(selectedGunAmmo);
                if (ammo != null) {
                    inventory.setItem(i, ammo);
                }
            }
        }
    }
    
    /**
     * 빈 슬롯 찾기
     */
    private int getRandomSlot(Inventory inventory) {
        List<Integer> emptySlots = new ArrayList<>();
        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
                emptySlots.add(i);
            }
        }
        
        if (emptySlots.isEmpty()) {
            return random.nextInt(27);
        }
        
        return emptySlots.get(random.nextInt(emptySlots.size()));
    }
    
    /**
     * 티어별 총기 선택
     */
    private GunData selectRandomGun() {
        int commonWeight = config.getGunTierCommon();
        int uncommonWeight = config.getGunTierUncommon();
        int rareWeight = config.getGunTierRare();
        int epicWeight = config.getGunTierEpic();
        int legendaryWeight = config.getGunTierLegendary();
        int totalWeight = commonWeight + uncommonWeight + rareWeight + epicWeight + legendaryWeight;
        
        int roll = random.nextInt(totalWeight);
        
        if (roll < legendaryWeight) {
            return getRandomLegendaryGun();
        } else if (roll < legendaryWeight + epicWeight) {
            return getRandomEpicGun();
        } else if (roll < legendaryWeight + epicWeight + rareWeight) {
            return getRandomRareGun();
        } else if (roll < legendaryWeight + epicWeight + rareWeight + uncommonWeight) {
            return getRandomUncommonGun();
        } else {
            return getRandomCommonGun();
        }
    }
    
    /**
     * Common 총기
     */
    private GunData getRandomCommonGun() {
        List<GunData> guns = Arrays.asList(
            new GunData(createTaczGun("tacz:glock_17", 17, "SEMI"), "9mm"),
            new GunData(createTaczGun("tacz:m1911", 7, "SEMI"), "45acp"),
            new GunData(createTaczGun("tacz:uzi", 12, "AUTO"), "9mm"),
            new GunData(createTaczGun("tacz:hk_mp5a5", 20, "AUTO"), "9mm"),
            new GunData(createTaczGun("tacz:db_long", 2, "SEMI"), "12g"),
            new GunData(createTaczGun("tacz:m870", 5, "SEMI"), "12g")
        );
        return guns.get(random.nextInt(guns.size()));
    }
    
    /**
     * Uncommon 총기
     */
    private GunData getRandomUncommonGun() {
        List<GunData> guns = Arrays.asList(
            new GunData(createTaczGun("tacz:scar_l", 30, "AUTO"), "556x45"),
            new GunData(createTaczGun("tacz:aug", 30, "AUTO"), "556x45"),
            new GunData(createTaczGun("tacz:m16a1", 20, "AUTO"), "556x45"),
            new GunData(createTaczGun("tacz:hk416d", 30, "AUTO"), "556x45"),
            new GunData(createTaczGun("tacz:ump45", 25, "AUTO"), "45acp"),
            new GunData(createTaczGun("tacz:vector45", 21, "AUTO"), "45acp"),
            new GunData(createTaczGun("tacz:spas_12", 5, "SEMI"), "12g"),
            new GunData(createTaczGun("tacz:aa12", 6, "SEMI"), "12g"),
            new GunData(createTaczGun("tacz:m1014", 6, "SEMI"), "12g")
        );
        return guns.get(random.nextInt(guns.size()));
    }
    
    /**
     * Rare 총기
     */
    private GunData getRandomRareGun() {
        List<GunData> guns = Arrays.asList(
            new GunData(createTaczGun("tacz:deagle", 7, "SEMI"), "50ae"),
            new GunData(createTaczGun("tacz:sks_tactical", 10, "SEMI"), "762x39"),
            new GunData(createTaczGun("tacz:qbz_191", 30, "AUTO"), "556x45"),
            new GunData(createTaczGun("tacz:ak47", 30, "AUTO"), "762x39"),
            new GunData(createTaczGun("tacz:m700", 5, "SEMI"), "30_06"),
            new GunData(createTaczGun("tacz:springfield1873", 1, "SEMI"), "30_06")
        );
        return guns.get(random.nextInt(guns.size()));
    }
    
    /**
     * Epic 총기
     */
    private GunData getRandomEpicGun() {
        List<GunData> guns = Arrays.asList(
            new GunData(createTaczGun("tacz:deagle_golden", 9, "SEMI"), "357mag"),
            new GunData(createTaczGun("tacz:timeless50", 6, "SEMI"), "50ae"),
            new GunData(createTaczGun("tacz:mk14", 10, "SEMI"), "762x39"),
            new GunData(createTaczGun("tacz:scar_h", 20, "SEMI"), "762x39"),
            new GunData(createTaczGun("tacz:fn_fal", 20, "SEMI"), "762x39"),
            new GunData(createTaczGun("tacz:ai_awp", 5, "SEMI"), "338"),
            new GunData(createTaczGun("tacz:rpk", 40, "AUTO"), "762x39"),
            new GunData(createTaczGun("tacz:fn_evolys", 30, "AUTO"), "556x45"),
            new GunData(createTaczGun("tacz:m249", 64, "AUTO"), "556x45")
        );
        return guns.get(random.nextInt(guns.size()));
    }
    
    /**
     * Legendary 총기
     */
    private GunData getRandomLegendaryGun() {
        List<GunData> guns = Arrays.asList(
            new GunData(createTaczGun("tacz:m107", 10, "SEMI"), "50bmg"),
            new GunData(createTaczGun("tacz:m95", 5, "SEMI"), "50bmg"),
            new GunData(createTaczGunWithHeat("tacz:minigun", 9999, "AUTO", 46.0f), "762x39"),
            new GunData(createTaczGun("tacz:rpg7", 1, "SEMI"), "rpg_rocket")
        );
        return guns.get(random.nextInt(guns.size()));
    }
    
    /**
     * TACZ 총기 아이템 생성
     */
    private ItemStack createTaczGun(String gunId, int ammo, String fireMode) {
        return com.battleroyale.util.TaczItemUtil.createGun(gunId, ammo, fireMode);
    }
    
    /**
     * Heat가 있는 TACZ 총기 (미니건용)
     */
    private ItemStack createTaczGunWithHeat(String gunId, int ammo, String fireMode, float heat) {
        return com.battleroyale.util.TaczItemUtil.createGun(gunId, ammo, fireMode, heat);
    }
    
    /**
     * 랜덤 부착물
     */
    private ItemStack getRandomAttachment() {
        List<String> attachments = Arrays.asList(
            "tacz:sight_sro_dot", "tacz:sight_srs_02", "tacz:sight_pk06_rifle", "tacz:sight_t2",
            "tacz:sight_552", "tacz:scope_hamr", "tacz:scope_lpvo_1_6", "tacz:scope_mk5hd",
            "tacz:muzzle_silencer_knight_qd", "tacz:muzzle_silencer_ursus", "tacz:muzzle_silencer_vulture",
            "tacz:muzzle_brake_cyclone_d2", "tacz:muzzle_brake_pioneer", "tacz:muzzle_brake_timeless50",
            "tacz:grip_se_5", "tacz:grip_osovets_black", "tacz:grip_td", "tacz:grip_vertical_military",
            "tacz:extended_mag_2", "tacz:extended_mag_3", "tacz:light_extended_mag_2",
            "tacz:laser_compact", "tacz:laser_lopro"
        );
        
        String attachmentId = attachments.get(random.nextInt(attachments.size()));
        return com.battleroyale.util.TaczItemUtil.createAttachment(attachmentId);
    }
    
    /**
     * 랜덤 음식
     */
    private ItemStack getRandomFood() {
        List<Material> foods = Arrays.asList(
            Material.COOKED_BEEF,
            Material.COOKED_PORKCHOP,
            Material.COOKED_CHICKEN,
            Material.GOLDEN_CARROT,
            Material.GOLDEN_APPLE
        );
        
        Material food = foods.get(random.nextInt(foods.size()));
        int amount = food == Material.GOLDEN_APPLE ? 1 : random.nextInt(4) + 1;
        return new ItemStack(food, amount);
    }
    
    /**
     * 랜덤 장비
     */
    private ItemStack getRandomEquipment() {
        boolean isDiamond = random.nextDouble() < 0.2; // 20% 확률로 다이아
        
        List<Material> ironEquipment = Arrays.asList(
            Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS,
            Material.IRON_BOOTS, Material.IRON_SWORD, Material.IRON_PICKAXE
        );
        
        List<Material> diamondEquipment = Arrays.asList(
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS, Material.DIAMOND_SWORD, Material.DIAMOND_PICKAXE
        );
        
        List<Material> equipment = isDiamond ? diamondEquipment : ironEquipment;
        return new ItemStack(equipment.get(random.nextInt(equipment.size())));
    }
    
    /**
     * 랜덤 탄약
     */
    private ItemStack getRandomAmmo(String preferredAmmo) {
        Map<String, String> ammoIdMap = new HashMap<>();
        ammoIdMap.put("50ae", "tacz:50ae");
        ammoIdMap.put("9mm", "tacz:9mm");
        ammoIdMap.put("357mag", "tacz:357mag");
        ammoIdMap.put("45acp", "tacz:45acp");
        ammoIdMap.put("556x45", "tacz:556x45");
        ammoIdMap.put("30_06", "tacz:30_06");
        ammoIdMap.put("338", "tacz:338");
        ammoIdMap.put("50bmg", "tacz:50bmg");
        ammoIdMap.put("762x39", "tacz:762x39");
        ammoIdMap.put("12g", "tacz:12g");
        ammoIdMap.put("rpg_rocket", "tacz:rpg_rocket");
        
        Map<String, Integer> ammoMaxCounts = new HashMap<>();
        ammoMaxCounts.put("50ae", 48);
        ammoMaxCounts.put("9mm", 60);
        ammoMaxCounts.put("357mag", 48);
        ammoMaxCounts.put("45acp", 60);
        ammoMaxCounts.put("556x45", 60);
        ammoMaxCounts.put("30_06", 36);
        ammoMaxCounts.put("338", 48);
        ammoMaxCounts.put("50bmg", 30);
        ammoMaxCounts.put("762x39", 60);
        ammoMaxCounts.put("12g", 36);
        ammoMaxCounts.put("rpg_rocket", 6);
        
        String ammoType;
        if (preferredAmmo != null && ammoMaxCounts.containsKey(preferredAmmo)) {
            ammoType = preferredAmmo;
        } else {
            List<String> ammoTypes = new ArrayList<>(ammoMaxCounts.keySet());
            ammoType = ammoTypes.get(random.nextInt(ammoTypes.size()));
        }
        
        int maxCount = ammoMaxCounts.get(ammoType);
        boolean fullSet = random.nextBoolean(); // 50% 1세트, 50% 반세트
        int amount = fullSet ? maxCount : maxCount / 2;
        
        String ammoId = ammoIdMap.get(ammoType);
        return com.battleroyale.util.TaczItemUtil.createAmmo(ammoId, amount);
    }
    
    /**
     * 총기 데이터 클래스
     */
    private static class GunData {
        ItemStack itemStack;
        String ammoType;
        
        GunData(ItemStack itemStack, String ammoType) {
            this.itemStack = itemStack;
            this.ammoType = ammoType;
        }
    }
}
