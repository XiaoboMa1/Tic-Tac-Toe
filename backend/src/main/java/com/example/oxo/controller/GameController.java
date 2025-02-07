package com.example.oxo.controller;

import com.example.oxo.model.MoveException;
import com.example.oxo.service.GameService;
import com.example.oxo.controller.MoveRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/oxo")
// @CrossOrigin 删除, 避免与全局配置冲突
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
// 修改后的 makeMove 方法
	@PostMapping("/move")
	public ResponseEntity<?> makeMove(@RequestBody MoveRequest request) {
		try {
			gameService.handleIncomingCommand(request.getCommand());
			return ResponseEntity.ok(gameService.getGameState());
		} catch (MoveException e) {
			// 返回400错误 + JSON格式错误信息
			return ResponseEntity.badRequest().body(
					Map.of("error", "Invalid Move: " + e.getMessage())
			);
		} catch (Exception e) {
			// 捕获其他未处理的异常，返回500 错误
			return ResponseEntity.internalServerError().body(
					Map.of("error", "Internal server error: " + e.getMessage())
			);
		}
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
