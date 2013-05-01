package scaatis.rrr;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import scaatis.rrr.tracktiles.CheckPoint;
import scaatis.rrr.tracktiles.Curve;
import scaatis.rrr.tracktiles.FinishLine;
import scaatis.rrr.tracktiles.Straight;
import scaatis.rrr.tracktiles.TrackTile;

public class TrackLoader {
	public static Track loadTrack(String path) throws IOException {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		Direction startDir = null;
		String line = reader.readLine();
		ArrayList<TrackTile> tiles = new ArrayList<>();
		while (line != null) {
			String[] tokens = line.split(" ");
			if (tokens.length != 2) {
				reader.close();
				throw new RuntimeException(
						"Invalid track format - wrong number of tokens in line :"
								+ line);
			}
			Direction dir;
			try {
				 dir = Direction.valueOf(tokens[1]);
			} catch (IllegalArgumentException e) {
				reader.close();
				throw new RuntimeException(e);
			}
			if(startDir == null) {
				startDir = dir;
			}
			
			if(tokens[0].equals("Straight")) {
				tiles.add(new Straight(dir));
			} else if(tokens[0].equals("Curve")) {
				tiles.add(new Curve(dir));
			} else if(tokens[0].equals("FinishLine")) {
				tiles.add(new FinishLine(dir));
			} else if(tokens[0].equals("CheckPoint")) {
				tiles.add(new CheckPoint(dir));
			}

			line = reader.readLine();
		}
		reader.close();
		return new Track(startDir, tiles);
	}
}
