package de.srendi.advancedperipherals.common.addons.sable;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import de.srendi.advancedperipherals.common.addons.computercraft.operations.SphereOperationContext;
import de.srendi.advancedperipherals.common.addons.computercraft.owner.IPeripheralOwner;
import de.srendi.advancedperipherals.common.addons.computercraft.owner.OperationAbility;
import de.srendi.advancedperipherals.common.addons.computercraft.owner.PeripheralOwnerAbility;
import de.srendi.advancedperipherals.lib.peripherals.IPeripheralOperation;
import de.srendi.advancedperipherals.lib.peripherals.IPeripheralPlugin;

import java.util.Map;

import static de.srendi.advancedperipherals.common.addons.computercraft.operations.SphereOperation.SCAN_SHIPS;

public class ShipScannerPlugin implements IPeripheralPlugin {

    private final IPeripheralOwner owner;

    public ShipScannerPlugin(IPeripheralOwner owner) {
        this.owner = owner;
    }

    @Override
    public IPeripheralOperation<?>[] getOperations() {
        return new IPeripheralOperation[]{SCAN_SHIPS};
    }

    @LuaFunction(mainThread = true)
    public final MethodResult scanShips(int radius) throws LuaException {
        OperationAbility operationAbility = owner.getAbility(PeripheralOwnerAbility.OPERATION);
        if (operationAbility == null)
            throw new IllegalArgumentException("This shouldn't happen at all");
        return operationAbility.performOperation(SCAN_SHIPS, new SphereOperationContext(radius), context -> {
            if (radius > SCAN_SHIPS.getMaxCostRadius())
                return MethodResult.of(null, "Radius exceeds max value");
            return null;
        }, context -> {
            return MethodResult.of(SableHelper.scanSubLevels(owner.getLevel(), SableHelper.toGlobalVec3(owner.getLevel(), owner.getPos()), radius));
        }, null, null);
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Object> getShip() throws LuaException {
        Map<String, Object> ship = SableHelper.getContainingSubLevel(owner.getLevel(), owner.getPos(), SableHelper.toGlobalVec3(owner.getLevel(), owner.getPos()));
        if (ship == null)
            throw new LuaException("There is no ship here");
        return ship;
    }

    @LuaFunction
    public final MethodResult scanShipCost(int radius) {
        int estimatedCost = estimateShipCost(radius);
        if (estimatedCost < 0)
            return MethodResult.of(null, "Radius exceeds max value");
        return MethodResult.of(estimatedCost);
    }

    private static int estimateShipCost(int radius) {
        if (radius <= SCAN_SHIPS.getMaxFreeRadius()) {
            return 0;
        }
        if (radius > SCAN_SHIPS.getMaxCostRadius()) {
            return -1;
        }
        return SCAN_SHIPS.getCost(SphereOperationContext.of(radius));
    }
}
