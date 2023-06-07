/**
   A class for storing, manipulating, and printing TicTacToe boards
   Add a trialToWin method on May 19, 2017
   v2.1: Add a trialDoubleTreats method on May 23, 2017
   v2.8: ...
*/

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
public class Board {
  public static final int SIZE = 3;
  private final Tool[][] board;
  private final ArrayList<Move> moves;
  JFrame frame = new JFrame();
  JPanel title_panel = new JPanel();
  JPanel button_panel = new JPanel();
  JLabel textfield = new JLabel();
  JButton[] button = new JButton[SIZE*SIZE];  //

  public Board() {
    board = new Tool[SIZE][SIZE];
    clear();
    moves = new ArrayList<>();

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(600, 600); // 调整窗口大小为较小的值
    frame.getContentPane().setBackground(new Color(50, 50, 50));
    frame.setLayout(new BorderLayout());
    frame.setVisible(true);

    textfield.setBackground(new Color(25, 25, 25));
    textfield.setForeground(new Color(0, 255, 0));
    textfield.setFont(new Font("Serif", Font.BOLD, 30));
    textfield.setHorizontalAlignment(JLabel.CENTER);
    textfield.setText("Welcome to TicTacToe");
    textfield.setOpaque(true);

    title_panel.setLayout(new BorderLayout());
    title_panel.setBounds(0, 0, 600, 100);

    button_panel.setLayout(new GridLayout(3, 3));
    button_panel.setBackground(new Color(255, 255, 255));

    for (int i = 0; i < SIZE * SIZE; i++) {
      int label = i;
      button[i] = new JButton();
      button_panel.add(button[i]);
      button[i].setFont(new Font("Serif", Font.BOLD, 60)); // 调整按钮的字体大小
      button[i].setFocusable(false);
      button[i].addActionListener(event -> MultiplayerTicTacToe.getButtonMove(label));
    }

    title_panel.add(textfield);
    frame.add(title_panel, BorderLayout.NORTH);
    frame.add(button_panel);
  }

  public void clear () {
    for (int i = 0; i < SIZE; i++)
      for (int j = 0; j < SIZE; j++)
        board[i][j] = Tool.EMPTY;
  }

  public void showTurn(Tool t) {
    String announce = t.toString() + " player.";
    textfield.setText(announce);
  }

  public void showResult(boolean isWon, Tool winT) {
    String announce = "Draw!";
    if(isWon) {
      announce = winT.toString() + " ";
      announce += " player has won!";
    }
    textfield.setText(announce);
  }
  public void show () {
    for(int i = 0; i < 9;i++){
      button[i].setText(board[i / 3][i % 3].toString());
    }

    System.out.println( "Here is the current board:" );
    System.out.println();
    
    for (int r = 1; r <= SIZE; r++) {
      for (int c = 1; c <= SIZE; c++) {
        System.out.print( board[r-1][c-1] );
        if (c != SIZE)   // Print strut after all but last column
          System.out.print( "|" );
      }
      System.out.println();     
  
      if (r != SIZE)   // Print row line after all but last row
        System.out.println( "-+-+-" );
    }
  
    System.out.println();     
  }
  
  public ArrayList<Move> getMoves () {
    return moves;
  }
       
  public boolean isGameWon () {
    final Tool[][] b = board;  // a local variable for shorter expressions

    // Check (short circuit) all rows, columns and diagonals for a win
    return
      b[0][0]!=Tool.EMPTY && b[0][0]==b[0][1] && b[0][1]==b[0][2] ||  // Row 0
      b[1][0]!=Tool.EMPTY && b[1][0]==b[1][1] && b[1][1]==b[1][2] ||  // Row 1
      b[2][0]!=Tool.EMPTY && b[2][0]==b[2][1] && b[2][1]==b[2][2] ||  // Row 2
                                                                      
      b[0][0]!=Tool.EMPTY && b[0][0]==b[1][0] && b[1][0]==b[2][0] ||  // Col 0
      b[0][1]!=Tool.EMPTY && b[0][1]==b[1][1] && b[1][1]==b[2][1] ||  // Col 1
      b[0][2]!=Tool.EMPTY && b[0][2]==b[1][2] && b[1][2]==b[2][2] ||  // Col 2
                                                                      
      b[1][1]!=Tool.EMPTY && b[0][0]==b[1][1] && b[1][1]==b[2][2] ||  // Dia 1
      b[1][1]!=Tool.EMPTY && b[2][0]==b[1][1] && b[1][1]==b[0][2] ;   // Dia 2
  }
 
  public boolean isFull () {
    for (int i = 0; i < SIZE; i++)
      for (int j = 0; j < SIZE; j++)
        if (board[i][j] == Tool.EMPTY) return false;
  
    return true;
  }
  
  public ArrayList<Move> getEmptyMoves () {
    ArrayList<Move> emptyMoves = new ArrayList<>();
    for (int i = 0; i < SIZE; i++)
      for (int j = 0; j < SIZE; j++)
        if (board[i][j] == Tool.EMPTY) 
          emptyMoves.add( new Move( i+1, j+1));
  
    return emptyMoves;
  }

  public boolean isValid (Move move) {
    int r = move.getRow();
    int c = move.getColumn();
    return board[r-1][c-1] == Tool.EMPTY;
  }
  
  public void handleMove (Move move, Tool player) {
    int r = move.getRow();
    int c = move.getColumn();
  
    System.out.println();
    System.out.println( "The move for " + player + " is " + r + ", " + c );
  
    board[r-1][c-1] = player;     // Place the player's mark on the board
    moves.add( move);
  }
  
  public void setMove (Move move, Tool player) {
    board[move.getRow()-1][move.getColumn()-1] = player;
    moves.add( move);
  }
  
  public void clearMove (Move move) {
    board[move.getRow()-1][move.getColumn()-1] = Tool.EMPTY; 
    moves.remove( moves.size()-1);
  }
  
  public boolean trialToWin (Move move, Tool player) {
    if (!isValid( move)) return false;
  
    int r = move.getRow();
    int c = move.getColumn();
    
    board[r-1][c-1] = player;         // just a trial move
    boolean result = isGameWon();
    board[r-1][c-1] = Tool.EMPTY;     // clear the trial move
    
    return result;
  }
  
  public boolean trialDoubleTreats (Move move, Tool player) {
    if (!isValid( move)) return false;
  
    int r = move.getRow();
    int c = move.getColumn();
    
    board[r-1][c-1] = player;             // the trial move
  
    boolean singleThreat = false;
    boolean doubleThreat = false;
    for (int i = 0; i < SIZE && !doubleThreat; i++) {
      for (int j = 0; j < SIZE; j++) {
        if (board[i][j] == Tool.EMPTY) {
          board[i][j] = player;           // the next trial move
          if (isGameWon()) {
            if (!singleThreat) {
              singleThreat = true;
            } else {
              doubleThreat = true;  
              board[i][j] = Tool.EMPTY;   // clear the next trial move
              break;                      // break the inner for loop
            } 
          }
          board[i][j] = Tool.EMPTY;       // clear the next trial move
        }        
      }
    }
  
    board[r-1][c-1] = Tool.EMPTY;         // clear the trial move
    
    return doubleThreat;
  }
}
