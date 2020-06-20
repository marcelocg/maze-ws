package mcg.mazes.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mcg.mazes.model.Maze;

@RestController
@RequestMapping("/api/maze")
public class MazeController {

	@GetMapping("/generate")
	public ResponseEntity<List<Integer>> getMaze(@RequestParam int width, @RequestParam int height, @RequestParam int threshold) {
		return new ResponseEntity<List<Integer>>((new Maze(width, height, threshold)).toList(), HttpStatus.OK);
	}
}
