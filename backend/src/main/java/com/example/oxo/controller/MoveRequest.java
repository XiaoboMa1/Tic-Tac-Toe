package com.example.oxo.controller;

public class MoveRequest {
	private String command;

	public MoveRequest() {}

	public MoveRequest(String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
}
