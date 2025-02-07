package com.example.oxo.model;

public class Player {
	private char letter;

	public Player(char playingLetter) {
		this.letter = playingLetter;
	}

	public char getPlayingLetter() {
		return letter;
	}

	public void setPlayingLetter(char letter) {
		this.letter = letter;
	}
}
