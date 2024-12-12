import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class QuizGameClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to the Quiz Game Server!");
            String serverResponse;
            List<String> answers = new ArrayList<>();

            // Read all questions in one go
            while ((serverResponse = in.readLine()) != null) {
                System.out.println(serverResponse);

                // When "Your Answer:" is received, collect the answer
                if (serverResponse.equals("Your Answer:")) {
                    String answer = userInput.readLine();
                    answers.add(answer);
                    out.println(answer); // Send the answer immediately
                }

                // Stop reading once the game is over
                if (serverResponse.contains("Game Over")) {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
