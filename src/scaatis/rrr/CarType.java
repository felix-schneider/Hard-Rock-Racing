package scaatis.rrr;

public enum CarType {
	AIRBLADE("Airblade"), BATTLE_TRAK("Battle Trak"), DIRT_DEVIL("Dirt Devil"), HAVAC(
			"Havac"), MARAUDER("Marauder");

	private String name;

	private CarType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static CarType getFromString(String name) {
		CarType[] val = values();
		CarType res = null;
		for (CarType type : val) {
			if (type.getName().equals(name)) {
				res = type;
			}
		}
		return res;
	}
}
