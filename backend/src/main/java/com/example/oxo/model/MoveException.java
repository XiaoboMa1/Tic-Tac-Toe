package com.example.oxo.model;

public class MoveException extends Exception {
	public MoveException(String message) {
		super(message);
	}

	public static class InvalidIdentifierLengthException extends MoveException {
		public InvalidIdentifierLengthException(int length) {
			super("Identifier length is invalid: " + length);
		}
	}

	public static class InvalidIdentifierCharacterException extends MoveException {
		public enum CharacterType { ROW, COLUMN }
		public InvalidIdentifierCharacterException(CharacterType type, char invalidChar) {
			super("Invalid " + type + " character: " + invalidChar);
		}
	}

	public static class OutsideCellRangeException extends MoveException {
		public enum CellInfo { ROW, COLUMN }
		public OutsideCellRangeException(CellInfo type, int index) {
			super(type + " index out of range: " + index);
		}
	}

	public static class CellAlreadyTakenException extends MoveException {
		public CellAlreadyTakenException(int row, int col) {
			super("Cell is already taken: (" + row + "," + col + ")");
		}
	}
}
