package mcg.mazes.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Cell {
	int x;
	int y;

	int region = 0;

	private Maze maze;

	private boolean wallNorth = false;
	private boolean wallSouth = false;
	private boolean wallEast = false;
	private boolean wallWest = false;

	public static final int NO_WALLS = 0;
	public static final int NORTH = 1;
	public static final int EAST  = 2;
	public static final int SOUTH = 4;
	public static final int WEST  = 8;

	public static final int NO_CORNERS = 0;
	public static final int NORTHWEST_CORNER = 16;
	public static final int NORTHEAST_CORNER = 32;
	public static final int SOUTHEAST_CORNER = 64;
	public static final int SOUTHWEST_CORNER = 128;
	public static final int ALL_CORNERS = 240;

	public static final int HORIZONTAL_PASSAGE = NORTH + SOUTH;
	public static final int VERTICAL_PASSAGE = EAST + WEST;

	public static final int NORTH_DEAD_END = NORTH + EAST + WEST;
	public static final int SOUTH_DEAD_END = SOUTH + EAST + WEST;
	public static final int EAST_DEAD_END = NORTH + SOUTH + EAST;
	public static final int WEST_DEAD_END = NORTH + SOUTH + WEST;

	public Cell(int x, int y, Maze maze) {
		this.x = x;
		this.y = y;
		this.maze = maze;

		this.wallWest = (x == 0);
		this.wallNorth = (y == 0);
		this.wallEast = (x == maze.sizeX - 1);
		this.wallSouth = (y == maze.sizeY - 1);

	}

	public boolean hasNorthWall() {
		return wallNorth;
	}

	public boolean hasSouthWall() {
		return wallSouth;
	}

	public boolean hasEastWall() {
		return wallEast;
	}

	public boolean hasWestWall() {
		return wallWest;
	}

	public String toString() {
		return "(" + x + "," + y + ") Borders: " + this.whichWalls();
	}

	public boolean isInRegion() {
		return this.region >= 0;
	}

	private boolean isInDifferentRegion(Cell cell) {
		return cell.isInRegion() && cell.region != this.region;
	}

	public boolean hasNorthNeighbor() {
		return this.y > 0;
	}

	public boolean hasSouthNeighbor() {
		return this.y < maze.sizeY - 1;
	}

	public boolean hasEastNeighbor() {
		return this.x < maze.sizeX - 1;
	}

	public boolean hasWestNeighbor() {
		return this.x > 0;
	}

	public Collection<Cell> getNeighborhood() {
		Collection<Cell> neighborhood = new ArrayList<Cell>();

		if (this.hasNorthNeighbor())
			neighborhood.add(this.getNorthNeighbor());
		if (this.hasSouthNeighbor())
			neighborhood.add(this.getSouthNeighbor());
		if (this.hasEastNeighbor())
			neighborhood.add(this.getEastNeighbor());
		if (this.hasWestNeighbor())
			neighborhood.add(this.getWestNeighbor());

		return neighborhood;
	}

	public Cell getNorthNeighbor() {
		return maze.getCell(this.x, this.y - 1);
	}

	public Cell getSouthNeighbor() {
		return maze.getCell(this.x, this.y + 1);
	}

	public Cell getEastNeighbor() {
		return maze.getCell(this.x + 1, this.y);
	}

	public Cell getWestNeighbor() {
		return maze.getCell(this.x - 1, this.y);
	}

	public String whichWalls() {
		String walls;

		walls = (wallNorth ? "N" : "") + (wallSouth ? "S" : "") + (wallEast ? "E" : "") + (wallWest ? "W" : "");

		return walls;
	}

	public void setWalls(int borders) {
		wallNorth = (borders & NORTH) > 0;
		wallSouth = (borders & SOUTH) > 0;
		wallEast = (borders & EAST) > 0;
		wallWest = (borders & WEST) > 0;
	}

	public int getWalls() {
		return (wallNorth ? NORTH : 0) + (wallSouth ? SOUTH : 0) + (wallEast ? EAST : 0) + (wallWest ? WEST : 0);
	}

	public int getBorderWalls() {
		int borderWalls = 0;

		if (wallNorth && (y > 0) && isInDifferentRegion(getNeighbor(NORTH)))
			borderWalls += NORTH;
		if (wallSouth && (y < maze.sizeY - 1) && isInDifferentRegion(getNeighbor(SOUTH)))
			borderWalls += SOUTH;
		if (wallEast && (x < maze.sizeX - 1) && isInDifferentRegion(getNeighbor(EAST)))
			borderWalls += EAST;
		if (wallWest && (x > 0) && isInDifferentRegion(getNeighbor(WEST)))
			borderWalls += WEST;

		return borderWalls;
	}

	public List<Integer> getNeighborWallsList() {
		int walls = getBorderWalls();
		List<Integer> borders = Arrays.asList(NORTH, SOUTH, EAST, WEST);

		return borders.stream().filter(b -> (walls & b) > 0) // select the walls that are set
				.collect(Collectors.toList());
	}

	public Cell getNeighbor(int direction) {
		if (direction == NORTH)
			return this.getNorthNeighbor();
		else if (direction == SOUTH)
			return this.getSouthNeighbor();
		else if (direction == WEST)
			return this.getWestNeighbor();
		else
			return this.getEastNeighbor();
	}

	public void removeOneInternalWall() {
		List<Integer> walls = this.getNeighborWallsList();
		int wall = 0;

		if (walls.size() > 1)
			wall = walls.get(ThreadLocalRandom.current().nextInt(0, walls.size()));
		else
			wall = walls.get(0);

		this.setWalls(this.getWalls() - wall);

		int neighborWall = 0;
		if (wall == NORTH)
			neighborWall = SOUTH;
		if (wall == SOUTH)
			neighborWall = NORTH;
		if (wall == WEST)
			neighborWall = EAST;
		if (wall == EAST)
			neighborWall = WEST;

		Cell neighbor = this.getNeighbor(wall);
		neighbor.setWalls(neighbor.getWalls() - neighborWall);

	}

	public void setRegion(int region) {
		this.region = region;
	}

	public int getRegion() {
		return this.region;
	}

	private boolean hasNortheastCorner() {
		return (!hasNorthWall() && !hasEastWall() && ((hasEastNeighbor() && getEastNeighbor().hasNorthWall())
				|| (hasNorthNeighbor() && getNorthNeighbor().hasEastWall())));
	}

	private boolean hasNorthwestCorner() {
		return (!hasNorthWall() && !hasWestWall() && ((hasWestNeighbor() && getWestNeighbor().hasNorthWall())
				|| (hasNorthNeighbor() && getNorthNeighbor().hasWestWall())));
	}

	private boolean hasSoutheastCorner() {
		return (!hasSouthWall() && !hasEastWall() && ((hasEastNeighbor() && getEastNeighbor().hasSouthWall())
				|| (hasSouthNeighbor() && getSouthNeighbor().hasEastWall())));
	}

	private boolean hasSouthwestCorner() {
		return (!hasSouthWall() && !hasWestWall() && ((hasWestNeighbor() && getWestNeighbor().hasSouthWall())
				|| (hasSouthNeighbor() && getSouthNeighbor().hasWestWall())));
	}

	public int getCorners() {
		int corners = 0;
		if (hasNortheastCorner()) {
			corners += NORTHEAST_CORNER;
		}
		if (hasNorthwestCorner()) {
			corners += NORTHWEST_CORNER;
		}
		if (hasSoutheastCorner()) {
			corners += SOUTHEAST_CORNER;
		}
		if (hasSouthwestCorner()) {
			corners += SOUTHWEST_CORNER;
		}
		return corners;
	}

	public int getWallsAndCorners() {
		return getWalls() + getCorners();
	}
}
