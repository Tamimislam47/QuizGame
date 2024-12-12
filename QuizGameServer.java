import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class QuizGameServer {
    private static final int MAX_CLIENTS = 5;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Quiz Game Server is running...");
            List<Thread> clientThreads = new ArrayList<>();

            while (true) {
                if (clientThreads.size() < MAX_CLIENTS) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket);
                    ClientHandler handler = new ClientHandler(clientSocket);
                    Thread thread = new Thread(handler);
                    clientThreads.add(thread);
                    thread.start();
                } else {
                    System.out.println("Maximum client limit reached. Rejecting connection...");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String quizJson = fetchQuizDataFromDatabase();
                // System.out.println(quizJson);
                List<QuizQuestion> quizQuestions = parseQuizJson(quizJson);

                out.println("Welcome to the Quiz Game!");

                int score = 0;

                // Loop through all questions
                for (QuizQuestion question : quizQuestions) {
                    out.println(question.getQuestTitle());
                    question.getOptions().forEach((key, value) -> out.println(key + ": " + value));

                    out.println("Your Answer:");
                    String clientAnswer = in.readLine().trim();

                    if (question.getAnswer().equals(clientAnswer)) {
                        score++;
                        out.println("Correct!");
                    } else {
                        out.println("Wrong! You answered: " + clientAnswer);
                        out.println("The correct answer was: " + question.getAnswer());
                    }
                }

                out.println("Game Over! Your final score is: " + score);
                System.out.println("Client " + socket + " scored: " + score);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String fetchQuizDataFromDatabase() {
            return """
                    [
                      {
                        "quesId": 1,
                        "questTitle": "1. What does HTTP stand for?",
                        "options": {
                            "a": "HyperText Transfer Protocol",
                            "b": "HyperTool Transfer Protocol",
                            "c": "HyperText Test Protocol",
                            "d": "HyperTransfer Text Protocol"
                        },
                        "answer": "a"
                       }
                    ]
                    """;
        }

        private List<QuizQuestion> parseQuizJson(String json) {
            List<QuizQuestion> quizQuestions = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                QuizQuestion question = new QuizQuestion();

                question.setQuesId(jsonObject.getInt("quesId"));
                question.setQuestTitle(jsonObject.getString("questTitle"));

                Map<String, String> options = new HashMap<>();
                JSONObject optionsObject = jsonObject.getJSONObject("options");
                for (String key : optionsObject.keySet()) {
                    options.put(key, optionsObject.getString(key));
                }
                question.setOptions(options);
                question.setAnswer(jsonObject.getString("answer"));

                quizQuestions.add(question);
            }
            return quizQuestions;
        }
    }

    static class QuizQuestion {
        private int quesId;
        private String questTitle;
        private Map<String, String> options;
        private String answer;

        public int getQuesId() {
            return quesId;
        }

        public void setQuesId(int quesId) {
            this.quesId = quesId;
        }

        public String getQuestTitle() {
            return questTitle;
        }

        public void setQuestTitle(String questTitle) {
            this.questTitle = questTitle;
        }

        public Map<String, String> getOptions() {
            return options;
        }

        public void setOptions(Map<String, String> options) {
            this.options = options;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }
}
