package scaatis.rrr;

public enum RaceCharacter {

	CYBERHAWK("Cyberhawk"), IVANZYPHER("Ivanzypher"), JAKE_BADLANDS(
			"Jake Badlands"), KATARINA_LYONS("Katarina Lyons"), RIP("Rip"), SNAKE_SANDERS(
			"Snake Sanders"), TARQUINN("Tarquinn"), VIPER_MACKAY("Viper Mackay");

	private String name;

	private RaceCharacter(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static RaceCharacter getFromName(String name) {
		RaceCharacter[] val = RaceCharacter.values();
		for (RaceCharacter ch : val) {
			if (ch.getName().equals(name)) {
				return ch;
			}
		}
		return null;
	}
}
