package dev.quantumfusion.dashloader.api.option;

import dev.quantumfusion.dashloader.DashConstants;
import java.util.EnumMap;
import net.fabricmc.loader.api.FabricLoader;
import static dev.quantumfusion.dashloader.DashLoader.DL;

public class ConfigHandler {

	private static final EnumMap<Option, Boolean> OPTION_ACTIVE = new EnumMap<>(Option.class);
	private static final String DISABLE_OPTION_TAG = DashConstants.DASH_DISABLE_OPTION_TAG;

	public static void update() {
		DL.config.reloadConfig();
		DL.config.config.options.forEach((s, aBoolean) -> {
			try {
				var option = Option.valueOf(s.toUpperCase());
				OPTION_ACTIVE.put(option, false);
				DL.log.warn("Disabled Optional Feature {} from DashLoader config.", s);
			} catch (IllegalArgumentException illegalArgumentException) {
				DL.log.error("Could not disable Optional Feature {} as it does not exist.", s);
			}
		});

		for (var modContainer : FabricLoader.getInstance().getAllMods()) {
			var mod = modContainer.getMetadata();
			if (mod.containsCustomValue(DISABLE_OPTION_TAG)) {
				for (var value : mod.getCustomValue(DISABLE_OPTION_TAG).getAsArray()) {
					final String feature = value.getAsString();
					try {
						var option = Option.valueOf(feature.toUpperCase());
						OPTION_ACTIVE.put(option, false);
						DL.log.warn("Disabled Optional Feature {} from {} config. {}", feature, mod.getId(), mod.getName());
					} catch (IllegalArgumentException illegalArgumentException) {
						DL.log.error("Could not disable Optional Feature {} as it does not exist.", feature);
					}
				}
			}
		}
	}

	public static boolean shouldApplyMixin(String name) {
		for (Option value : Option.values()) {
			if (name.contains(value.mixinContains)) {
				return OPTION_ACTIVE.get(value);
			}
		}
		return true;
	}

	public static String getJsonName(Option option) {
		final char[] name = option.name().toCharArray();
		StringBuilder sb = new StringBuilder();
		boolean nextHighCase = false;
		for (char c : name) {
			if (c == '_') {
				nextHighCase = true;
			} else {
				if (nextHighCase) {
					sb.append(Character.toUpperCase(c));
					nextHighCase = false;
				} else {
					sb.append(Character.toLowerCase(c));
				}
			}
		}
		return sb.toString();
	}

	public static boolean optionActive(Option option) {
		return OPTION_ACTIVE.get(option);
	}


	static {
		for (Option value : Option.values()) {
			OPTION_ACTIVE.put(value, true);
		}
	}
}
