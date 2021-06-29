package pl.adabawolska.simpleeconomygamespringboot.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import pl.adabawolska.simpleeconomygamespringboot.building.entity.Building;
import pl.adabawolska.simpleeconomygamespringboot.building.properties.BuildingProperties;
import pl.adabawolska.simpleeconomygamespringboot.building.service.BuildingService;
import pl.adabawolska.simpleeconomygamespringboot.resource.entity.Resource;
import pl.adabawolska.simpleeconomygamespringboot.resource.service.ResourceService;
import pl.adabawolska.simpleeconomygamespringboot.unit.entity.Unit;
import pl.adabawolska.simpleeconomygamespringboot.unit.properties.UnitProperties;
import pl.adabawolska.simpleeconomygamespringboot.unit.service.UnitService;

import java.util.List;

@Configuration
@EnableScheduling
public class ScheduleConfig {
    BuildingService buildingService;
    ResourceService resourceService;
    UnitService unitService;
    UnitProperties unitProperties;
    BuildingProperties buildingProperties;

    @Autowired
    public ScheduleConfig(BuildingService buildingService, UnitService unitService,
                                   ResourceService resourceService, UnitProperties unitProperties,
                                   BuildingProperties buildingProperties) {
        this.buildingService = buildingService;
        this.unitService = unitService;
        this.resourceService = resourceService;
        this.unitProperties = unitProperties;
        this.buildingProperties = buildingProperties;
    }

    @Scheduled(fixedDelay = 1000)
    public void scheduleFixedDelayTask() {
        int scheduleDelayInSeconds = 1000/100;
        List<Building> allBuildings = buildingService.findAllBuildings();
        for (Building building: allBuildings) {
            Long userId = building.getUser().getId();
            Resource resource = resourceService.findResourceByUserId(userId);
            Unit unit = unitService.findUnitByUserId(userId);

            Long mud = resource.getMudQuantity();
            Long stone = resource.getStoneQuantity();
            Long meat = resource.getMeatQuantity();

            Long mudProd = building.getMudGatherersCottageQuantity() * buildingProperties.getMudGatherersCottageProd();
            resource.setMudQuantity(mud + mudProd);

            Long stoneProd = building.getStoneQuarryQuantity() * buildingProperties.getStoneQuarryProd();
            resource.setStoneQuantity(stone + stoneProd);

            Long meatCost = (unit.getGoblinArcherQuantity() * unitProperties.getGoblinArcherMeatCost()
                    + unit.getOrcWarriorQuantity() * unitProperties.getOrcWarriorMeatCost()
                    + unit.getUglyTrollQuantity() * unitProperties.getGoblinArcherMeatCost())
                    * scheduleDelayInSeconds;

            long meatProd = building.getHuntersHutQuantity() * buildingProperties.getHuntersHutProd();
            long meatSum = meat - meatCost + meatProd;
            meatSum = desertion(unit, meatSum);
            resource.setMeatQuantity(meatSum);

            resourceService.saveResource(resource);
        }

    }
    private long desertion(Unit unit, Long meatSum) {
        while (meatSum < 0) {
            if (unit.getUglyTrollQuantity() > 0) {
                unit.setUglyTrollQuantity(unit.getUglyTrollQuantity() - 1);
                meatSum += unitProperties.getUglyTrollMeatCost();
                continue;
            }
            if (unit.getOrcWarriorQuantity() > 0) {
                unit.setOrcWarriorQuantity(unit.getOrcWarriorQuantity() - 1);
                meatSum += unitProperties.getOrcWarriorMeatCost();
                continue;
            }

            unit.setGoblinArcherQuantity(unit.getGoblinArcherQuantity() - 1);
            meatSum += unitProperties.getGoblinArcherMeatCost();
        }
        unitService.saveUnit(unit);
        return meatSum;
    }
}
