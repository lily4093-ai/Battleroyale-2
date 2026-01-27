package com.battleroyale.supply;

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
    
    // 총기 티어별 가중치
    private static final int COMMON_WEIGHT = 50;
    private static final int UNCOMMON_WEIGHT = 30;
    private static final int RARE_WEIGHT = 15;
    private static final int EPIC_WEIGHT = 4;
    private static final int LEGENDARY_WEIGHT = 1;
    private static final int TOTAL_GUN_WEIGHT = COMMON_WEIGHT + UNCOMMON_WEIGHT + RARE_WEIGHT + EPIC_WEIGHT + LEGENDARY_WEIGHT;
    
    /**
     * 보급 상자에 루팅 생성
     */
    public void generateLoot(Inventory inventory) {
        inventory.clear();
        
        String selectedGunAmmo = null;
        
        // 1. 총기 (35% 확률)
        if (random.nextDouble() < 0.35) {
            GunData gun = selectRandomGun();
            if (gun != null) {
                inventory.setItem(getRandomSlot(inventory), gun.itemStack);
                selectedGunAmmo = gun.ammoType;
            }
        }
        
        // 2. 부착물 (슬롯당 8% 확률, 최대 2개)
        int attachmentCount = 0;
        for (int i = 0; i < 27 && attachmentCount < 2; i++) {
            if (random.nextDouble() < 0.08) {
                ItemStack attachment = getRandomAttachment();
                if (attachment != null) {
                    inventory.setItem(getRandomSlot(inventory), attachment);
                    attachmentCount++;
                }
            }
        }
        
        // 3. 음식 (슬롯당 15% 확률, 최대 3개)
        int foodCount = 0;
        for (int i = 0; i < 27 && foodCount < 3; i++) {
            if (random.nextDouble() < 0.15) {
                ItemStack food = getRandomFood();
                if (food != null) {
                    inventory.setItem(getRandomSlot(inventory), food);
                    foodCount++;
                }
            }
        }
        
        // 4. 장비 (슬롯당 10% 확률, 최대 2개)
        int equipmentCount = 0;
        for (int i = 0; i < 27 && equipmentCount < 2; i++) {
            if (random.nextDouble() < 0.10) {
                ItemStack equipment = getRandomEquipment();
                if (equipment != null) {
                    inventory.setItem(getRandomSlot(inventory), equipment);
                    equipmentCount++;
                }
            }
        }
        
        // 5. 조약돌 (25% 확률, 16개)
        if (random.nextDouble() < 0.25) {
            inventory.setItem(getRandomSlot(inventory), new ItemStack(Material.COBBLESTONE, 16));
        }
        
        // 6. 탄약 (빈 칸당 6% 확률)
        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null && random.nextDouble() < 0.06) {
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
        int roll = random.nextInt(TOTAL_GUN_WEIGHT);
        
        if (roll < LEGENDARY_WEIGHT) {
            return getRandomLegendaryGun();
        } else if (roll < LEGENDARY_WEIGHT + EPIC_WEIGHT) {
            return getRandomEpicGun();
        } else if (roll < LEGENDARY_WEIGHT + EPIC_WEIGHT + RARE_WEIGHT) {
            return getRandomRareGun();
        } else if (roll < LEGENDARY_WEIGHT + EPIC_WEIGHT + RARE_WEIGHT + UNCOMMON_WEIGHT) {
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
        // Note: Paper 플러그인에서는 NBT를 직접 다루기 어려우므로
        // 실제로는 TACZ API를 사용하거나 NMS/Reflection을 사용해야 합니다
        // 여기서는 기본 구조만 제공합니다
        ItemStack gun = new ItemStack(Material.IRON_HOE); // TACZ 총기는 보통 커스텀 아이템
        // TODO: TACZ API를 사용하여 실제 총기 데이터 설정
        // gun.setItemMeta() 등으로 NBT 데이터 설정 필요
        return gun;
    }
    
    /**
     * Heat가 있는 TACZ 총기 (미니건용)
     */
    private ItemStack createTaczGunWithHeat(String gunId, int ammo, String fireMode, float heat) {
        ItemStack gun = createTaczGun(gunId, ammo, fireMode);
        // TODO: HeatAmount NBT 추가
        return gun;
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
        ItemStack attachment = new ItemStack(Material.IRON_NUGGET);
        // TODO: TACZ API로 부착물 데이터 설정
        return attachment;
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
        
        ItemStack ammo = new ItemStack(Material.ARROW, amount); // TACZ 탄약은 커스텀 아이템
        // TODO: TACZ API로 탄약 데이터 설정
        return ammo;
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
