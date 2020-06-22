package mcg.mazes.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import mcg.mazes.utils.Constants;

public class Maze {

	int sizeX;
	int sizeY;

	int threshold = Constants.THRESHOLD;

	private ArrayList<Cell> grid;

	static int printNum = 0;

	public Maze() throws IOException {
		this(4, 4);
	}

	public Maze(int sizeX, int sizeY) {
		this(sizeX, sizeY, Constants.THRESHOLD);
	}

	public Maze(int sizeX, int sizeY, int threshold) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;

		this.threshold = threshold;

		this.grid = generateGrid();

		this.generateMaze();
	}

	public int getHeight() {
		return this.sizeY;
	}

	public int getWidth() {
		return this.sizeX;
	}

	private ArrayList<Cell> generateGrid() {
		ArrayList<Cell> grid = new ArrayList<Cell>();

		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				Cell cell = new Cell(x, y, this);
				grid.add(cell);
			}
		}

		return grid;
	}

	private void generateMaze() {
		split(grid);
		clearRegionMarks();
	}

	public void print() {
		System.out.println(this);
	}

	private void clearRegionMarks() {
		for (Cell cell : grid) {
			cell.setRegion(Constants.NO_REGION);
		}
	}

	private void split(List<Cell> region) {
		if (region.size() <= this.threshold)
			return;

		clearRegionMarks();

		ArrayList<Cell> unsplit = new ArrayList<Cell>(region);
		ArrayList<Cell> subRegionA = new ArrayList<Cell>();
		ArrayList<Cell> subRegionB = new ArrayList<Cell>();

		if (Constants.DEBUGLEVEL > 1)
			System.out.println("Iniciando Split");
		if (Constants.DEBUGLEVEL >= 1) {
			print();
			pause();
		}

		int indexSeedA = getRandomCellIndex(unsplit); 
		Cell seedA = unsplit.get(indexSeedA);
		seedA.setRegion(Constants.A);
		subRegionA.add(seedA);
		unsplit.remove(indexSeedA);
		
		int indexSeedB = getRandomCellIndex(unsplit); 
		Cell seedB = unsplit.get(indexSeedB);
		seedB.setRegion(Constants.B);
		subRegionB.add(seedB);
		unsplit.remove(indexSeedB);
		
		Set<Cell> set = new HashSet<Cell>();
		set.add(seedA);
		set.add(seedB);

		while (unsplit.size() > 0) {
			Cell currentCell = set.toArray(new Cell[0])[ThreadLocalRandom.current().nextInt(0, set.size())];
			set.remove(currentCell);

			Collection<Cell> neighbors = currentCell.getNeighborhood().stream()
					.filter(c -> !c.isInRegion() && region.contains(c)).collect(Collectors.toList());

			for (Cell n : neighbors) {
				n.setRegion(currentCell.getRegion());
				unsplit.remove(n);
				if (currentCell.getRegion() == Constants.A) {
					subRegionA.add(n);
				} else {
					subRegionB.add(n);
				}
			}

			set.addAll(neighbors);
			//unsplitCellsCount = region.stream().filter(c -> !c.isInRegion()).count();
		}

		if (Constants.DEBUGLEVEL > 1)
			System.out.println("Split concluido.");
		if (Constants.DEBUGLEVEL >= 1) {
			print();
			pause();
		}
		if (Constants.DEBUGLEVEL > 1)
			System.out.println("Vou construir a fronteira entre as regioes...");

		buildBorder(region);

		if (Constants.DEBUGLEVEL > 1)
			System.out.println("Fronteira construida e uma parede retirada.");
		if (Constants.DEBUGLEVEL >= 1) {
			print();
			pause();
		}

//		List<Cell> subRegionA = region.stream().filter(c -> c.getRegion() == Constants.A).collect(Collectors.toList());
//		List<Cell> subRegionB = region.stream().filter(c -> c.getRegion() == Constants.B).collect(Collectors.toList());

		if (Constants.DEBUGLEVEL > 1)
			System.out.println("Invocando o split da regiao A.");

		split(subRegionA);

		if (Constants.DEBUGLEVEL > 1)
			System.out.println("Invocando o split da regiao B.");

		split(subRegionB);
	}

	private void buildBorder(List<Cell> region) {
		Set<Cell> border = new HashSet<Cell>();

		// 1. Choose one subregion (A or B) from the region passed as argument
		List<Cell> subRegionA = region.stream().filter(c -> c.getRegion() == Constants.A).collect(Collectors.toList());

		// 2. For each cell in this subregion,
		for (Cell c : subRegionA) {
			// 2.1 For each of it's neighbors that also belongs to the region
			// but is on a different subregion
			List<Cell> neighborhood = c.getNeighborhood().stream()
					.filter(n -> region.contains(n) && n.getRegion() == Constants.B).collect(Collectors.toList());
			for (Cell n : neighborhood) {
				// 2.2.1 put a wall between them
				buildWall(c, n);
				// 2.2.2 add the cell to a border collection
				border.add(c);
			}
		}

		// 3. Choose one random cell from the frontier collection
		Cell cell = getRandomCell(Arrays.asList(border.toArray(new Cell[0])));

		// 4. Remove one of the cell's walls
		cell.removeOneInternalWall();
	}

	public Cell getCell(int x, int y) {
		return grid.stream().filter(c -> c.x == x && c.y == y).findFirst().get();
	}

	private void buildWall(Cell cell, Cell neighbor) {
		int border = 0;
		int borderNeighbor = 0;

		border += (neighbor.x == (cell.x + 1) ? Cell.EAST : 0);
		border += (neighbor.x == (cell.x - 1) ? Cell.WEST : 0);
		border += (neighbor.y == (cell.y + 1) ? Cell.SOUTH : 0);
		border += (neighbor.y == (cell.y - 1) ? Cell.NORTH : 0);

		borderNeighbor += (cell.x == (neighbor.x + 1) ? Cell.EAST : 0);
		borderNeighbor += (cell.x == (neighbor.x - 1) ? Cell.WEST : 0);
		borderNeighbor += (cell.y == (neighbor.y + 1) ? Cell.SOUTH : 0);
		borderNeighbor += (cell.y == (neighbor.y - 1) ? Cell.NORTH : 0);

		cell.setWalls(cell.getWalls() + border);
		neighbor.setWalls(neighbor.getWalls() + borderNeighbor);
	}

	private Cell getRandomCell(List<Cell> cells) {
		return cells.get(ThreadLocalRandom.current().nextInt(cells.size()));
	}

	private int getRandomCellIndex(List<Cell> cells) {
		return ThreadLocalRandom.current().nextInt(cells.size());
	}

	private Cell getRandomUnsplitCell(List<Cell> region) {
		List<Cell> unsplitCells = region.stream().filter(c -> !c.isInRegion()).collect(Collectors.toList());
		return getRandomCell(unsplitCells);
	}

	public List<Integer> toList() {
		return grid.stream().map(Cell::getWallsAndCorners).collect(Collectors.toList());
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("");

		for (int x = 0; x < sizeX; x++) {
//      sb.append("+---");
			sb.append("----");
		}
//    sb.append("+\n");
		sb.append(" \n");

		for (int y = 0; y < sizeY; y++) {
			sb.append("|");
//      String bottom = "+";
			StringBuffer bottom = new StringBuffer(" ");

			for (int x = 0; x < sizeX; x++) {
				Cell cell = this.getCell(x, y);

				sb.append(" ").append(cell.getRegionPic()).append(" ").append(cell.hasEastWall() ? "|" : " ");

				bottom.append(cell.hasSouthWall() ? "---" : "   ");
//        bottom.append("+");
				bottom.append(" ");
			}

			sb.append("\n").append(bottom).append("\n");
		}

		return sb.toString();
	}

	private void pause() {
//    try {
//      System.in.read(new byte[2]);
//    } catch (IOException e) {
//    }
	}

	public static void main(String[] args) {
		for(int i = 0; i < 3; i++) {
			long start = System.currentTimeMillis();
			Maze maze = new Maze(100, 100);
			maze.toList();
			long finish = System.currentTimeMillis();
			float duration = finish - start;
			System.out.printf("Round: " + i + " - Creation time: %d\n", duration / 1000);
		}

//		start = System.currentTimeMillis();
//
//	    TiledExporter.toJSON(maze);
//		maze.print();
//		System.out.println(maze.toList());
//
//		finish = System.currentTimeMillis();
//		duration = finish - start;
//
//		System.out.printf("Export time: %d\n", duration);
	}
}
