import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class Controller implements Initializable {
    private static final int PLAY_1 = 1;
    private static final int PLAY_2 = 2;
    private static final int EMPTY = 0;
    private static final int BOUND = 90;
    private static final int OFFSET = 15;
    private int[] position = {0, 0};

    private Socket socket;
    public static DataInputStream dataInputStream;
    public PrintStream printStream;

    public String player_name;

    @FXML
    private Pane base_square;

    @FXML
    private Rectangle game_panel;

    private boolean init = true;
    private static boolean TURN = false;

    private static final int[][] chessBoard = new int[3][3];
    private static final boolean[][] flag = new boolean[3][3];

    public int x_axis;
    public int y_axis;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        game_panel.setOnMouseClicked(event -> {
            x_axis = (int) (event.getX() / BOUND);
            y_axis = (int) (event.getY() / BOUND);
            String msg = "position:" + x_axis + "," + y_axis + "by " + player_name;
            printStream.println(msg);
        });
    }


    private boolean refreshBoard(int x, int y) {
        if (chessBoard[x][y] == EMPTY) {
            chessBoard[x][y] = TURN ? PLAY_1 : PLAY_2;
            drawChess();
            return true;
        }
        return false;
    }

    private void drawChess() {
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                if (flag[i][j]) {
                    // This square has been drawing, ignore.
                    continue;
                }
                switch (chessBoard[i][j]) {
                    case PLAY_1:
                        drawCircle(i, j);
                        break;
                    case PLAY_2:
                        drawLine(i, j);
                        break;
                    case EMPTY:
                        // do nothing
                        break;
                    default:
                        System.err.println("Invalid value!");
                }
            }
        }
    }

    private void drawCircle(int i, int j) {
        Circle circle = new Circle();
        base_square.getChildren().add(circle);
        circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
        circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
        circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
        circle.setStroke(Color.RED);
        circle.setFill(Color.TRANSPARENT);
        flag[i][j] = true;
    }

    private void drawLine(int i, int j) {
        Line line_a = new Line();
        Line line_b = new Line();
        base_square.getChildren().add(line_a);
        base_square.getChildren().add(line_b);
        line_a.setStartX(i * BOUND + OFFSET * 1.5);
        line_a.setStartY(j * BOUND + OFFSET * 1.5);
        line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
        line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_a.setStroke(Color.BLUE);

        line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
        line_b.setStartY(j * BOUND + OFFSET * 1.5);
        line_b.setEndX(i * BOUND + OFFSET * 1.5);
        line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_b.setStroke(Color.BLUE);
        flag[i][j] = true;
    }

//    //these two methods can not be static, in order to be invoked by client.
//    public int[] getPosition(){
//        click = false;
//        return position;
//    }
//    public boolean getClick(){
//        return click;
//    }

    public void setSocket(Socket s) throws IOException {
        socket = s;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.printStream = new PrintStream(socket.getOutputStream());
    }

    public void setPlayer_name(String n) {
        player_name = n;
    }

    public void callback(){
        if (refreshBoard(x_axis, y_axis)) {
            TURN = !TURN;
        }
    }
    public void callback(int x, int y){
        refreshBoard(x, y);
    }

    public void start(){
        new Thread(() -> {
            try {
                while (true) {
//                if (init) {
//                    //sleep的原因是一开始变量尚未赋值，但是线程已开启
//                    TimeUnit.MILLISECONDS.sleep(500);
//                    init = false;
//                }
                    String data = dataInputStream.readLine();
                    if (data.equals("1")) {
                        Platform.runLater(() -> {
                            if (refreshBoard(x_axis, y_axis)) {
                                TURN = !TURN;
                            }
                        });
                    } else if (data.startsWith("position:")) {
                        int x = Integer.parseInt(data.substring(9, 10));
                        int y = Integer.parseInt(data.substring(11, 12));
                        Platform.runLater(() -> {
                            refreshBoard(x, y);
                            TURN = !TURN;
                        });
                    } else {
                        System.out.println(data);
                    }
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }).start();
    }
}
