package com.example.oxo.controller;

import com.example.oxo.model.MoveException;
import com.example.oxo.service.GameService;
import com.example.oxo.controller.MoveRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/oxo")
@CrossOrigin
public class GameController {
	private final GameService gameService;

	public GameController(GameService gameService) {
		this.gameService = gameService;
	}

	/** 获取当前棋局状态（玩家信息、棋盘、当前玩家、赢家等） */
	@GetMapping("/state")
	public Object getGameState() {
		return gameService.getGameState();
	}

	/** 处理玩家在前端输入的指令（如"a1", "b2"...） */
	@PostMapping("/move")
	public Object makeMove(@RequestBody MoveRequest request) {
		try {
			gameService.handleIncomingCommand(request.getCommand());
		} catch (MoveException e) {
			return "Invalid Move: " + e.getMessage();
		}
		return gameService.getGameState();
	}

	/** 设置玩家数量，并分配字母 */
	@PostMapping("/setPlayers")
	public Object setPlayers(@RequestParam int count) {
		gameService.setPlayers(count);
		return gameService.getGameState();
	}

	/** 设置棋盘大小（行数与列数），使玩家可以在输入框里指定行列数 */
	@PostMapping("/setSize")
	public Object setBoardSize(@RequestParam int rows, @RequestParam int cols) {
		gameService.setBoardSize(rows, cols);
		return gameService.getGameState();
	}
	@PostMapping("/reset")
	public Object resetGame() {
		// 调用 Service 的 resetGame()
		gameService.resetGame();
		// 返回一个最新状态
		return gameService.getGameState();
	}

}
