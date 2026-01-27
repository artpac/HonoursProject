//package AI;
//
//import Game.*;
//import java.io.*;
//import java.net.*;
//import java.util.*;
//import org.json.*;
//
///**
// * LLM integration for strategic decision-making
// */
//public class LLMIntegration {
//    private static final String API_URL = "https://api.anthropic.com/v1/messages";
//    private static final String MODEL = "claude-sonnet-4-20250514";
//    private boolean enabled;
//
//    public LLMIntegration() {
//        // Check if API is available
//        this.enabled = checkAPIAvailability();
//    }
//
//    /**
//     * Get strategic advice from LLM
//     */
//    public String getStrategicAdvice(String boardState, List<AIMove> possibleMoves) {
//        if (!enabled) {
//            return getFallbackStrategy(possibleMoves);
//        }
//
//        try {
//            String prompt = buildPrompt(boardState, possibleMoves);
//            String response = queryLLM(prompt);
//            return response;
//        } catch (Exception e) {
//            System.err.println("LLM query failed: " + e.getMessage());
//            return getFallbackStrategy(possibleMoves);
//        }
//    }
//
//    /**
//     * Build prompt for LLM
//     */
//    private String buildPrompt(String boardState, List<AIMove> moves) {
//        StringBuilder prompt = new StringBuilder();
//        prompt.append("You are a Hive board game expert. Analyze this position and suggest the best move.\n\n");
//        prompt.append("Current board state:\n");
//        prompt.append(boardState);
//        prompt.append("\n\nAvailable moves:\n");
//
//        for (int i = 0; i < Math.min(moves.size(), 10); i++) {
//            AIMove move = moves.get(i);
//            prompt.append(String.format("%d. %s %s from (%s) to (%d,%d)\n",
//                    i + 1,
//                    move.getPiece().getColor().equals(java.awt.Color.WHITE) ? "White" : "Black",
//                    move.getPiece().getType().name(),
//                    move.getFrom() != null ? move.getFrom().getQ() + "," + move.getFrom().getR() : "reserve",
//                    move.getTo().getQ(),
//                    move.getTo().getR()
//            ));
//        }
//
//        prompt.append("\nProvide strategic analysis in JSON format:\n");
//        prompt.append("{\n");
//        prompt.append("  \"recommended_move\": <number 1-10>,\n");
//        prompt.append("  \"reasoning\": \"<explanation>\",\n");
//        prompt.append("  \"strategy\": \"<aggressive/defensive/balanced>\"\n");
//        prompt.append("}");
//
//        return prompt.toString();
//    }
//
//    /**
//     * Query LLM API
//     */
//    private String queryLLM(String prompt) throws IOException {
//        // Create JSON request
//        JSONObject request = new JSONObject();
//        request.put("model", MODEL);
//        request.put("max_tokens", 500);
//
//        JSONArray messages = new JSONArray();
//        JSONObject message = new JSONObject();
//        message.put("role", "user");
//        message.put("content", prompt);
//        messages.put(message);
//        request.put("messages", messages);
//
//        // Make HTTP request
//        URL url = new URL(API_URL);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestMethod("POST");
//        conn.setRequestProperty("Content-Type", "application/json");
//        conn.setRequestProperty("x-api-key", System.getenv("ANTHROPIC_API_KEY"));
//        conn.setDoOutput(true);
//
//        // Send request
//        try (OutputStream os = conn.getOutputStream()) {
//            byte[] input = request.toString().getBytes("utf-8");
//            os.write(input, 0, input.length);
//        }
//
//        // Read response
//        StringBuilder response = new StringBuilder();
//        try (BufferedReader br = new BufferedReader(
//                new InputStreamReader(conn.getInputStream(), "utf-8"))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                response.append(line.trim());
//            }
//        }
//
//        // Parse JSON response
//        JSONObject jsonResponse = new JSONObject(response.toString());
//        JSONArray content = jsonResponse.getJSONArray("content");
//        return content.getJSONObject(0).getString("text");
//    }
//
//    /**
//     * Parse LLM JSON response
//     */
//    public AIMove parseLLMResponse(String response, List<AIMove> moves) {
//        try {
//            // Extract JSON from response (may have markdown formatting)
//            String json = response;
//            if (response.contains("```json")) {
//                json = response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1);
//            }
//
//            JSONObject parsed = new JSONObject(json);
//            int moveNum = parsed.getInt("recommended_move");
//
//            if (moveNum >= 1 && moveNum <= moves.size()) {
//                AIMove selected = moves.get(moveNum - 1);
//
//                // Log LLM reasoning
//                String reasoning = parsed.optString("reasoning", "No reasoning provided");
//                System.out.println("LLM Strategy: " + reasoning);
//
//                return selected;
//            }
//        } catch (Exception e) {
//            System.err.println("Failed to parse LLM response: " + e.getMessage());
//        }
//
//        return null;
//    }
//
//    /**
//     * Fallback strategy when LLM unavailable
//     */
//    private String getFallbackStrategy(List<AIMove> moves) {
//        // Simple heuristic-based selection
//        Random rand = new Random();
//        int selectedIdx = rand.nextInt(Math.min(3, moves.size()));
//
//        return String.format("{\"recommended_move\": %d, \"reasoning\": \"Fallback heuristic\", \"strategy\": \"balanced\"}",
//                selectedIdx + 1);
//    }
//
//    /**
//     * Check if API is available
//     */
//    private boolean checkAPIAvailability() {
//        String apiKey = System.getenv("ANTHROPIC_API_KEY");
//        if (apiKey == null || apiKey.isEmpty()) {
//            System.out.println("LLM integration disabled: ANTHROPIC_API_KEY not set");
//            return false;
//        }
//        return true;
//    }
//
//    /**
//     * Get opening book advice
//     */
//    public AIMove getOpeningMove(GameState state, List<AIMove> moves) {
//        // Hardcoded opening theory
//        int turnCount = state.getTurnCount();
//
//        if (turnCount == 0) {
//            // First move: place any piece at center
//            return moves.stream()
//                    .filter(m -> m.getTo().equals(new HexCoord(0, 0)))
//                    .findFirst()
//                    .orElse(moves.get(0));
//        } else if (turnCount == 1) {
//            // Second move: place adjacent
//            return moves.get(0);
//        } else if (turnCount < 6) {
//            // Early game: prioritize spreading pieces
//            return selectSpreadingMove(moves, state);
//        }
//
//        return null; // Use LLM for other positions
//    }
//
//    /**
//     * Select move that spreads pieces
//     */
//    private AIMove selectSpreadingMove(List<AIMove> moves, GameState state) {
//        HiveBoard board = state.getBoard();
//        AIMove bestMove = moves.get(0);
//        double maxDistance = 0.0;
//
//        for (AIMove move : moves) {
//            if (move.getType() == MoveType.PLACE) {
//                // Calculate average distance to existing pieces
//                double totalDist = 0.0;
//                int count = 0;
//
//                for (HexCoord existing : board.getAllCoordinates()) {
//                    int dq = Math.abs(move.getTo().getQ() - existing.getQ());
//                    int dr = Math.abs(move.getTo().getR() - existing.getR());
//                    totalDist += Math.sqrt(dq * dq + dr * dr);
//                    count++;
//                }
//
//                double avgDist = count > 0 ? totalDist / count : 0.0;
//                if (avgDist > maxDistance) {
//                    maxDistance = avgDist;
//                    bestMove = move;
//                }
//            }
//        }
//
//        return bestMove;
//    }
//
//    /**
//     * Analyze position for teaching purposes
//     */
//    public String explainPosition(GameState state, AIMove move) {
//        if (!enabled) {
//            return "Analysis unavailable: LLM not configured";
//        }
//
//        String boardState = serializeBoard(state);
//        String prompt = String.format(
//                "Explain why this move is good in Hive:\n%s\nMove: %s at (%d,%d)",
//                boardState,
//                move.getPiece().getType().name(),
//                move.getTo().getQ(),
//                move.getTo().getR()
//        );
//
//        try {
//            return queryLLM(prompt);
//        } catch (IOException e) {
//            return "Analysis failed: " + e.getMessage();
//        }
//    }
//
//    private String serializeBoard(GameState state) {
//        StringBuilder sb = new StringBuilder();
//        for (Map.Entry<HexCoord, List<Piece>> entry : state.getBoard().getBoard().entrySet()) {
//            sb.append(entry.getValue().get(0).toString())
//                    .append(" at (").append(entry.getKey().getQ())
//                    .append(",").append(entry.getKey().getR()).append(")\n");
//        }
//        return sb.toString();
//    }
//}