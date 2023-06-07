import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class MultiplayerTicTacToe {
    private static Tool player;
    private static Tool player1;
    private static Tool player2;
    private static Board board;

    private final String ip;
    private final int port = 8888;
    private ServerSocket serverSocket;
    private final boolean isServer;
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private final TicTacToeAgent agent;

    public MultiplayerTicTacToe() {
        System.out.println("Please input the IP: ");
        Scanner scanner = new Scanner(System.in);
        ip = scanner.nextLine();

        System.out.println("Server or client? (y/n): ");
        String choice = scanner.next();
        if (choice.equalsIgnoreCase("y")) {
            isServer = true;
            initializeServer();
            waitForClient();
        } else {
            isServer = false;
            connectToServer();
        }

        player1 = Tool.X;
        player2 = Tool.O;
        if(isServer) player = player1;
        else player= player2;
        board = new Board();
        agent = agentCreator();

        try {
            startGame();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static void getButtonMove(int num) {
        _row = num / 3 + 1;
        _column = num % 3 + 1;
        inputDone = true;
    }

    private void initializeServer() {
        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName(ip));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void waitForClient() {
        try {
            System.out.println("Waiting for the client to connect...");
            socket = serverSocket.accept();
            System.out.println("Client connected: " + socket.getInetAddress());
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void connectToServer() {
        try {
            socket = new Socket(ip, port);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            System.out.println("Connected to the server: " + socket.getInetAddress());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void startGame() throws IOException, InterruptedException {
        int choice;
        if (isServer){
            choice = dis.readInt();
            processChoice(choice);
        }else {
            System.out.println("1. Play with person 2. Play with computer");
            Scanner scanner = new Scanner(System.in);
            choice = scanner.nextInt();
            sendChoiceToServer(choice);
            processChoice(choice);
        }
    }

    private void playGame(boolean isPvP) {
        boolean gameOver = false;

        while (!gameOver) {
            board.show();
            board.showTurn(player);

            if (isServer) {
                Move move;

                if (isPvP) {
                    move = getAMoveFromHuman();
                    assert move != null;
                    sendMoveToOpponent(move);
                } else {
                    move = getAMoveFromAI();
                    sendMoveToOpponent(move);
                }

                board.handleMove(move, player);
                board.show();

                if (board.isGameWon() || board.isFull()) {
                    board.showResult(board.isGameWon(), player);
                    gameOver = playAgain();
                    if (gameOver) break;
                    board.clear(); // 清空棋盘
                }

                move = receiveMoveFromClient();
                board.handleMove(move, getOpponentPlayer());
                board.show();

                if (board.isGameWon() || board.isFull()) {
                    board.showResult(board.isGameWon(), player);
                    gameOver = playAgain();
                    if (gameOver) break;
                    board.clear();
                }
            } else {
                Move move = receiveMoveFromServer();
                board.handleMove(move, getOpponentPlayer());
                board.show();

                if ((board.isGameWon() || board.isFull())) {
                    board.showResult(board.isGameWon(), getOpponentPlayer());
                    gameOver = playAgain();
                    if (gameOver) break;
                    board.clear();
                }

                board.showTurn(player);
                move = getAMoveFromHuman();

                assert move != null;
                board.handleMove(move, player);
                board.show();
                sendMoveToOpponent(move);
                if (board.isGameWon() || board.isFull()) {
                    board.showResult(board.isGameWon(), player);
                    gameOver = playAgain();
                    if (gameOver) break;
                    board.clear();
                }
            }
        }

        closeConnection();
    }

    private boolean playAgain() {
        int choice = JOptionPane.showConfirmDialog(null, "Do you want to play again?", "Play Again", JOptionPane.YES_NO_OPTION);
        return choice != JOptionPane.YES_OPTION;
    }

    private void pvp() {
        playGame(true);
    }

    private void pve() {
        playGame(false);
    }


    private TicTacToeAgent agentCreator () {
        TicTacToeAgent agent;
        agent = new WisdomAgent( board, player1, player2, 100);
        return agent;
    }

    private void sendChoiceToServer(int choice) {
        try {
            dos.writeInt(choice);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void processChoice(int choice) {
        if (choice == 1) {
            pvp();
        } else if (choice == 2) {
            pve();
        } else {
            System.out.println("Invalid choice. Please select 1 or 2.");
        }
    }

    @SuppressWarnings("BusyWait")
    private Move getAMoveFromHuman() {
        Move move;
        inputDone = false;
        while (!inputDone) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            move = new Move(_row, _column);
            if (board.isValid(move)) {
                inputDone = true;
                return move;
            } else {
                System.out.println("Invalid move! Please try again.");
            }
        }
        return null;
    }

    private Move getAMoveFromAI(){
        Move move;
        move = agent.nextMove();
        return move;
    }

    private void sendMoveToOpponent(Move move) {
        try {
            dos.writeInt(move.getRow());
            dos.writeInt(move.getColumn());
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private Tool getOpponentPlayer() {
        return (player == player1) ? player2 : player1;
    }

    private Move receiveMoveFromClient() {
        return getMove();
    }

    private Move receiveMoveFromServer() {
        return getMove();
    }

    private Move getMove() {
        try {
            _row = dis.readInt();
            _column = dis.readInt();
            return new Move(_row, _column);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    private void closeConnection() {
        try {
            if (dos != null) {
                dos.close();
            }
            if (dis != null) {
                dis.close();
            }
            if (socket != null) {
                socket.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean inputDone = true;
    private static int _row = 0;
    private static int _column = 0;


    public static void main(String[] args) {
        MultiplayerTicTacToe multiplayerTicTacToe = new MultiplayerTicTacToe();
    }
}







