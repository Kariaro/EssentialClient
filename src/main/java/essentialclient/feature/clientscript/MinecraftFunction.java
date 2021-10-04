package essentialclient.feature.clientscript;

import essentialclient.utils.interfaces.MinecraftClientInvoker;
import essentialclient.utils.inventory.InventoryUtils;
import me.senseiwells.arucas.throwables.Error;
import me.senseiwells.arucas.throwables.ErrorRuntime;
import me.senseiwells.arucas.throwables.ThrowStop;
import me.senseiwells.arucas.values.*;
import me.senseiwells.arucas.values.functions.BuiltInFunction;
import me.senseiwells.arucas.values.functions.FunctionDefinition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.ScreenshotUtils;
import net.minecraft.item.Item;
import net.minecraft.screen.*;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.LinkedList;
import java.util.List;

public class MinecraftFunction extends BuiltInFunction {

    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final String mustBeItem = "String must be item type, for example \"grass_block\" or \"diamond\"";

    public MinecraftFunction(String name, List<String> argumentNames, FunctionDefinition function) {
        super(name, argumentNames, function);
    }

    public MinecraftFunction(String name, String argument, FunctionDefinition function) {
        this(name, List.of(argument), function);
    }

    public MinecraftFunction(String name, FunctionDefinition function) {
        this(name, new LinkedList<>(), function);
    }

    public static void initialiseMinecraftFunctions() {
        new MinecraftFunction("use", "type", function -> {
            final String error = "Must pass \"hold\", 'stop\" or \"once\" into rightMouse()";
            StringValue stringValue = (StringValue) function.getValueForType(StringValue.class, 0, error);
            switch (stringValue.value.toLowerCase()) {
                case "hold" -> client.options.keyUse.setPressed(true);
                case "stop" -> client.options.keyUse.setPressed(false);
                case "once" -> ((MinecraftClientInvoker) client).rightClickMouseAccessor();
                default -> throw function.throwInvalidParameterError(error);
            }
            return new NullValue();
        });

        new MinecraftFunction("attack", "type", function -> {
            final String error = "Must pass \"hold\", 'stop\" or \"once\" into leftMouse()";
            StringValue stringValue = (StringValue) function.getValueForType(StringValue.class, 0, error);
            switch (stringValue.value.toLowerCase()) {
                case "hold" -> client.options.keyAttack.setPressed(true);
                case "stop" -> client.options.keyAttack.setPressed(false);
                case "once" -> ((MinecraftClientInvoker) client).leftClickMouseAccessor();
                default -> throw function.throwInvalidParameterError(error);
            }
            return new NullValue();
        });

        new MinecraftFunction("setSelectSlot", "slotNum", function -> {
            final String error = "Number must be between 1-9";
            NumberValue numberValue = (NumberValue) function.getValueForType(NumberValue.class, 0, error);
            if (numberValue.value < 1 || numberValue.value > 9)
                throw function.throwInvalidParameterError(error);
            assert client.player != null;
            client.player.inventory.selectedSlot = numberValue.value.intValue() - 1;
            return new NullValue();
        });

        new MinecraftFunction("say", "text", function -> {
            assert client.player != null;
            client.player.sendChatMessage(function.getValueFromTable(function.argumentNames.get(0)).value.toString());
            return new NullValue();
        });

        new MinecraftFunction("message", "text", function -> {
            assert client.player != null;
            client.player.sendMessage(new LiteralText(function.getValueFromTable(function.argumentNames.get(0)).value.toString()), false);
            return new NullValue();
        });

        new MinecraftFunction("inventory", "action", function -> {
            final String error = "String must be \"open\" or \"close\"";
            StringValue stringValue = (StringValue) function.getValueForType(StringValue.class, 0, error);
            assert client.player != null;
            switch (stringValue.value) {
                case "open" -> client.openScreen(new InventoryScreen(client.player));
                case "close" -> client.player.closeHandledScreen();
                default -> throw function.throwInvalidParameterError(error);
            }
            return new NullValue();
        });

        new MinecraftFunction("setWalking", "boolean", function -> setKey(function, client.options.keyForward));
        new MinecraftFunction("setSneaking", "boolean", function -> setKey(function, client.options.keySneak));

        new MinecraftFunction("setSprinting", "boolean", function -> {
            BooleanValue booleanValue = (BooleanValue) function.getValueForType(BooleanValue.class, 0, null);
            assert client.player != null;
            client.player.setSprinting(booleanValue.value);
            return new NullValue();
        });

        new MinecraftFunction("dropItemInHand", "boolean", function -> {
            BooleanValue booleanValue = (BooleanValue) function.getValueForType(BooleanValue.class, 0, null);
            assert client.player != null;
            client.player.dropSelectedItem(booleanValue.value);
            return new NullValue();
        });

        new MinecraftFunction("dropAll", "itemType", function -> {
            StringValue stringValue = (StringValue) function.getValueForType(StringValue.class, 0, mustBeItem);
            InventoryUtils.dropAllItemType(client.player, stringValue.value);
            return new NullValue();
        });

        new MinecraftFunction("tradeIndex", "index", function -> {
            NumberValue numberValue = (NumberValue) function.getValueForType(NumberValue.class, 0, null);
            BooleanValue booleanValue = (BooleanValue) function.getValueForType(BooleanValue.class, 1, null);
            InventoryUtils.tradeAllItems(client, numberValue.value.intValue(), booleanValue.value);
            return new NullValue();
        });

        new MinecraftFunction("tradeFor", "itemType", function -> {
            StringValue stringValue = (StringValue) function.getValueForType(StringValue.class, 0, mustBeItem);
            BooleanValue booleanValue = (BooleanValue) function.getValueForType(BooleanValue.class, 1, null);
            Item item = Registry.ITEM.get(new Identifier(stringValue.value));
            int index = InventoryUtils.getIndexOfItem(client, item);
            if (index == -1)
                throw new ErrorRuntime("Villager does not have that trade", function.startPos, function.endPos, function.context);
            InventoryUtils.tradeAllItems(client, index, booleanValue.value);
            return new NullValue();
        });

        new MinecraftFunction("screenshot", function -> {
            ScreenshotUtils.saveScreenshot(client.runDirectory, client.getWindow().getWidth(), client.getWindow().getHeight(), client.getFramebuffer(), text -> client.execute(() -> client.inGameHud.getChatHud().addMessage(text)));
            return new NullValue();
        });

        new MinecraftFunction("look", List.of("yaw", "pitch"), function -> {
            NumberValue numberValue = (NumberValue) function.getValueForType(NumberValue.class, 0, null);
            NumberValue numberValue2 = (NumberValue) function.getValueForType(NumberValue.class, 1, null);
            assert client.player != null;
            client.player.yaw = numberValue.value;
            client.player.pitch = numberValue2.value;
            return new NullValue();
        });

        new MinecraftFunction("jump", function -> {
            assert client.player != null;
            if (client.player.isOnGround())
                client.player.jump();
            return new NullValue();
        });

        new MinecraftFunction("hold", function -> {
            try {
                Thread.sleep(Long.MAX_VALUE);
            }
            catch (InterruptedException e) {
                throw new ThrowStop();
            }
            return new NullValue();
        });

        new MinecraftFunction("getCurrentSlot", function -> {
            assert client.player != null;
            return new NumberValue(client.player.inventory.selectedSlot + 1);
        });

        new MinecraftFunction("getHeldItem", function -> {
            assert client.player != null;
            return new StringValue(Registry.ITEM.getId(client.player.inventory.getMainHandStack().getItem()).getNamespace());
        });

        new MinecraftFunction("getLookingAtBlock", function -> {
            assert client.player != null;
            HitResult result = client.player.raycast(20D, 0.0F, true);
            if (result.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult) result).getBlockPos();
                return new StringValue(Registry.BLOCK.getId(client.player.world.getBlockState(blockPos).getBlock()).getPath());
            }
            return new StringValue("air");
        });

        new MinecraftFunction("getLookingAtEntity", function -> {
            if (client.targetedEntity != null)
                return new StringValue(Registry.ENTITY_TYPE.getId(client.targetedEntity.getType()).getPath());
            return new StringValue("none");
        });

        new MinecraftFunction("getHealth", function -> {
            assert client.player != null;
            return new NumberValue(client.player.getHealth());
        });

        new MinecraftFunction("getPos", "axis", function -> {
            final String error = "String must be \"x\", \"y\", \"z\", \"yaw\", or \"pitch\"";
            StringValue stringValue = (StringValue) function.getValueForType(StringValue.class, 0, error);
            float floatValue;
            assert client.player != null;
            switch (stringValue.value) {
                case "x" -> floatValue = (float) client.player.getX();
                case "y" -> floatValue = (float) client.player.getY();
                case "z" -> floatValue = (float) client.player.getZ();
                case "pitch" -> floatValue = client.player.pitch;
                case "yaw" -> {
                    floatValue = client.player.yaw % 360;
                    floatValue = floatValue < -180 ? 360 + floatValue : floatValue;
                }
                default -> throw function.throwInvalidParameterError(error);
            }
            return new NumberValue(floatValue);
        });

        new MinecraftFunction("getDimension", function -> {
            assert client.player != null;
            return new StringValue(client.player.world.getRegistryKey().getValue().getPath());
        });

        new MinecraftFunction("getBlockAt", List.of("x", "y", "z"), function -> {
            final String error = "Position must be in range of player";
            NumberValue num1 = (NumberValue) function.getValueForType(NumberValue.class, 0, error);
            NumberValue num2 = (NumberValue) function.getValueForType(NumberValue.class, 1, error);
            NumberValue num3 = (NumberValue) function.getValueForType(NumberValue.class, 2, error);
            BlockPos blockPos = new BlockPos(Math.floor(num1.value), num2.value, Math.floor(num3.value));
            assert client.player != null;
            return new StringValue(Registry.BLOCK.getId(client.player.world.getBlockState(blockPos).getBlock()).getPath());
        });

        new MinecraftFunction("getScriptsPath", function -> new StringValue(ClientScript.getDir().toString()));

        new MinecraftFunction("isTradeDisabled", "arg", function -> {
            final String error = "Parameter for isTradeDisabled() should either be an item type (e.g. \"grass_block\") or an index";
            Value<?> value = function.getValueFromTable(function.argumentNames.get(0));
            if (value instanceof NumberValue numberValue)
                return new BooleanValue(InventoryUtils.checkTradeDisabled(client, numberValue.value.intValue()));
            if (value instanceof StringValue stringValue)
                return new BooleanValue(InventoryUtils.checkTradeDisabled(client, Registry.ITEM.get(new Identifier(stringValue.value))));
            throw function.throwInvalidParameterError(error);
        });

        new MinecraftFunction("doesVillagerHaveTrade", "itemType", function -> {
            StringValue stringValue = (StringValue) function.getValueForType(StringValue.class, 0, mustBeItem);
            return new BooleanValue(InventoryUtils.checkHasTrade(client, Registry.ITEM.get(new Identifier(stringValue.value))));
        });

        new MinecraftFunction("isInventoryFull", function -> {
            assert client.player != null;
            return new BooleanValue(client.player.inventory.getEmptySlot() != -1);
        });

        new MinecraftFunction("isInInventoryGui", function -> {
            assert client.player != null;
            ScreenHandler screenHandler = client.player.currentScreenHandler;
            return new BooleanValue(
                screenHandler instanceof GenericContainerScreenHandler ||
                screenHandler instanceof MerchantScreenHandler ||
                screenHandler instanceof HopperScreenHandler ||
                screenHandler instanceof FurnaceScreenHandler ||
                client.currentScreen instanceof InventoryScreen
            );
        });

        new MinecraftFunction("isBlockEntity", "block", function -> {
            StringValue stringValue = (StringValue) function.getValueForType(StringValue.class, 0, mustBeItem);
            return new BooleanValue(Registry.BLOCK.get(new Identifier(stringValue.value)).hasBlockEntity());
        });

        new MinecraftFunction("santaIsCool", function -> {
            System.out.println("is santa cool?");
            return new BooleanValue(false);
        });


        // Add any other functions here!

    }

    @Override
    public Value<?> execute(List<Value<?>> arguments) throws Error {
        if (client.player == null)
            throw new ErrorRuntime("Player is null", this.startPos, this.endPos, this.context);
        this.context = this.generateNewContext();
        this.checkAndPopulateArguments(arguments, this.argumentNames, this.context);
        return this.function.execute(this);
    }

    private static NullValue setKey(BuiltInFunction function, KeyBinding keyBinding) throws Error {
        BooleanValue booleanValue = (BooleanValue) function.getValueForType(BooleanValue.class, 0, null);
        keyBinding.setPressed(booleanValue.value);
        return new NullValue();
    }

    @Override
    public Value<?> copy() {
        return new MinecraftFunction(this.value, this.argumentNames, this.function).setPos(this.startPos, this.endPos).setContext(this.context);
    }
}